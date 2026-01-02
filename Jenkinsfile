pipeline {

    agent any

    environment {
        ANDROID_HOME = '/opt/Android'

        // Jenkins credentials binding
        // Creates:  BMCredentials_USR  and  BMCredentials_PSW
        BMCredentials = credentials('BMCredentials')
        perfectotoken = credentials('perfectotoken')
        DCT_API_KEY = credentials('dct-api-key')
    }

    stages {

        stage('Setup Python') {
            steps {
                sh '''
                /usr/bin/python -m venv venv
                . venv/bin/activate
                pip install requests
                '''
            }
        }
        stage('Update Configuration') {
            steps {
                script {
                    def perfectoKey   = env.perfectotoken
                    def bmApiKey      = env.BMCredentials_USR
                    def bmApiSecret   = env.BMCredentials_PSW

                    echo "Using BlazeMeter API Key: ${bmApiKey}"

                    // UPDATE CONFIG.PY (Linux path)
                    def configFilePath = "./auto/config.py"
                    def content = readFile(configFilePath)

                    content = content.replaceAll(/token_PerfectoKey/, perfectoKey)
                    content = content.replaceAll(/token_BMAPIKey/, bmApiKey)
                    content = content.replaceAll(/token_BMAPISecret/, bmApiSecret)

                    writeFile(file: configFilePath, text: content)

                    // Install Python dependency
                    sh "sudo -u jenkins python3.12 -m pip install mysql-connector-python"

                    // echo 'Setting up DCT configuration for Jenkins user'
                    // sh "sudo -u jenkins /src/dct-toolkit create_config dctUrl=${env.dctUrl} apiKey=${env.dctApiKey} --insecureSSL --unsafeHostnameCheck"
                }
            }
        }

        
stage('Revert Database to Snapshot - Delphix') {
    steps {
        script {
            def snapshotid  = params.snapshotid
            def snapshotvdb = params.snapshotvdb
            def dctDir      = "/var/lib/jenkins/.dct-toolkit"
            def defaultCfg  = "${dctDir}/dct-toolkit.properties.default"
            def activeCfg   = "${dctDir}/dct-toolkit.properties"

            echo 'Updating DCT Toolkit configuration'

            withEnv([
                "DCT_API_KEY=${env.DCT_API_KEY}",
                "DCT_URL=${params.DCT_URL}"
            ]) {
                sh """
                    cp ${defaultCfg} ${activeCfg}
                    sed -i "s|^api.key=.*|api.key=\${DCT_API_KEY}|" ${activeCfg}
                    sed -i "s|^dct.url=.*|dct.url=${DCT_URL}|" "${activeCfg}"

                    chown jenkins:jenkins ${activeCfg}
                    chmod 600 ${activeCfg}
                """
            }

            echo 'Reverting Database to Snapshot'
            sh "/usr/bin/python3.12 ./auto/delphix_synch.py ${snapshotvdb} ${snapshotid}"
        }
    }
}

        stage('Create Virtual Service and Generate Synthetic Data') {
            steps {
                script {
                    sh '/usr/bin/python ./auto/generatedata.py ./auto/registration-data-model-full.json 2'
                    sh '/usr/bin/python ./auto/upload-csv-perfecto.py'

                    def outputVS = sh(script: '/usr/bin/python ./auto/Update_mock.py', returnStdout: true).trim()

                    def scriptOutput = sh(script: '/usr/bin/python ./auto/Create_mock.py', returnStdout: true).trim()
                    def endpointMatch = scriptOutput =~ /Virtual Service Started - Endpoint details:? (.*)/
                    echo "🔗 Virtual Service Endpoint: ${endpointMatch ? endpointMatch[0][1].trim() : 'NONE'}"
                }
            }
        }

        stage('Build Mobile App') {
            steps {
                sh '''
                    export ANDROID_HOME=$ANDROID_HOME
                    ls -lia $ANDROID_HOME

                    export APP_VERSION=1.4.$BUILD_NUMBER
                    echo "Building version: $APP_VERSION"

                    sed -i "s/<string name=\\"app_version\\">[^<]*<\\/string>/<string name=\\"app_version\\">$APP_VERSION<\\/string>/" ./app/src/main/res/values/strings.xml

                    # Build using Gradle Wrapper
                    chmod +x ./gradlew
                    ./gradlew assembleDebug --info
                    ./gradlew assembleDebugAndroidTest --info
                '''
            }
        }

        stage('Upload Mobile App to Perfecto') {
            steps {
                dir("app/build/outputs/apk/debug/") {
                    sh '''
                        curl --location 'https://demo.app.perfectomobile.com/repository/api/v1/artifacts' \
                            -H 'Content-Type: multipart/form-data' \
                            -H 'Perfecto-Authorization: '$perfectotoken \
                            -F 'inputStream=@Digital-Bank-1.6.apk' \
                            -F 'requestPart={"artifactLocator":"PUBLIC:Digital-Bank-wip.apk", "tags":["Bank"], "mimeType":"multipart/form-data", "override": true, "artifactType": "ANDROID"}'
                    '''
                }
            }
        }

        stage('Execute User Registration on Mobile - Perfecto') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh '/usr/bin/python -u ./auto/run_scriptless_test.py'
                }
            }
        }

stage('Execute Load & EUX Test') {
    steps {
        script {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                sh """
                    # Run BlazeMeter test and stream logs to console
                    /usr/bin/python -u ./auto/run_perf_multi_test_param.py ${BlazeMeterTest}
                """
            }
        }
    }
}


        stage('Remove Virtual Service') {
            steps { sh '/usr/bin/python ./auto/delete_mock.py' }
        }

    }
}