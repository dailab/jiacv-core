@echo off

set LOCALCLASSPATH=
for %%i in ("..\lib\*") do call "lcp.bat" %%i

"%JAVA_HOME%\bin\java" -cp %LOCALCLASSPATH%;.;..\conf\ de.dailab.jiangtng.agentcore.Agent simple.xml

pause