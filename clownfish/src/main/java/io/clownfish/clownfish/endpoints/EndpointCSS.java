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

import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.utils.FolderUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class EndpointCSS {
    @Autowired private FolderUtil folderUtil;
    @Autowired CfStylesheetService cfstylesheetService;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(EndpointCSS.class);
    
    @GetMapping(path = "/{name}.css")
    public void universalGetCSS(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        BufferedReader br = null;
        try {
            response.setContentType("text/css");
            response.setCharacterEncoding("UTF-8");
            br = new BufferedReader(new InputStreamReader(new FileInputStream(folderUtil.getCss_folder() + File.separator + name + ".css"), "UTF-8"));
            StringBuilder sb = new StringBuilder(1024);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            PrintWriter outwriter = response.getWriter();
            outwriter.println(sb);
        } catch (IOException ex) {
            CfStylesheet cfstylesheet = null;
            try {
                cfstylesheet = cfstylesheetService.findByName(name);
                response.setContentType("text/css");
                response.setCharacterEncoding("UTF-8");
                PrintWriter outwriter = response.getWriter();
                outwriter.println(cfstylesheet.getContent());
            } catch (IOException iex) {
                System.out.println("CSS NOT FOUND");
            }
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
}
