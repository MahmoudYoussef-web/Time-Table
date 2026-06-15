@setlocal EnableExtensions EnableDelayedExpansion
set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
if not "%JAVA_HOME%"=="" goto foundJava
if exist "%USERPROFILE%\.jdks\ms-21.0.9" set "JAVA_HOME=%USERPROFILE%\.jdks\ms-21.0.9"
:foundJava
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%WRAPPER_JAR%" echo Maven wrapper JAR not found. & exit /b 1
"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR:~0,-1%" org.apache.maven.wrapper.MavenWrapperMain %*
if ERRORLEVEL 1 exit /b 1
@endlocal
