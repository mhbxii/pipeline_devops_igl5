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
        stage('Package') {
            steps { sh 'mvn package -DskipTests' }
        }
    }

    post {
        always { junit 'target/surefire-reports/*.xml' }
    }
}