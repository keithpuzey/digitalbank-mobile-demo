pipeline {
    agent any

    environment {
        ANDROID_HOME = '/opt/Android'
        // Inject Jenkins secret text credentials
        BMCredentials = credentials('BMCredentials')
        PerfectoToken = credentials('Demo-Perfecto')
    }

    stages {
        stage('Setup Python Environment') {
            steps {
                script {
                    echo 'Setting up Python virtual environment and installing dependencies'
                    sh '''
                        python3.8 -m venv venv
                        source venv/bin/activate
                        pip install --upgrade pip
                        pip install requests mysql-connector-python
                    '''
                }
            }
        }

        stage('Update Configuration') {
            steps {
                script {
                    echo 'Updating config.py with Jenkins credentials'
                    def configFilePath = './auto/config.py'
                    def configFileContent = readFile(configFilePath)

                    // Replace placeholders with actual tokens
                    configFileContent = configFileContent.replaceAll(/token_perfectotoken/, env.PerfectoToken)
                    configFileContent = configFileContent.replaceAll(/token_BMCredentials/, env.BMCredentials)

                    writeFile(file: configFilePath, text: configFileContent)
                    echo 'Configuration updated'
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
                    echo "BMCredentials is set: ${env.BMCredentials}"
                    sh 'source venv/bin/activate && python3.8 ./auto/generatedata.py ./auto/registration-data-model-full.json 2'
                    sh 'source venv/bin/activate && python3.8 ./auto/upload-csv-perfecto.py'

                    def updateOutput = sh(script: 'source venv/bin/activate && python3.8 ./auto/Update_mock.py', returnStdout: true).trim()
                    def scriptOutput = sh(script: 'source venv/bin/activate && python3.8 ./auto/Create_mock.py', returnStdout: true).trim()
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
                        source ../../../../venv/bin/activate
                        curl --location 'https://demo.app.perfectomobile.com/repository/api/v1/artifacts' \
                            -H "Perfecto-Authorization: $PerfectoToken" \
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
                            def scriptOutput = sh(script: 'source venv/bin/activate && python3.8 ./auto/run_scriptless_test.py', returnStdout: true).trim()
                            def reasonMatch = scriptOutput =~ /Reason: (.+)/
                            def testGridReportUrlMatch = scriptOutput =~ /Test Grid Report: (.+)/
                            def devicesMatch = scriptOutput =~ /Devices: (.+)/

                            def reason = reasonMatch ? reasonMatch[0][1].trim() : null
                            def testGridReportUrl = testGridReportUrlMatch ? testGridReportUrlMatch[0][1].trim() : null
                            def devices = devicesMatch ? devicesMatch[0][1].trim() : null

                            echo "Mobile Test Overview:"
                            echo "Test Grid Report URL: ${testGridReportUrl}"
                            echo "Devices: ${devices}"
                            echo "Final Status: ${reason}"

                            shouldRerun = reason == 'ResourcesUnavailable'
                            if (shouldRerun) {
                                echo 'Resources unavailable. Rerunning...'
                            }
                        }
                    }
                }
            }
        }

        stage('Execute Load and EUX (Mobile and Web) Test') {
            steps {
                script {
                    def scriptOutput = sh(script: 'source venv/bin/activate && python3.8 ./auto/run_perf_multi_test_param.py $BlazeMeterTest', returnStdout: true).trim()
                    def testUrlMatch = scriptOutput =~ /Test URL (.+)/
                    def testUrl = testUrlMatch ? testUrlMatch[0][1].trim() : null

                    if (testUrl) {
                        env.TEST_URL = testUrl
                        echo "Test URL: ${env.TEST_URL}"
                    } else {
                        echo "Test URL not found in the script output."
                    }
                }
            }
        }

        stage('Remove Virtual Service') {
            steps {
                sh 'source venv/bin/activate && python3.8 ./auto/delete_mock.py'
            }
        }

        stage('Remove Test Environment - Puppet') {
            steps {
                sh 'sudo /usr/local/bin/puppet apply remove_tomcat_host.pp'
            }
        }
    }
}
