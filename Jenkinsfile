def library
node {
  checkout scm
  def result = load('releng/Jenkinsfile.groovy')(this)
  assert result != null
  library = result
}

pipeline {
  agent {
    kubernetes {
      yaml library.YAML_BUILD_AGENT
    }
  }

  options {
     buildDiscarder(logRotator(numToKeepStr: '3', daysToKeepStr: '10'))
     disableConcurrentBuilds()
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
