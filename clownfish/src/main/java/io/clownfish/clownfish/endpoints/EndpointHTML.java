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
package io.clownfish.clownfish.endpoints;

import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author raine
 */
@RestController
public class EndpointHTML {
    @Autowired CfTemplateService cftemplateService;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(EndpointHTML.class);
    
    @GetMapping(path = {"/{name}.tpl", "/{name}.html"})
    public void universalGetTemplate(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        CfTemplate cftemplate = null;
        try {
            cftemplate = cftemplateService.findByName(name);
            if (2 == cftemplate.getScriptlanguage()) {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                PrintWriter outwriter = response.getWriter();
                outwriter.println(cftemplate.getContent());
            } else {
                LOGGER.warn("ONLY HTML Templates");
            }
        } catch (IOException ex) {
            LOGGER.warn("Template NOT FOUND");
        }
    }
}
