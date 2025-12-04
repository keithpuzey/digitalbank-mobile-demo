
pipeline {
    agent any

    // Jenkins environment variables
    environment {
        ANDROID_HOME = '/opt/Android'
        BMCredentials = credentials('BMCredentials')
        PerfectoToken = credentials('Demo-Perfecto')
    }

    stages {

        stage('Update Configuration') {
            steps {
                script {
                    echo "BMCredentials: ****"
                    echo "PerfectoToken: ****"

                    // Install Python dependency for Jenkins user
                    sh 'python3.8 -m pip install --user mysql-connector-python'

                    // Update config.py file with tokens
                    updateConfigFile(env.PerfectoToken, env.BMCredentials)

                    echo 'Setting up DCT configuration for Jenkins user'
                    sh "/src/dct-toolkit create_config dctUrl=${env.dctUrl} apiKey=${env.dctApiKey} --insecureSSL --unsafeHostnameCheck"
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
                script {
                    echo 'Creating Synthetic Data and Virtual Service'

                    sh 'echo "BMCredentials is set: $BMCredentials"'
                    sh "python3.8 ./auto/generatedata.py ./auto/registration-data-model-full.json 2"
                    sh "python3.8 ./auto/upload-csv-perfecto.py"

                    def updateOutput = sh(script: 'python3.8 ./auto/Update_mock.py', returnStdout: true).trim()

                    def scriptOutput = sh(script: 'python3.8 ./auto/Create_mock.py', returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details (.+)/
                    def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                    echo "Mock Service Endpoint: ${endpoint}"
                }
            }
        }

        stage('Build Mobile App') {
            steps {
                echo 'Build Latest Version of Mobile APK'
                sh '''
                    export ANDROID_HOME=$ANDROID_HOME
                    export APP_VERSION=1.4.$BUILD_NUMBER
                    sed -i "s/<string name=\\"app_version\\">[^<]*<\\/string>/<string name=\\"app_version\\">$APP_VERSION<\\/string>/" ./app/src/main/res/values/strings.xml

                    /opt/gradle/gradle/bin/gradle assembleDebug --info
                    /opt/gradle/gradle/bin/gradle assembleDebugAndroidTest --info
                '''
            }
        }

        stage('Upload Mobile App to Perfecto') {
            steps {
                dir("/var/lib/jenkins/workspace/DBank Mobile Pipeline/app/build/outputs/apk/debug/") {
                    sh '''
                        curl --location 'https://demo.app.perfectomobile.com/repository/api/v1/artifacts' \
                            -H 'Content-Type: multipart/form-data' \
                            -H 'Perfecto-Authorization: '$PerfectoToken \
                            -F 'inputStream=@Digital-Bank-1.4.apk' \
                            -F 'requestPart={"artifactLocator":"PUBLIC:Digital-Bank-wip.apk", "tags":["Bank"], "mimeType":"multipart/form-data", "override": true, "artifactType": "ANDROID"}' \
                            -v
                    '''
                }
            }
        }

        stage('Execute Mobile Registration Test - Perfecto') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    script {
                        boolean shouldRerun = true
                        while (shouldRerun) {
                            def output = sh(script: 'python3.8 ./auto/run_scriptless_test.py', returnStdout: true).trim()

                            def reasonMatch = output =~ /Reason: (.+)/
                            def testGridReportUrlMatch = output =~ /Test Grid Report: (.+)/
                            def devicesMatch = output =~ /Devices: (.+)/

                            def reason = reasonMatch ? reasonMatch[0][1].trim() : null
                            def testGridReportUrl = testGridReportUrlMatch ? testGridReportUrlMatch[0][1].trim() : null
                            def devices = devicesMatch ? devicesMatch[0][1].trim() : null

                            echo "Mobile Test Overview:"
                            echo "Test Grid Report URL: ${testGridReportUrl}"
                            echo "Devices : ${devices}"
                            echo "Final Status: ${reason}"

                            shouldRerun = (reason == 'ResourcesUnavailable')
                            if (shouldRerun) {
                                echo 'Resources unavailable. Rerunning the script...'
                            }
                        }
                    }
                }
            }
        }

        stage('Execute Load and EUX Test') {
            steps {
                script {
                    def output = sh(script: "python3.8 ./auto/run_perf_multi_test_param.py $BlazeMeterTest", returnStdout: true).trim()
                    def testUrlMatch = output =~ /Test URL (.+)/
                    def testUrl = testUrlMatch ? testUrlMatch[0][1].trim() : null

                    if (testUrl) {
                        env.TEST_URL = testUrl
                        echo "Test URL: ${env.TEST_URL}"
                    } else {
                        echo "Test URL not found."
                    }
                }
            }
        }

        stage('Remove Virtual Service') {
            steps {
                sh 'python3.8 ./auto/delete_mock.py'
            }
        }

        stage('Remove Test Environment - Puppet') {
            steps {
                sh 'sudo /usr/local/bin/puppet apply remove_tomcat_host.pp'
            }
        }
    }
}

// Helper method to update config.py
def updateConfigFile(perfectoToken, BMCredentials) {
    def configFilePath = './auto/config.py'
    def content = readFile(configFilePath)

    content = content.replaceAll(/token_perfectotoken/, perfectoToken)
    content = content.replaceAll(/token_BMCredentials/, BMCredentials)

    writeFile(file: configFilePath, text: content)
}
