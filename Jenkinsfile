
pipeline {
  environment {
     build = load('releng/Jenkinsfile.groovy').org.eclipse.rcptt.jenkins.Build.new(this)
  }
  agent {
    kubernetes {
      label 'rcptt-build-agent-3.5.4'
      yaml build.YAML_BUILD_AGENT
    }
  }

  stages {
    stage('Start Build and Test') {
      steps {
        script {
          build.build_and_test(false)
        }
      }
    }
  }

  post {
    always {
      script {
        build.post_build_actions()
      }
    }
  }
}
