/*
 * Copyright 2021 SulzbachR.
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

import com.google.common.reflect.ClassPath;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.faces.view.ViewScoped;

/**
 *
 * @author SulzbachR
 */
@ViewScoped
@Component
public class BeanUtil implements Serializable {
    private Set<Class> templatebeans = null;
    private @Getter @Setter ArrayList<Class> loadabletemplatebeans = new ArrayList<>();
    
    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);
    
    public BeanUtil() {
    }

    public void init(String libloaderpath) {
        try {
            templatebeans = findAllClassesInLibfolder(libloaderpath);
            templatebeans.forEach(cl -> {
                    if (null != cl.getCanonicalName()) {
                        LOGGER.info("EXTERNAL CLASS -> " + cl.getCanonicalName());
                        loadabletemplatebeans.add(cl);
                    }
                }
            );
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    private Set<Class> findAllClassesInPackage(String packageName) throws IOException {
        return ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName()
                .equalsIgnoreCase(packageName))
                .map(clazz -> clazz.load())
                .collect(Collectors.toSet());
    }
    
    private Set<Class> findAllClassesInLibfolder(String libdir) throws IOException {
        File dependencyDirectory = new File(libdir);
        File[] files = dependencyDirectory.listFiles();
        ArrayList<URL> urls = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".jar")) {
                urls.add(files[i].toURI().toURL());
            }
        }
        
        URL[] urlArr = new URL[urls.size()];
        urlArr = urls.toArray(urlArr);
        
        URLClassLoader cl = new URLClassLoader(urlArr);
        return ClassPath.from(cl)
                .getTopLevelClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName()
                .startsWith("io.clownfish.ext"))
                .filter(clazz -> isClassLoadable(clazz))
                .map(clazz -> clazz.load())
                .collect(Collectors.toSet());
    }
    
    private boolean isClassLoadable(ClassPath.ClassInfo clazz)
    {
        try {
            clazz.load();
        } catch (UnsupportedClassVersionError ex) {
            return false;
        }
        return true;
    }
}
