
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        NEXUS_SNAPSHOT_URL =
            'http://localhost:8081/repository/events-snapshots/'

        MAVEN_ARTIFACT =
            'tn.esprit:eventsProject:1.0.0-SNAPSHOT:jar'
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
                bat 'docker --version'
                bat 'docker info'
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
                    artifacts: 'target/eventsProject-1.0.0-SNAPSHOT.jar',
                    fingerprint: true
                )
            }
        }

        stage('5 - SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeLocal') {
                    bat '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
                    '''
                }
            }
        }

        stage('6 - Publish to Nexus') {
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

        stage('7 - Download JAR from Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus_secret',
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    bat '''
                        if exist target\\nexus-artifact (
                            rmdir /S /Q target\\nexus-artifact
                        )

                        mkdir target\\nexus-artifact

                        mvn -B -s settings.xml ^
                          org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy ^
                          "-Dartifact=%MAVEN_ARTIFACT%" ^
                          "-DoutputDirectory=target/nexus-artifact" ^
                          "-Dmdep.stripVersion=true" ^
                          "-DremoteRepositories=events-nexus::default::%NEXUS_SNAPSHOT_URL%"

                        if errorlevel 1 exit /b 1

                        if not exist "target\\nexus-artifact\\eventsProject.jar" (
                            echo ERROR: JAR not downloaded from Nexus
                            exit /b 1
                        )

                        copy /Y "target\\nexus-artifact\\eventsProject.jar" "target\\app.jar"

                        if errorlevel 1 exit /b 1
                    '''

                    archiveArtifacts(
                        artifacts: 'target/app.jar',
                        fingerprint: true
                    )
                }
            }
        }

        stage('8 - Docker Build and Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub_credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    bat '''
                        docker build ^
                          -t "%DOCKERHUB_USERNAME%/events-project:%BUILD_NUMBER%" .
                    '''

                    powershell '''
                        $ErrorActionPreference = "Stop"

                        try {
                            $env:DOCKERHUB_TOKEN |
                                docker login `
                                    --username $env:DOCKERHUB_USERNAME `
                                    --password-stdin

                            if ($LASTEXITCODE -ne 0) {
                                throw "Docker Hub login failed."
                            }

                            docker push "$env:DOCKERHUB_USERNAME/events-project:$env:BUILD_NUMBER"

                            if ($LASTEXITCODE -ne 0) {
                                throw "Docker Hub push failed."
                            }
                        }
                        finally {
                            docker logout 2>$null
                        }
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Succes : build, tests, SonarQube, Nexus et Docker Hub.'
        }

        failure {
            echo 'Echec : consultez la Console Output de Jenkins.'
        }

        always {
            echo 'Pipeline terminee.'
        }
    }
}
