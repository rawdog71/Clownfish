/*
 * Copyright 2025 SulzbachR.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.clownfish.clownfish.beans.JsonSAPFormParameter;
import io.clownfish.clownfish.jsonator.mapping.JsonMapper;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.utils.ClownfishUtil;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@Scope("request")
@Component
public class JSONatorBean {
    private CfTemplateService cftemplateService;
    
    public void init(CfTemplateService cftemplateService) {
        this.cftemplateService = cftemplateService;
    }
    
    public String mapJSON(String templateName, Map parametermap) throws IOException
    {
        List<JsonSAPFormParameter> postmap = ClownfishUtil.getJsonSAPFormParameterList(parametermap);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode on = objectMapper.createObjectNode();
        for (JsonSAPFormParameter param : postmap) {
            on.put(param.getName(), (String) param.getValue());
        }
        JsonMapper jsonmapper = new JsonMapper(on);
        return jsonmapper.map(cftemplateService.findByName(templateName).getContent());
    }
}
