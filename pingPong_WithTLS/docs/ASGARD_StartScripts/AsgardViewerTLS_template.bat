REM Please customize the placeholders in angle brackets.
set JAVA_OPTS= ^
-Djavax.net.ssl.keyStore=<PATH>\bin\docs\stores\asgard\keystore.jks ^
-Djavax.net.ssl.keyStorePassword=changeit ^
-Djavax.net.ssl.trustStore=<PATH>\bin\docs\stores\asgard\truststore.jks ^
-Djavax.net.ssl.trustStorePassword=changeit
<PATH_TO_ASGARD>\AsgardViewer.bat
