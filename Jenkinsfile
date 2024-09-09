
pipeline {
    agent any

    environment {

    }

    stages {
        stage('Update Configuration') {
            steps {
                script {
                    // Read environment variables from Jenkins
                    def perfectotoken = env.perfectotoken
                    def BMCredentials = env.BMCredentials
   
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
                echo 'Running Stage sv1...'
                    // Execute the script and capture the output
                    def scriptOutput = sh(script: 'sudo /usr/bin/python ./auto/Create_mock.py --name "SV1"', returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"
            }
        }
        stage('sv2') {
            when {
                expression {
                    return isStageInCommitMessage('sv2')
                }
            }
            steps {
                echo 'Running Stage sv2...'
                       // Execute the script and capture the output
                    def scriptOutput = sh(script: 'sudo /usr/bin/python ./auto/Create_mock.py  --name "SV2"', returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"
            }
        }
        stage('sv3') {
            when {
                expression {
                    return isStageInCommitMessage('sv3')
                }
            }
            steps {
                echo 'Running Stage sv3...'
                        // Execute the script and capture the output
                    def scriptOutput = sh(script: 'sudo /usr/bin/python ./auto/Create_mock.py  --name "SV3"', returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"
            }
        }

 
    }
}

def updateConfigFile(perfectotoken, BMCredentials) {
    // Define the path to your config.py file
    def configFilePath = '\\auto\\config.py'

    // Read the content of the config.py file
    def configFileContent = readFile(configFilePath)

    // Modify the content with the new tokens
    configFileContent = configFileContent.replaceAll(/token_perfectotoken/, perfectotoken)
    configFileContent = configFileContent.replaceAll(/token_BMCredentials/, BMCredentials)

    // Write the updated content back to the config.py file
    writeFile(file: configFilePath, text: configFileContent)
}

def isStageInCommitMessage(String stageName) {
    // Get the latest commit message
    def commitMessage = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()

    // Check if the commit message contains the stage name
    return commitMessage.contains(stageName)
}
