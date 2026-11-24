// ApiCoffeSoft_Java/Jenkinsfile
pipeline {
    agent any

    environment {
        APP_NAME      = 'cafesoft-api'
        JAR_NAME      = 'ApiSpringCafeSoft-0.0.1-SNAPSHOT.jar'
        EC2_STAGING   = credentials('ec2-staging-host')
        EC2_PROD      = credentials('ec2-prod-host')
        SSH_KEY       = credentials('ec2-ssh-key')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec'
                }
            }
        }

        stage('Quality Gate – SonarCloud') {
            steps {
                withSonarQubeEnv('SonarCloud') {
                    sh '''./mvnw sonar:sonar \
                        -Dsonar.projectKey=alanronaldosc-dev_ApiCoffeSoft_Java \
                        -Dsonar.organization=alanronaldosc-dev'''
                }
                // Espera el resultado del quality gate (máx 5 min)
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy – Staging') {
            steps {
                sh './mvnw -B package -DskipTests'
                sshagent(['ec2-ssh-key']) {
                    sh """
                        scp -o StrictHostKeyChecking=no \
                            target/${JAR_NAME} \
                            ec2-user@${EC2_STAGING}:/opt/cafesoft-api-staging/
                        ssh ec2-user@${EC2_STAGING} \
                            'sudo systemctl restart cafesoft-api-staging'
                    """
                }
            }
        }

        stage('Smoke Test – Staging') {
            steps {
                sh '''
                    sleep 30
                    for i in 1 2 3 4 5; do
                        curl -f http://${EC2_STAGING}:8081/api/health && exit 0
                        sleep 10
                    done
                    exit 1
                '''
            }
        }

        stage('Aprobación para Producción') {
            when { branch 'master' }
            steps {
                input message: '¿Desplegar a PRODUCCIÓN?',
                      ok: 'Sí, desplegar',
                      submitter: 'admin,lead-dev'
            }
        }

        stage('Deploy – Producción') {
            when { branch 'master' }
            steps {
                sshagent(['ec2-ssh-key']) {
                    sh """
                        scp -o StrictHostKeyChecking=no \
                            target/${JAR_NAME} \
                            ec2-user@${EC2_PROD}:/opt/cafesoft-api/
                        ssh ec2-user@${EC2_PROD} \
                            'sudo systemctl restart cafesoft-api'
                    """
                }
            }
        }

        stage('Health Check – Producción') {
            when { branch 'master' }
            steps {
                sh '''
                    sleep 45
                    for i in 1 2 3 4 5; do
                        curl -f http://${EC2_PROD}:8080/api/health && exit 0
                        sleep 15
                    done
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline completado. Versión en producción."
        }
        failure {
            echo "Pipeline falló. Revisar logs."
            // Aquí puedes agregar notificación por email o Slack
        }
    }
}
