pipeline {
    agent any
    options { skipDefaultCheckout(true); disableConcurrentBuilds(); buildDiscarder(logRotator(numToKeepStr: '10')) }
    parameters {
        booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Requires configured SonarQube installation named local-sonar and webhook')
        booleanParam(name: 'PUBLISH_NEXUS', defaultValue: false, description: 'Requires authorized Nexus publication and nexus credential')
        booleanParam(name: 'PUBLISH_IMAGE', defaultValue: false, description: 'Requires authorized Docker Hub publication and dockerhub credential')
        booleanParam(name: 'DEPLOY_K8S', defaultValue: false, description: 'Requires image publication and kubeconfig credential accessible from the agent')
        booleanParam(name: 'DEPLOY_LOCAL_KIND', defaultValue: true, description: 'Load the built image directly into the existing local kind cluster and verify it')
        string(name: 'NEXUS_URL', defaultValue: 'http://nexus:8081', description: 'Base URL without trailing slash')
        string(name: 'DOCKER_REPOSITORY', defaultValue: '', description: 'Docker Hub account/repository for authorized publication')
    }
    environment {
        JAVA_HOME = '/opt/java17'
        PATH = "/opt/java17/bin:${env.PATH}"
    }
    triggers { pollSCM('H/5 * * * *') }
    stages {
        stage('Checkout') {
            steps {
                script {
                    def revision = checkout scm
                    env.GIT_COMMIT = revision.GIT_COMMIT
                    env.IMAGE_TAG = "tache-${revision.GIT_COMMIT.take(12)}"
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
                        docker push "$DOCKER_REPOSITORY:$IMAGE_TAG"
                    '''
                }
            }
        }
        stage('Deploy local kind') {
            when { expression { params.DEPLOY_LOCAL_KIND } }
            steps {
                script { if (params.DEPLOY_K8S) { error('Choose one deployment destination') } }
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''set -eu
                        archive=$(mktemp --suffix=.tar)
                        trap 'rm -f "$archive"' EXIT
                        docker image save --platform=linux/amd64 -o "$archive" events-tache:$IMAGE_TAG
                        kind load image-archive "$archive" --name events-tache
                        kubectl --context kind-events-tache -n events-tache set image deployment/events events=events-tache:$IMAGE_TAG
                        kubectl --context kind-events-tache -n events-tache rollout status deployment/events --timeout=240s
                    '''
                }
            }
        }
        stage('Verify local deployment') {
            when { expression { params.DEPLOY_LOCAL_KIND } }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''set -eu
                        kubectl --context kind-events-tache -n events-tache port-forward --address 127.0.0.1 service/events 18090:8089 > /tmp/events-forward-$BUILD_NUMBER.log 2>&1 &
                        forward_pid=$!
                        trap 'kill "$forward_pid" 2>/dev/null || true' EXIT
                        curl --fail --retry 20 --retry-connrefused --retry-delay 2 http://127.0.0.1:18090/events/actuator/health/readiness
                        curl --fail http://127.0.0.1:18090/events/taches
                    '''
                }
            }
        }
        stage('Deploy Kubernetes') {
            when { expression { params.DEPLOY_K8S } }
            steps {
                script { if (!params.PUBLISH_IMAGE) { error('Deployment requires a published image') } }
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''kubectl --context kind-events-tache -n events-tache set image deployment/events events="$DOCKER_REPOSITORY:$IMAGE_TAG"
                        kubectl --context kind-events-tache -n events-tache rollout status deployment/events --timeout=240s
                        kubectl --context kind-events-tache -n events-tache run verify-$BUILD_NUMBER --image=curlimages/curl:8.12.1 --restart=Never --rm -i --pod-running-timeout=120s -- --fail --retry 12 --retry-all-errors --retry-delay 5 http://events:8089/events/actuator/health/readiness
                    '''
                }
            }
        }
    }
    post { success { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true } }
}
