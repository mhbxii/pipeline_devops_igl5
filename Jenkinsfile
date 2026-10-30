pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        // Nexus
        NEXUS_BASE_URL = 'http://localhost:8081'
        NEXUS_SNAPSHOT_REPO = 'events-snapshots'

        // Coordonnees Maven
        MAVEN_GROUP_ID = 'tn.esprit'
        MAVEN_ARTIFACT_ID = 'eventsProject'
        MAVEN_VERSION = '1.0.0-SNAPSHOT'
    }

    stages {

        stage('1 - Checkout') {
            steps {
                checkout scm
            }
        }

        stage('2 - Verify Tools') {
            steps {
                bat 'git --version'
                bat 'mvn -version'
                bat 'java -version'
                bat 'docker --version'
                bat 'docker info'
            }
        }

        stage('3 - Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
            post {
                always {
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('4 - Archive JAR') {
            steps {
                archiveArtifacts(
                    artifacts: 'target/eventsProject-1.0.0-SNAPSHOT.jar',
                    fingerprint: true
                )
            }
        }

        stage('5 - SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeLocal') {
                    bat '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
                    '''
                }
            }
        }

        stage('6 - Publish to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus_secret',
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    bat 'mvn -B -s settings.xml deploy -DskipTests'
                }
            }
        }

        stage('7 - Download JAR from Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus_secret',
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    powershell '''
                        $ErrorActionPreference = "Stop"

                        $repoUrl = "$env:NEXUS_BASE_URL/repository/$env:NEXUS_SNAPSHOT_REPO/"
                        $artifactPath = "tn/esprit/eventsProject/1.0.0-SNAPSHOT"
                        $metadataUrl = "$repoUrl$artifactPath/maven-metadata.xml"

                        $pair = "$env:NEXUS_USERNAME`:$env:NEXUS_PASSWORD"
                        $encoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
                        $headers = @{ Authorization = "Basic $encoded" }

                        Write-Host "Reading snapshot metadata from Nexus..."
                        [xml]$metadata = Invoke-RestMethod -Uri $metadataUrl -Headers $headers -Method Get

                        $snapshotVersions = $metadata.SelectNodes("//*[local-name()='snapshotVersion']")
                        if (-not $snapshotVersions -or $snapshotVersions.Count -eq 0) {
                            throw "No snapshot versions found in Nexus metadata."
                        }

                        $jarVersion = $snapshotVersions |
                            Where-Object {
                                $extensionNode = $_.SelectSingleNode("./*[local-name()='extension']")
                                $classifierNode = $_.SelectSingleNode("./*[local-name()='classifier']")
                                $extensionNode -and
                                $extensionNode.InnerText -eq "jar" -and
                                ( -not $classifierNode -or [string]::IsNullOrWhiteSpace($classifierNode.InnerText) )
                            } |
                            Sort-Object {
                                $updatedNode = $_.SelectSingleNode("./*[local-name()='updated']")
                                if ($updatedNode) { $updatedNode.InnerText } else { "" }
                            } -Descending |
                            Select-Object -First 1

                        if (-not $jarVersion) { throw "No executable JAR version found in Nexus metadata." }

                        $valueNode = $jarVersion.SelectSingleNode("./*[local-name()='value']")
                        if (-not $valueNode -or [string]::IsNullOrWhiteSpace($valueNode.InnerText)) {
                            throw "Could not resolve the timestamped snapshot version."
                        }

                        $resolvedVersion = $valueNode.InnerText
                        $jarUrl = "$repoUrl$artifactPath/eventsProject-$resolvedVersion.jar"

                        New-Item -ItemType Directory -Force -Path "target/nexus-artifact" | Out-Null
                        $downloadPath = Join-Path (Get-Location).Path "target/nexus-artifact/eventsProject.jar"

                        Write-Host "Downloading JAR from Nexus..."
                        Write-Host "Resolved version: $resolvedVersion"

                        Invoke-WebRequest -Uri $jarUrl -Headers $headers -OutFile $downloadPath -UseBasicParsing

                        if (-not (Test-Path $downloadPath)) { throw "JAR download failed." }
                        if ((Get-Item $downloadPath).Length -eq 0) { throw "Downloaded JAR is empty." }

                        Copy-Item -Path $downloadPath -Destination "target/app.jar" -Force
                        Write-Host "JAR downloaded successfully."
                        Get-Item "target/app.jar" | Select-Object Name, Length
                    '''

                    archiveArtifacts(
                        artifacts: 'target/app.jar',
                        fingerprint: true
                    )
                }
            }
        }

        stage('8 - Docker Build and Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub_credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    bat '''
                        @echo off

                        echo ===== BUILD =====
                        docker build -t "%DOCKERHUB_USERNAME%/events-project:%BUILD_NUMBER%" .
                        if errorlevel 1 exit /b 1

                        echo ===== LOGIN =====
                        echo %DOCKERHUB_TOKEN%| docker login -u %DOCKERHUB_USERNAME% --password-stdin
                        if errorlevel 1 (
                            echo ERROR: Docker Hub login failed
                            exit /b 1
                        )

                        echo ===== PUSH =====
                        docker push "%DOCKERHUB_USERNAME%/events-project:%BUILD_NUMBER%"
                        if errorlevel 1 (
                            docker logout
                            exit /b 1
                        )

                        docker logout
                        echo SUCCESS: Image published
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Build, tests, SonarQube, Nexus and Docker Hub.'
        }
        failure {
            echo 'FAILURE: Consultez la Console Output de Jenkins.'
        }
        always {
            echo 'Pipeline terminee.'
        }
    }
}