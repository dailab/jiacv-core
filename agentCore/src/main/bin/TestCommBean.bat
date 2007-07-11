@echo off

set LOCALCLASSPATH=
for %%i in ("..\lib\*") do call "lcp.bat" %%i

REM set CLASSPATH=C:\Users\Nakor\.m2\repository
REM 
REM set CLASSPATH=%CLASSPATH%;C:\Users\Nakor\Eclipse\JIACTNGworkspace\CommDev
REM 
REM set CLASSPATH=%CLASSPATH%;C:\Users\Nakor\Eclipse\activemq
REM 
REM set CLASSPATH=%CLASSPATH%;C:\Users\Nakor\Eclipse\JIACTNGworkspace\CommDev\target\classes
REM 
REM set CLASSPATH=%CLASSPATH%;C:\Users\Nakor\Eclipse\JIACTNGworkspace\CommDev\target\test-classes
REM 
REM set CLASSPATH=%CLASSPATH%;C:\Users\Nakor\Eclipse\activemq\apache-activemq-4.1.0-incubator.jar
REM 
REM set CLASSPATH=%CLASSPATH%;C:\Users\Nakor\.m2\repository\org\springframework\spring-beans\2.0-m2\spring-beans-2.0-m2.jar

"%JAVA_HOME%\bin\java" -cp %LOCALCLASSPATH%;.;..\conf\ de.dailab.jiactng.agentcore.comm.examples.TestCommBean

pause