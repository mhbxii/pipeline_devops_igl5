pipeline {
    agent any

    stages {
        stage('Clean') {
            steps { sh 'mvn clean' }
        }
        stage('Test') {
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
    }

    post {
        always { junit 'target/surefire-reports/*.xml' }
    }
}