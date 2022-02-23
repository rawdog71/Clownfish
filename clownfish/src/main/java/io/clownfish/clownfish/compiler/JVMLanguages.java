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
package io.clownfish.clownfish.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author raine
 */
public enum JVMLanguages {
    JAVA(0, "Java", "java", "javac", "javac "), 
    KOTLIN(1, "Kotlin", "kt", "kotlinc", "kotlinc-jvm"), 
    GROOVY(2, "Groovy", "groovy", "groovyc", "Groovy compiler version"), 
    SCALA(3, "Scala", "scala", "scalac", "Scala compiler version");

    private @Getter @Setter int id;
    private @Getter @Setter String name;
    private @Getter @Setter String ext;
    private @Getter @Setter String compiler;
    private @Getter @Setter String result;
    private @Getter @Setter ArrayList<File> filelist = new ArrayList<>();

    JVMLanguages(int id, String nameOfLanguage, String extOfLanguage, String compilerOfLanguage, String resultOfLanguage) {
        this.id = id;
        this.name = nameOfLanguage;
        this.ext = extOfLanguage;
        this.compiler = compilerOfLanguage;
        this.result = resultOfLanguage;
    }
    
    public static JVMLanguages valueOf(int val) {
        return JVMLanguages.stream().filter(v -> v.getId() == val).findFirst().get();
    }
	
    public static Stream<JVMLanguages> stream() {
        return Stream.of(JVMLanguages.values()); 
    }
}
