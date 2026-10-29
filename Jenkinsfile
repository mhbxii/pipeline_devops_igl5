
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

        // ==========================================
        // 1. Checkout
        // ==========================================
        stage('1 - Checkout') {
            steps {
                checkout scm
            }
        }

        // ==========================================
        // 2. Verify Tools
        // ==========================================
        stage('2 - Verify Tools') {
            steps {
                bat 'git --version'
                bat 'mvn -version'
                bat 'java -version'
                bat 'docker --version'
                bat 'docker info'
            }
        }

        // ==========================================
        // 3. Build and Test
        // ==========================================
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

        // ==========================================
        // 4. Archive JAR
        // ==========================================
        stage('4 - Archive JAR') {
            steps {
                archiveArtifacts(
                    artifacts: 'target/eventsProject-1.0.0-SNAPSHOT.jar',
                    fingerprint: true
                )
            }
        }

        // ==========================================
        // 5. SonarQube Analysis
        // ==========================================
        stage('5 - SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeLocal') {
                    bat '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
                    '''
                }
            }
        }

        // ==========================================
        // 6. Publish to Nexus
        // ==========================================
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

        // ==========================================
        // 7. Download JAR from Nexus
        // ==========================================
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

                        # Authentication Nexus
                        $pair = "$env:NEXUS_USERNAME`:$env:NEXUS_PASSWORD"
                        $encoded = [Convert]::ToBase64String(
                            [Text.Encoding]::UTF8.GetBytes($pair)
                        )

                        $headers = @{
                            Authorization = "Basic $encoded"
                        }

                        Write-Host "Reading snapshot metadata from Nexus..."

                        [xml]$metadata = Invoke-RestMethod `
                            -Uri $metadataUrl `
                            -Headers $headers `
                            -Method Get

                        $snapshotVersions = $metadata.SelectNodes(
                            "//*[local-name()='snapshotVersion']"
                        )

                        if (-not $snapshotVersions -or $snapshotVersions.Count -eq 0) {
                            throw "No snapshot versions found in Nexus metadata."
                        }

                        # Find the latest JAR without a classifier
                        $jarVersion = $snapshotVersions |
                            Where-Object {
                                $extensionNode = $_.SelectSingleNode(
                                    "./*[local-name()='extension']"
                                )

                                $classifierNode = $_.SelectSingleNode(
                                    "./*[local-name()='classifier']"
                                )

                                $extensionNode -and
                                $extensionNode.InnerText -eq "jar" -and
                                (
                                    -not $classifierNode -or
                                    [string]::IsNullOrWhiteSpace(
                                        $classifierNode.InnerText
                                    )
                                )
                            } |
                            Sort-Object {
                                $updatedNode = $_.SelectSingleNode(
                                    "./*[local-name()='updated']"
                                )

                                if ($updatedNode) {
                                    $updatedNode.InnerText
                                } else {
                                    ""
                                }
                            } -Descending |
                            Select-Object -First 1

                        if (-not $jarVersion) {
                            throw "No executable JAR version found in Nexus metadata."
                        }

                        $valueNode = $jarVersion.SelectSingleNode(
                            "./*[local-name()='value']"
                        )

                        if (
                            -not $valueNode -or
                            [string]::IsNullOrWhiteSpace($valueNode.InnerText)
                        ) {
                            throw "Could not resolve the timestamped snapshot version."
                        }

                        $resolvedVersion = $valueNode.InnerText
                        $jarUrl = "$repoUrl$artifactPath/eventsProject-$resolvedVersion.jar"

                        # Download the artifact
                        New-Item -ItemType Directory `
                            -Force -Path "target/nexus-artifact" |
                            Out-Null

                        $downloadPath = Join-Path `
                            (Get-Location).Path `
                            "target/nexus-artifact/eventsProject.jar"

                        Write-Host "Downloading JAR from Nexus..."
                        Write-Host "Resolved version: $resolvedVersion"

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

                        Write-Host "JAR downloaded successfully."

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

        // ==========================================
        // 8. Docker Build and Push
        // ==========================================
        stage('8 - Docker Build and Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub_credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    // Build the Docker image
                    bat '''
                        @echo off

                        echo ==========================================
                        echo STEP 1: Building Docker image
                        echo ==========================================

                        docker build ^
                            -t "%DOCKERHUB_USERNAME%/events-project:%BUILD_NUMBER%" .

                        if errorlevel 1 (
                            echo ERROR: Docker image build failed.
                            exit /b 1
                        )
                    '''

                    // Login and push the image
                    powershell '''
                        $ErrorActionPreference = "Stop"

                        $username = $env:DOCKERHUB_USERNAME.Trim()
                        $token = $env:DOCKERHUB_TOKEN.Trim()
                        $image = "$username/events-project:$env:BUILD_NUMBER"

                        if ([string]::IsNullOrWhiteSpace($username)) {
                            throw "Docker Hub username is empty."
                        }

                        if ([string]::IsNullOrWhiteSpace($token)) {
                            throw "Docker Hub token is empty."
                        }

                        Write-Host "=========================================="
                        Write-Host "STEP 2: Docker Hub authentication"
                        Write-Host "=========================================="
                        Write-Host "Docker Hub username: $username"
                        Write-Host "Image to publish: $image"
                        Write-Host "Token received by Jenkins: Yes"

                        try {
                            # Remove any previous Docker Hub session.
                            docker logout 2>$null | Out-Null

                            # Send the token through standard input.
                            $token | docker login `
                                --username $username `
                                --password-stdin

                            if ($LASTEXITCODE -ne 0) {
                                throw "Docker Hub authentication failed. Check Jenkins credentials and token permissions."
                            }

                            Write-Host "Authentication successful."

                            Write-Host "=========================================="
                            Write-Host "STEP 3: Pushing image to Docker Hub"
                            Write-Host "=========================================="

                            docker push $image

                            if ($LASTEXITCODE -ne 0) {
                                throw "Docker image push failed."
                            }

                            Write-Host "=========================================="
                            Write-Host "SUCCESS: Image published"
                            Write-Host "Image: $image"
                            Write-Host "=========================================="
                        }
                        finally {
                            docker logout 2>$null | Out-Null
                        }
                    '''
                }
            }
        }
    }

    // ==========================================
    // Pipeline result
    // ==========================================
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
