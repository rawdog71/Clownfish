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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raine
 */

public class ClassPathUtil {
    private static final Class[] parameters = new Class[] { URL.class };
  
    public static void addPath(String libpath) {
        File dependencyDirectory = new File(libpath);
        File[] files = dependencyDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".jar")) {
                try {
                    addFile(files[i]);
                } catch (IOException ex) {
                    Logger.getLogger(ClassPathUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static void addFile(String aFileName) throws IOException
    {
        File f = new File(aFileName);
        addFile(f);
    }
  
    public static void addFile(File aFile) throws IOException
    {
        addURL(aFile.toURI().toURL());
    }
  
    public static void addURL(URL aURL) throws IOException
    {
        ArrayList<URL> urlarray = new ArrayList<>();
        urlarray.add(aURL);
        URL[] urllist = new URL[urlarray.size()];
        urllist = urlarray.toArray(urllist);
        URLClassLoader sysloader = new URLClassLoader(urllist);
        Class sysclass = URLClassLoader.class;
        try
        {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { aURL });
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw new IOException("Error adding " + aURL + " to system classloader");
        }
    }
}