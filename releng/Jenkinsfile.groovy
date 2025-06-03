/*******************************************************************************
* Copyright (c) 2020 Xored Software Inc and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-v20.html
*  
* Contributors:
* 	Xored Software Inc - initial API and implementation and/or initial documentation
********************************************************************************/
package org.eclipse.rcptt.jenkins

class Build implements Serializable {
  // The container does not start under the Jenkins user. /home/jenkins is mounted in a container and is a single file space
  private final String BUILD_CONTAINER_NAME="ubuntu"
  private final String BUILD_CONTAINER="""
    - name: $BUILD_CONTAINER_NAME
      image: basilevs/ubuntu-rcptt:3.7.2
      imagePullPolicy: Always
      tty: true
      resources:
        limits:
          memory: "4Gi"
          cpu: "1"
        requests:
          memory: "4Gi"
          cpu: "1"
      env:
      - name: "HOME"
        value: "/tmp"
      - name: "MAVEN_OPTS"
        value: "-Duser.home=/home/jenkins"
      - name: "XDG_CONFIG_HOME"
        value: "/tmp"
      volumeMounts:
      - name: settings-xml
        mountPath: /home/jenkins/.m2/settings.xml
        subPath: settings.xml
        readOnly: true
      - name: settings-security-xml
        mountPath: /home/jenkins/.m2/settings-security.xml
        subPath: settings-security.xml
        readOnly: true
      - name: m2-repo
        mountPath: /home/jenkins/.m2/repository"""
  private final String BUILD_CONTAINER_VOLUMES="""
    - name: settings-xml
      secret:
        secretName: m2-secret-dir
        items:
        - key: settings.xml
          path: settings.xml
    - name: settings-security-xml
      secret:
        secretName: m2-secret-dir
        items:
        - key: settings-security.xml
          path: settings-security.xml
    - name: m2-repo
      emptyDir: {}"""
  private final String SSH_DEPLOY_CONTAINER_NAME="jnlp"
  private final String SSH_DEPLOY_CONTAINER="""
    - name: $SSH_DEPLOY_CONTAINER_NAME
      env:
      - name: "JAVA_TOOL_OPTIONS"
        value: "-Xmx1G"
      volumeMounts:
      - name: volume-known-hosts
        mountPath: /home/jenkins/.ssh"""
  private final String SSH_DEPLOY_CONTAINER_VOLUMES="""
    - name: volume-known-hosts
      configMap:
        name: known-hosts"""

  private final String CREDENTIAL="genie.rcptt@projects-storage.eclipse.org"
  private final String SSH_CLIENT="ssh $CREDENTIAL"

  private final String FULL_REPOSITORY_TARGET="repository/full/target"
  private final String RCPTT_REPOSITORY_TARGET="repository/rcptt/target"
  private final String PRODUCTS_DIR="$FULL_REPOSITORY_TARGET/products"
  private final String FULL_REPOSITORY_DIR="$FULL_REPOSITORY_TARGET/repository"
  private final String RCPTT_REPOSITORY_DIR="$RCPTT_REPOSITORY_TARGET/repository"
  private final String RUNNER_DIR="runner/product/target/products"
  private final String RUNTIME_DIR="runtime/updates"
  private final String RUNTIME_DIR_E3="$RUNTIME_DIR/org.eclipse.rcptt.updates.runtime/q7"
  private final String RUNTIME_DIR_E4="$RUNTIME_DIR/org.eclipse.rcptt.updates.runtime.e4x/q7"
  private final String DOC_DIR="releng/doc"

  private final String DOWNLOADS_HOME="/home/data/httpd/download.eclipse.org/rcptt"
  private final String[] PLATFORMS=["linux.gtk.x86_64", "macosx.cocoa.x86_64", "macosx.cocoa.aarch64", "win32.win32.x86_64"];

  private final def script

  String YAML_BUILD_AGENT="""
apiVersion: v1
kind: Pod
spec:
  containers:
$BUILD_CONTAINER
  volumes:
$BUILD_CONTAINER_VOLUMES
"""

  String YAML_BUILD_AND_DEPLOY_AGENT="""
apiVersion: v1
kind: Pod
spec:
  containers:
$BUILD_CONTAINER
$SSH_DEPLOY_CONTAINER
  volumes:
$BUILD_CONTAINER_VOLUMES
$SSH_DEPLOY_CONTAINER_VOLUMES
"""

  Build(def script) {
    this.script = script
  }

  void build_and_test(Boolean sign) {
      build(sign)

      this.script.stage("RCPTT Test") {
          rcptt_tests()
      }
      this.script.stage("Mockup Test") {
          mockup_tests()
      }
  }

  void build(Boolean sign) {
      try {
        _build(sign)
        get_version() // print productVersion and productQualifier
      } finally {
        this.script.stage("Archive") {
            archive()
        }
      }
  }

  void set_milestone(String decorator) {
      withBuildContainer() {
        def version = get_version_from_pom().split("-")[0]
        this.script.sh "./update_version.sh $version $decorator"
      }
  }

  void _build(Boolean sign) {
    withBuildContainer() {
			sh "env"
      mvn "--version"
      def mvn = { pom ->
          this.mvn "clean ${sign ? "--activate-profiles sign" : ""} --file ${pom}"
      }
      this.script.xvnc() {
        mvn "releng/mirroring/pom.xml verify"
        mvn "releng/core/pom.xml verify"
        mvn "releng/runtime/pom.xml -P runtime4x verify"
        mvn "releng/ide/pom.xml verify"
        mvn "releng/rap/pom.xml -P core verify"
        mvn "releng/rap/pom.xml -P ide verify"
        mvn "releng/rcptt/pom.xml verify"
        mvn "releng/runner/pom.xml verify"
        mvn "maven-plugin/pom.xml install"
      }
      this.script.sh "./$DOC_DIR/generate-doc.sh -Dmaven.repo.local=${getWorkspace()}/m2 -B -e"
    }
  }

  void archive() {
    this.script.junit "**/target/*-reports/*.xml"
    this.script.fingerprint "$RUNTIME_DIR/org.eclipse.rcptt.updates.runtime*/q7/**/*.*"
    this.script.archiveArtifacts allowEmptyArchive: false, artifacts: "**/*.hrpof, repository/**/target/repository/**/*, $PRODUCTS_DIR/*, $RUNNER_DIR/*.zip, maven-plugin/rcptt-maven-*/target/rcptt-maven-*.jar, $DOC_DIR/target/doc/**/*, **/target/**/*.log, **/target/dash/*summary, **/target/**/bundles.info, **/target/**/*.ini"
  }

  private void sh_with_return(String command) {
    def res = this.script.sh (
      script: command,
      returnStdout: true
    ).trim()
    return res
  }

  private void get_version_from_pom() {
    return sh_with_return("mvn -q -Dexec.executable=echo -Dexec.args='\${project.version}' --non-recursive exec:exec -f releng/pom.xml")
  }

  private void get_version() {
    return sh_with_return(". $FULL_REPOSITORY_TARGET/publisher.properties && echo \$productVersion")
  }

  private void get_qualifier() {
    return sh_with_return(". $FULL_REPOSITORY_TARGET/publisher.properties && echo \$productQualifier")
  }

  void rcptt_tests() {
    withBuildContainer() {
      _run_tests(
        "${getWorkspace()}/$RUNNER_DIR/org.eclipse.rcptt.runner.headless*-linux.gtk.x86_64.zip",
        "rcpttTests",
        "-DrcpttPath=${getWorkspace()}/$PRODUCTS_DIR/org.eclipse.rcptt.platform.product-linux.gtk.x86_64.zip"
      )
      this.script.junit "rcpttTests/target/*-reports/*.xml"
    }
  }

  void mockup_tests() {
    withBuildContainer() {
      this.script.dir('mockups') {
        this.script.git "https://github.com/xored/q7.quality.mockups.git"
      }
      _run_tests(
          "${getWorkspace()}/$RUNNER_DIR/org.eclipse.rcptt.runner.headless*-linux.gtk.x86_64.zip",
          "mockups/rcpttTests",
          "-DmockupsRepository=https://ci.eclipse.org/rcptt/job/mockups/lastSuccessfulBuild/artifact/repository/target/repository"
      )
      this.script.junit "mockups/rcpttTests/target/*-reports/*.xml"
    }
  }

  void tests(String repo, String runner, String args) {
    withBuildContainer() {
      this.script.git repo
      _run_tests(runner, "rcpttTests", args)
    }
  }

  private void _run_tests(String runner, String dir, String args) {
		try {
	    this.script.xvnc() {
	      this.script.sh "mvn clean verify -B -f ${dir}/pom.xml \
	          -Dmaven.repo.local=${getWorkspace()}/m2 -e \
	          -Dci-maven-version=2.6.0-SNAPSHOT \
	          -DexplicitRunner=`readlink -f ${runner}` \
	          ${args}"
	    }
	    this.script.sh "test -f ${dir}/target/results/tests.html"
    } finally {
	    this.script.archiveArtifacts allowEmptyArchive: false, artifacts: "${dir}/target/results/**/*, ${dir}/target/**/*log,${dir}/target/surefire-reports/**, **/*.hprof"
    }
  }

  void post_build_actions() {
    withBuildContainer() {
      this.script.sh "jps -v"
      this.script.sh "ps x"
    }
  }

  void deploy(String mode, String arg = "M0") {
    switch(mode) {
      case "Release": release(); break;
      case "Milestone": milestone(arg); break;
      case "Nightly": nightly(); break;
    }
  }

  private getWorkspace() {
    return this.script.env.WORKSPACE
  }

  private void get_version_storage_folder(String type, String version) {
    return "$DOWNLOADS_HOME/$type/$version"
  }

  private void get_storage_folder(String type, String version, String subfolder) {
    return "${get_version_storage_folder(type, version)}/$subfolder"
  }

  void nightly() {
    def buildsToKeep = 5

    def type = "nightly"
    def version = get_version()
    def qualifier = get_qualifier()
    def qualifiedDecoration = "-N$qualifier"

    this.script.container(SSH_DEPLOY_CONTAINER_NAME) {
      def storageFolder = get_version_storage_folder(type, version)
      this.script.sshagent(["projects-storage.eclipse.org-bot-ssh"]) {
        def oldBuilds = sh_with_return("$SSH_CLIENT ls -r $storageFolder | grep -v latest | tail -n +${buildsToKeep}")
        for(old in oldBuilds.split("\n")) {
          this.script.sh "$SSH_CLIENT rm -rf $storageFolder/$old"
        }
      }
      copy_files(type, version, qualifier, qualifiedDecoration, true)

      def storageFolderLatest = get_storage_folder(type, version, "latest")
      this.script.sshagent(["projects-storage.eclipse.org-bot-ssh"]) {
        this.script.sh "$SSH_CLIENT rm -rf ${storageFolderLatest}"
      }
      copy_files(type, version, "latest", "-nightly", true)
    }

    maven_deploy(version+"-SNAPSHOT")
  }

  void milestone(String milestone) {
    def type = "milestone"
    def version = get_version()
    def qualifiedDecoration = "-$milestone"

    this.script.container(SSH_DEPLOY_CONTAINER_NAME) {
      copy_files(type, version, milestone, qualifiedDecoration, false)
    }

    maven_deploy("$version-$milestone")
  }

  void release() {
    def type = "release"
    def version = get_version()
    def qualifiedDecoration = ""

    this.script.container(SSH_DEPLOY_CONTAINER_NAME) {
      copy_files(type, version, "", qualifiedDecoration, false)
    }

    maven_deploy(version)
  }

  private void copy_files(String type, String version, String subfolder, String qualifiedDecoration, Boolean copy_full) { // subfolder is empty for type == release
    def storageFolder = get_storage_folder(type, version, subfolder)

    this.script.sshagent(["projects-storage.eclipse.org-bot-ssh"]) {
      this.script.sh "$SSH_CLIENT test -d $storageFolder && echo $type ${version}${qualifiedDecoration} already exists && exit 1 || echo $type ${version}${qualifiedDecoration} does not exist yet"

      this.script.sh "$SSH_CLIENT mkdir -p $storageFolder"
      this.script.sh "$SSH_CLIENT mkdir $storageFolder/runner"

      for(item in [ [ RCPTT_REPOSITORY_DIR, "repository" ],
                    [ RUNTIME_DIR_E4, "runtime4x" ],
                    [ "$DOC_DIR/target/doc", "doc" ],
                    [ "$RCPTT_REPOSITORY_TARGET/rcptt.repository-*.zip", "repository-${version}${qualifiedDecoration}.zip" ]
                    ]) {
        this.script.sh "scp -r ${item[0]} $CREDENTIAL:$storageFolder/${item[1]}"
      }

      this.script.sh "$SSH_CLIENT mkdir $storageFolder/ide"
      for(platform in PLATFORMS) {
        this.script.sh "scp -r $PRODUCTS_DIR/org.eclipse.rcptt.platform.product-${platform}.zip $CREDENTIAL:$storageFolder/ide/rcptt.ide-${version}${qualifiedDecoration}-${platform}.zip"
        this.script.sh "scp -r $RUNNER_DIR/org.eclipse.rcptt.runner.headless*-${platform}.zip $CREDENTIAL:$storageFolder/runner/rcptt.runner-${version}${qualifiedDecoration}-${platform}.zip"
      }

      if(copy_full) {
        this.script.sh "scp -r $FULL_REPOSITORY_DIR $CREDENTIAL:$storageFolder/full"
      }

    }
  }

  def withBuildContainer(operation) {
    this.script.container(BUILD_CONTAINER_NAME) {
      operation()
    }
  }

  private void maven_deploy(String version) {
    withBuildContainer() {
      mvn('-Dtycho.mode=maven -f runner/product org.eclipse.tycho:tycho-versions-plugin::set-version -DnewVersion=' + version)
      mvn('-Dtycho.mode=maven -f maven-plugin/pom.xml clean versions:set -DnewVersion=' + version)
      mvn('-f releng/runner/pom.xml clean deploy')
      mvn('-f maven-plugin/pom.xml clean deploy')
    }
  }
  
  private def sh(arguments) {
    return this.script.sh(arguments)
  }
  
  private void mvn(String arguments) {
    sh("mvn -Dmaven.repo.local=${getWorkspace()}/m2 -Dtycho.localArtifacts=ignore --errors --batch-mode --no-transfer-progress " + arguments)
  } 

}

return { script -> new Build(script) }
