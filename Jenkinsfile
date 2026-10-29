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
                @echo off

                echo ===== BUILD =====
                docker build -t "%DOCKERHUB_USERNAME%/events-project:%BUILD_NUMBER%" .
                if errorlevel 1 exit /b 1

                echo ===== LOGIN =====
                echo %DOCKERHUB_TOKEN%| docker login -u %DOCKERHUB_USERNAME% --password-stdin
                if errorlevel 1 (
                    echo ERROR: Docker Hub login failed
                    exit /b 1
                )

                echo ===== PUSH =====
                docker push "%DOCKERHUB_USERNAME%/events-project:%BUILD_NUMBER%"
                if errorlevel 1 (
                    docker logout
                    exit /b 1
                )

                docker logout
                echo SUCCESS: Image published
            '''
        }
    }
}