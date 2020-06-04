package org.springframework.graal.util;

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
                .filter(ze ->
                        ze.getName().endsWith(".class")
                        || ze.getName().endsWith(".xml")
                        || ze.getName().endsWith(".properties")
                )
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
                        System.out.println("Deleted: "+ jarFilePath + "; from "+jarDisk);
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
            "org/springframework/jdbc/CannotGetJdbcConnectionException.class" //spring.factories
            ,"javax/validation/ValidationException.class" //spring.factories
            ,"org/springframework/boot/diagnostics/FailureAnalysis.class" //service providers?
            ,"org/springframework/boot/json/JsonParser.class"
            ,"org/apache/commons/logging/LogFactoryService.class"
            ,"org/apache/commons/logging/LogFactory.class"

            ,"org/springframework/validation/FieldError.class" //reflection warning
            ,"org/springframework/validation/ObjectError.class" //reflection warning
            ,"com/fasterxml/jackson/databind/ObjectMapper.class" //reflection warning
            ,"org/springframework/context/support/DefaultMessageSourceResolvable.class" //reflection warning
            ,"org/springframework/util/concurrent/DelegatingCompletableFuture.class" //GRAAL ERROR?
            ,"org/springframework/context/MessageSourceResolvable.class" //reflection warning
            ,"com/fasterxml/jackson/databind/ObjectMapper.class" //reflection warning
            ,"javax/security/auth/message/config/AuthConfigProvider.class" //reflection warning
            ,"org/apache/coyote/UpgradeToken.class" //reflection warning
            ,"org/springframework/web/servlet/mvc/method/RequestMappingInfo.class" //reflection warning
            ,"org/springframework/web/servlet/ModelAndView.class" //reflection warning
            ,"org/apache/catalina/TrackedWebResource.class" //reflection warning
            ,"org/springframework/web/HttpRequestHandler.class" //reflection warning
            ,"org/springframework/web/servlet/handler/RequestMatchResult.class" //reflection warning
            ,"org/springframework/web/servlet/handler/AbstractHandlerMapping.class" //reflection warning
            ,"org/apache/catalina/authenticator/jaspic/PersistentProviderRegistrations.class" //reflection warning
            ,"org/springframework/web/bind/annotation/CrossOrigin.class" //reflection warning
            ,"org/springframework/web/method/annotation/SessionAttributesHandler.class" //reflection warning
            ,"org/springframework/web/server/ResponseStatusException.class" //reflection warning
            ,"org/springframework/web/context/request/async/AsyncRequestTimeoutException.class" //reflection warning
            ,"org/springframework/web/method/annotation/ModelFactory.class" //reflection warning
            ,"org/springframework/web/method/support/ModelAndViewContainer.class" //reflection warning
            ,"org/springframework/http/CacheControl.class" //reflection warning
            ,"org/springframework/boot/autoconfigure/web/servlet/error/DefaultErrorViewResolver.class" //reflection warning
            ,"org/springframework/boot/web/server/ErrorPageRegistrarBeanPostProcessor.class" //reflection warning
            ,"org/apache/tomcat/util/descriptor/web/ErrorPage.class" //reflection warning
            ,"com/fasterxml/jackson/databind/InjectableValues.class" //reflection warning
            ,"com/fasterxml/jackson/databind/jsontype/NamedType.class" //reflection warning
            ,"com/fasterxml/jackson/core/io/CharacterEscapes.class" //reflection warning
            ,"com/fasterxml/jackson/databind/jsonFormatVisitors/JsonFormatVisitorWrapper.class" //reflection warning
            ,"com/fasterxml/jackson/databind/module/SimpleValueInstantiators.class" //reflection warning
            ,"com/fasterxml/jackson/core/io/InputDecorator.class" //reflection warning
            ,"com/fasterxml/jackson/core/io/OutputDecorator.class" //reflection warning
            ,"com/fasterxml/jackson/databind/deser/ValueInstantiator.class" //reflection warning
            ,"com/fasterxml/jackson/databind/PropertyNamingStrategy.class" //reflection warning

            ,"ch/qos/logback/classic/servlet/LogbackServletContainerInitializer.class" //graal reflection error (service)
            ,"org/springframework/web/SpringServletContainerInitializer.class" //graal reflection error (service)
            ,"org/apache/logging/log4j/spi/LoggerContext.class" //graal NoClassDefFoundError
            ,"org/springframework/boot/BeanDefinitionLoader.class" //graal NoClassDefFoundError
            ,"org/springframework/boot/SpringBootExceptionHandler.class" //graal NoClassDefFoundError
            ,"org/springframework/beans/factory/support/BeanNameGenerator.class" //graal NoClassDefFoundError
            ,"org/springframework/boot/ExitCodeGenerator.class" //graal NoClassDefFoundError: [Lorg/springframework/boot/ExitCodeGenerator
            ,"org/springframework/web/method/annotation/ExceptionHandlerMethodResolver.class" //graal ClassNotFoundException
            ,"org/springframework/web/servlet/mvc/annotation/ModelAndViewResolver.class" //graal ClassNotFoundException
            ,"com/fasterxml/jackson/module/paramnames/ParameterNamesModule.class" //graal ClassNotFoundException
            ,"com/fasterxml/jackson/module/paramnames/ParameterNamesAnnotationIntrospector.class" //graal ClassNotFoundException
            ,"com/fasterxml/jackson/annotation/JsonCreator.class" //graal ClassNotFoundException
            ,"com/fasterxml/jackson/databind/ser/FilterProvider.class" //graal ClassNotFoundException
            ,"com/fasterxml/jackson/databind/util/LinkedNode.class" //graal ClassNotFoundException


            ,"org/apache/catalina/TomcatPrincipal.class" //runtime NoClassDefFoundError
            ,"org/apache/catalina/servlets/DefaultServlet.class" //graal spring-feature
            ,"org/apache/catalina/servlets/Target_DefaultServlet.class" //graal spring-feature


            ,"javax/servlet/http/LocalStrings.properties" //required resource
            ,"javax/servlet/LocalStrings.properties" //required resource
            ,"org/springframework/web/servlet/DispatcherServlet.properties" //required resource


            //new commits to master + sebastian optimized jafu-webmvc
            ,"org/springframework/context/i18n/TimeZoneAwareLocaleContext.class" //runtime NoClassDefFoundError
            ,"org/springframework/core/io/support/EncodedResource.class" //graal NoClassDefFoundError
            ,"org/springframework/web/accept/ContentNegotiationManager.class" //graal spring-feature
            ,"org/springframework/web/method/ControllerAdviceBean.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/ExceptionHandlerExceptionResolver.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/Target_ExceptionHandlerExceptionResolver.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/RequestMappingHandlerAdapter.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/Target_RequestMappingHandlerAdapter.class" //graal spring-feature
            ,"org/springframework/web/servlet/config/annotation/WebMvcConfigurationSupport.class" //graal spring-feature
            ,"org/springframework/web/servlet/config/annotation/Target_WebMvcConfigurationSupport.class" //graal spring-feature
            ,"org/springframework/web/servlet/function/support/RouterFunctionMapping.class" //graal spring-feature
            ,"org/springframework/web/servlet/function/support/Target_RouterFunctionMapping.class" //graal spring-feature
            ,"org/springframework/web/servlet/config/annotation/AsyncSupportConfigurer.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/AbstractHandlerMethodAdapter.class" //graal spring-feature
            ,"org/springframework/web/servlet/config/annotation/ResourceHandlerRegistry.class" //graal spring-feature
            ,"org/springframework/web/servlet/support/WebContentGenerator.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/RequestMappingHandlerMapping.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/RequestMappingInfoHandlerMapping.class" //graal spring-feature
            ,"org/springframework/web/servlet/config/annotation/ViewControllerRegistry.class" //graal spring-feature
            ,"org/springframework/web/bind/support/ConfigurableWebBindingInitializer.class" //graal spring-feature
            ,"org/springframework/web/method/support/CompositeUriComponentsContributor.class" //graal spring-feature
            ,"org/springframework/web/servlet/handler/AbstractHandlerMethodMapping.class" //graal spring-feature
            ,"org/springframework/web/servlet/handler/AbstractHandlerMethodExceptionResolver.class" //graal spring-feature
            ,"org/springframework/web/method/support/UriComponentsContributor.class" //graal spring-feature
            ,"org/springframework/web/method/support/HandlerMethodArgumentResolverComposite.class" //graal spring-feature
            ,"org/springframework/web/method/support/HandlerMethodArgumentResolver.class" //graal spring-feature
            ,"org/springframework/web/method/support/HandlerMethodReturnValueHandlerComposite.class" //graal spring-feature
            ,"org/springframework/web/method/support/HandlerMethodReturnValueHandler.class" //graal spring-feature
            ,"org/springframework/web/bind/support/SessionAttributeStore.class" //graal spring-feature
            ,"org/springframework/web/HttpRequestMethodNotSupportedException.class" //graal spring-feature
            ,"org/springframework/web/HttpSessionRequiredException.class" //graal spring-feature
            ,"org/springframework/web/method/HandlerMethod.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/ServletInvocableHandlerMethod.class" //graal spring-feature
            ,"org/springframework/web/method/support/InvocableHandlerMethod.class" //graal spring-feature
            ,"org/springframework/web/bind/support/WebDataBinderFactory.class" //graal spring-feature
            ,"org/springframework/web/method/annotation/InitBinderDataBinderFactory.class" //graal spring-feature
            ,"org/springframework/web/bind/support/DefaultDataBinderFactory.class" //graal spring-feature
            ,"org/springframework/web/method/annotation/InitBinderDataBinderFactory.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/ServletRequestDataBinderFactory.class" //graal spring-feature
            ,"org/springframework/core/ReactiveAdapterRegistry.class" //graal spring-feature
            ,"org/springframework/web/bind/MissingServletRequestParameterException.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/support/DefaultHandlerExceptionResolver.class" //graal spring-feature
            ,"org/springframework/web/bind/MissingPathVariableException.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/RequestBodyAdvice.class" //graal spring-feature
            ,"org/springframework/web/bind/MethodArgumentNotValidException.class" //graal spring-feature
            ,"org/springframework/web/bind/UnsatisfiedServletRequestParameterException.class" //graal spring-feature
            ,"org/springframework/web/servlet/handler/HandlerMethodMappingNamingStrategy.class" //graal spring-feature
            ,"org/springframework/web/multipart/support/MissingServletRequestPartException.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/condition/RequestCondition.class" //graal spring-feature
            ,"org/springframework/web/servlet/mvc/method/annotation/ResponseBodyAdvice.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/admin/SpringApplicationAdminJmxAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/jmx/JmxAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/amqp/RabbitAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/batch/BatchAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/cache/CacheAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/couchbase/CouchbaseAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/hazelcast/HazelcastAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/redis/RedisAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/cassandra/CassandraAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/context/MessageSourceAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/dao/PersistenceExceptionTranslationAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/cassandra/CassandraDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/cassandra/CassandraReactiveDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/cassandra/CassandraReactiveRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/cassandra/CassandraRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/couchbase/CouchbaseDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/couchbase/CouchbaseReactiveDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/couchbase/CouchbaseReactiveRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/couchbase/CouchbaseRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/elasticsearch/ElasticsearchDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/elasticsearch/ElasticsearchRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/elasticsearch/ReactiveElasticsearchRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/elasticsearch/ReactiveElasticsearchRestClientAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/jdbc/JdbcRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/jpa/JpaRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/ldap/LdapRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/mongo/MongoDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/mongo/MongoReactiveDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/mongo/MongoReactiveRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/mongo/MongoRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/neo4j/Neo4jDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/neo4j/Neo4jRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/solr/SolrRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/r2dbc/R2dbcDataAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/r2dbc/R2dbcRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/r2dbc/R2dbcTransactionManagerAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/redis/RedisReactiveAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/redis/RedisRepositoriesAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/rest/RepositoryRestMvcAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/data/web/SpringDataWebAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/elasticsearch/ElasticsearchRestClientAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/flyway/FlywayAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/freemarker/FreeMarkerAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/groovy/template/GroovyTemplateAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/h2/H2ConsoleAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/hateoas/HypermediaAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/hazelcast/HazelcastJpaDependencyAutoConfiguration.class" //graal spring-feature
            ,"org/springframework/boot/autoconfigure/**" //graal spring-feature

            ,"org/apache/logging/log4j/**" //graal spring-feature

            ,"com/fasterxml/jackson/databind/ObjectMapper.class" //graal spring-feature
            ,"com/fasterxml/jackson/databind/ObjectReader.class"
            ,"com/fasterxml/jackson/core/io/OutputDecorator.class"
            ,"com/fasterxml/jackson/core/JsonFactory.class"
            ,"com/fasterxml/jackson/core/JsonEncoding.class"
            ,"com/fasterxml/jackson/databind/ObjectMapper.class"
            ,"com/fasterxml/jackson/databind/jsontype/NamedType.class"
            ,"com/fasterxml/jackson/core/io/InputDecorator.class"
            ,"com/fasterxml/jackson/core/FormatSchema.class"
            ,"com/fasterxml/jackson/core/io/OutputDecorator;.class"
            ,"com/fasterxml/jackson/databind/node/ArrayNode.class"
            ,"com/fasterxml/jackson/core/io/OutputDecorator;.class"
            ,"com/fasterxml/jackson/databind/node/ArrayNode.class"
            ,"com/fasterxml/jackson/databind/deser/ValueInstantiator.class"
            ,"com/fasterxml/jackson/annotation/JsonInclude.class"
            ,"com/fasterxml/jackson/annotation/JsonAutoDetect.class"
            ,"com/fasterxml/jackson/annotation/JsonSetter.class"
            ,"com/fasterxml/jackson/core/io/CharacterEscapes.class"
            ,"com/fasterxml/jackson/databind/module/SimpleValueInstantiators.class"
            ,"com/fasterxml/jackson/databind/InjectableValues.class"

            ,"org/apache/catalina/TrackedWebResource.class"
            ,"org/springframework/web/servlet/ModelAndView.class"
            ,"org/springframework/web/servlet/handler/RequestMatchResult.class"
            ,"org/springframework/web/bind/annotation/CrossOrigin.class"
            ,"org/springframework/core/io/ProtocolResolver.class"
            ,"org/springframework/web/servlet/handler/AbstractHandlerMapping.class"
            ,"org/springframework/web/HttpRequestHandler.class"
            ,"org/springframework/web/context/request/async/AsyncRequestTimeoutException.class"
            ,"org/springframework/web/servlet/handler/AbstractHandlerMapping.class"
            ,"org/springframework/web/server/ResponseStatusException.class"
            ,"org/springframework/web/method/annotation/ModelFactory.class"
            ,"org/springframework/web/method/annotation/SessionAttributesHandler.class"
            ,"org/springframework/web/method/support/ModelAndViewContainer.class"
            ,"org/springframework/http/CacheControl.class"
            ,"org/springframework/boot/autoconfigure/web/servlet/error/DefaultErrorViewResolver.class"
            ,"org/springframework/boot/web/server/ErrorPageRegistrarBeanPostProcessor.class"
            ,"org/apache/tomcat/util/descriptor/web/ErrorPage.class"
            ,"org/springframework/boot/autoconfigure/web/servlet/error/DefaultErrorViewResolver.class"
            ,"org/springframework/boot/web/server/ErrorPageRegistrarBeanPostProcessor.class"
            ,"org/apache/tomcat/util/descriptor/web/ErrorPage.class"
            ,"org/springframework/boot/autoconfigure/web/servlet/error/DefaultErrorViewResolver.class"
            ,"org/springframework/boot/BeanDefinitionLoader.class"
            ,"org/springframework/boot/SpringBootExceptionHandler.class"
            ,"org/springframework/beans/factory/support/BeanNameGenerator.class"
            ,"org/springframework/boot/ExitCodeGenerator.class"
            ,"org/springframework/boot/logging/LoggerConfiguration.class"
            ,"org/apache/tomcat/websocket/pojo/PojoMethodMapping.class"
            ,"com/fasterxml/jackson/databind/ObjectMapper.class"
            ,"com/fasterxml/jackson/core/io/IOContext.class"
            ,"ch/qos/logback/classic/spi/ThrowableProxy.class"
            ,"ch/qos/logback/core/joran/spi/RuleStore.class"
            ,"ch/qos/logback/core/recovery/ResilientFileOutputStream.class"
            ,"ch/qos/logback/core/recovery/ResilientOutputStreamBase.class"
            ,"com/fasterxml/jackson/core/format/MatchStrength.class"
            ,"com/fasterxml/jackson/core/format/InputAccessor.class"
            ,"ch/qos/logback/core/joran/action/Action.class"
            ,"ch/qos/logback/classic/spi/ThrowableProxyUtil.class"
            ,"ch/qos/logback/core/joran/action/ImplicitAction.class"
            ,"org/springframework/beans/factory/config/BeanDefinitionCustomizer.class"
            ,"org/apache/catalina/loader/ResourceEntry.class"
            ,"kotlin/KotlinVersion.class"
            ,"kotlin/reflect/jvm/internal/ReflectionFactoryImpl.class"
            ,"org/apache/tomcat/websocket/server/WsHttpUpgradeHandler.class"
            ,"org/apache/tomcat/websocket/pojo/PojoEndpointServer.class"
            ,"org/springframework/beans/factory/support/AutowireCandidateQualifier.class"
            ,"org/springframework/web/cors/UrlBasedCorsConfigurationSource.class"
            ,"com/fasterxml/**"
            ,"ch/qos/**"

//            ,"org/apache/logging/log4j/spi/AbstractLogger.class" //graal spring-feature
//            ,"org/apache/logging/log4j/spi/LocationAwareLogger.class" //graal spring-feature
//            ,"org/apache/logging/log4j/message/Message.class" //graal spring-feature
//            ,"org/apache/logging/log4j/LoggingException.class" //graal spring-feature
//            ,"org/apache/logging/log4j/message/MessageFactory2.class" //graal spring-feature


//            //GRAAL 192
//            "org/apache/coyote/UpgradeToken.class",
//            "javax/servlet/descriptor/JspConfigDescriptor.class",
//            "javax/security/auth/message/config/AuthConfigProvider.class",
//            "org/apache/catalina/authenticator/jaspic/PersistentProviderRegistrations.class",
//            "org/apache/catalina/core/NamingContextListener.class",
//            "org/apache/tomcat/util/net/SSLHostConfigCertificate.class",
//            "javax/servlet/http/HttpServlet.class",
//            "org/apache/catalina/TomcatPrincipal.class",
//            "org/apache/tomcat/util/http/parser/TokenList.class",
//            "org/apache/tomcat/util/http/ValuesEnumerator.class",
//            "org/apache/tomcat/PeriodicEventListener.class",
//            "org/apache/tomcat/util/http/parser/TokenList.class",
//            //GRAAL 200_EE
//            ,
//            "javax/servlet/ServletContainerInitializer.class",
//            "org/apache/tomcat/util/net/SSLContext.class"
//
//            //JAFU-WEBMVC
//            ,
//            "com/fasterxml/jackson/module/paramnames/ParameterNamesModule.class",
//            "org/springframework/web/servlet/mvc/method/RequestMappingInfo.class",
//            "com/fasterxml/jackson/module/paramnames/ParameterNamesAnnotationIntrospector.class",
//            "com/fasterxml/jackson/annotation/JsonCreator.class",
//            "org/springframework/web/method/annotation/ExceptionHandlerMethodResolver.class",

//            "org/springframework/web/SpringServletContainerInitializer.class",
//            "org/apache/logging/log4j/spi/LoggerContext.class",
//            "org/springframework/web/servlet/mvc/annotation/ModelAndViewResolver.class",
//            "com/fasterxml/jackson/databind/util/LinkedNode.class",
//            "com/fasterxml/jackson/databind/ser/FilterProvider.class",
//            "org/springframework/context/i18n/TimeZoneAwareLocaleContext.class"
//
//            //JAFU-WEBMVC - Reflection warnings
              ,"org/springframework/web/bind/annotation/RestController.class"
              ,"org/springframework/beans/factory/support/BeanDefinitionResource.class"


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
            classname = classname.replaceAll("\\$.*\\.class", ".class");
            present = classnames.contains(classname) || alwaysPresent.contains(classname);
        }
        if (present) {
            return true;
        }

        //find and match patterns
        AntPathMatcher matcher = new AntPathMatcher();
        for (String name : alwaysPresent) {
            if (name.contains("*")) {
                if (matcher.match(name, classname)) {
                    return true;
                }
            }
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
