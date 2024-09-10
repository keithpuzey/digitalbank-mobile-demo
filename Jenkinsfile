pipeline {
    agent any

    environment {

        BMCredentials = credentials('BM_CREDENTIALS') // Example of credentials binding
    }

    stages {
        stage('Update Configuration') {
            steps {
                script {
                    // Define the custom function inside the script block
                    def updateConfigFile = { BMCredentials ->
                        def configFilePath = 'auto/config.py'
                        def configFileContent = readFile(configFilePath)
                    // Replace the placeholder "token_BMCredentials" with the actual credentials
                        configFileContent = configFileContent.replaceAll(/"token_BMCredentials"/, "\"${BMCredentials}\"")
                        writeFile(file: configFilePath, text: configFileContent)
                    }

                    // Call the function
                    updateConfigFile(env.BMCredentials)
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
