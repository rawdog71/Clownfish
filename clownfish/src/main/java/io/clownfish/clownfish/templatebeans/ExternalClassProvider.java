/*
 * Copyright 2022 raine.
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
package io.clownfish.clownfish.templatebeans;

import io.clownfish.clownfish.compiler.CfClassCompiler;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Scope("request")
@Component
public class ExternalClassProvider implements Serializable {
    private final HashMap<String, Object> classMap;
    private final CfClassCompiler cfclassCompiler;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ExternalClassProvider.class);

    public ExternalClassProvider(CfClassCompiler cfclassCompiler) {
        this.cfclassCompiler = cfclassCompiler;
        classMap = new HashMap<>();
    }
    
    public Object get(String instancename) {
        return classMap.get(instancename);
    }
    
    public Object newInstance(String instancename, String clazzname) {
        Constructor<?> ctor;
        for (Class c : cfclassCompiler.getClassesList()) {
            if (0 == c.getName().compareTo(clazzname)) {
                try {
                    ctor = c.getConstructor();
                    Object object = ctor.newInstance();
                    classMap.put(instancename, object);
                    return object;
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.error(ex.getMessage());
                    return null;
                }
            }
        }
        return null;
    }
}
