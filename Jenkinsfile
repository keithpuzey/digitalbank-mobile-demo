pipeline {
    agent any

    environment {
        ANDROID_HOME = '/opt/Android'
        BMCredentials = credentials('BMCredentials')
        PerfectoToken = credentials('Demo-Perfecto')
    }

    stages {

        stage('Setup Python Environment') {
            steps {
                script {
                    // Install Python dependencies for Jenkins user
                    sh '''
                        python3.8 -m pip install --user --upgrade pip
                        python3.8 -m pip install --user requests mysql-connector-python
                    '''
                }
            }
        }

        stage('Update Configuration') {
            steps {
                script {
                    echo "BMCredentials: ****"
                    echo "PerfectoToken: ****"
                    
                    // Update config.py with the correct credentials
                    def configFile = './auto/config.py'
                    def content = readFile(configFile)
                    content = content.replaceAll(/token_BMCredentials/, env.BMCredentials)
                    content = content.replaceAll(/token_PerfectoToken/, env.PerfectoToken)
                    writeFile(file: configFile, text: content)
                    
                    echo 'Configuration updated successfully.'
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
                    sh 'python3.8 ./auto/generatedata.py ./auto/registration-data-model-full.json 2'
                    sh 'python3.8 ./auto/upload-csv-perfecto.py'
                    sh 'python3.8 ./auto/Update_mock.py'
                    sh 'python3.8 ./auto/Create_mock.py'
                }
            }
        }

        stage('Build Mobile App') {
            steps {
                echo 'Creating Latest Version of Mobile APK'
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
                            -H 'Perfecto-Authorization: '${PerfectoToken} \
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
                            def scriptOutput = sh(script: 'python3.8 ./auto/run_scriptless_test.py', returnStdout: true).trim()
                            echo "Script Output: ${scriptOutput}"
                            def reasonMatch = scriptOutput =~ /Reason: (.+)/
                            def reason = reasonMatch ? reasonMatch[0][1].trim() : null
                            shouldRerun = (reason == 'ResourcesUnavailable')
                        }
                    }
                }
            }
        }

        stage('Execute Load and EUX Test - BlazeMeter') {
            steps {
                script {
                    def scriptOutput = sh(script: 'python3.8 ./auto/run_perf_multi_test_param.py $BlazeMeterTest', returnStdout: true).trim()
                    def testUrlMatch = scriptOutput =~ /Test URL (.+)/
                    env.TEST_URL = testUrlMatch ? testUrlMatch[0][1].trim() : ''
                    echo "Test URL: ${env.TEST_URL}"
                }
            }
        }

        stage('Cleanup') {
            steps {
                echo 'Removing Virtual Service and Test Environment'
                sh 'python3.8 ./auto/delete_mock.py'
                sh 'sudo /usr/local/bin/puppet apply remove_tomcat_host.pp'
            }
        }
    }
}
