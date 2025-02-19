# Contributing to [Eclipse RCP Testing Tool](https://projects.eclipse.org/projects/technology.rcptt) (RCPTT)

## Developer resources

- [Search and report issues](https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues)
  - Be sure to search for existing bugs before you create another one.
- [Get source code](https://github.com/eclipse-rcptt/org.eclipse.rcptt)

### Building the source
+ An OpenJDK version 17 is required.
+ Clone the repository: `git clone -b master git@github.com:eclipse-rcptt/org.eclipse.rcptt.git`
+ Maven Tycho Build: run `build.sh` on Unix or or `build.cmd` in Windows
+ For IDE builds set the active target platform to the latest one in `releng/target-platforms`.

### Coding standards
* Coding style: please use the `formatter.xml` in the top directory.

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team contributors must
electronically sign the Eclipse Contributor Agreement (ECA).

* http://www.eclipse.org/legal/ECA.php

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project. The non-committer must
additionally have an Eclipse Foundation account and must have a signed Eclipse
Contributor Agreement (ECA) on file.

For more information, please see the Eclipse Committer Handbook:
https://www.eclipse.org/projects/handbook/#resources-commit

## Contact

Contact the project developers via the project's "dev" list.

* https://accounts.eclipse.org/mailing-list/rcptt-dev

