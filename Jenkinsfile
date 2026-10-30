pipeline {
    agent any // S'exécute sur n'importe quel agent disponible

    stages {
        // Étape 1 : Récupération du code source depuis le gestionnaire de code (ex: Git)
        stage('1 - Checkout') {
            steps {
                checkout scm
            }
        }

        // Étape 2 : Votre stage de vérification des outils (sur Windows)
        stage('9 - Verify AWS Tools') {
            steps {
                // Utilisation de 'bat' car les agents Jenkins utilisent l'invite de commande Windows (cmd)
                bat 'aws --version'
                bat 'terraform --version'
                bat 'kubectl version --client'
            }
        }
        
        // Vous pouvez ajouter d'autres stages ici (ex: terraform plan, apply, etc.)
    }

    post {
        always {
            echo "Pipeline terminée."
        }
        failure {
            echo "La pipeline a échoué. Vérifiez si AWS CLI, Terraform ou kubectl sont bien installés et accessibles dans le PATH de l'agent."
        }
    }
}