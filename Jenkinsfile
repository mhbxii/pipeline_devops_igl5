pipeline {
    agent any
    options { skipDefaultCheckout(true); disableConcurrentBuilds(); timeout(time: 90, unit: 'MINUTES'); buildDiscarder(logRotator(numToKeepStr: '10')) }
    parameters {
        booleanParam(name: 'RUN_TERRAFORM', defaultValue: true, description: 'Validate and plan the AWS EKS infrastructure')
        booleanParam(name: 'APPLY_TERRAFORM', defaultValue: false, description: 'Request manual approval of the saved AWS EKS plan before applying')
        booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Use configured local-sonar installation and verify its quality gate')
        booleanParam(name: 'PUBLISH_NEXUS', defaultValue: true, description: 'Publish to the configured local Nexus using the nexus credential')
        booleanParam(name: 'PUBLISH_IMAGE', defaultValue: true, description: 'Publish to the user-approved public Docker Hub repository using dockerhub credential')
        booleanParam(name: 'DEPLOY_EKS', defaultValue: true, description: 'Deploy this build image to the prepared AWS EKS application')
        string(name: 'NEXUS_URL', defaultValue: 'http://nexus:8081', description: 'Base URL without trailing slash')
        string(name: 'DOCKER_REPOSITORY', defaultValue: 'ayyoub1/events-tache', description: 'User-approved public destination')
    }
    environment {
        JAVA_HOME = '/opt/java17'
        PATH = "/opt/java17/bin:${env.PATH}"
    }
    triggers { pollSCM('H/5 * * * *') }
    stages {
        stage('Validate configuration') {
            steps {
                script {
                    if (params.APPLY_TERRAFORM && !params.RUN_TERRAFORM) { error('APPLY_TERRAFORM requires RUN_TERRAFORM') }
                    if (params.DEPLOY_EKS && !params.RUN_SONAR) {
                        error('Deployment requires Sonar analysis and its quality gate')
                    }
                    if (params.DEPLOY_EKS && !params.PUBLISH_IMAGE) { error('Remote deployment requires image publication') }
                }
            }
        }
        stage('Checkout') {
            steps {
                script {
                    def revision = checkout scm
                    env.GIT_COMMIT = revision.GIT_COMMIT
                    env.IMAGE_TAG = "tache-${revision.GIT_COMMIT.take(12)}-${env.BUILD_NUMBER}"
                }
                sh 'java -version && mvn -version'
            }
        }
        stage('Compile') { steps { sh 'mvn -B clean compile' } }
        stage('Tests and coverage') {
            steps { sh 'mvn -B test' }
            post {
                always {
                    junit 'target/surefire-reports/TEST-*.xml'
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }
        stage('SonarQube analysis') {
            when { expression { params.RUN_SONAR } }
            steps {
                withEnv(['JAVA_HOME=/opt/java/openjdk', 'PATH+SONAR_JAVA=/opt/java/openjdk/bin']) {
                    withSonarQubeEnv('local-sonar') {
                        sh '''set +x
                            export SONAR_TOKEN="$SONAR_AUTH_TOKEN"
                            mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.8.0.7211:sonar -Dsonar.scanner.skipJreProvisioning=true -Dsonar.projectKey=events-tache -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }
        stage('Quality gate') {
            when { expression { params.RUN_SONAR } }
            steps { timeout(time: 10, unit: 'MINUTES') { waitForQualityGate abortPipeline: true } }
        }
        stage('Terraform EKS validate') {
            when { expression { params.RUN_TERRAFORM } }
            steps {
                sh '''set -eu
                    command -v terraform >/dev/null || { echo 'Rebuild Jenkins with Terraform first; see docs/JENKINS_TERRAFORM.md'; exit 1; }
                    test -s /terraform-eks-state/terraform.tfstate || { echo 'Canonical AWS state is missing; refusing to create a second state.'; exit 1; }
                    terraform -chdir=terraform/aws-eks fmt -check
                    terraform -chdir=terraform/aws-eks init -input=false -reconfigure -lockfile=readonly -no-color -backend-config=path=/terraform-eks-state/terraform.tfstate
                    terraform -chdir=terraform/aws-eks validate -no-color
                '''
            }
        }
        stage('Terraform EKS plan') {
            when { expression { params.RUN_TERRAFORM } }
            steps {
                script { env.TF_PLAN_DIR = sh(script: 'mktemp -d /tmp/events-terraform.XXXXXXXX', returnStdout: true).trim() }
                withCredentials([
                    file(credentialsId: 'aws-lab-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE'),
                    file(credentialsId: 'aws-eks-tfvars', variable: 'EKS_TFVARS')
                ]) {
                    sh '''set -eu
                        set +x
                        umask 077
                        export TF_IN_AUTOMATION=1 AWS_EC2_METADATA_DISABLED=true
                        status=0
                        terraform -chdir=terraform/aws-eks plan -input=false -no-color -lock-timeout=60s -var-file="$EKS_TFVARS" -var=aws_profile=events-tache-lab -detailed-exitcode -out="$TF_PLAN_DIR/eks.tfplan" || status=$?
                        case "$status" in
                            0) echo 'Terraform EKS: no changes.' ;;
                            2) echo 'Terraform EKS: review the proposed AWS changes and cost before approval.' ;;
                            *) exit "$status" ;;
                        esac
                        printf '%s' "$status" > "$TF_PLAN_DIR/status"
                    '''
                    script { env.TF_HAS_CHANGES = sh(script: 'cat "$TF_PLAN_DIR/status"', returnStdout: true).trim() }
                }
                script {
                    if (env.TF_HAS_CHANGES == '2' && params.DEPLOY_EKS && !params.APPLY_TERRAFORM) {
                        error('AWS infrastructure changes require review and approved apply before delivery. For a plan-only run, disable DEPLOY_EKS.')
                    }
                }
            }
        }
        stage('Terraform EKS approval and apply') {
            when { expression { params.RUN_TERRAFORM && params.APPLY_TERRAFORM && env.TF_HAS_CHANGES == '2' } }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    input message: 'Review this build Terraform EKS plan and AWS cost. Apply this exact plan?', ok: 'Approve AWS apply', submitter: 'tache-admin'
                }
                withCredentials([file(credentialsId: 'aws-lab-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE')]) {
                    sh '''set -eu
                        set +x
                        export TF_IN_AUTOMATION=1 AWS_EC2_METADATA_DISABLED=true
                        terraform -chdir=terraform/aws-eks apply -input=false -no-color -lock-timeout=60s "$TF_PLAN_DIR/eks.tfplan"
                    '''
                }
            }
        }
        stage('Check EKS delivery access') {
            when { expression { params.DEPLOY_EKS } }
            steps {
                withCredentials([file(credentialsId: 'eks-kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''set -eu
                        kubectl version --client
                        kubectl --context events-tache-eks --request-timeout=30s -n events-tache get deployment/events service/events
                        echo 'Existing EKS application is accessible. Fresh clusters require the bootstrap described in docs/AWS_EKS_OPERATIONS.md.'
                    '''
                }
            }
        }
        stage('Package') { steps { sh 'mvn -B package' } }
        stage('Publish Nexus') {
            when { expression { params.PUBLISH_NEXUS } }
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh 'mvn -B -s ci/settings.xml deploy'
                }
            }
        }
        stage('Build Docker image') {
            steps { sh 'docker build -t events-tache:$IMAGE_TAG .' }
        }
        stage('Publish Docker image') {
            when { expression { params.PUBLISH_IMAGE } }
            steps {
                script { if (!params.DOCKER_REPOSITORY.trim()) { error('DOCKER_REPOSITORY is required') } }
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh '''set +x
                        export DOCKER_CONFIG=$(mktemp -d)
                        trap 'rm -rf "$DOCKER_CONFIG"' EXIT
                        printf '%s' "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USER" --password-stdin
                        docker tag events-tache:$IMAGE_TAG "$DOCKER_REPOSITORY:$IMAGE_TAG"
                        mkdir "$DOCKER_CONFIG/image"
                        docker image save --platform=linux/amd64 -o "$DOCKER_CONFIG/image.tar" events-tache:$IMAGE_TAG
                        tar -xf "$DOCKER_CONFIG/image.tar" -C "$DOCKER_CONFIG/image"
                        java ci/PushRegistry.java "$DOCKER_CONFIG/image" "$DOCKER_REPOSITORY" "$IMAGE_TAG"
                    '''
                }
            }
        }
        stage('Deploy AWS EKS') {
            when { expression { params.DEPLOY_EKS } }
            steps {
                withCredentials([file(credentialsId: 'eks-kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''set -eu
                        kubectl --context events-tache-eks -n events-tache get deployment/events >/dev/null
                        kubectl --context events-tache-eks -n events-tache set image deployment/events events="$DOCKER_REPOSITORY:$IMAGE_TAG"
                        kubectl --context events-tache-eks -n events-tache rollout status deployment/events --timeout=600s
                    '''
                }
            }
        }
        stage('Verify AWS application') {
            when { expression { params.DEPLOY_EKS } }
            steps {
                withCredentials([file(credentialsId: 'eks-kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''set -eu
                        kubectl --context events-tache-eks -n events-tache port-forward --address 127.0.0.1 service/events 18190:8089 > /tmp/events-forward-$BUILD_NUMBER.log 2>&1 &
                        forward_pid=$!
                        trap 'kill "$forward_pid" 2>/dev/null || true' EXIT
                        curl --fail --retry 20 --retry-connrefused --retry-delay 2 http://127.0.0.1:18190/events/actuator/health/readiness
                        curl --fail http://127.0.0.1:18190/events/taches
                    '''
                }
            }
        }
    }
    post {
        always {
            sh '''set -eu
                case "${TF_PLAN_DIR:-}" in
                    /tmp/events-terraform.*) rm -rf -- "$TF_PLAN_DIR" ;;
                esac
            '''
        }
        success { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true }
    }
}
