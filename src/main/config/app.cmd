@echo on
set
D:
SET WEBROOT_PATH=D:\home\site\wwwroot
cd %WEBROOT_PATH%
echo "%JAVA_HOME%\bin\java.exe" -Djava.net.preferIPv4Stack=true -Ddw.server.applicationConnectors[0].port=%HTTP_PLATFORM_PORT% -classpath "%WEBROOT_PATH%\classes;%WEBROOT_PATH%\repo\*" ${app.mainClass} server "%WEBROOT_PATH%\config.yml"
"%JAVA_HOME%\bin\java.exe" -Djava.net.preferIPv4Stack=true -Ddw.server.applicationConnectors[0].port=%HTTP_PLATFORM_PORT% -classpath "%WEBROOT_PATH%\classes;%WEBROOT_PATH%\repo\*" ${app.mainClass} server "%WEBROOT_PATH%\config.yml"