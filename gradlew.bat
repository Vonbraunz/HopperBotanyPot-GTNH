@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem  Gradle startup script for Windows
@rem ##########################################################################
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

for /f "usebackq tokens=*" %%j in (`java -XshowSettings:all -version 2^>^&1 ^| find "java.home"`) do (
  set JAVA_HOME_OUT=%%j
)

java -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
