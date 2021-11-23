package io.clownfish.clownfish.compiler;

import io.clownfish.clownfish.serviceinterface.CfJavaService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class CfClassLoader extends ClassLoader
{
    @Autowired transient CfJavaService cfJavaService;
    @Autowired transient CfClassCompiler cfClassCompiler;
    private final String className;
    private final byte[] byteCode;

    public CfClassLoader(String className, byte[] byteCode)
    {
        super(CfClassLoader.class.getClassLoader());

        this.className = className;
        this.byteCode = byteCode;
    }

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CfClassLoader.class);

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        if (name.equals(className))
        {
            byte[] bytes = new byte[0];

            try
            {
                bytes = loadClassFromContent(name);
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage());
            }

            return defineClass(name, bytes, 0, bytes.length);
        }

        return super.findClass(name);
    }

    private byte[] loadClassFromContent(String name) throws ClassNotFoundException, IOException
    {
        return cfClassCompiler.compileClass(/*this,*/ name, cfJavaService.findByName(name).getContent()).getResourceAsStream(name).readAllBytes();
    }
}
