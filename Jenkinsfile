pipeline {
    agent any

    environment {
        ANDROID_HOME = '/opt/Android'
    }

    stages {

        stage('Deploy Puppet Manifest') {
            steps {

                sh 'sudo /usr/local/bin/puppet apply docker_tomcat_host.pp'
            }
        }

        stage('Call API') {
            steps {
                script {
                    // Call your API here
                    sh 'sleep 20'

                }
            }
        }

        stage('Remove Puppet Manifest') {
            steps {

                sh 'sudo /usr/local/bin/puppet apply remove_tomcat_host.pp'
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
    }
}
