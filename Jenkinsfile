
pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('1 - Checkout') {
            steps {
                echo 'Recuperation du code source depuis Git...'
                checkout scm
            }
        }

        stage('2 - Build and Test') {
            steps {
                echo 'Compilation et execution des tests Maven...'
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

        stage('3 - SonarQube Analysis') {
            steps {
                echo 'Analyse du code avec SonarQube...'

                withSonarQubeEnv('SonarQube') {
                    bat 'mvn -B sonar:sonar'
                }
            }
        }

        stage('4 - Quality Gate') {
            steps {
                echo 'Verification du Quality Gate SonarQube...'

                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()

                        if (qg.status != 'OK') {
                            error "Quality Gate echoue : ${qg.status}"
                        }

                        echo 'Quality Gate valide.'
                    }
                }
            }
        }

        stage('5 - Publish to Nexus') {
            steps {
                echo 'Publication de l artefact dans Nexus...'

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
            echo 'SUCCES : tests, analyse SonarQube et publication Nexus termines.'
        }

        failure {
            echo 'ECHEC : consultez la console Jenkins pour identifier le probleme.'
        }

        always {
            echo 'Fin de l execution du pipeline.'
        }
    }
}
