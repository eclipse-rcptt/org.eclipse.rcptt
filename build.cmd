set MAVEN_OPTS=-Xms512M -Xmx1500M
set PWD=%CD:\=/%

set TARGETS=clean verify

set OPTIONS=-Dtycho.localArtifacts=ignore

REM call mvn clean verify %OPTIONS% %* -f clean-pom.xml  || exit 102

call mvn %TARGETS% %OPTIONS% %* -f releng/mirroring/pom.xml ^
	|| goto :EOF

call mvn %TARGETS% %OPTIONS% %* -f releng/core/pom.xml ^
    || goto :EOF

call mvn %TARGETS% %OPTIONS% %* -f releng/runtime/pom.xml -P runtime4x ^
    || goto :EOF

call mvn %TARGETS% %OPTIONS% %* -f releng/ide/pom.xml  ^
    || goto :EOF

