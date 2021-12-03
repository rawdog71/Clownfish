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

import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author raine
 */
@Scope("singleton")
@Component
public class ClassPathUtil implements Serializable {
    private @Getter HashSet<Class<?>> class_set = new HashSet<>();
  
    public void addPath(String libpath) {
        File dependencyDirectory = new File(libpath);
        File[] files = dependencyDirectory.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (!files[i].getName().endsWith(".jar")) {
                ArrayUtils.remove(files, i);
            }
        }
        try {
            addURL(files);
        } catch (IOException e) {
            Logger.getLogger(ClassPathUtil.class.getName()).log(Level.SEVERE, null, e);
        }
    }
  
    public void addURL(File[] files) throws IOException
    {
        //List<ClassInfo> cilist = ClassPath.from(sysloader).getTopLevelClasses("org.joda.time").asList();
        ClassLoader sysloader = ClassLoader.getSystemClassLoader();
        // Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        // method.setAccessible(true);

        JarFile[] jarFiles = new JarFile[files.length];
        URL[] urls = new URL[jarFiles.length];

        for (int i = 0; i < jarFiles.length; i++)
        {
            jarFiles[i] = new JarFile(files[i]);
            urls[i] = new URL("jar:file:" + files[i].toString() + "!/");
        }

        URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls);

        for (JarFile jar : jarFiles)
        {
            int count = 0;
            Enumeration<JarEntry> jarEntries = jar.entries();
            while (jarEntries.hasMoreElements())
            {
                count++;
                JarEntry je = jarEntries.nextElement();
                System.out.println("Element " + count + ": " + je.getName());

                if (je.isDirectory() || !je.getName().endsWith(".class"))
                    continue;

                // -6 because of .class
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                try
                {
                    Class<?> c = urlClassLoader.loadClass(className);
                    class_set.add(c);
                }
                catch (ClassNotFoundException e)
                {
                    Logger.getLogger(ClassPathUtil.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }

        // method.invoke(urlClassLoader, url);
        // class_set.add(ClassPath.from(urlClassLoader).getAllClasses().stream().map(clazz -> clazz.load()).getClass());
    }
}