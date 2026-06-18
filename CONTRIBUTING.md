# Contributing to [Eclipse RCP Testing Tool](https://projects.eclipse.org/projects/technology.rcptt) (RCPTT)

## Developer resources

- [Search and report issues](https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues)
  - Be sure to search for existing bugs before you create another one.
- [Get source code](https://github.com/eclipse-rcptt/org.eclipse.rcptt)

### Building the source
+ An OpenJDK version 21 is required.
+ Clone the repository: `git clone -b master git@github.com:eclipse-rcptt/org.eclipse.rcptt.git`
+ Maven Tycho Build: run `build.sh` on Unix or or `build.cmd` in Windows
  + When graphical context is unavailable (headless builds) skip tests with argument `-Dmaven.test.skip=true`
  + Maven  uses `<repository>` declarations in project file (does not use PDE target definitions below).
+ For interactive IDE experience, install Eclipse IDE for Eclipse Commiters and configure PDE's active target platform to the latest target definition in `releng/target-platforms`.

### Coding standards
* Coding style: please use the `formatter.xml` in the top directory.

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team contributors must
electronically sign the Eclipse Contributor Agreement (ECA). For more information, please see the Eclipse Committer Handbook:
* https://www.eclipse.org/projects/handbook/#resources-commit
* http://www.eclipse.org/legal/ECA.php



## Contact

Contact the project developers via the project's "dev" list.

* https://accounts.eclipse.org/mailing-list/rcptt-dev

