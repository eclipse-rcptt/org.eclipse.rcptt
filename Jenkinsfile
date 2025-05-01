/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
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
     timestamps()
     buildDiscarder(logRotator(numToKeepStr: '10', daysToKeepStr: '10', removeLastBuild: true, artifactNumToKeepStr: '1'))
     disableConcurrentBuilds()
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
