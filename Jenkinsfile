pipeline {
    agent any

    environment {
        AWS_ACCESS_KEY_ID     = credentials('aws-access-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
        AWS_SESSION_TOKEN     = credentials('aws-session-token')
        AWS_DEFAULT_REGION    = 'us-east-1'
        TF_STATE              = "${env.HOME}/.tfstate/events.tfstate"
        TF_PLUGIN_CACHE_DIR = "${env.HOME}/.terraform.d/plugin-cache"
    }

    stages {
        stage('Mvn Clean') {
            steps { sh 'mvn clean' }
        }
        stage('Unit Tests') {
            steps { sh 'mvn test' }
        }
        stage('SonarQube') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar'
                }
            }
        }
        stage('Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                    sh 'mvn deploy -DskipTests'
                }
            }
        }
        stage('Building our image') {
            steps {
                sh 'docker build -t mhbx12/eventsproject:${BUILD_NUMBER} -t mhbx12/eventsproject:latest .'
            }
        }
        stage('Deploy our image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                    sh 'echo $DH_PASS | docker login -u $DH_USER --password-stdin'
                    sh 'docker push mhbx12/eventsproject:${BUILD_NUMBER}'
                    sh 'docker push mhbx12/eventsproject:latest'
                }
            }
        }
        stage('Test AWS credentials') {
            steps {
                sh 'aws sts get-caller-identity'
            }
        }
        stage('Setup Terraform') {
            steps {
                dir('terraform') {
                    sh 'mkdir -p $HOME/.tfstate'
                    sh 'terraform init -input=false'
                    sh 'terraform apply -auto-approve -input=false -state=$TF_STATE'
                }
            }
        }
        stage('Update kubeconfig') {
            steps {
                sh 'aws eks update-kubeconfig --name events-cluster --region us-east-1'
                sh 'kubectl get nodes'
            }
        }
        stage('Deploy to AWS Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/mysql.yaml'
                sh 'kubectl rollout status deployment/mysqldb --timeout=300s'
                sh 'sed "s|IMAGE_TAG|${BUILD_NUMBER}|" k8s/deployment.yaml | kubectl apply -f -'
                sh 'kubectl apply -f k8s/service.yaml'
                sh 'kubectl rollout status deployment/events-app --timeout=300s'
                sh 'kubectl get pods,svc -o wide'
            }
        }
}

    post {
        always { junit 'target/surefire-reports/*.xml' }
    }
}