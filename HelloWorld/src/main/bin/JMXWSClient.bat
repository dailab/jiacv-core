@echo off

set LOCALCLASSPATH=
for %%i in ("..\lib\*") do call "lcp.bat" %%i

"%JAVA_HOME%\bin\java" -cp %LOCALCLASSPATH%;.;..\conf\ de.dailab.jiactng.examples.helloWorld.Client 

pause