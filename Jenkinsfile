def library
node {
  checkout scm
  result = load('releng/Jenkinsfile.groovy')(this)
  assert result != null
  library = result
}

pipeline {
  agent {
    kubernetes {
      label 'rcptt-build-agent-3.5.4'
      yaml library.YAML_BUILD_AGENT
    }
  }

  options {
     timestamps()
     buildDiscarder(logRotator(numToKeepStr: '3', daysToKeepStr: '10'))
     disableConcurrentBuilds()
     timestamps ()
     timeout(time: 10, unit: 'HOURS')
  }

  stages {
    stage('Start Build and Test') {
      steps {
        script {
          library.build_and_test(false)
        }
      }
    }
  }

  post {
    always {
      script {
        library.post_build_actions()
      }
    }
  }
}
