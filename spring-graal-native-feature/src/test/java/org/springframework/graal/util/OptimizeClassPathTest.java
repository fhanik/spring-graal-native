package org.springframework.graal.util;

import org.junit.Ignore;
import org.junit.Test;

public class OptimizeClassPathTest  {

    @Test
    @Ignore
    public void testOptimizeClassPathTest() throws Exception {
        OptimizeClassPath.main(
            "/development/pivotal/cloudfoundry/spring-projects/graal/spring-graal-native/spring-graal-native-samples/springmvc-tomcat/target/native-image",
            "class_histogram.txt",
            "BOOT-INF/classes:BOOT-INF/lib:BOOT-INF/lib/jackson-module-parameter-names-2.11.0.rc1.jar:BOOT-INF/lib/spring-boot-starter-2.3.0.M4.jar:BOOT-INF/lib/jackson-annotations-2.11.0.rc1.jar:BOOT-INF/lib/logback-core-1.2.3.jar:BOOT-INF/lib/jackson-core-2.11.0.rc1.jar:BOOT-INF/lib/spring-boot-starter-json-2.3.0.M4.jar:BOOT-INF/lib/spring-core-5.2.5.RELEASE.jar:BOOT-INF/lib/spring-boot-starter-logging-2.3.0.M4.jar:BOOT-INF/lib/spring-beans-5.2.5.RELEASE.jar:BOOT-INF/lib/spring-boot-starter-tomcat-2.3.0.M4.jar:BOOT-INF/lib/jul-to-slf4j-1.7.30.jar:BOOT-INF/lib/logback-classic-1.2.3.jar:BOOT-INF/lib/log4j-api-2.13.1.jar:BOOT-INF/lib/spring-boot-autoconfigure-2.3.0.M4.jar:BOOT-INF/lib/spring-aop-5.2.5.RELEASE.jar:BOOT-INF/lib/snakeyaml-1.26.jar:BOOT-INF/lib/spring-jcl-5.2.5.RELEASE.jar:BOOT-INF/lib/jakarta.annotation-api-1.3.5.jar:BOOT-INF/lib/spring-webmvc-5.2.5.RELEASE.jar:BOOT-INF/lib/jackson-databind-2.11.0.rc1.jar:BOOT-INF/lib/spring-boot-starter-web-2.3.0.M4.jar:BOOT-INF/lib/spring-context-indexer-5.2.5.RELEASE.jar:BOOT-INF/lib/jakarta.el-3.0.3.jar:BOOT-INF/lib/jackson-datatype-jdk8-2.11.0.rc1.jar:BOOT-INF/lib/spring-web-5.2.5.RELEASE.jar:BOOT-INF/lib/spring-boot-2.3.0.M4.jar:BOOT-INF/lib/spring-expression-5.2.5.RELEASE.jar:BOOT-INF/lib/log4j-to-slf4j-2.13.1.jar:BOOT-INF/lib/tomcat-embed-core-9.0.33.jar:BOOT-INF/lib/slf4j-api-1.7.30.jar:BOOT-INF/lib/jackson-datatype-jsr310-2.11.0.rc1.jar:BOOT-INF/lib/spring-context-5.2.5.RELEASE.jar::../../../../spring-graal-native/target/spring-graal-native-0.6.0.BUILD-SNAPSHOT.jar"
        );
    }
}