set -x
mvn -f releng/core/pom.xml -PlatestPlatform -Decl.executionEnvironment=JavaSE-26 -Dtycho.localArtifacts=ignore clean verify --batch-mode --no-transfer-progress
