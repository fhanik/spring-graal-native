#!/usr/bin/env bash

ARTIFACT=springmvc-tomcat
MAINCLASS=com.example.tomcat.TomcatApplication
VERSION=0.0.1-SNAPSHOT

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native-image

echo "Packaging $ARTIFACT with Maven"
mvn -ntp package > target/native-image/output.txt

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native-image
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH

echo "Performing class analysis on $ARTIFACT"
rm -f class_histogram.txt
CLASS_AGENT=../../../../spring-graalvm-native-feature/target/spring-graalvm-native-feature-0.7.0.BUILD-SNAPSHOT-classlist-agent.jar
FEATURE=../../../../spring-graalvm-native-feature/target/spring-graalvm-native-feature-0.7.0.BUILD-SNAPSHOT.jar
rm -rf graal/META-INF 2>/dev/null
mkdir -p graal/META-INF/native-image
java -javaagent:$CLASS_AGENT -cp $CP $MAINCLASS >> output.txt 2>&1 &
PID=$!
sleep 3
curl -m 1 http://localhost:8080 > /dev/null 2>&1
sleep 1 && kill -9 $PID

cat output.txt |grep "Class\-Agent\-Transform\: " 2>&1 > class_histogram.txt
echo "Starting classpath reduction:"
java -cp $CP org.springframework.graal.util.OptimizeClassPath `pwd` class_histogram.txt $CP  >> output.txt 2>&1
grep "^Class\: " output.txt > class_presence.txt
grep "^Deleted\: " output.txt > class_deleted.txt


GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"
{ time native-image \
  --verbose \
  -H:EnableURLProtocols=http \
  -H:Name=$ARTIFACT \
  -Dspring.native.remove-yaml-support=true \
  -Dspring.native.remove-xml-support=true \
  -Dspring.native.remove-spel-support=true \
  -Dspring.native.remove-jmx-support=true \
  -cp $FEATURE:$CP $MAINCLASS >> output.txt ; } 2>> output.txt

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
