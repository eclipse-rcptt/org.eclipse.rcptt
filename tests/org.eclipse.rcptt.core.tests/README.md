# org.eclipse.rcptt.core.tests

Tests of RCPTT model (`org.eclipse.rcptt.core`).

## Reproducing concurrency problems of issue 176

[Issue 176](https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176): RCPTT model is not thread safe.
Symptoms are `ModelException: Empty resource ...`, `... does not exist` errors and deadlocks between workspace scheduling rules and the indexer.
This bundle contains reproducers that let a developer, or an unattended agent, iterate on a fix.
None of them are active in a default build.

### Fast loop

Tycho 4.0.13 needs Maven 3.9.x. Use the CI image `basilevs/ubuntu-rcptt:3.7.2` (Java 21, Maven 3.9.9) if your local Maven differs.

Install core bundles once (repeat after any change outside of this bundle):
```
mvn install -f releng/mirroring/pom.xml -Dtycho.localArtifacts=ignore
mvn install -f releng/core/pom.xml -DskipTests
```
Then run a test class (about 40 seconds):
```
mvn -o verify -f tests/org.eclipse.rcptt.core.tests/pom.xml -Dtest=Q7NamedElementTest -Drcptt.issue176=true
```
`-Dtest=Class#method` does not narrow selection, select classes.

In Docker:
```
docker run --rm -v rcptt-m2:/root/.m2/repository -v $PWD:/work -w /work basilevs/ubuntu-rcptt:3.7.2 \
  mvn -B -o verify -f tests/org.eclipse.rcptt.core.tests/pom.xml -Dtest=Q7NamedElementTest -Drcptt.issue176=true
```

### Switches

| Property | Default | Meaning |
|---|---|---|
| `rcptt.issue176` | `false` | Runs reproducers known to fail (otherwise they are skipped with an assumption) |
| `rcptt.stress.factor` | `1` | Multiplies durations and iteration counts of time-boxed loops. Values below 1 are allowed |
| `rcptt.testArgLine` | empty | Extra test JVM arguments, for example `-XX:ActiveProcessorCount=1` |
| `rcptt.hangDumpDir` | `target/hang-dumps` | Where `HangWatchdog` writes dumps (they are also printed to stderr) |
| `rcptt.byteman.maxDelayMs` | `20` | Maximum random delay of `byteman/race-delays.btm` rules, pass via `rcptt.testArgLine` |
| `rcptt.byteman.blockMs` | `5000` | Blocking time of `byteman/block-loading.btm`, pass via `rcptt.testArgLine` |
| `rcptt.freeze.thresholdMs` | `1000` | Threshold of `UnrelatedElementAccessTest`, pass via `rcptt.testArgLine` |

Maven profile `issue176-byteman` starts the test JVM with [Byteman](https://byteman.jboss.org/) agent and both rule files from `byteman/`.
It downloads Byteman, so run it once without `-o`.

### Reproducers

| Test | Needs | Status on master | Reproduces |
|---|---|---|---|
| `Q7NamedElementTest.existsIsNoiseResistant` | `rcptt.issue176` | Fails every run in ~10 s | `Empty resource` from indexer while files are modified under workspace rule |
| `ModelDeadlockTest` | `rcptt.issue176` | Fails every run in ~70 s | Deadlock: element change listener waits for index (like `TagsWatcher`) while holding workspace rule, indexer waits for this rule. Dumps threads, jobs, rule owners and indexer queue |
| `PlainTextPersistenceModelTest.ensureNoCrosstalkBetweenDifferentFiles` | `rcptt.issue176` | Passes without Byteman, fails with `issue176-byteman` | Content of one file seen in another |
| `Q7NamedElementTest.noResourceleaks*`, `DeltaTests` | none | Fail intermittently in CI and on Windows 11 aarch64 | Stress loop material |
| `UnrelatedElementAccessTest` | `issue176-byteman` | Passes | Guard: loading of one element must not block access to another (UI must not freeze) |
| `ValueLockStressTest` | none | Passes | Guard for per-key lock of `ModelCache` |

Iteration-bound tests (`noResourceleaks*`, `reopenIfClosed`) time out with `race-delays.btm` because every iteration is slowed down.
Use `-Drcptt.stress.factor=0.1` or exclude them when Byteman is active.

### Stress loop

`stress/stress-loop.sh` repeats a Maven run and records `results.csv` (exit code, duration, failed tests, first failure line) and per-run Maven log, surefire reports, Eclipse log, hang dumps.
A run exceeding `RUN_TIMEOUT` is thread dumped with `jcmd` and killed.
See the header of the script for parameters.
```
RUNS=50 TESTS=Q7NamedElementTest tests/org.eclipse.rcptt.core.tests/stress/stress-loop.sh
```

`stress/docker-matrix.sh` runs the loop in the CI image under different constraints: `unlimited`, `cpus1`, `cpus05`, `apc1` (`-XX:ActiveProcessorCount=1`), `noise` (sidecar container burning all CPUs), `tmpfs` (sources and workspace on tmpfs).
Throttle file system with `DOCKER_EXTRA="--device-write-bps /dev/sda:1mb"`.
```
RUNS=20 TESTS=Q7NamedElementTest,ModelDeadlockTest tests/org.eclipse.rcptt.core.tests/stress/docker-matrix.sh cpus1 unlimited
```

Workflow `.github/workflows/stress.yml` (manual dispatch) runs the loop for a given number of minutes on Ubuntu (3 configurations) and on `windows-11-arm` (x64 JVM under emulation, as target platform has no Windows aarch64 fragments) and uploads results.
GitHub limits a job to 6 hours, including about 10 minutes of installation.

### Rules for an unattended fix search

- Allowed test changes: new tests. Existing reproducers may only be cleaned up so that they don't affect other tests.
- Forbidden: deleting or loosening assertions, removing `NoErrorsInLog` rules, hiding errors from the Eclipse log, raising timeouts, disabling reproducers.
- Iteration: edit, reinstall changed core bundles, run the fast loop with `-Drcptt.issue176=true` on `Q7NamedElementTest` and `ModelDeadlockTest`, read the first failure, repeat. Run the stress matrix only on candidate fixes.
- Done when:
  - `Q7NamedElementTest`, `ModelDeadlockTest`, `PlainTextPersistenceModelTest` with `-Drcptt.issue176=true` pass 50 consecutive runs both with `--cpus=1` and without limits;
  - the same with `-Pissue176-byteman` pass a fixed number of runs, `UnrelatedElementAccessTest` included;
  - `docker-matrix.sh` with fixed `RUNS` has no failures;
  - full core build `mvn verify -f releng/core/pom.xml` and the CI self test job pass.
