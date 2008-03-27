#!/bin/bash

# set classpath
DYNCLASSPATH=''
for i in $( ls ../lib ); do
	DYNCLASSPATH=$DYNCLASSPATH':'../lib/$i
done
# echo "Dynamic Classpath: "$DYNCLASSPATH

# construct classpath
export CLASSPATH='.:'$CLASSPATH':'$DYNCLASSPATH

# Java Options
JAVA_OPTIONS='-Dcom.sun.management.jmxremote -cp '$CLASSPATH':../conf/'
# echo 'Java Options: '$JAVA_OPTIONS

# Main Class
MAIN_CLASS='de.dailab.jiactng.agentcore.SimpleAgentNode'

# Main Class Parameters
MAIN_CLASS_PARAMETERS='pingPong.xml'


#
# Java execution
#
echo $JAVA_HOME/bin/java $JAVA_OPTIONS $MAIN_CLASS $MAIN_CLASS_PARAMETERS

$JAVA_HOME/bin/java $JAVA_OPTIONS $MAIN_CLASS $MAIN_CLASS_PARAMETERS

