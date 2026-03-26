
pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                echo 'Running Maven build and tests...'
                bat 'mvn clean verify'
            }
        }

        stage('Backend - SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                bat 'mvn sonar:sonar'
            }
        }

        stage('Trigger Frontend CI') {
            steps {
                echo 'Triggering frontend CI pipeline...'
                build job: 'group7_frontend', wait: true

            }
        }
    }

    post {
        success {
            echo 'Backend CI pipeline passed!'
        }
        failure {
            echo 'Backend CI pipeline failed.'
        }
    }
}
