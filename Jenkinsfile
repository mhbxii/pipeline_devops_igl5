
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
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
                    artifacts: 'target/*.jar',
                    fingerprint: true
                )
            }
        }

        stage('5 - SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeLocal') {
                    bat 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar'
                }
            }
        }

        stage('7 - Publish to Nexus') {
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
    }

    post {
        success {
            echo 'Succes : build, tests, SonarQube et publication Nexus.'
        }

        failure {
            echo 'Echec : consultez la Console Output de Jenkins.'
        }

        always {
            echo 'Pipeline termine.'
        }
    }
}
