/*
 * Copyright 2021 raine.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.compiler.CfClassLoader;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.LoggerFactory;

/**
 *
 * @author raine
 */
@Scope("singleton")
@Component
public class ClassPathUtil implements Serializable {
    private @Getter HashSet<Class<?>> class_set = new HashSet<>();
    private CfClassLoader cfclassLoader;
    
    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClassPathUtil.class);

    public void init(CfClassLoader cfclassLoader_)
    {
        cfclassLoader = cfclassLoader_;
    }

    public void addPath(String libpath) {
        File dependencyDirectory = new File(libpath);
        File[] files = dependencyDirectory.listFiles();

        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].getName().endsWith(".jar")) {
                    ArrayUtils.remove(files, i);
                }
            }
            try {
                loadClassesFromJar(files);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void loadClassesFromJar(File[] files) throws IOException
    {
        //CfClassLoader cl = (CfClassLoader) ClassLoader.getSystemClassLoader();
        JarFile[] jarFiles = new JarFile[files.length];
        URL[] urls = new URL[jarFiles.length];

        for (int i = 0; i < jarFiles.length; i++)
        {
            jarFiles[i] = new JarFile(files[i]);
            urls[i] = new URL("jar:file:" + files[i].toString() + "!/");
            cfclassLoader.add(urls[i]);
        }

        for (JarFile jar : jarFiles)
        {
            Enumeration<JarEntry> jarEntries = jar.entries();
            while (jarEntries.hasMoreElements())
            {
                JarEntry je = jarEntries.nextElement();

                if (je.isDirectory() || !je.getName().endsWith(".class"))
                    continue;

                // -6 because of .class
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                try
                {
                    Class<?> c = cfclassLoader.loadClass(className);
                    System.out.println(c.getCanonicalName() + ": Class loader " + c.getClassLoader());
                    System.out.println(c.getCanonicalName() + " package: " + c.getPackageName());

                    class_set.add(c);
                }
                catch (ClassNotFoundException | NoClassDefFoundError e)
                {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }
}