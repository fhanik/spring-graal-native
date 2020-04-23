#!/usr/bin/env bash

ARTIFACT=springmvc-tomcat
MAINCLASS=com.example.tomcat.TomcatApplication
VERSION=0.0.1-SNAPSHOT
FEATURE=../../../../spring-graal-native/target/spring-graal-native-0.7.0.BUILD-SNAPSHOT.jar

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# trap ctrl-c and call ctrl_c()
trap ctrl_c INT

function ctrl_c() {
        echo "** Trapped CTRL-C"
        kill -9 $TRAP_PID
}

rm -rf target
mkdir -p target/native-image

echo "Packaging $ARTIFACT with Maven"
mvn -DskipTests package > target/native-image/output.txt

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native-image
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH:$FEATURE

if [ ! -f "$FEATURE" ]; then
    printf "${RED}FAILURE${NC}: $FEATURE does not exist, please build the root project before building this sample.\n"
    exit 1
fi

echo "Generating reflection files for $ARTIFACT"
rm -rf graal/META-INF 2>/dev/null
mkdir -p graal/META-INF/native-image
java -javaagent:/development/pivotal/cloudfoundry/spring-projects/graal/instrumentation-agent/target/instrumentation-agent-1.0.jar \
  -Djava.library.path="$JAVA_HOME/jre/lib/amd64" \
  -cp $CP $MAINCLASS &
export TRAP_PID=$!
sleep 5
echo "PID=$TRAP_PID"
sleep 3600

