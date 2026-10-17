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
        stage('Docker build') {
            steps {
                sh 'docker build -t mhbx12/eventsproject:${BUILD_NUMBER} -t mhbx12/eventsproject:latest .'
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                    sh 'echo $DH_PASS | docker login -u $DH_USER --password-stdin'
                    sh 'docker push mhbx12/eventsproject:${BUILD_NUMBER}'
                    sh 'docker push mhbx12/eventsproject:latest'
                }
            }
        }
    }

    post {
        always { junit 'target/surefire-reports/*.xml' }
    }
}