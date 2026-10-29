
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        // Nexus : adapter si Jenkins ne partage pas le reseau de Nexus
        NEXUS_BASE_URL = 'http://localhost:8081'
        NEXUS_SNAPSHOT_REPO = 'events-snapshots'

        // Coordonnees exactes de ton pom.xml
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

                        $headers = @{
                            Authorization = "Basic " + [Convert]::ToBase64String(
                                [Text.Encoding]::UTF8.GetBytes(
                                    "$env:NEXUS_USERNAME`:$env:NEXUS_PASSWORD"
                                )
                            )
                        }

                        Write-Host "Reading snapshot metadata from Nexus..."
                        Write-Host $metadataUrl

                        $metadata = Invoke-RestMethod `
                            -Uri $metadataUrl `
                            -Headers $headers `
                            -Method Get

                        # Select the latest JAR snapshot without a classifier.
                        $snapshotVersions = $metadata.SelectNodes(
                            "//*[local-name()='snapshotVersion']"
                        )

                        if (-not $snapshotVersions) {
                            throw "No snapshot versions found in Nexus metadata."
                        }

                        $jarVersion = $snapshotVersions |
                            Where-Object {
                                $_.SelectSingleNode("./*[local-name()='extension']").InnerText -eq "jar" -and
                                $null -eq $_.SelectSingleNode("./*[local-name()='classifier']")
                            } |
                            Sort-Object {
                                $_.SelectSingleNode("./*[local-name()='updated']").InnerText
                            } -Descending |
                            Select-Object -First 1

                        if (-not $jarVersion) {
                            throw "No executable JAR version found in Nexus metadata."
                        }

                        $resolvedVersion = $jarVersion.SelectSingleNode(
                            "./*[local-name()='value']"
                        ).InnerText

                        if ([string]::IsNullOrWhiteSpace($resolvedVersion)) {
                            throw "Could not resolve the timestamped snapshot version."
                        }

                        $jarUrl = "$repoUrl$artifactPath/eventsProject-$resolvedVersion.jar"

                        New-Item -ItemType Directory `
                            -Force -Path "target/nexus-artifact" | Out-Null

                        $downloadPath = Join-Path `
                            (Get-Location).Path `
                            "target/nexus-artifact/eventsProject.jar"

                        Write-Host "Downloading artifact from Nexus..."
                        Invoke-WebRequest `
                            -Uri $jarUrl `
                            -Headers $headers `
                            -OutFile $downloadPath `
                            -UseBasicParsing

                        if (-not (Test-Path $downloadPath)) {
                            throw "JAR download failed."
                        }

                        if ((Get-Item $downloadPath).Length -eq 0) {
                            throw "Downloaded JAR is empty."
                        }

                        Copy-Item `
                            -Path $downloadPath `
                            -Destination "target/app.jar" `
                            -Force

                        if (-not (Test-Path "target/app.jar")) {
                            throw "target/app.jar was not created."
                        }

                        Write-Host "JAR downloaded successfully from Nexus."
                        Get-Item "target/app.jar" |
                            Select-Object Name, Length
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
                        docker build ^
                          -t "%DOCKERHUB_USERNAME%/events-project:%BUILD_NUMBER%" .
                    '''

                    powershell '''
                        $ErrorActionPreference = "Stop"

                        try {
                            $env:DOCKERHUB_TOKEN |
                                docker login `
                                    --username $env:DOCKERHUB_USERNAME `
                                    --password-stdin

                            if ($LASTEXITCODE -ne 0) {
                                throw "Docker Hub login failed."
                            }

                            $image = "$env:DOCKERHUB_USERNAME/events-project:$env:BUILD_NUMBER"

                            docker push $image

                            if ($LASTEXITCODE -ne 0) {
                                throw "Docker Hub push failed."
                            }

                            Write-Host "Published image: $image"
                        }
                        finally {
                            docker logout 2>$null
                        }
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Build, tests, SonarQube, Nexus et Docker Hub.'
        }

        failure {
            echo 'FAILURE: Consultez la Console Output de Jenkins.'
        }

        always {
            echo 'Pipeline terminee.'
        }
    }
}
