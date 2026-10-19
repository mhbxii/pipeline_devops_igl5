pipeline {
    agent any

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
            withCredentials([
                string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY'),
                string(credentialsId: 'aws-session-token', variable: 'AWS_SESSION_TOKEN')
            ]) {
                sh 'AWS_DEFAULT_REGION=us-east-1 aws sts get-caller-identity'
            }
        }
}
    }

    post {
        always { junit 'target/surefire-reports/*.xml' }
    }
}