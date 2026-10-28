
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Tools') {
            steps {
                bat 'git --version'
                bat 'mvn -version'
                bat 'java -version'
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
        }

        stage('Archive JAR') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar',
                                 fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Build et tests réussis.'
        }

        failure {
            echo 'Échec du pipeline : consulter la Console Output.'
        }

        always {
            echo 'Pipeline terminé.'
        }
    }
}
