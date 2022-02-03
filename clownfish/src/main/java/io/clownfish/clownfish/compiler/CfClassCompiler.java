package io.clownfish.clownfish.compiler;

import freemarker.template.DefaultMapAdapter;
import io.clownfish.clownfish.Clownfish;
import io.clownfish.clownfish.beans.LoginBean;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.utils.ClownfishUtil;
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
public class CfClassCompiler implements Runnable
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
    public static CfCompilerLanguages compilerlanguages = null;

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CfClassCompiler.class);

    public CfClassCompiler() {
        if (null == compilerlanguages) {
            compilerlanguages = new CfCompilerLanguages();
        }
    }
    
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
    
    public CfCompilerLanguages getcompilerlanguages() {
        return compilerlanguages;
    }

    public ArrayList<Class<?>> compileClasses(ArrayList<File> srcfiles, boolean withMessage)
    {
        compilerlanguages.clearFilelist();
        for (File f : srcfiles) {
            for (JVMLanguages language : compilerlanguages.getJvm_languages().keySet()) {
                if (compilerlanguages.getJvm_languages().get(language)) {
                    if (0 == FilenameUtils.getExtension(f.getName()).compareToIgnoreCase(language.getExt())) {
                        language.getFilelist().add(f);
                    }
                }
            }
        }
        setCompileOut(new StringWriter());
        
        for (JVMLanguages language : compilerlanguages.getJvm_languages().keySet()) {
            if (compilerlanguages.getJvm_languages().get(language)) {
                switch (language) {
                    case KOTLIN:
                    case GROOVY:
                    case SCALA:
                        compile(language, withMessage);
                        break;
                    case JAVA:
                        if (!language.getFilelist().isEmpty()) {
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

                                Iterable<? extends JavaFileObject> compilationUnits = jfm.getJavaFileObjectsFromFiles(language.getFilelist());
                                JavaCompiler.CompilationTask task = javac.getTask(compileOut, jfm, null, options,
                                        Collections.emptySet(), compilationUnits);

                                for (JavaFileObject jfo : compilationUnits)
                                    LOGGER.info("Compiling " + jfo.getName() + "...");

                                if (task.call())
                                {
                                    for (File file : language.getFilelist())
                                    {
                                        String className = file.getName().replaceFirst("[.][^.]+$", "");
                                        LOGGER.info("LOADING " + className + "...");
                                        classesList.add(cfclassLoader.loadClass("io.clownfish.java." + className));
                                    }
                                    LOGGER.info(compileOut.toString());
                                    if (withMessage)
                                    {
                                        compileOut.flush();
                                        FacesMessage message = new FacesMessage("Compiled class(es) successfully");
                                        message.setSeverity(FacesMessage.SEVERITY_INFO);
                                        FacesContext.getCurrentInstance().addMessage(null, message);
                                    }
                                }
                                else
                                {
                                    LOGGER.error(compileOut.toString());
                                    if (withMessage)
                                    {
                                        compileOut.flush();
                                        FacesMessage message = new FacesMessage("Compilation failed! Check compile log.");
                                        message.setSeverity(FacesMessage.SEVERITY_ERROR);
                                        FacesContext.getCurrentInstance().addMessage(null, message);
                                    }
                                }
                            }
                            catch (ClassNotFoundException | IOException | IllegalStateException e)
                            {
                                compileOut.flush();
                                LOGGER.error(e.getMessage());
                            }
                        }
                        break;
                }
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
        StringBuilder classPath = new StringBuilder(System.getProperty("java.class.path"));

        // $CLOWNFISH_DIR/cache/src/io/clownfish/internal
        classPath.append(ClownfishUtil.classpathDelim()).append(getTmpdir().toString());

        // $CLOWNFISH_DIR/maven/
        List<File> maven = Files.walk(Paths.get(propertyUtil.getPropertyValue("folder_maven")))
                        .filter(Files::isRegularFile).sorted(Comparator.reverseOrder()).map(Path::toFile)
                        .collect(Collectors.toList());

        if (!maven.isEmpty())
        {
            maven.stream().filter(file -> file.getName().endsWith(".jar")).collect(Collectors.toList())
                    .forEach(jar -> classPath.append(ClownfishUtil.classpathDelim()).append(jar.getAbsolutePath()));
        }

        // $CLOWNFISH_DIR/libs
        List<File> libs = Files.walk(Paths.get(propertyUtil.getPropertyValue("folder_libs")))
                .filter(Files::isRegularFile).sorted(Comparator.reverseOrder()).map(Path::toFile)
                .collect(Collectors.toList());

        if (!libs.isEmpty())
        {
            libs.stream().filter(file -> file.getName().endsWith(".jar")).collect(Collectors.toList())
                    .forEach(jar -> classPath.append(ClownfishUtil.classpathDelim()).append(jar.getAbsolutePath()));
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
            if (tmpdir != null)
            {
                File src = new File(getTmpdir().toFile() + File.separator + java.getName() + getClassExtension(java));
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
        Path tmp = Files.createTempDirectory(Paths.get(propertyUtil.getPropertyValue("folder_libs")), "src");

        // $CLOWNFISH_DIR/libs/src/io/clownfish/internal
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
    
    private String getClassExtension(CfJava java) {
        String extension = "";
        for (JVMLanguages language : compilerlanguages.getJvm_languages().keySet()) {
            if (compilerlanguages.getJvm_languages().get(language)) {
                if (java.getLanguage() == language.getId()) {
                    extension = "." + language.getExt();
                    break;
                }
            }
        }
        return extension;
    }
    
    private void compile(JVMLanguages language, boolean withMessage) {
        if (!language.getFilelist().isEmpty()) {
            try {
                File tmpDirRoot = new File(getTmpdir().getParent().getParent().getParent().toString());
                cfclassLoader.add(tmpDirRoot.toURI().toURL());

                ProcessBuilder builder = new ProcessBuilder();
                for (File jvmfile : language.getFilelist()) {
                    String className = jvmfile.getName().replaceFirst("[.][^.]+$", "");
                    LOGGER.info("COMPILING " + className + "...");
                    compileOut.append("COMPILING " + className + "...\n");
                    compileOut.flush();
                    if (ClownfishUtil.isWindows()) {
                        builder.command("cmd.exe", "/c", language.getCompiler() + " -d " + tmpDirRoot.toString() + " " + jvmfile.getCanonicalPath());
                    } else {
                        builder.command("sh", "-c", language.getCompiler() + " -d " + tmpDirRoot.toString() + " " + jvmfile.getCanonicalPath());
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

                for (File file : language.getFilelist())
                {
                    String className = file.getName().replaceFirst("[.][^.]+$", "");
                    LOGGER.info("LOADING " + className + "...");
                    compileOut.append("LOADING " + className + "...\n");
                    compileOut.flush();
                    classesList.add(cfclassLoader.loadClass("io.clownfish." + language.getName().toLowerCase() + "." + className));
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error(ex.getMessage());
                compileOut.append(ex.getMessage() + "\n");
                compileOut.flush();
            }
        }
    }

    @Override
    public void run() {
        compileAll(false);
    }
}