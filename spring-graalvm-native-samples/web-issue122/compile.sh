#!/usr/bin/env bash

ARTIFACT=web-issue122
MAINCLASS=net.yisu.example.demo.DemoApplication
VERSION=0.0.1-SNAPSHOT

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native-image

rm -rf target/native-image
mkdir -p target/native-image
echo "Packaging $ARTIFACT with Maven"
#mvn -DskipTests package > target/native-image/output.txt
./gradlew build

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native-image
jar -xvf ../../build/libs/$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH

echo "Performing class analysis on $ARTIFACT"
CLASS_AGENT=../../../../spring-graal-native-feature/target/spring-graal-native-feature-0.6.0.BUILD-SNAPSHOT-classlist-agent.jar
rm -rf graal/META-INF 2>/dev/null
mkdir -p graal/META-INF/native-image
java -javaagent:$CLASS_AGENT -cp $CP $MAINCLASS >> output.txt 2>&1 &
PID=$!
sleep 3
curl -m 1 http://localhost:8080 > /dev/null 2>&1
sleep 1 && kill -9 $PID

cat output.txt |grep "Class\-Agent\-Transform\: " 2>&1 > class_histogram.txt
echo "Starting classpath reduction:" >> output.txt
java -cp $CP org.springframework.graalvm.util.OptimizeClassPath `pwd` class_histogram.txt $CP  >> output.txt 2>&1
#

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"
{ time native-image \
  --verbose \
  --no-server \
  -H:EnableURLProtocols=http,jar \
  -H:+TraceClassInitialization \
  -H:Name=$ARTIFACT \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 \
  -Dspring.native.remove-unused-autoconfig=true \
  -Dspring.native.remove-yaml-support=true \
  -cp $CP $MAINCLASS >> output.txt ; } 2>> output.txt

if [[ -f $ARTIFACT ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  mv ./$ARTIFACT ..
  exit 0
else
  cat output.txt
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
  exit 1
fi
