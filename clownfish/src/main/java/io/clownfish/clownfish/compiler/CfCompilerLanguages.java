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

import io.clownfish.clownfish.utils.ClownfishUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author raine
 */
public class CfCompilerLanguages {
    private @Getter @Setter HashMap<JVMLanguages, Boolean> jvm_languages = null;
    
    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CfCompilerLanguages.class);

    public CfCompilerLanguages() {
        jvm_languages = new HashMap<>();
        checkLanguages();
    }
    
    public void clearFilelist() {
        if (!jvm_languages.isEmpty()) {
            JVMLanguages[] languages = JVMLanguages.values();
            for (JVMLanguages language : languages) {
                language.getFilelist().clear();
            }
        }
    }
    
    private void checkLanguages() {
        if (jvm_languages.isEmpty()) {
            JVMLanguages[] languages = JVMLanguages.values();
            ProcessBuilder builder = new ProcessBuilder();
            for (JVMLanguages language : languages)
            {
                try {
                    if (ClownfishUtil.isWindows()) {
                        builder.command("cmd.exe", "/c", language.getCompiler() + " -version");
                    } else {
                        builder.command("sh", "-c", language.getCompiler() + " -version");
                    }
                    builder.redirectErrorStream(true);
                    String result = IOUtils.toString(builder.start().getInputStream(), StandardCharsets.UTF_8);

                    if (result.contains(language.getResult())) {
                        jvm_languages.put(language, Boolean.TRUE);
                        LOGGER.info("JVM Compiler " + language.name() + " -> TRUE");
                    } else {
                        LOGGER.info("JVM Compiler " + language.name() + " -> FALSE");
                        jvm_languages.put(language, Boolean.FALSE);
                    }
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
    }
}
