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

        // AWS / EKS
        AWS_DEFAULT_REGION = 'us-east-1'
        AWS_REGION = 'us-east-1'
        EKS_CLUSTER_NAME = 'events-cluster'
        K8S_NAMESPACE = 'events-project'
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

        stage('9 - Terraform Init & Validate') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'aws_session_token', variable: 'AWS_SESSION_TOKEN')
                ]) {
                    dir('terraform') {
                        bat '''
                            @echo off
                            echo ===== TERRAFORM VERSION =====
                            terraform --version
                            if errorlevel 1 (
                                echo ERROR: Terraform not found. Install Terraform first.
                                exit /b 1
                            )

                            echo ===== TERRAFORM INIT =====
                            terraform init -input=false
                            if errorlevel 1 exit /b 1

                            echo ===== TERRAFORM FORMAT CHECK =====
                            terraform fmt -check -recursive
                            if errorlevel 1 (
                                echo WARNING: Terraform files are not properly formatted. Running fmt...
                                terraform fmt -recursive
                            )

                            echo ===== TERRAFORM VALIDATE =====
                            terraform validate
                            if errorlevel 1 exit /b 1

                            echo ===== TERRAFORM PLAN =====
                            terraform plan -out=tfplan -input=false
                            if errorlevel 1 exit /b 1

                            echo SUCCESS: Terraform validated and plan generated.
                        '''
                    }
                    archiveArtifacts(
                        artifacts: 'terraform/tfplan',
                        allowEmptyArchive: true,
                        fingerprint: true
                    )
                }
            }
        }

        stage('10 - Terraform Apply and Read Outputs') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'aws_session_token', variable: 'AWS_SESSION_TOKEN')
                ]) {
                    dir('terraform') {
                        bat '''
                            @echo off
                            terraform apply -auto-approve -input=false tfplan
                            if errorlevel 1 exit /b 1
                        '''

                        script {
                            // Noms alignes sur outputs.tf : cluster_name et cluster_endpoint
                            def clusterName = bat(
                                returnStdout: true,
                                script: '@echo off\r\nterraform output -raw cluster_name'
                            ).trim()

                            def clusterEndpoint = bat(
                                returnStdout: true,
                                script: '@echo off\r\nterraform output -raw cluster_endpoint'
                            ).trim()

                            if (!clusterName || !clusterEndpoint) {
                                error('Terraform outputs are empty. Check outputs.tf.')
                            }

                            env.EKS_CLUSTER_NAME = clusterName
                            env.EKS_CLUSTER_ENDPOINT = clusterEndpoint

                            echo "EKS cluster name: ${env.EKS_CLUSTER_NAME}"
                            echo "EKS cluster endpoint: ${env.EKS_CLUSTER_ENDPOINT}"
                        }

                        bat 'terraform output'
                    }
                }
            }
        }

        stage('11 - Configure Kubernetes') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'aws_session_token', variable: 'AWS_SESSION_TOKEN')
                ]) {
                    bat '''
                        @echo off
                        echo ===== CONFIGURE KUBECONFIG =====
                        aws eks update-kubeconfig --region %AWS_DEFAULT_REGION% --name %EKS_CLUSTER_NAME%
                        if errorlevel 1 (
                            echo ERROR: Failed to configure kubeconfig
                            exit /b 1
                        )

                        echo ===== VERIFY CLUSTER ACCESS =====
                        kubectl cluster-info
                        if errorlevel 1 (
                            echo ERROR: Cannot reach the EKS cluster
                            exit /b 1
                        )

                        echo ===== VERIFY NODES =====
                        kubectl get nodes
                        if errorlevel 1 (
                            echo ERROR: Cannot list cluster nodes
                            exit /b 1
                        )

                        echo SUCCESS: Kubernetes cluster configured and accessible.
                    '''
                }
            }
        }

        stage('12 - Kubernetes Deployment') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'aws_session_token', variable: 'AWS_SESSION_TOKEN'),
                    usernamePassword(
                        credentialsId: 'dockerhub_credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    powershell '''
                        $ErrorActionPreference = "Stop"

                        Write-Host "===== CREATE NAMESPACE ====="
                        kubectl apply -f k8s/namespace.yaml

                        Write-Host "===== DEPLOY CONFIGMAP ====="
                        kubectl apply -f k8s/configmap.yaml

                        Write-Host "===== DEPLOY SECRET ====="
                        kubectl apply -f k8s/secret.yaml

                        Write-Host "===== DEPLOY MYSQL ====="
                        kubectl apply -f k8s/mysql-deployment.yaml

                        Write-Host "===== PREPARE DEPLOYMENT MANIFEST ====="
                        $deploymentFile = "k8s/deployment.yaml"
                        $tempFile = "k8s/deployment-rendered.yaml"
                        $content = Get-Content $deploymentFile -Raw
                        $newImage = "$env:DOCKERHUB_USERNAME/events-project:$env:BUILD_NUMBER"
                        $content = $content -replace "IMAGE_PLACEHOLDER", $newImage
                        $content | Set-Content $tempFile -NoNewline
                        Write-Host "Image set to: $newImage"

                        Write-Host "===== DEPLOY APPLICATION ====="
                        kubectl apply -f $tempFile
                        Remove-Item $tempFile -Force

                        Write-Host "===== DEPLOY SERVICE ====="
                        kubectl apply -f k8s/service.yaml

                        Write-Host "===== WAIT FOR MYSQL TO BE READY ====="
                        kubectl rollout status deployment/events-mysql -n events-project --timeout=120s

                        Write-Host "===== WAIT FOR ROLLOUT ====="
                        kubectl rollout status deployment/events-project -n events-project --timeout=300s

                        Write-Host "===== POD STATUS ====="
                        kubectl get pods -n events-project -o wide

                        Write-Host "===== SERVICE STATUS ====="
                        kubectl get services -n events-project

                        Write-Host "===== APPLICATION ACCESS INFO ====="
                        $svc = kubectl get svc events-project-service -n events-project -o jsonpath="{.status.loadBalancer.ingress[0].hostname}" 2>$null
                        if ($svc) {
                            Write-Host "Application URL: http://${svc}/events/"
                        } else {
                            Write-Host "LoadBalancer is provisioning. Check later with:"
                            Write-Host "  kubectl get svc events-project-service -n events-project"
                        }

                        Write-Host "SUCCESS: Kubernetes deployment completed."
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Build, tests, SonarQube, Nexus, Docker Hub, Terraform and Kubernetes deployment completed.'
        }
        failure {
            echo 'FAILURE: Consultez la Console Output de Jenkins.'
        }
        always {
            echo 'Pipeline terminee.'
        }
    }
}