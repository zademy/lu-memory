
set JAVA_HOME=C:\Java\jdk-25.0.2
echo %JAVA_HOME%
set PATH=%JAVA_HOME%\bin;%PATH%

SET MAVEN_HOME=C:\Maven\apache-maven-3.9.12
echo %MAVEN_HOME%
set PATH=%MAVEN_HOME%\bin;%PATH%

mvn -T 14 clean install -Dmaven.test.skip=true 