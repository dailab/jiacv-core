@echo off

set LOCALCLASSPATH=
for %%i in ("..\lib\*") do call "lcp.bat" %%i

"%JAVA_HOME%\bin\java" -cp %LOCALCLASSPATH%;. de.dailab.jiactng.sercho.rulesengine.client.RulesRmiClient localhost 20000 http://192.168.2.2:8080/shea/services/Notification

pause