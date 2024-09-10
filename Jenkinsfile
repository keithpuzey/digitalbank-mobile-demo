pipeline {
    agent any
    environment {
        // Use the credentials() helper to bind credentials to environment variables
        BMCredentials = credentials('BMCredentials') // The ID from step 1
    }
    stages {
        stage('Update Configuration') {
            steps {
                script {
                    // Define the custom function inside the script block
                    // Access API key and secret from the environment variables
                    def apiKey = env.BMCredentials_USR  // API key as username
                    def apiSecret = env.BMCredentials_PSW // API secret as password

                    // Use the credentials in your script or pass them to other commands
                    echo "Using API Key: ${apiKey}"
            
                    def configFilePath = '\\auto\\config.py'

                    // Read the content of the config.py file
                    def configFileContent = readFile(configFilePath)
                    configFileContent = configFileContent.replaceAll(/token_BMCredentials/, apikey)
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
