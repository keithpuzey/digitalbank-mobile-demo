pipeline {
    agent any

    environment {
        ANDROID_HOME = '/opt/Android'
    }

    stages {
        stage('Update Config') {
            steps {
                script {
                    // Read environment variables from Jenkins
                    def perfectotoken = env.perfectotoken
                    def BMCredentials = env.BMCredentials

                    // Update config.py file with the tokens
                    updateConfigFile(perfectotoken, BMCredentials)
                }
            }
        }

        stage('Deploy Puppet Manifest') {
            steps {
                sh 'sudo /usr/local/bin/puppet apply docker_tomcat_host.pp'
            }
        }

        stage('Create Mock Service and Generate Data') {
            steps {
                script {
                    sh 'sudo /usr/bin/python ./auto/Create_mock.py'
                    sh 'sudo /usr/bin/python ./auto/generatedata.py '/var/lib/jenkins/workspace/DBank Mobile Pipeline/auto/results/registration-data-model-full.json' 25'
                }
            }
        }

        stage('Set Environment and Build APK') {
            steps {
                script {
                    sh '''
                        export ANDROID_HOME=$ANDROID_HOME
                        ls -lia $ANDROID_HOME
                        echo $BUILD_NUMBER
                        export APP_VERSION=1.3.$BUILD_NUMBER
                        sed -i "s/<string name=\\"app_version\\">[^<]*<\\/string>/<string name=\\"app_version\\">$APP_VERSION<\\/string>/" ./app/src/main/res/values/strings.xml
                        /opt/gradle/gradle/bin/gradle assembleDebug --info
                        /opt/gradle/gradle/bin/gradle assembleDebugAndroidTest --info
                    '''
                }
            }
        }

        stage('Upload APK') {
            steps {
                script {
                    // Assuming the APK is located at /var/lib/jenkins/workspace/Digital Bank Mobile/app/build/outputs/apk/debug/
                    dir("/var/lib/jenkins/workspace/Digital Bank Mobile/app/build/outputs/apk/debug/") {
                        sh '''
                            curl --location 'https://demo.app.perfectomobile.com/repository/api/v1/artifacts' \
                                -H 'Content-Type: multipart/form-data' \
                                -H 'Perfecto-Authorization: '$perfectotoken \
                                -F 'inputStream=@Digital-Bank-1.3.apk' \
                                -F 'requestPart={"artifactLocator":"PUBLIC:Digital-Bank-wip.apk", "tags":["Bank"], "mimeType":"multipart/form-data", "override": true, "artifactType": "ANDROID"}' \
                                -v
                        '''
                    }
                }
            }
        }

        stage('Execute Mobile and Load Test') {
            steps {
               sh 'sudo /usr/bin/python ./auto/delete_mock.py'
            }
        }

        stage('Remove Mock Service') {
            steps {
               sh 'sudo /usr/bin/python ./auto/delete_mock.py'
            }
        }

        stage('Remove Puppet Manifest') {
            steps {
                sh 'sudo /usr/local/bin/puppet apply remove_tomcat_host.pp'
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
