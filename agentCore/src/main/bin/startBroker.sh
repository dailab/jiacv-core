#!/bin/sh

LOCALCLASSPATH=

# add in the dependency .jar files
DIRLIBS=`pwd $0`/../lib/*.jar

for i in ${DIRLIBS}
do
    # if the directory is empty, then it will return the input string
    # this is stupid, so case for it
    if [ "$i" != "${DIRLIBS}" ] ; then
      if [ -z "$LOCALCLASSPATH" ] ; then
        LOCALCLASSPATH=$i
      else
        LOCALCLASSPATH="$i":$LOCALCLASSPATH
      fi
    fi
done

$JAVA_HOME/bin/java -cp $LOCALCLASSPATH de.dailab.jiactng.agentcore.comm.examples.BrokerStart