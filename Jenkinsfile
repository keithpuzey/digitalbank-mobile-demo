pipeline {
    agent any

    environment {
        ANDROID_HOME = '/opt/Android'
        BMCredentials = credentials('BMCredentials')
        PerfectoToken = credentials('Demo-Perfecto')
    }

    stages {

        stage('Verify Python Scripts') {
            steps {
                echo 'Checking that Python scripts exist and are valid'
                sh 'ls -la ${WORKSPACE}/auto'
                sh 'head -n 5 ${WORKSPACE}/auto/generatedata.py'
            }
        }

        stage('Install Python Packages') {
            steps {
                echo 'Installing required Python packages'
                sh 'python3.8 -m pip install --upgrade pip'
                sh 'python3.8 -m pip install requests mysql-connector-python'
            }
        }

        stage('Update Configuration') {
            steps {
                script {
                    // Update config.py with Jenkins credentials
                    def configFilePath = "${WORKSPACE}/auto/config.py"
                    def configFileContent = readFile(configFilePath)

                    configFileContent = configFileContent.replaceAll(/token_perfectotoken/, env.PerfectoToken)
                    configFileContent = configFileContent.replaceAll(/token_BMCredentials/, env.BMCredentials)

                    writeFile(file: configFilePath, text: configFileContent)
                    echo 'config.py updated with Jenkins credentials'
                }
            }
        }

        stage('Generate Synthetic Data') {
            steps {
                echo "BMCredentials is set: ${env.BMCredentials}"
                sh "python3.8 ${WORKSPACE}/auto/generatedata.py ${WORKSPACE}/auto/registration-data-model-full.json 2"
            }
        }

        stage('Upload CSV to Perfecto') {
            steps {
                sh "python3.8 ${WORKSPACE}/auto/upload-csv-perfecto.py"
            }
        }

        stage('Create Virtual Service') {
            steps {
                def scriptOutput = sh(script: "python3.8 ${WORKSPACE}/auto/Create_mock.py", returnStdout: true).trim()
                def endpointMatch = scriptOutput =~ /Mock Service Started - Endpoint details (.+)/
                def endpoint = endpointMatch ? endpointMatch[0][1].trim() : null
                echo "Mock Service Endpoint: ${endpoint}"
            }
        }

        stage('Build Mobile App') {
            steps {
                sh '''
                    export ANDROID_HOME=$ANDROID_HOME
                    export APP_VERSION=1.4.$BUILD_NUMBER
                    /opt/gradle/gradle/bin/gradle assembleDebug --info
                    /opt/gradle/gradle/bin/gradle assembleDebugAndroidTest --info
                '''
            }
        }

        stage('Upload Mobile App to Perfecto') {
            steps {
                dir("${WORKSPACE}/app/build/outputs/apk/debug/") {
                    sh '''
                        curl --location 'https://demo.app.perfectomobile.com/repository/api/v1/artifacts' \
                             -H "Perfecto-Authorization: $PerfectoToken" \
                             -F "inputStream=@Digital-Bank-1.4.apk" \
                             -F 'requestPart={"artifactLocator":"PUBLIC:Digital-Bank-wip.apk", "tags":["Bank"], "mimeType":"multipart/form-data", "override": true, "artifactType": "ANDROID"}' \
                             -v
                    '''
                }
            }
        }

        stage('Execute Mobile Registration Test') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh "python3.8 ${WORKSPACE}/auto/run_scriptless_test.py"
                }
            }
        }

        stage('Cleanup Virtual Service') {
            steps {
                sh "python3.8 ${WORKSPACE}/auto/delete_mock.py"
            }
        }
    }
}
