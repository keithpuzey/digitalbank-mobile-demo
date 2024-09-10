pipeline {
    agent any

    environment {
                    // Read environment variables from Jenkins
  
                    def BMCredentials = env.BMCredentials
    }

    stages {
        stage('Update Configuration') {
            steps {
                script {
                    // Define the custom function inside the script block
                      def configFilePath = '\\auto\\config.py'

                      // Read the content of the config.py file
                      def configFileContent = readFile(configFilePath)
                      configFileContent = configFileContent.replaceAll(/token_BMCredentials/, BMCredentials)
                      writeFile(file: configFilePath, text: configFileContent)
                    }
                }
            }

        stage('sv1') {
            when {
                expression {
                    return isStageInCommitMessage('sv1')
                }
            }
            steps {
                script {
                    echo 'Running Stage sv1...'
                    def serviceName = "SV1-${env.BUILD_ID}"
                    def scriptOutput = sh(script: "sudo /usr/bin/python ./auto/Create_mock.py --name '${serviceName}'", returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details: (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"
                }
            }
        }
        stage('sv2') {
            when {
                expression {
                    return isStageInCommitMessage('sv2')
                }
            }
            steps {
                script {
                    echo 'Running Stage sv2...'
                    def serviceName = "SV2-${env.BUILD_ID}"
                    def scriptOutput = sh(script: "sudo /usr/bin/python ./auto/Create_mock.py --name '${serviceName}'", returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details: (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"
                }
            }
        }
        stage('sv3') {
            when {
                expression {
                    return isStageInCommitMessage('sv3')
                }
            }
            steps {
                script {
                    echo 'Running Stage sv3...'
                    def serviceName = "SV3-${env.BUILD_ID}"
                    def scriptOutput = sh(script: "sudo /usr/bin/python ./auto/Create_mock.py --name '${serviceName}'", returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details: (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"
                }
            }
        }
    }
}



def isStageInCommitMessage(String stageName) {
    def commitMessage = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
    return commitMessage.contains(stageName)
}
