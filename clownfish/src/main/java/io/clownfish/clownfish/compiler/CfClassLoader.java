package io.clownfish.clownfish.compiler;

import net.openhft.compiler.CompilerUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class CfClassLoader extends ClassLoader
{
    private final String JAVA_SRC;

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CfClassLoader.class);

    public CfClassLoader(String src)
    {
        super(CfClassLoader.class.getClassLoader());
        JAVA_SRC = src;
    }

    @Override
    public Class<?> loadClass(String name)
    {
        return findClass(name);
    }

    private byte[] loadByteCode(String name, String src) throws ClassNotFoundException, IOException
    {
        return Objects.requireNonNull(CompilerUtils.CACHED_COMPILER.loadFromJava(name, src).getResourceAsStream(name)).readAllBytes();
    }

    @Override
    protected Class<?> findClass(String name)
    {
        Class<?> clazz = null;

        try
        {
            if (super.findLoadedClass(name) == null)
            {
                clazz = CompilerUtils.CACHED_COMPILER.loadFromJava(this, name, JAVA_SRC);
            }
            else
            {
                super.findClass(name);
            }

            // clazz = CompilerUtils.CACHED_COMPILER.loadFromJava(this, name, JAVA_SRC);
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.error(e.getMessage());
        }

        return clazz;
    }
}
