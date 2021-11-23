package io.clownfish.clownfish.compiler;

import io.clownfish.clownfish.beans.LoginBean;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.serviceinterface.CfJavaversionService;
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
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named("javaCompiler")
@Scope("singleton")
@Component
public class CfClassCompiler
{
    @Inject LoginBean loginbean;
    @Autowired CfJavaService cfjavaService;
    @Autowired CfJavaversionService cfjavaversionService;
    private @Getter @Setter CfJava selectedJava = null;
    private @Getter @Setter String javaName = "";

    static Pattern publicClassNameRegex = Pattern.compile("public +(?:class|interface) +(\\w+)");
    static Pattern classNameRegex = Pattern.compile("(?:class|interface) +(\\w+)");

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CfClassCompiler.class);

    public CfClassCompiler() {}

    static Class<?> compileClass(/*CfClassLoader classLoader,*/ String className, String javaSource) throws ClassNotFoundException
    {
//        Matcher matcher = publicClassNameRegex.matcher(javaSource);
//
//        if (matcher.find())
//        {
//            className = matcher.group(1);
//            LOGGER.info("Class name: " + className);
//        }
//        else
//        {
//            matcher = classNameRegex.matcher(javaSource);
//            if (matcher.find())
//            {
//                className = matcher.group(1);
//                LOGGER.info("Class name: " + className);
//            }
//        }

        return CompilerUtils.CACHED_COMPILER.loadFromJava(/*classLoader,*/ className, javaSource);
    }

    public void onSelect(AjaxBehaviorEvent event)
    {
        if (null != selectedJava)
        {
            javaName = selectedJava.getName();
        }
        else
        {
            javaName = "";
        }
    }

    public void onCompile(ActionEvent actionEvent)
    {
        if (null != selectedJava)
        {
            // CfClassLoader cfClassLoader = new CfClassLoader();

            try
            {
                Class<?> newClass = compileClass(/*cfClassLoader,*/ javaName, selectedJava.getContent());

                LOGGER.info("Class name: " + newClass.getCanonicalName());
                LOGGER.info("Class package name: " + newClass.getPackageName());
                LOGGER.info("Class loader: " + newClass.getClassLoader());
                LOGGER.info("Main method invocation:");
                newClass.getMethod("main", String[].class).invoke(null, (Object) null);
            }
            catch (ClassNotFoundException | NoSuchMethodException e)
            {
                if (e instanceof ClassNotFoundException)
                    LOGGER.error("Class " + ((ClassNotFoundException) e).getClass() + " not found");
                if (e instanceof NoSuchMethodException)
                    LOGGER.error("No such method: " + ((NoSuchMethodException) e).getMessage());

                FacesMessage message = new FacesMessage("Could not compile " + selectedJava.getName() + ": " +
                        (e instanceof ClassNotFoundException ? "ClassNotFoundException" : "NoSuchMethodException"));
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
            catch (InvocationTargetException | IllegalAccessException e)
            {
                LOGGER.error(e.getMessage());
            }

            FacesMessage message = new FacesMessage("Compiled " + selectedJava.getName() + " successfully");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        else
        {
            FacesMessage message = new FacesMessage("No Java selected. Nothing changed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}