package io.clownfish.clownfish.compiler;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

@Named("javaClassLoader")
@Scope("singleton")
@Component
public final class CfClassLoader extends URLClassLoader
{
    static
    {
        registerAsParallelCapable();
    }

    public CfClassLoader(String name, ClassLoader parent)
    {
        super(name, new URL[0], parent);
    }

    /*
     * Required when this ClassLoader is used as the system ClassLoader
     */
    public CfClassLoader(ClassLoader parent)
    {
        this("classpath", parent);
    }

    public CfClassLoader()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public void add(URL url)
    {
        addURL(url);
    }

    public static CfClassLoader findAncestor(ClassLoader cl)
    {
        do
        {
            if (cl instanceof CfClassLoader)
                return (CfClassLoader) cl;

            cl = cl.getParent();
        }
        while (cl != null);

        return null;
    }

    /*
     *  Required for Java Agents when this ClassLoader is used as the system ClassLoader
     */
    @SuppressWarnings("unused")
    private void appendToClassPathForInstrumentation(String jarfile) throws IOException
    {
        add(Paths.get(jarfile).toRealPath().toUri().toURL());
    }
}