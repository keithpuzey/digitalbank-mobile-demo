pipeline {
    agent any

    environment {
        ANDROID_HOME = '/opt/Android'
    }

    stages {
        stage('Update Configuration') {
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

        stage('Create Environment -  Puppet') {
            steps {
                description 'Creating environment using Puppet'
                sh 'sudo /usr/local/bin/puppet apply docker_tomcat_host.pp'
            }
        }
        stage('Synch Masked Production Data - Delphix') {
            description 'Prepare Data and make available to testing platform'
            steps {
               sh 'sudo /usr/bin/python ./auto/delphix_synch.py'
               }
            }
        }
        stage('Create Mock Service and Generate Synthetic Data') {
            steps {
                description 'Creating Synthetic Data and Mock Service'
                script {
                    sh 'sudo /usr/bin/python ./auto/Create_mock.py'
                    sh 'sudo /usr/bin/python ./auto/generatedata.py ./auto/registration-data-model-full.json 2'
                    sh 'sudo /usr/bin/python ./auto/Update_mock.py'
                    sh 'sudo /usr/bin/python ./auto/upload-csv-perfecto.py'
                }
            }
        }

        stage('Build Mobile App') {
            steps {
                description 'Create LAtest Version of Mobile APK'
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

        stage('Upload Mobile App to Perfecto') {
            steps {
                description 'Upload latest build version to Perfecto'
                script {
                    // Assuming the APK is located at /var/lib/jenkins/workspace/Digital Bank Mobile/app/build/outputs/apk/debug/
                    dir("/var/lib/jenkins/workspace/DBank Mobile Pipeline/app/build/outputs/apk/debug/") {
                        sh '''
                            curl --location 'https://demo.app.perfectomobile.com/repository/api/v1/artifacts' \
                                -H 'Content-Type: multipart/form-data' \
                                -H 'Perfecto-Authorization: '$perfectotoken \
                                -F 'inputStream=@Digital-Bank-1.4.apk' \
                                -F 'requestPart={"artifactLocator":"PUBLIC:Digital-Bank-wip.apk", "tags":["Bank"], "mimeType":"multipart/form-data", "override": true, "artifactType": "ANDROID"}' \
                                -v
                        '''
                    }
                }
            }
        }

        stage('Execute Mobile - Registration Test') {
            steps {
                description 'Execute User Regsitration Tests using Synthetic Data on Mobile Devices - Perfecto'
                script {
                    def scriptOutput = sh(script: 'sudo /usr/bin/python ./auto/run_scriptless_test.py', returnStdout: true).trim()

                    // Capture environment variables
                    def reason = sh(script: 'echo $reason', returnStdout: true).trim()
                    def testGridReportUrl = sh(script: 'echo $TEST_GRID_REPORT_URL', returnStdout: true).trim()
                    def devices = sh(script: 'echo $devices', returnStdout: true).trim()

                    // Print or use the captured values as needed
                    echo "Mobile Test Overview:"
                    echo "Execution Reason: ${reason}"
                    echo "Test Grid Report URL: ${testGridReportUrl}"
                    echo "Devies : ${devices}"
                }
            }
        }

        stage('Execute  Load and EUX (Mobile and Web)  Test') {
            steps {
                description 'Execute Load and EUX Test - BlazeMeter'
                sh 'sudo /usr/bin/python ./auto/run_perf_multi_test_param.py $BlazeMeterTest'
            }
        }

        stage('Remove Mock Service') {
            steps {
                description 'Remove Mock Service'
                sh 'sudo /usr/bin/python ./auto/delete_mock.py'
            }
        }

        stage('Remove Test Environment - Puppet') {
            steps {
                description 'Remove Test Environment'
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
