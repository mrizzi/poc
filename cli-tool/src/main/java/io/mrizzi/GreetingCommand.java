package io.mrizzi;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Result;
import org.openrewrite.config.Environment;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Command(name = "greeting", mixinStandardHelpOptions = true)
public class GreetingCommand implements Runnable {

    static Recipe recipe = null;
    static JavaParser javaParser = null;
    static {
        System.out.println("ClassLoader.getSystemClassLoader() = " + ClassLoader.getSystemClassLoader());
        System.out.println("GreetingCommand.class.getClassLoader() = " + GreetingCommand.class.getClassLoader());
        System.out.println("Thread.currentThread().getContextClassLoader() = " + Thread.currentThread().getContextClassLoader());
        ClassGraph classGraph = new ClassGraph();
        System.out.println("initial classGraph = " + classGraph);
        classGraph.acceptPaths("META-INF/rewrite");
//        classGraph.enableMemoryMapping();
        classGraph.disableModuleScanning() // added for GraalVM
//                .disableDirScanning() // added for GraalVM
//                .disableNestedJarScanning() // added for GraalVM
//                .disableRuntimeInvisibleAnnotations() // added for GraalVM
        .addClassLoader(ClassLoader.getSystemClassLoader())
//        .addClassLoader(Thread.currentThread().getContextClassLoader())
//        .enableAnnotationInfo().enableMethodInfo().initializeLoadedClasses().scan();
        ;
        System.out.println("configured classGraph = " + classGraph);
        System.out.println("...and now scan...");
        ScanResult scanResult = classGraph.scan();
        System.out.println("scanResult = " + scanResult);
        System.out.println("\t---Resources found---");
        scanResult.getAllResources().forEach(System.out::println);

        // put any rewrite recipe jars on this main method's runtime classpath
        // and either construct the recipe directly or via an Environment
//        Environment environment = Environment.builder().scanRuntimeClasspath().build();
        Environment.Builder builder = Environment.builder();
        scanResult.getAllResources().forEach(resource -> {
            try {
                builder.load(new YamlResourceLoader(resource.open(), resource.getURI(), new Properties()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Environment environment = builder.build();
        System.out.println("environment.listRecipes = " + environment.listRecipes());
        recipe = environment.activateRecipes("org.openrewrite.java.cleanup.CommonStaticAnalysis");
        System.out.println("recipe = " + ((ArrayList)recipe.getRecipeList()).toString());

        // create a JavaParser instance 
/*
        javaParser = JavaParser.fromJavaVersion().build();
        System.out.println("javaParser = " + javaParser);
*/
    }

    @Parameters(paramLabel = "<name>", defaultValue = "picocli",
        description = "Your name.")
    String name;

    @Override
    public void run() {
        System.out.printf("Hello %s, go go commando!\n", name);
        // determine your project directory and provide a list of
        // paths to jars that represent the project's classpath
        Path projectDir = Paths.get(".");
/*
        MavenCli cli = new MavenCli();
        System.setProperty("maven.multiModuleProjectDirectory", ".");
        File classpathTmpFile = Paths.get(".","classpatha").toFile();
        System.setProperty("mdep.outputFile", classpathTmpFile.getAbsolutePath());
*/
/**
        final Charset charset = StandardCharsets.UTF_8;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = null;
        try {
            ps = new PrintStream(baos, true, charset.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        cli.doMain(new String[]{"-q", "dependency:build-classpath"}, ".", ps, ps);
        String content = baos.toString(charset);
        System.out.println("@@@ content " + content);
        ps.close();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
**/
/*
        cli.doMain(new String[]{"-q", "dependency:build-classpath"}, ".", System.out, System.out);

        List<Path> classpath = null;
        try {
            classpath = Arrays.stream(Files.readString(classpathTmpFile.toPath()).split(":"))
                    .map(Paths::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
//        List<Path> classpath = emptyList();
        List<Path> classpath = Collections.singletonList(Paths.get("/usr/lib/jvm/java-11"));
        System.out.println("Classpath " + classpath);

/*
        // put any rewrite recipe jars on this main method's runtime classpath
        // and either construct the recipe directly or via an Environment
        Environment environment = Environment.builder().scanRuntimeClasspath().build();
        Recipe recipe = environment.activateRecipes("org.openrewrite.java.cleanup.CommonStaticAnalysis");

*/
        // create a JavaParser instance with your classpath
        JavaParser javaParser = JavaParser.fromJavaVersion()
                .classpath(classpath)
                .build();
//        javaParser.setClasspath(classpath);

        // walk the directory structure where your Java sources are located
        // and create a list of them
        List<Path> sourcePaths = null;
        try {
            sourcePaths = Files.find(projectDir, 999, (p, bfa) ->
                            bfa.isRegularFile() && p.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parser the source files into ASTs
        List<J.CompilationUnit> cus = javaParser.parse(sourcePaths, projectDir,
                new InMemoryExecutionContext(Throwable::printStackTrace));

        // collect results
        List<Result> results = recipe.run(cus);

        for (Result result : results) {
            // print diffs to the console
            System.out.println(result.diff(projectDir));

            // or overwrite the file on disk with changes.
            // Files.writeString(result.getAfter().getSourcePath(),
            //        result.getAfter().printAll());
        }
        
    }

}
