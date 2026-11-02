#!/bin/bash
# Runs stress-loop.sh in CI Docker image under different resource constraints
# to reproduce https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176
# See ../README.md
#
# Usage: tests/org.eclipse.rcptt.core.tests/stress/docker-matrix.sh [config...]
# Configurations:
#   unlimited  no constraints
#   cpus1      --cpus=1
#   cpus05     --cpus=0.5
#   apc1       -XX:ActiveProcessorCount=1 (JVM thinks there is one CPU, but threads run in parallel)
#   noise      sidecar container keeps all CPUs busy
#   tmpfs      sources and test workspace are copied to tmpfs
# Default: all of the above.
#
# Environment variables:
#   IMAGE          Docker image (default basilevs/ubuntu-rcptt:3.7.2, as in .github/workflows/verify.yml)
#   M2_VOLUME      Docker volume or host directory for Maven local repository (default rcptt-m2)
#   SKIP_INSTALL   true to skip installation of core bundles into M2_VOLUME
#   DOCKER_EXTRA   extra docker run options for all configurations,
#                  for example "--device-write-bps /dev/sda:1mb" to throttle file system
#   all variables of stress-loop.sh (RUNS, DURATION_MINUTES, TESTS, FACTOR, BYTEMAN, ...)
# Results: tests/org.eclipse.rcptt.core.tests/target/stress/<config>/results.csv

set -u
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO=$(cd "$SCRIPT_DIR/../../.." && pwd)
IMAGE=${IMAGE:-basilevs/ubuntu-rcptt:3.7.2}
M2_VOLUME=${M2_VOLUME:-rcptt-m2}
SKIP_INSTALL=${SKIP_INSTALL:-false}
DOCKER_EXTRA=${DOCKER_EXTRA:-}
PROJECT=tests/org.eclipse.rcptt.core.tests
CONFIGS=("$@")
[ ${#CONFIGS[@]} -eq 0 ] && CONFIGS=(unlimited cpus1 cpus05 apc1 noise tmpfs)

pass_env=()
for name in RUNS DURATION_MINUTES TESTS FACTOR ISSUE176 BYTEMAN MVN_ARGS RUN_TIMEOUT; do
	[ -n "${!name:-}" ] && pass_env+=(-e "$name=${!name}")
done

docker_run() {
	# shellcheck disable=SC2086 # DOCKER_EXTRA is intentionally split
	docker run --rm $DOCKER_EXTRA -v "$M2_VOLUME:/root/.m2/repository" -v "$REPO:/work" -w /work \
		-e MAVEN_OPTS=-Xmx1024m "${pass_env[@]}" "$@"
}

if [ "$SKIP_INSTALL" != "true" ]; then
	echo "Installing core bundles into $M2_VOLUME"
	docker_run "$IMAGE" bash -c 'mvn -B -ntp -q install -f releng/mirroring/pom.xml -Dtycho.localArtifacts=ignore && mvn -B -ntp -q install -f releng/core/pom.xml -DskipTests' || exit 1
	# Fetch Byteman for offline runs
	docker_run "$IMAGE" bash -c "mvn -B -ntp -q generate-test-resources -Pissue176-byteman -f $PROJECT/pom.xml" || exit 1
fi

overall=0
for config in "${CONFIGS[@]}"; do
	opts=()
	test_argline=""
	command="$PROJECT/stress/stress-loop.sh"
	noise=""
	case "$config" in
		unlimited) ;;
		cpus1) opts=(--cpus=1) ;;
		cpus05) opts=(--cpus=0.5) ;;
		apc1) test_argline="-XX:ActiveProcessorCount=1" ;;
		noise)
			noise=rcptt-stress-noise-$$
			docker run -d --rm --name "$noise" "$IMAGE" bash -c 'for i in $(seq $(nproc)); do (while :; do :; done) & done; wait' > /dev/null || exit 1
			;;
		tmpfs)
			opts=(--tmpfs "/fast:exec,size=3g")
			# forceContextQualifier: there is no Git repository to compute qualifier from
			command="tar -C /work --exclude=.git --exclude=target -cf - . | tar -C /fast -xf - && cd /fast && MVN_ARGS=\"\${MVN_ARGS--o} -DforceContextQualifier=stress\" OUT=/work/$PROJECT/target/stress/tmpfs $PROJECT/stress/stress-loop.sh"
			;;
		*) echo "Unknown configuration $config" >&2; exit 2 ;;
	esac
	echo "=== Configuration $config"
	docker_run "${opts[@]}" -e "TEST_ARGLINE=$test_argline" -e "OUT=/work/$PROJECT/target/stress/$config" \
		"$IMAGE" bash -c "$command; code=\$?; chmod -R a+rwX /work/$PROJECT/target; exit \$code"
	code=$?
	[ -n "$noise" ] && docker stop -t 1 "$noise" > /dev/null
	[ "$code" -ne 0 ] && overall=1
done

for config in "${CONFIGS[@]}"; do
	results="$REPO/$PROJECT/target/stress/$config/results.csv"
	[ -f "$results" ] || continue
	total=$(($(wc -l < "$results") - 1))
	failed=$(tail -n +2 "$results" | awk -F, '$2 != 0' | wc -l)
	echo "$config: $failed/$total runs failed"
done
exit $overall
