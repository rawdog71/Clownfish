/*
 * Copyright 2020 SulzbachR.
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
package io.clownfish.clownfish.beans;

import com.google.gson.Gson;
import io.clownfish.clownfish.datamodels.Reef;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author SulzbachR
 */
@Scope("session")
@Named("reefbean")
public class ReefBean implements Serializable {
    @Autowired @Getter @Setter Reef reef;
    
    private @Getter @Setter List<CfClass> classlist;
    private @Getter @Setter List<CfTemplate> templatelist;
    private @Getter @Setter List<CfStylesheet> stylesheetlist;
    private @Getter @Setter List<CfJavascript> javascriptlist;
    
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfTemplateService cftemplateService;
    @Autowired transient CfStylesheetService cfstylesheetService;
    @Autowired transient CfJavascriptService cfjavascriptService;
    @Autowired transient CfAttributService cfattributService;
    
    @PostConstruct
    public void init() {
        classlist = cfclassService.findAll();
        templatelist = cftemplateService.findAll();
        stylesheetlist = cfstylesheetService.findAll();
        javascriptlist = cfjavascriptService.findAll();
    }
    
    public void onExport() {
        try {
            reef.getAttributlist().clear();
            for (CfClass clazz : reef.getClasslist()) {
                List<CfAttribut> attributliste = cfattributService.findByClassref(clazz);
                for (CfAttribut attribut : attributliste) {
                    reef.getAttributlist().add(attribut);
                }
            }
            Gson gson = new Gson();
            String json = gson.toJson(reef);
            System.out.println(json);
            System.out.println("Export Reef");
            OutputStream out = null;
            String filename = reef.getName() + ".json";
            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
            out = response.getOutputStream();
            response.setContentType("application/json");
            response.addHeader("Content-Disposition", "attachment; filename=\""+filename+"\"");
            out.write(json.getBytes(Charset.forName("UTF-8")));
            out.flush();
            try {
                if (null != out) {
                    out.close();
                }
                FacesContext.getCurrentInstance().responseComplete();
            } catch (IOException ex) {
                
            }
        } catch (IOException ex) {
            Logger.getLogger(ReefBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
