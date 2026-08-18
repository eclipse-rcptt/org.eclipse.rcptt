set -e
java -version
echo "=== Step 1: mirroring (fast) ==="
mvn -q clean verify -f releng/mirroring/pom.xml -Dtycho.localArtifacts=ignore --batch-mode --no-transfer-progress || { echo "MIRRORING_FAILED=$?"; }
echo "=== Step 2: force target platform + compile ecl on JavaSE-26 ==="
mvn -f releng/ecl clean compile -PlatestPlatform -Decl.executionEnvironment=JavaSE-26 -Dtycho.localArtifacts=ignore --batch-mode --no-transfer-progress 2>&1 | tail -60
