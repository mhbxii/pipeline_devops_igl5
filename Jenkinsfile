pipeline {
    agent any

    stages {

        stage('0 - Infos') {
            steps {
                bat '''
                    @echo off
                    whoami
                    docker context ls
                '''
            }
        }

        stage('A - bat') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub_credentials',
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    bat '''
                        @echo off
                        echo %DOCKERHUB_TOKEN%| docker login -u %DOCKERHUB_USERNAME% --password-stdin
                        if errorlevel 1 exit /b 1
                        echo A - LOGIN OK
                        docker logout
                    '''
                }
            }
        }

        stage('B - powershell sans BOM') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub_credentials',
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    powershell '''
                        $OutputEncoding = New-Object System.Text.UTF8Encoding($false)
                        $env:DOCKERHUB_TOKEN | docker login -u $env:DOCKERHUB_USERNAME --password-stdin
                        if ($LASTEXITCODE -ne 0) { throw "B - LOGIN FAILED" }
                        Write-Host "B - LOGIN OK"
                        docker logout
                    '''
                }
            }
        }

        stage('C - bat avec -p (test seulement)') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub_credentials',
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    bat '''
                        @echo off
                        docker login -u %DOCKERHUB_USERNAME% -p %DOCKERHUB_TOKEN%
                        if errorlevel 1 exit /b 1
                        echo C - LOGIN OK
                        docker logout
                    '''
                }
            }
        }
    }
}