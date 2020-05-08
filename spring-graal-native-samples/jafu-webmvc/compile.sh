#!/usr/bin/env bash

ARTIFACT=jafu-webmvc
MAINCLASS=com.example.jafu.DemoApplication
VERSION=0.0.1-SNAPSHOT
FEATURE=../../../../spring-graal-native/target/spring-graal-native-0.7.0.BUILD-SNAPSHOT.jar

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

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

if [ ! -f "$FEATURE" ]; then
    printf "${RED}FAILURE${NC}: $FEATURE does not exist, please build the root project before building this sample.\n"
    exit 1
fi

# Avoids clashing substitutions from this project deps and the feature deps
rm BOOT-INF/lib/svm-20.*.jar

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH:$FEATURE

echo "Performing class analysis on $ARTIFACT"
rm -f class_histogram.txt
CLASS_AGENT=../../../../spring-graal-native-feature/target/spring-graal-native-feature-0.7.0.BUILD-SNAPSHOT-classlist-agent.jar
rm -rf graal/META-INF 2>/dev/null
mkdir -p graal/META-INF/native-image
java -javaagent:$CLASS_AGENT -cp $CP $MAINCLASS >> output.txt 2>&1 &
PID=$!
sleep 3
curl -m 1 http://localhost:8080 > /dev/null 2>&1
sleep 1 && kill -9 $PID

cat output.txt |grep "Class\-Agent\-Transform\: " 2>&1 > class_histogram.txt
echo "Starting classpath reduction:"
java -cp $CP:$FEATURE org.springframework.graal.util.OptimizeClassPath `pwd` class_histogram.txt $CP  >> output.txt 2>&1

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"
{ time native-image \
  --verbose \
  --no-server \
  --no-fallback \
  -H:EnableURLProtocols=http \
  -H:Name=$ARTIFACT \
  -H:+ReportExceptionStackTraces \
  -Dspring.graal.mode=functional \
  -H:+PrintAnalysisCallTree -H:+PrintGraphFile -H:+PrintImageObjectTree \
  -H:+TraceClassInitialization \
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
