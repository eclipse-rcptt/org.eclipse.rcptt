# RCPTT Ubuntu build environment image

This directory defines the Docker image used by the CI jobs in
`.github/workflows/verify.yml` (published as `basilevs/ubuntu-rcptt:<version>`).
The image bundles Maven, a JDK, and the native/X libraries (WebKitGTK, fonts,
TigerVNC) required to build RCPTT and run its SWT/UI tests headlessly.

## Versioning strategy

`Dockerfile` always tracks the **latest** supported build environment (currently
the newest JVM). Every meaningful change to the environment — most importantly a
JDK bump — is published as a **new immutable tag**; existing tags are never
mutated. This lets the CI lanes pin to different environments:

| CI job                              | Environment            | Image tag                     |
| ----------------------------------- | ---------------------- | ----------------------------- |
| `build`, `self_test`, `mockup_test` | stable / oldest JVM    | older, proven tag (e.g. 3.7.2)|
| `test_latest`                       | latest JVM + platform  | newest tag (e.g. 3.8.0)       |

The stable lanes stay on an older, proven tag so the release artifacts and
self-tests keep running on the supported baseline. The `test_latest` lane uses
the newest tag to surface incompatibilities with the latest JVM and Eclipse
Platform early; it is expected to fail when a new dependency version is not yet
compatible.

Because the whole distro is upgraded together with the JDK (Ubuntu, WebKitGTK,
fonts, etc.), the `Dockerfile` is edited directly rather than parameterized —
a JDK bump often requires adjusting apt package names too. Verify the `apt-get`
package list still resolves whenever the base image's Ubuntu release changes.

## Keeping the Tycho BREE in sync

The runtime JVM (this image's base) is independent from the Tycho
target-platform execution environment (BREE). When bumping the JVM used by
`test_latest`, also raise the `latestPlatform` profile's `ecl.executionEnvironment`
and `ide.executionEnvironment` in `releng/pom.xml` to the matching `JavaSE-XX`,
and confirm the pinned Tycho version recognizes that execution environment.

## Publishing a new image

Publishing is a manual maintainer step. Either run the
`Publish build environment image` workflow (`.github/workflows/publish-buildenv.yml`)
via *Run workflow*, providing the new `repository:tag`, or build and push
locally:

```sh
docker build -t basilevs/ubuntu-rcptt:<version> releng/buildenv/ubuntu
docker push basilevs/ubuntu-rcptt:<version>
```

The workflow requires `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` repository
secrets with write access to the `basilevs/ubuntu-rcptt` repository.

After publishing, update the `test_latest` job's `container.image` in
`.github/workflows/verify.yml` to the new tag.
