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
                    echo 'Setting up DCT configuration for Jenkins user'
                    sh "sudo -u jenkins /src/dct-toolkit create_config dctUrl=${env.dctUrl} apiKey=${env.dctApiKey} --insecureSSL --unsafeHostnameCheck"
                }
            }
        }

        stage('Create Environment - Puppet') {
            steps {
                echo 'Creating environment using Puppet'
                sh 'sudo /usr/local/bin/puppet apply docker_tomcat_host.pp'
            }
        }

        stage('Revert Database to Snapshot - Delphix') {
            steps {
                script {
                    // Read environment variables from Jenkins
                    def snapshotid = env.snapshotid
                    def snapshotvdb = env.snapshotvdb
                    echo 'Registered Users in Database Before Snapshot Refresh'
                    sh 'sudo chmod 777 ./auto/listbankusers.sh'
                    sh 'sudo ./auto/listbankusers.sh'
                    echo 'Revert Database to Snapshot'
                    sh "sudo /usr/bin/python ./auto/delphix_synch.py ${snapshotvdb} ${snapshotid}"
                    echo 'Registered Users in Database after Snapshot Refresh'
                    sh 'sudo ./auto/listbankusers.sh'
                }
            }
        }

        stage('Create Mock Service and Generate Synthetic Data') {
            steps {
                echo 'Creating Synthetic Data and Mock Service'
                script {
                    sh 'sudo /usr/bin/python ./auto/generatedata.py ./auto/registration-data-model-full.json 2'

                    sh 'sudo /usr/bin/python ./auto/upload-csv-perfecto.py'
                    def updateOutput = sh(script: 'sudo /usr/bin/python ./auto/Update_mock.py', returnStdout: true).trim()

                    // Extract the endpoint details using regular expressions
                    // Execute the script and capture the output
                    def scriptOutput = sh(script: 'sudo /usr/bin/python ./auto/Create_mock.py', returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"

                }
            }
        }


        stage('Build Mobile App') {
            steps {
                echo 'Create Latest Version of Mobile APK'
                sh '''
                    export ANDROID_HOME=$ANDROID_HOME
                    ls -lia $ANDROID_HOME
                    echo $BUILD_NUMBER
                    export APP_VERSION=1.4.$BUILD_NUMBER
                    sed -i "s/<string name=\\"app_version\\">[^<]*<\\/string>/<string name=\\"app_version\\">$APP_VERSION<\\/string>/" ./app/src/main/res/values/strings.xml
                    # Update the BASE_URL to redirect to dev environment
                    sed -i 's|http://dbankdemo.com/bank/|http://dev.dbankdemo.com/bank/|' ./app/src/main/java/xyz/digitalbank/demo/Constants/Constant.java
                    sed -i 's|http://dbankdemo.com/bank/|http://dev.dbankdemo.com/bank/|' ./app/src/main/java/xyz/digitalbank/demo/Constants/ConstantsManager.java
                    sed -i 's|http://dbankdemo.com/bank/|http://dev.dbankdemo.com/bank/|' ./app/src/main/java/xyz/digitalbank/demo/Fragments/ConstantsEditActivity.java

                    /opt/gradle/gradle/bin/gradle assembleDebug --info
                    /opt/gradle/gradle/bin/gradle assembleDebugAndroidTest --info
                '''
            }
        }

        stage('Upload Mobile App to Perfecto') {
            steps {
                echo 'Upload latest build version to Perfecto'
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

        stage('Execute Mobile - Registration Test') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Execute User Registration Tests using Synthetic Data on Mobile Devices - Perfecto'
                    script {
                        boolean shouldRerun = true
                        while (shouldRerun) {
                            def scriptOutput = sh(script: 'sudo /usr/bin/python ./auto/run_scriptless_test.py', returnStdout: true).trim()

                            // Parse the output to extract values
                            def reasonMatch = scriptOutput =~ /Reason: (.+)/
                            def testGridReportUrlMatch = scriptOutput =~ /Test Grid Report: (.+)/
                            def devicesMatch = scriptOutput =~ /Devices: (.+)/

                            def reason = reasonMatch ? reasonMatch[0][1].trim() : null
                            def testGridReportUrl = testGridReportUrlMatch ? testGridReportUrlMatch[0][1].trim() : null
                            def devices = devicesMatch ? devicesMatch[0][1].trim() : null

                            // Print or use the captured values as needed
                            echo "Mobile Test Overview:"
                            echo "Test Grid Report URL: ${testGridReportUrl}"
                            echo "Devices : ${devices}"
                            echo "Final Status: ${reason}"

                            // Check if the script should be rerun based on the reason
                            if (reason == 'ResourcesUnavailable') {
                                echo 'Reason: ResourcesUnavailable. Rerunning the script...'
                                shouldRerun = true
                            } else {
                                shouldRerun = false
                            }
                        }
                    }
                }
            }
        }

        stage('Confirm User Registration has worked') {
            steps {
                    echo 'Registered Users in Database after Registration Test'
                    sh 'sudo chmod 777 ./auto/listbankusers.sh'
                    sh 'sudo ./auto/listbankusers.sh'
            }
        }

        stage('Execute Load and EUX (Mobile and Web) Test') {
            steps {
                echo 'Execute Load and EUX Test - BlazeMeter'
                script {
                    def scriptOutput = sh(script: 'sudo /usr/bin/python ./auto/run_perf_multi_test_param.py $BlazeMeterTest', returnStdout: true).trim()

                    // Extract the test URL from the script output
                    def testUrlMatch = scriptOutput =~ /Test URL (.+)/
                    def testUrl = testUrlMatch ? testUrlMatch[0][1].trim() : null

                    // Assign the test URL to a Jenkins variable
                    if (testUrl) {
                        env.TEST_URL = testUrl
                        echo "Test URL: ${env.TEST_URL}"
                    } else {
                        echo "Test URL not found in the script output."
                    }
                }
            }
        }

        stage('Remove Mock Service') {
            steps {
                echo 'Remove Mock Service'
                sh 'sudo /usr/bin/python ./auto/delete_mock.py'
            }
        }

        stage('Remove Test Environment - Puppet') {
            steps {
                echo 'Remove Test Environment'
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
