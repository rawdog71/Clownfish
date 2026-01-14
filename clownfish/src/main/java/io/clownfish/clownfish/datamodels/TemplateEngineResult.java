/*
 * Copyright 2026 SulzbachR.
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
package io.clownfish.clownfish.datamodels;

import freemarker.template.Configuration;
import java.util.Map;
import lombok.Data;
import org.apache.velocity.VelocityContext;

/**
 *
 * @author SulzbachR
 */
@Data
public class TemplateEngineResult {
    private boolean scripted = false;
    
    // Freemarker spezifisch
    private Map fmRoot;
    private Configuration freemarkerCfg;
    private freemarker.template.Template fmTemplate;
    
    // Velocity spezifisch
    private VelocityContext velContext;
    private org.apache.velocity.Template velTemplate;
}