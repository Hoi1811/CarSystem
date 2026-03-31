@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

set SETTINGS=%USERPROFILE%\.m2\settings_default.xml

echo Using JAVA_HOME: %JAVA_HOME%
echo Using settings: %SETTINGS%

call mvnw -s %SETTINGS% spring-boot:run

pause