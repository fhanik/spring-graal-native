#!/usr/bin/env bash

set -e

ARTIFACT=tomcat-basic
MAINCLASS=com.example.tomcat.TomcatOnlyApplication
VERSION=0.0.1-SNAPSHOT

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native-image

echo "Packaging $ARTIFACT with Maven"
mvn -ntp package > target/native-image/output.txt

cd target/native-image

JAR="${ARTIFACT}-${VERSION}-jar-with-dependencies.jar"
rm -f $ARTIFACT

CP=../$JAR

echo "Generating reflection files for $ARTIFACT"
rm -rf graal/META-INF 2>/dev/null
mkdir -p graal/META-INF/native-image
java -agentlib:native-image-agent=config-output-dir=graal/META-INF/native-image -cp $CP $MAINCLASS >> output.txt 2>&1 &
PID=$!
sleep 3
curl -m 1 http://localhost:8080 > /dev/null 2>&1
sleep 1 && kill -9 $PID

echo "Performing class analysis on $ARTIFACT"
java -verbose:class -cp $CP $MAINCLASS >> output.txt 2>&1 &
PID=$!
sleep 3
curl -m 1 http://localhost:8080 > /dev/null 2>&1
sleep 1 && kill -9 $PID

cat output.txt |grep "\[Loaded" 2>&1 > class_histogram.txt

java -cp $CP:$FEATURE org.springframework.graal.util.OptimizeClassPath `pwd` class_histogram.txt $CP  >> output.txt 2>&1

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"

{ time native-image \
  --verbose --no-server \
  --initialize-at-build-time=org.apache.el.parser.SimpleNode \
  --report-unsupported-elements-at-runtime \
  --allow-incomplete-classpath \
  -H:EnableURLProtocols=http,jar \
  -H:ResourceConfigurationFiles=../../tomcat-resource.json \
  -H:ReflectionConfigurationFiles=../../tomcat-reflection.json \
  -H:+TraceClassInitialization \
  -H:Name=$ARTIFACT \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 \
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
