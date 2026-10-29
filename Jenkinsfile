pipeline {
    agent any
    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
        timeout(time: 45, unit: 'MINUTES')
        skipDefaultCheckout(true)
    }
    parameters {
        choice(name: 'SOURCE_MODE', choices: ['LOCAL', 'REMOTE'], description: 'LOCAL uses committed code mounted from this PC; REMOTE clones GitHub.')
        string(name: 'BUILD_BRANCH', defaultValue: 'logistics', description: 'Branch to verify')
        booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Analyze and wait for the quality gate')
        booleanParam(name: 'PUBLISH_NEXUS', defaultValue: true, description: 'Publish the verified Maven artifact')
        booleanParam(name: 'BUILD_IMAGE', defaultValue: true, description: 'Build the application image')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: false, description: 'Publish to Docker Hub using stored credentials')
        string(name: 'IMAGE_NAMESPACE', defaultValue: 'arfaoui131', description: 'Docker Hub username or organization')
        booleanParam(name: 'DEPLOY_KUBERNETES', defaultValue: false, description: 'Deploy to the local events-devops kind cluster')
    }
    environment {
        JAVA_HOME = '/opt/jdk17'
        PATH = "/opt/jdk17/bin:/opt/maven/bin:${env.PATH}"
        NEXUS_URL = 'http://nexus:8081'
    }
    stages {
        stage('Checkout') {
            steps {
                script {
                    if (!(params.BUILD_BRANCH ==~ /[A-Za-z0-9][A-Za-z0-9._\/-]*/)) {
                        error('Invalid branch name')
                    }
                    deleteDir()
                    if (params.SOURCE_MODE == 'LOCAL') {
                        sh 'git -c safe.directory=/opt/project clone --no-hardlinks --branch "$BUILD_BRANCH" /opt/project .'
                    } else {
                        git branch: params.BUILD_BRANCH, url: env.REPOSITORY_URL
                    }
                    env.IMAGE_TAG = sh(script: 'git rev-parse --short=12 HEAD', returnStdout: true).trim()
                }
            }
        }
        stage('Maven verify') {
            steps { sh 'mvn -B -ntp clean verify' }
            post {
                always { junit testResults: 'target/surefire-reports/*.xml' }
                success { archiveArtifacts artifacts: 'target/*.jar,target/site/jacoco/**', fingerprint: true }
            }
        }
        stage('Sonar quality gate') {
            when { expression { params.RUN_SONAR } }
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh 'mvn -B -ntp sonar:sonar -Dsonar.qualitygate.wait=true -Dsonar.qualitygate.timeout=600'
                    }
                }
            }
        }
        stage('Publish JAR to Nexus') {
            when { expression { params.PUBLISH_NEXUS } }
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh 'mvn -B -ntp -s devops/maven/settings.xml deploy -DskipTests -Dnexus.url="$NEXUS_URL"'
                }
            }
        }
        stage('Build Docker image') {
            when { expression { params.BUILD_IMAGE } }
            steps { sh 'docker build --target artifact -t events-logistics:local -t "events-logistics:$IMAGE_TAG" .' }
        }
        stage('Push to Docker Hub') {
            when { expression { params.PUSH_IMAGE } }
            steps {
                script {
                    if (!params.BUILD_IMAGE || !(params.IMAGE_NAMESPACE ==~ /[a-z0-9][a-z0-9_-]*/)) {
                        error('Pushing requires BUILD_IMAGE and a valid Docker Hub namespace')
                    }
                }
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_TOKEN')]) {
                    sh '''
                        set +x
                        export DOCKER_CONFIG=$(mktemp -d)
                        trap 'docker logout >/dev/null 2>&1; rm -rf "$DOCKER_CONFIG"' EXIT
                        printf '%s' "$REGISTRY_TOKEN" | docker login -u "$REGISTRY_USER" --password-stdin
                        docker tag "events-logistics:$IMAGE_TAG" "$IMAGE_NAMESPACE/events-logistics:$IMAGE_TAG"
                        docker push "$IMAGE_NAMESPACE/events-logistics:$IMAGE_TAG"
                    '''
                }
            }
        }
        stage('Validate Terraform') {
            steps {
                sh 'terraform -chdir=infra fmt -check && terraform -chdir=infra init -backend=false -input=false && terraform -chdir=infra validate'
            }
        }
        stage('Deploy to Kubernetes') {
            when { expression { params.DEPLOY_KUBERNETES } }
            steps {
                script { if (!params.BUILD_IMAGE) { error('Deployment requires BUILD_IMAGE') } }
                withCredentials([
                    string(credentialsId: 'mysql-password', variable: 'MYSQL_PASSWORD'),
                    string(credentialsId: 'mysql-root-password', variable: 'MYSQL_ROOT_PASSWORD')
                ]) {
                    sh '''
                        docker save "events-logistics:$IMAGE_TAG" | docker exec -i events-devops-control-plane ctr -n k8s.io images import -
                        sh scripts/deploy-k8s.sh "events-logistics:$IMAGE_TAG"
                    '''
                }
            }
        }
    }
    post {
        success { echo 'Verified logistics delivery completed. See tests, coverage, Sonar and artifact links.' }
        failure { echo 'Delivery stopped at the failing stage. Fix it before publishing/deploying.' }
    }
}
