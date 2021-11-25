package io.clownfish.clownfish.compiler;

import io.clownfish.clownfish.beans.LoginBean;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import lombok.Getter;
import lombok.Setter;
import net.openhft.compiler.CompilerUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named("javaCompiler")
@Scope("singleton")
@Component
public class CfClassCompiler
{
    @Inject LoginBean loginbean;
    @Autowired CfJavaService cfjavaService;
    private @Getter @Setter List<CfJava> javaListSelected;
    private @Getter @Setter Set<Class<?>> loadedClasses = new HashSet<>();

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CfClassCompiler.class);

    public CfClassCompiler() {}

    //TODO: Custom ClassLoader with the ability to unload/reload already loaded classes at runtime to allow
    // changes to Java templates in Clownfish without a restart

    // ClassLoader before = Thread.currentThread().getContextClassLoader();
    // Thread.currentThread().setContextClassLoader(new CfClassLoader(selectedJava.getContent()));
    // ClassLoader after = Thread.currentThread().getContextClassLoader();
    // Class<?> newClass = Thread.currentThread().getContextClassLoader().loadClass("io.clownfish.internal." + javaName);
    public Class<?> compileClass(CfJava java)
    {
        Class<?> newClass = null;
        try
        {
            newClass = CompilerUtils.CACHED_COMPILER.loadFromJava("io.clownfish.internal." + java.getName(), java.getContent());
            loadedClasses.add(newClass);

            LOGGER.info("Successfully compiled " + java.getName());
            LOGGER.info("Class name: " + newClass.getCanonicalName());
            LOGGER.info("Class package name: " + newClass.getPackageName());
            LOGGER.info("Class loader: " + newClass.getClassLoader());
            // LOGGER.info("Class path: " + newClass.getResource(newClass.getSimpleName() + ".class").getURI());
            // LOGGER.info("Main method invocation:");
            // newClass.getMethod("main", String[].class).invoke(null, (Object) null);
        }
        catch (ClassNotFoundException /*| NoSuchMethodException | IllegalAccessException | InvocationTargetException*/ e)
        {
            LOGGER.error(e.getMessage());
        }

        return newClass;
    }

    public void onCompileSelected(ActionEvent actionEvent)
    {
        for (CfJava java : javaListSelected)
        {
            compileClass(java);
        }

        FacesMessage message = new FacesMessage("Compiled class(es) successfully");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void onCompileAll(ActionEvent actionEvent)
    {
        for (CfJava java : cfjavaService.findAll())
        {
            compileClass(java);
        }

        FacesMessage message = new FacesMessage("Compiled class(es) successfully");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
}