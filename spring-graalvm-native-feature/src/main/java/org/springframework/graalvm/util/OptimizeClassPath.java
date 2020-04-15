package org.springframework.graalvm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class OptimizeClassPath {

    public static void main(String... args) throws Exception {
        String directory = args[0];
        String file = args[1];
        String classpath = args[2];

        System.out.println("Directory: "+directory);
        System.out.println("Histogram File: "+file);
        System.out.println("Classpath: "+classpath);

        List<Path> jars = loadJarFilesFromClassPath(directory, classpath);
        Set<String> classnames = readLoadedClassesFromHistogram(directory, file);
        optimizeJars(jars, classnames);
    }


    private static void optimizeJars(List<Path> jars, Set<String> classnames) {
        jars.stream()
            .forEach(jar -> optimizeJar(jar, classnames));
    }
    private static void optimizeJar(Path jar, Set<String> classnames) {
        Map<String, String> jarProperties = new HashMap<>();
        jarProperties.put("create", "false");

        System.out.println();
        System.out.println("Optimizing JAR: "+jar.toString());

        Set<String> toBeDeleted = new HashSet<>();
        try (ZipFile zf  = new ZipFile(jar.toFile())) {
            zf.stream()
                .filter(ze -> ze.getName().endsWith(".class") || ze.getName().endsWith("native-image.properties"))
                .forEach(
                    ze -> {
                        boolean present = isClassPresent(classnames, ze.getName());
                        System.out.println("Class: "+ze.getName() +"; Present: "+present);
                        if (!present) {
                            toBeDeleted.add(ze.getName());
                        }

                    }
                );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        URI jarDisk = URI.create("jar:file:"+jar.toString());
        try (FileSystem fs = FileSystems.newFileSystem(jarDisk, jarProperties)) {
            toBeDeleted.stream()
                .forEach(filename -> {
                    Path jarFilePath = fs.getPath(filename);
                    /* Execute Delete */
                    try {
                        Files.delete(jarFilePath);
                        System.out.println("Deleted: "+ jarFilePath + "; from "+jarDisk.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static Set<String> alwaysPresent = new HashSet<>(
        Arrays.asList(
            "org/springframework/boot/diagnostics/FailureAnalysis.class",
            "org/springframework/boot/json/JsonParser.class",
            "org/springframework/validation/FieldError.class",
            "org/springframework/validation/ObjectError.class",
            "org/springframework/context/support/DefaultMessageSourceResolvable.class",
            "org/springframework/context/MessageSourceResolvable.class",
            "org/springframework/context/MessageSourceResolvable.class",
            "com/fasterxml/jackson/databind/ObjectMapper.class",
            //GRAAL 192
            "org/apache/coyote/UpgradeToken.class",
            "javax/servlet/descriptor/JspConfigDescriptor.class",
            "javax/security/auth/message/config/AuthConfigProvider.class",
            "org/apache/catalina/authenticator/jaspic/PersistentProviderRegistrations.class",
            "org/apache/catalina/core/NamingContextListener.class",
            "org/apache/tomcat/util/net/SSLHostConfigCertificate.class",
            "javax/servlet/http/HttpServlet.class",
            "org/apache/catalina/TomcatPrincipal.class",
            "org/apache/tomcat/util/http/parser/TokenList.class",
            "org/apache/tomcat/util/http/ValuesEnumerator.class",
            "org/apache/tomcat/PeriodicEventListener.class",
            "org/apache/tomcat/util/http/parser/TokenList.class",
            "org/apache/commons/logging/LogFactoryService.class",
            "org/apache/commons/logging/LogFactory.class"
            //GRAAL 200_EE
            ,
            "javax/servlet/ServletContainerInitializer.class",
            "org/apache/tomcat/util/net/SSLContext.class"

            //JAFU-WEBMVC
            ,
            "com/fasterxml/jackson/module/paramnames/ParameterNamesModule.class",
            "org/springframework/web/servlet/mvc/method/RequestMappingInfo.class",
            "com/fasterxml/jackson/module/paramnames/ParameterNamesAnnotationIntrospector.class",
            "com/fasterxml/jackson/annotation/JsonCreator.class",
            "org/springframework/web/method/annotation/ExceptionHandlerMethodResolver.class",
            "ch/qos/logback/classic/servlet/LogbackServletContainerInitializer.class",
            "org/springframework/web/SpringServletContainerInitializer.class",
            "org/apache/logging/log4j/spi/LoggerContext.class",
            "org/springframework/web/servlet/mvc/annotation/ModelAndViewResolver.class",
            "com/fasterxml/jackson/databind/util/LinkedNode.class",
            "com/fasterxml/jackson/databind/ser/FilterProvider.class",
            "org/springframework/context/i18n/TimeZoneAwareLocaleContext.class"

            //JAFU-WEBMVC - Reflection warnings
            ,
            "org/apache/catalina/TrackedWebResource.class",
            "org/springframework/web/servlet/ModelAndView.class",
            "org/springframework/web/servlet/handler/RequestMatchResult.class",
            "org/springframework/web/bind/annotation/CrossOrigin.class",
            "org/springframework/core/io/ProtocolResolver.class",
            "org/springframework/web/servlet/handler/AbstractHandlerMapping.class",
            "org/springframework/web/HttpRequestHandler.class",
            "org/springframework/web/context/request/async/AsyncRequestTimeoutException.class",
            "org/springframework/web/servlet/handler/AbstractHandlerMapping.class",
            "org/springframework/web/server/ResponseStatusException.class",
            "org/springframework/web/method/annotation/ModelFactory.class",
            "org/springframework/web/method/annotation/SessionAttributesHandler.class",
            "com/fasterxml/jackson/core/io/CharacterEscapes.class",
            "com/fasterxml/jackson/databind/module/SimpleValueInstantiators.class",
            "com/fasterxml/jackson/databind/InjectableValues.class",
            "org/springframework/web/method/support/ModelAndViewContainer.class",
            "com/fasterxml/jackson/databind/jsontype/NamedType.class",
            "com/fasterxml/jackson/core/io/InputDecorator.class",
            "com/fasterxml/jackson/core/FormatSchema.class",
            "org/springframework/http/CacheControl.class",
            "com/fasterxml/jackson/core/io/OutputDecorator;.class",
            "com/fasterxml/jackson/databind/node/ArrayNode.class",
            "com/fasterxml/jackson/core/io/OutputDecorator;.class",
            "com/fasterxml/jackson/databind/node/ArrayNode.class",
            "com/fasterxml/jackson/databind/deser/ValueInstantiator.class",
            "org/springframework/boot/autoconfigure/web/servlet/error/DefaultErrorViewResolver.class",
            "org/springframework/boot/web/server/ErrorPageRegistrarBeanPostProcessor.class",
            "org/apache/tomcat/util/descriptor/web/ErrorPage.class",
            "org/springframework/boot/autoconfigure/web/servlet/error/DefaultErrorViewResolver.class",
            "org/springframework/boot/web/server/ErrorPageRegistrarBeanPostProcessor.class",
            "org/apache/tomcat/util/descriptor/web/ErrorPage.class",
            "com/fasterxml/jackson/databind/ObjectReader.class",
            "com/fasterxml/jackson/core/io/OutputDecorator.class",
            "com.fasterxml.jackson.core.JsonFactory.class",
            "com/fasterxml/jackson/core/JsonEncoding.class",
            "com.fasterxml.jackson.databind.ObjectMapper.class",
            "com/fasterxml/jacks.classon/databind/PropertyNamingStrategy.class"
            ,"org/apache/tomcat/util/descriptor/web/MessageDestination.class"
            ,"org/apache/catalina/Cluster.class"


        )
    );
    private static boolean isClassPresent(Set<String> classnames, String classname) {
        boolean present = classnames.contains(classname) || alwaysPresent.contains(classname);
        if (present) {
            return true;
        }
        //if this is an inner class and the parent class is loaded
        //allow all inner class to remain
        int idx = classname.indexOf("$");
        if (idx > 0) {
            String parent = classname.replaceAll("\\$.*\\.class", ".class");
            return classnames.contains(parent) || alwaysPresent.contains(parent);
        }
        return false;
    }



    private static List<Path> loadJarFilesFromClassPath(String directory, String classpath) {
        List<Path> jars = Arrays.stream(classpath.split("\\:"))
            .filter(s -> s.endsWith(".jar") && !s.contains("spring-graal-native"))
            .map(s -> Paths.get(directory + "/" + s))
            .collect(Collectors.toList());

        jars.stream().forEach(p -> System.out.println("Path: "+p.toString()));

        return jars;
    }

    private static Set<String> readLoadedClassesFromHistogram(String directory, String file) throws IOException {
        Set<String> classes = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(new File(directory, file)));
        String s;
        while ((s = reader.readLine()) != null) {
            System.out.println("FILIP-TEST:"+s);
            String start = "Class-Agent-Transform:";
            if (s.startsWith(start)) {
                String classname = s.substring(start.length()+1);
                classes.add(classname);
                System.out.println("FILIP Identified Class=" + classname + " from "+file);
            }
        }

        return classes;
    }

}