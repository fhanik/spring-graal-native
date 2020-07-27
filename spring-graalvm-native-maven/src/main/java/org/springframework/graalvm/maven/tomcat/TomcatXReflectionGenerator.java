package org.springframework.graalvm.maven.tomcat;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "spring-graalvm-remove-tomcat-reflection", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true, threadSafe = false,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TomcatXReflectionGenerator
    extends AbstractMojo {

    @Parameter(defaultValue = "true", required = false)
    private boolean enabled = true;
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "generated/src/main/java/", readonly = true, required = false)
    private String generatedSourcesLocation;

    /**
     * Maven project helper utils.
     */
    @Component
    protected MavenProjectHelper projectHelper;

    private ClassLoader projectClassLoader = null;

    public TomcatXReflectionGenerator() {
    }

    public void execute()
        throws MojoExecutionException {

        try {

            if (isTomcatPresent()) {

                File destinationDirectory = new File(project.getBasedir(), generatedSourcesLocation);
                if (!destinationDirectory.exists()) {
                    destinationDirectory.mkdirs();
                }
                getLog().info("Generating Non Reflection Code for Apache Tomcat to: " +
                    destinationDirectory.getAbsolutePath());
                generateXReflectionSources(generatedSourcesLocation);
                project.addCompileSourceRoot(generatedSourcesLocation);

            } else {
                getLog().info("No Apache Tomcat Library Present. Skipping Apache Tomcat optimization");
            }
        } catch (RuntimeException e) {
            getLog().error(e);
            throw e;
        }
    }

    private void generateXReflectionSources(String destinationDirectory) {
        String[] args = {destinationDirectory};
        ClassLoader loader = getProjectClassLoader();
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class clazz = Class.forName("org.springframework.graalvm.maven.tomcat.ObjectReflectionPropertyInspector");
            Method main = clazz.getDeclaredMethod("main", String[].class);
            main.invoke(null, (Object) args);
        } catch (ClassNotFoundException | InvocationTargetException |NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private boolean isTomcatPresent() {
        boolean present;
        try {
            ClassLoader classLoader = getProjectClassLoader();
            Class.forName("org.apache.coyote.http11.Http11NioProtocol", false, classLoader);
            present = true;
        } catch (Exception e) {
            getLog().error(e);
            present = false;
        }
        return present;
    }

    private ClassLoader getProjectClassLoader() {
        if (projectClassLoader == null) {
            List<URL> classpathElements = new LinkedList<>();
            try {
                for(String mavenCompilePath: project.getCompileClasspathElements()) {
                    classpathElements.add(new File(mavenCompilePath).toURI().toURL());
                }
            } catch (DependencyResolutionRequiredException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
            URL[] classpath = classpathElements.toArray(new URL[classpathElements.size()]);
            projectClassLoader = new URLClassLoader(classpath, Thread.currentThread().getContextClassLoader());
        }

        return projectClassLoader;
    }


}
