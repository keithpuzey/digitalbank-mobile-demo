pipeline {
    agent any

    environment {
        ANDROID_HOME = '/opt/Android'
        BMCredentials = credentials('BMCredentials')       // BlazeMeter API key + secret
        perfectotoken = credentials('perfectotoken')       // Perfecto token
    }

    stages {
        stage('Update Configuration') {
            steps {
                script {
                    // Extract API credentials
                    def apiKey = env.BMCredentials_USR
                    def apiSecret = env.BMCredentials_PSW
                    def perfectoToken = env.perfectotoken

                    echo "Using BlazeMeter API Key: ${apiKey}"

                    def configFilePath = 'auto/config.py'

                    // Update config.py with credentials
                    def configContent = readFile(configFilePath)
                    configContent = configContent.replaceAll(/token_BMAPIKey/, apiKey)
                    configContent = configContent.replaceAll(/token_BMAPISecret/, apiSecret)
                    configContent = configContent.replaceAll(/token_PerfectoKey/, perfectoToken)
                    writeFile(file: configFilePath, text: configContent)

                    sh "sudo -u jenkins python3.8 -m pip install --upgrade mysql-connector-python"
                }
            }
        }

        stage('Create Environment - Puppet') {
            steps {
                echo 'Creating environment using Puppet'
                sh 'sudo /usr/local/bin/puppet apply docker_tomcat_host.pp'
            }
        }

        stage('Create Virtual Service and Generate Synthetic Data') {
            steps {
                echo 'Creating Synthetic Data and Virtual Service'
                script {
                    sh 'sudo /usr/bin/python3.8 auto/generatedata.py auto/registration-data-model-full.json 2'
                    sh 'sudo /usr/bin/python3.8 auto/upload-csv-perfecto.py'
                    def updateOutput = sh(script: 'sudo /usr/bin/python3.8 auto/Update_mock.py', returnStdout: true).trim()

                    // Capture endpoint from mock creation
                    def scriptOutput = sh(script: 'sudo /usr/bin/python3.8 auto/Create_mock.py', returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"
                }
            }
        }

        stage('Build Mobile App') {
            steps {
                echo 'Building latest version of Mobile APK'
                sh '''
                    export ANDROID_HOME=$ANDROID_HOME
                    export APP_VERSION=1.4.$BUILD_NUMBER
                    echo "Building version: $APP_VERSION"

                    sed -i "s/<string name=\\"app_version\\">[^<]*<\\/string>/<string name=\\"app_version\\">$APP_VERSION<\\/string>/" ./app/src/main/res/values/strings.xml
                    sed -i 's|http://dbankdemo.com/bank/|http://dbankdemo.com/bank/|' ./app/src/main/java/xyz/digitalbank/demo/Constants/Constant.java
                    sed -i 's|http://dbankdemo.com/bank/|http://dbankdemo.com/bank/|' ./app/src/main/java/xyz/digitalbank/demo/Constants/ConstantsManager.java
                    sed -i 's|http://dbankdemo.com/bank/|http://dbankdemo.com/bank/|' ./app/src/main/java/xyz/digitalbank/demo/Fragments/ConstantsEditActivity.java

                    chmod +x ./gradlew
                    ./gradlew assembleDebug --info
                    ./gradlew assembleDebugAndroidTest --info
                '''
            }
        }

        stage('Upload Mobile App to Perfecto') {
            steps {
                echo 'Uploading latest APK to Perfecto'
                dir("app/build/outputs/apk/debug/") {
                    sh '''
                        curl --location 'https://demo.app.perfectomobile.com/repository/api/v1/artifacts' \
                            -H "Content-Type: multipart/form-data" \
                            -H "Perfecto-Authorization: ${perfectotoken}" \
                            -F 'inputStream=@Digital-Bank-1.4.apk' \
                            -F 'requestPart={"artifactLocator":"PUBLIC:Digital-Bank-wip.apk","tags":["Bank"],"mimeType":"multipart/form-data","override":true,"artifactType":"ANDROID"}' \
                            -v
                    '''
                }
            }
        }

        stage('Execute Mobile Registration Test - Perfecto') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Running User Registration Tests on Mobile Devices - Perfecto'
                    script {
                        boolean shouldRerun = true
                        while (shouldRerun) {
                            def scriptOutput = sh(script: 'sudo /usr/bin/python3.8 auto/run_scriptless_test.py', returnStdout: true).trim()
                            def reasonMatch = scriptOutput =~ /Reason: (.+)/
                            def testGridReportUrlMatch = scriptOutput =~ /Test Grid Report: (.+)/
                            def devicesMatch = scriptOutput =~ /Devices: (.+)/

                            echo "Mobile Test Overview:"
                            echo "Test Grid Report URL: ${testGridReportUrlMatch ? testGridReportUrlMatch[0][1].trim() : 'N/A'}"
                            echo "Devices: ${devicesMatch ? devicesMatch[0][1].trim() : 'N/A'}"
                            echo "Final Status: ${reasonMatch ? reasonMatch[0][1].trim() : 'N/A'}"

                            if (reasonMatch && reasonMatch[0][1].trim() == 'ResourcesUnavailable') {
                                echo 'Resources unavailable. Rerunning the script...'
                                shouldRerun = true
                            } else {
                                shouldRerun = false
                            }
                        }
                    }
                }
            }
        }

        stage('Execute Load and EUX Test - BlazeMeter') {
            steps {
                echo 'Running Load and EUX Tests'
                script {
                    def scriptOutput = sh(script: 'sudo /usr/bin/python3.8 auto/run_perf_multi_test_param.py $BlazeMeterTest', returnStdout: true).trim()
                    def testUrlMatch = scriptOutput =~ /Test URL (.+)/
                    env.TEST_URL = testUrlMatch ? testUrlMatch[0][1].trim() : ''
                    echo "Test URL: ${env.TEST_URL ?: 'Not found'}"
                }
            }
        }

        stage('Remove Virtual Service') {
            steps {
                echo 'Removing Virtual Service'
                sh 'sudo /usr/bin/python3.8 auto/delete_mock.py'
            }
        }

        stage('Remove Test Environment - Puppet') {
            steps {
                echo 'Removing Test Environment'
                sh 'sudo /usr/local/bin/puppet apply remove_tomcat_host.pp'
            }
        }
    }
}