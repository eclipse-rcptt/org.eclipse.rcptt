#!/bin/bash
# Repeatedly runs org.eclipse.rcptt.core.tests test classes to measure failure rate
# of concurrency problems from https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176
# See ../README.md
#
# Prerequisite (once): core bundles installed into local Maven repository:
#   mvn install -f releng/mirroring/pom.xml -Dtycho.localArtifacts=ignore
#   mvn install -f releng/core/pom.xml -DskipTests
#
# Configuration (environment variables):
#   RUNS              number of runs (default 10)
#   DURATION_MINUTES  if set, keep running until this time is spent (RUNS is ignored)
#   TESTS             value for -Dtest (default Q7NamedElementTest)
#   FACTOR            -Drcptt.stress.factor, multiplies durations of time-boxed loops (default 1)
#   ISSUE176          -Drcptt.issue176, enables known failing reproducers (default true)
#   BYTEMAN           true to activate Maven profile issue176-byteman (default false)
#   TEST_ARGLINE      extra test JVM arguments, for example -XX:ActiveProcessorCount=1
#   MVN_ARGS          extra Maven arguments (default -o)
#   RUN_TIMEOUT       seconds before a run is considered hung, thread dumped and killed (default 1200)
#   OUT               output directory (default tests/org.eclipse.rcptt.core.tests/target/stress/local)
#
# Output: $OUT/results.csv (run,exit,seconds,failed tests,first failure) and
# $OUT/run-N/ with Maven log, surefire reports, Eclipse log, hang dumps and thread dumps of hung runs.
# Exit code is 0 only if all runs passed.

set -u
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_DIR=$(dirname "$SCRIPT_DIR")
RUNS=${RUNS:-10}
DURATION_MINUTES=${DURATION_MINUTES:-}
TESTS=${TESTS:-Q7NamedElementTest}
FACTOR=${FACTOR:-1}
ISSUE176=${ISSUE176:-true}
BYTEMAN=${BYTEMAN:-false}
TEST_ARGLINE=${TEST_ARGLINE:-}
MVN_ARGS=${MVN_ARGS--o}
RUN_TIMEOUT=${RUN_TIMEOUT:-1200}
OUT=${OUT:-$PROJECT_DIR/target/stress/local}
MVN=${MVN:-mvn}

mkdir -p "$OUT"
OUT=$(cd "$OUT" && pwd)
if command -v cygpath > /dev/null; then
	# Windows (Git Bash): pass paths understandable by both bash and Java
	PROJECT_DIR=$(cygpath -m "$PROJECT_DIR")
	OUT=$(cygpath -m "$OUT")
fi
RESULTS="$OUT/results.csv"
[ -f "$RESULTS" ] || echo "run,exit,seconds,failed,first_failure" > "$RESULTS"

profile=()
[ "$BYTEMAN" = "true" ] && profile=(-Pissue176-byteman)

# Lists PIDs of Java processes started for this test project (Maven and test runtime)
java_pids() {
	command -v jcmd > /dev/null || return 0
	jcmd -l 2> /dev/null | grep -v -i 'jcmd' | grep 'org.eclipse.rcptt.core.tests' | cut -d' ' -f1
}

start_time=$(date +%s)
run=0
failures=0
while true; do
	if [ -n "$DURATION_MINUTES" ]; then
		[ $(( $(date +%s) - start_time )) -ge $(( DURATION_MINUTES * 60 )) ] && break
	else
		[ "$run" -ge "$RUNS" ] && break
	fi
	run=$((run + 1))
	dir="$OUT/run-$run"
	mkdir -p "$dir"
	log="$dir/maven.log"
	rm -rf "$PROJECT_DIR/target/surefire-reports"
	run_start=$(date +%s)
	# shellcheck disable=SC2086 # MVN_ARGS is intentionally split
	"$MVN" -B -ntp $MVN_ARGS verify "${profile[@]}" -f "$PROJECT_DIR/pom.xml" \
		"-Dtest=$TESTS" -Dsurefire.failIfNoSpecifiedTests=false \
		"-Drcptt.issue176=$ISSUE176" "-Drcptt.stress.factor=$FACTOR" \
		"-Drcptt.testArgLine=$TEST_ARGLINE" "-Drcptt.hangDumpDir=$dir/hang-dumps" \
		> "$log" 2>&1 &
	pid=$!
	timed_out=false
	while kill -0 "$pid" 2> /dev/null; do
		if [ $(( $(date +%s) - run_start )) -ge "$RUN_TIMEOUT" ]; then
			echo "Run $run timed out after $RUN_TIMEOUT s, dumping threads" | tee -a "$log"
			for java in $(java_pids); do
				jcmd "$java" Thread.print -l > "$dir/threads-$java.txt" 2>&1
			done
			for java in $(java_pids); do
				if command -v taskkill > /dev/null; then
					taskkill //F //T //PID "$java" > /dev/null 2>&1 # Windows PIDs from jcmd are not known to MSYS kill
				else
					kill -9 "$java" 2> /dev/null
				fi
			done
			kill "$pid" 2> /dev/null
			timed_out=true
			break
		fi
		sleep 5
	done
	wait "$pid" 2> /dev/null
	code=$?
	[ "$timed_out" = "true" ] && code=124
	seconds=$(( $(date +%s) - run_start ))
	[ -d "$PROJECT_DIR/target/surefire-reports" ] && cp -r "$PROJECT_DIR/target/surefire-reports" "$dir/"
	[ -f "$PROJECT_DIR/target/work/data/.metadata/.log" ] && cp "$PROJECT_DIR/target/work/data/.metadata/.log" "$dir/eclipse.log"
	failed=$(grep -E '<<< (FAILURE|ERROR)!' "$log" | grep -v 'Tests run:' | sed -E 's/ -- Time elapsed.*//' | sort -u | tr '\n' ' ')
	first=$(grep -E -A1 '<<< (FAILURE|ERROR)!' "$log" | grep -v -E '<<<|^--$' | head -1 | tr -d '\r' | tr ',"' ";'" | cut -c1-300)
	[ "$code" -ne 0 ] && failures=$((failures + 1))
	echo "$run,$code,$seconds,\"$failed\",\"$first\"" >> "$RESULTS"
	echo "Run $run: exit $code in $seconds s. Failed runs: $failures/$run. $failed"
done

echo "Finished $run runs, $failures failed. Results: $RESULTS"
[ "$run" -gt 0 ] && [ "$failures" -eq 0 ]
