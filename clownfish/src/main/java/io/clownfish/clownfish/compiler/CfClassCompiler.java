package io.clownfish.clownfish.compiler;

import io.clownfish.clownfish.Clownfish;
import io.clownfish.clownfish.beans.LoginBean;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.utils.PropertyUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.primefaces.extensions.model.monacoeditor.EScrollbarHorizontal;
import org.primefaces.extensions.model.monacoeditor.EScrollbarVertical;
import org.primefaces.extensions.model.monacoeditor.ETheme;
import org.primefaces.extensions.model.monacoeditor.EditorOptions;
import org.primefaces.extensions.model.monacoeditor.EditorScrollbarOptions;

@Named("javaCompiler")
@Scope("singleton")
@Component
public class CfClassCompiler
{
    @Inject LoginBean loginbean;
    private static CfClassLoader cfclassLoader;
    private static PropertyUtil propertyUtil;
    private static CfJavaService cfjavaService;
    private @Getter @Setter List<CfJava> javaListSelected;
    private @Getter @Setter Map<Class<?>, List<Method>> classMethodMap = new HashMap<>();
    private static @Getter @Setter Path tmpdir;
    @Getter @Setter StringWriter compileOut;
    @Getter @Setter boolean verboseCompile = true;
    private static Clownfish clownfish;
    private @Getter @Setter EditorOptions editorOptions;
    @Getter @Setter ArrayList<Class<?>> classesList;

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CfClassCompiler.class);

    public CfClassCompiler() {}
    
    public void setClownfish(Clownfish clownfish) {
        this.clownfish = clownfish;
    }
    
    public void init(CfClassLoader cfclassLoader_, PropertyUtil propertyUtil_, CfJavaService cfjavaService_)
    {
        classesList = new ArrayList<>();
        cfclassLoader = cfclassLoader_;
        propertyUtil = propertyUtil_;
        cfjavaService = cfjavaService_;
        editorOptions = new EditorOptions();
        editorOptions.setLanguage("java");
        editorOptions.setTheme(ETheme.VS_DARK);
        editorOptions.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
    }

    public ArrayList<Class<?>> compileClasses(ArrayList<File> srcfiles, boolean withMessage)
    {
        ArrayList<File> java = new ArrayList<>();
        ArrayList<File> kotlin = new ArrayList<>();
        ArrayList<File> groovy = new ArrayList<>();
        for (File f : srcfiles) {
            switch (FilenameUtils.getExtension(f.getName())) {
                case "java":
                    java.add(f);
                    break;
                case "kt":
                    kotlin.add(f);
                    break;
                case "groovy":
                    groovy.add(f);
                    break;
            }
        }
        setCompileOut(new StringWriter());
        
        if (!kotlin.isEmpty()) {
            try {
                File tmpDirRoot = new File(getTmpdir().getParent().getParent().getParent().toString());
                cfclassLoader.add(tmpDirRoot.toURI().toURL());
                
                ProcessBuilder builder = new ProcessBuilder();
                for (File kotlinfile : kotlin) {
                    String className = kotlinfile.getName().replaceFirst("[.][^.]+$", "");
                    LOGGER.info("COMPILING " + className + "...");
                    compileOut.append("COMPILING " + className + "...\n");
                    compileOut.flush();
                    if (isWindows()) {
                        builder.command("cmd.exe", "/c", "kotlinc -d " + tmpDirRoot.toString() + " " + kotlinfile.getCanonicalPath());
                    } else {
                        builder.command("sh", "-c", "kotlinc -d " + tmpDirRoot.toString() + " " + kotlinfile.getCanonicalPath());
                    }
                    builder.directory(getTmpdir().toFile());
                    builder.redirectErrorStream(true);
                    String result = IOUtils.toString(builder.start().getInputStream(), StandardCharsets.UTF_8);
                    if (result.isBlank()) {
                        compileOut.append("OK\n");
                        compileOut.flush();
                        LOGGER.info("OK");
                        if (withMessage)
                        {
                            FacesMessage message = new FacesMessage("Compiled " + className + " successfully");
                            message.setSeverity(FacesMessage.SEVERITY_INFO);
                            FacesContext.getCurrentInstance().addMessage(null, message);
                        }
                    } else {
                        compileOut.append("ERROR\n");
                        compileOut.append(result);
                        compileOut.flush();
                        LOGGER.error("ERROR");
                        LOGGER.error(result);
                        if (withMessage)
                        {
                            FacesMessage message = new FacesMessage("Compiled " + className + " error", result);
                            message.setSeverity(FacesMessage.SEVERITY_ERROR);
                            FacesContext.getCurrentInstance().addMessage(null, message);
                        }
                    }
                }
                
                for (File file : kotlin)
                {
                    String className = file.getName().replaceFirst("[.][^.]+$", "");
                    LOGGER.info("LOADING " + className + "...");
                    compileOut.append("LOADING " + className + "...\n");
                    compileOut.flush();
                    classesList.add(cfclassLoader.loadClass("io.clownfish.kotlin." + className));
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error(ex.getMessage());
                compileOut.append(ex.getMessage() + "\n");
                compileOut.flush();
            }
        }
        
        if (!groovy.isEmpty()) {
            try {
                File tmpDirRoot = new File(getTmpdir().getParent().getParent().getParent().toString());
                cfclassLoader.add(tmpDirRoot.toURI().toURL());
                
                ProcessBuilder builder = new ProcessBuilder();
                for (File groovyfile : groovy) {
                    String className = groovyfile.getName().replaceFirst("[.][^.]+$", "");
                    LOGGER.info("COMPILING " + className + "...");
                    compileOut.append("COMPILING " + className + "...\n");
                    compileOut.flush();
                    if (isWindows()) {
                        builder.command("cmd.exe", "/c", "groovyc -d " + tmpDirRoot.toString() + " " + groovyfile.getCanonicalPath());
                    } else {
                        builder.command("sh", "-c", "groovyc -d " + tmpDirRoot.toString() + " " + groovyfile.getCanonicalPath());
                    }
                    builder.directory(getTmpdir().toFile());
                    builder.redirectErrorStream(true);
                    String result = IOUtils.toString(builder.start().getInputStream(), StandardCharsets.UTF_8);
                    if (result.isBlank()) {
                        compileOut.append("OK\n");
                        compileOut.flush();
                        LOGGER.info("OK");
                        if (withMessage)
                        {
                            FacesMessage message = new FacesMessage("Compiled " + className + " successfully");
                            message.setSeverity(FacesMessage.SEVERITY_INFO);
                            FacesContext.getCurrentInstance().addMessage(null, message);
                        }
                    } else {
                        compileOut.append("ERROR\n");
                        compileOut.append(result);
                        compileOut.flush();
                        LOGGER.error("ERROR");
                        LOGGER.error(result);
                        if (withMessage)
                        {
                            FacesMessage message = new FacesMessage("Compiled " + className + " error", result);
                            message.setSeverity(FacesMessage.SEVERITY_ERROR);
                            FacesContext.getCurrentInstance().addMessage(null, message);
                        }
                    }
                }
                
                for (File file : groovy)
                {
                    String className = file.getName().replaceFirst("[.][^.]+$", "");
                    LOGGER.info("LOADING " + className + "...");
                    compileOut.append("LOADING " + className + "...\n");
                    compileOut.flush();
                    classesList.add(cfclassLoader.loadClass("io.clownfish.groovy." + className));
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error(ex.getMessage());
                compileOut.append(ex.getMessage() + "\n");
                compileOut.flush();
            }
        }
        
        if (!java.isEmpty()) {
            try
            {
                List<String> options = new ArrayList<>(Arrays.asList("-classpath", constructClasspath()));

                File tmpDirRoot = new File(getTmpdir().getParent().getParent().getParent().toString());
                cfclassLoader.add(tmpDirRoot.toURI().toURL());

                options.addAll(Arrays.asList("-d", tmpDirRoot.toString()));

                if (verboseCompile)
                    options.addAll(List.of("-verbose"));

                JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager jfm = javac.getStandardFileManager(null, null, Charset.defaultCharset());
                jfm.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(getTmpdir().toFile()));

                Iterable<? extends JavaFileObject> compilationUnits = jfm.getJavaFileObjectsFromFiles(java);
                JavaCompiler.CompilationTask task = javac.getTask(compileOut, jfm, null, options,
                        Collections.emptySet(), compilationUnits);

                for (JavaFileObject jfo : compilationUnits)
                    LOGGER.info("Compiling " + jfo.getName() + "...");

                if (task.call())
                {
                    for (File file : java)
                    {
                        String className = file.getName().replaceFirst("[.][^.]+$", "");
                        LOGGER.info("LOADING " + className + "...");
                        classesList.add(cfclassLoader.loadClass("io.clownfish.java." + className));
                    }

                    if (withMessage)
                    {
                        FacesMessage message = new FacesMessage("Compiled class(es) successfully");
                        message.setSeverity(FacesMessage.SEVERITY_INFO);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                }
                else
                {
                    if (withMessage)
                    {
                        FacesMessage message = new FacesMessage("Compilation failed! Check compile log.");
                        message.setSeverity(FacesMessage.SEVERITY_ERROR);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                }
            }
            catch (ClassNotFoundException | IOException | IllegalStateException /*| NoSuchMethodException | IllegalAccessException | InvocationTargetException*/ e)
            {
                LOGGER.error(e.getMessage());
            }
        }
        
        if (!classesList.isEmpty()) {
            classMethodMap.clear();
            for (Class<?> clazz : classesList)
            {
                classMethodMap.put(clazz, new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods())));

                LOGGER.info("Class name: " + clazz.getCanonicalName());
                LOGGER.info("Class package name: " + clazz.getPackageName());
                LOGGER.info("Class loader: " + clazz.getClassLoader());

            }
            classMethodMap.forEach((k, v) -> v.forEach(method -> LOGGER.info(k.getSimpleName() + ": " + method.getName())));
            return classesList;
        } else {
            return null;
        }
    }

    // Create classpath String from temp dir, maven libs and custom libs for compilation
    public String constructClasspath() throws IOException
    {
        // StringBuilder classPath = new StringBuilder(System.getProperty("java.class.path").trim());
        StringBuilder classPath = new StringBuilder(System.getProperty("java.class.path"));

        // $CLOWNFISH_DIR/cache/src/io/clownfish/internal
        // classPath.append(classpathDelim()).append("\"").append(getTmpdir().toString()).append("\"");
        classPath.append(classpathDelim()).append(getTmpdir().toString());

        // $CLOWNFISH_DIR/maven/
        List<File> maven = Files.walk(Paths.get(propertyUtil.getPropertyValue("folder_maven")))
                        .filter(Files::isRegularFile).sorted(Comparator.reverseOrder()).map(Path::toFile)
                        .collect(Collectors.toList());

        if (!maven.isEmpty())
        {
            maven.stream().filter(file -> file.getName().endsWith(".jar")).collect(Collectors.toList())
                    //.forEach(jar -> classPath.append(classpathDelim()).append("\"").append(jar.getAbsolutePath()).append("\""));
                    .forEach(jar -> classPath.append(classpathDelim()).append(jar.getAbsolutePath()));
        }

        // $CLOWNFISH_DIR/libs
        List<File> libs = Files.walk(Paths.get(propertyUtil.getPropertyValue("folder_libs")))
                .filter(Files::isRegularFile).sorted(Comparator.reverseOrder()).map(Path::toFile)
                .collect(Collectors.toList());

        if (!libs.isEmpty())
        {
            libs.stream().filter(file -> file.getName().endsWith(".jar")).collect(Collectors.toList())
                    //.forEach(jar -> classPath.append(classpathDelim()).append("\"").append(jar.getAbsolutePath()).append("\""));
                    .forEach(jar -> classPath.append(classpathDelim()).append(jar.getAbsolutePath()));
        }
        LOGGER.info("CLASSPATH: " + classPath.toString());
        return classPath.toString();
    }

    public void onCompileAll()
    {
        compileAll(true);

        FacesMessage message = new FacesMessage("Compiled class(es) successfully");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void initClasspath() {
        clownfish.initClasspath();
    }
    
    public void compileAll(boolean withMessage) {
        if (getTmpdir() == null)
        {
            try
            {
                createTempDir();
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage());
            }
        }

        ArrayList<File> javas = new ArrayList<>();

        for (CfJava java : cfjavaService.findAll())
        {
            String extension = "";
            if (tmpdir != null)
            {
                switch (java.getLanguage()) {
                    case 0:
                        extension = ".java";
                        break;
                    case 1:
                        extension = ".kt";
                        break;
                    case 2:
                        extension = ".groovy";
                        break;
                }
                
                File src = new File(getTmpdir().toFile() + File.separator + java.getName() + extension);

                try (Writer srcWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(src), StandardCharsets.UTF_8)))
                {
                    srcWriter.write(java.getContent());
                    javas.add(src);
                }
                catch (IOException e)
                {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        compileClasses(javas, withMessage);
    }

    public void createTempDir() throws IOException
    {
        //CfClassLoader cl = (CfClassLoader) ClassLoader.getSystemClassLoader();
        Path tmp = Files.createTempDirectory(Paths.get(propertyUtil.getPropertyValue("folder_cache")), "src");

        // $CLOWNFISH_DIR/cache/src/io/clownfish/internal
        File dirs = new File(Paths.get(tmp.toString()
                + File.separator + "io"
                + File.separator + "clownfish"
                + File.separator + "internal").toString());
        dirs.mkdirs();
        setTmpdir(dirs.toPath());

        // Add temp dir to classpath
        cfclassLoader.add(dirs.toURI().toURL());

        // Recusively delete temp dir and contents on graceful shutdown
        Thread deleteHook = new Thread(() ->
        {
            try
            {
                Stream<Path> files = Files.walk(tmp);
                files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::deleteOnExit);
                files.close();

                Stream<Path> directories = Files.walk(tmp.getParent());
                directories.filter(dir -> Files.isDirectory(dir)).map(Path::toFile).forEach(File::deleteOnExit);
                directories.close();
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage());
            }
        });
        Runtime.getRuntime().addShutdownHook(deleteHook);
    }

    private boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private String classpathDelim()
    {
        return isWindows() ? ";" : ":";
    }
    
    private static class MyOutputStream extends OutputStream {
        private final Consumer<String> consumer;
        private final StringBuffer stringBuffer = new StringBuffer();

        public MyOutputStream(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void write(int b) throws IOException {
            stringBuffer.append((char) b);
        }

        @Override
        public void flush() {
            if (stringBuffer.length() != 0) {
                consumer.accept(stringBuffer.toString().trim());
                stringBuffer.delete(0, stringBuffer.length());
            }
        }
    }
}