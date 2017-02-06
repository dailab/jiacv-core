@echo off

set LOCALCLASSPATH=
for %%i in ("..\lib\*") do call "lcp.bat" %%i

"%JAVA_HOME%\bin\java" -Dcom.sun.management.jmxremote -cp %LOCALCLASSPATH%;.;..\resources\ de.dailab.jiactng.agentcore.SimpleAgentNode pingPong.xml


pause