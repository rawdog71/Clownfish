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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    final transient Logger logger = LoggerFactory.getLogger(ReefBean.class);
    
    @PostConstruct
    public void init() {
        cfclassService.evictAll();
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
            long checksum = getCRC32Checksum(reefcheck(reef).getBytes());
            reef.setChecksum(String.valueOf(checksum));
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
            logger.error(ex.getMessage());
        }
        
    }
    
    public void handleFileUpload(FileUploadEvent event) throws IOException {
        //String filename = event.getFile().getFileName().toLowerCase();
        //logger.info("FILE: " + filename);
        InputStream inputStream;
        inputStream = event.getFile().getInputstream();
        StringBuilder json = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
        }
        //logger.info("UPLOAD: " + json);
        Gson gson = new Gson();
        Reef newreef = gson.fromJson(json.toString(), Reef.class);
        long checksum = getCRC32Checksum(reefcheck(newreef).getBytes());
        if (0 == newreef.getChecksum().compareToIgnoreCase(String.valueOf(checksum))) {
            logger.info("CHECKSUM OK");
            importReef(newreef);
        } else {
            logger.info("CHECKSUM NOT OK");
        }
        logger.info(newreef.getName());
    }
    
    private String reefcheck(Reef chkreef) {
        String reefchk = "";
        reefchk += chkreef.getName();
        for (CfClass cfclass : chkreef.getClasslist()) {
            reefchk += cfclass.getName();
        }
        for (CfAttribut cfattribut : chkreef.getAttributlist()) {
            reefchk += cfattribut.getName()+cfattribut.getAttributetype().getName();
        }
        for (CfTemplate cftemplate : chkreef.getTemplatelist()) {
            reefchk += cftemplate.getName()+cftemplate.getContent()+cftemplate.getScriptLanguageTxt();
        }
        for (CfJavascript cfjavascript : chkreef.getJavascriptlist()) {
            reefchk += cfjavascript.getName()+cfjavascript.getContent();
        }
        for (CfStylesheet cfstylesheet : chkreef.getStylesheetlist()) {
            reefchk += cfstylesheet.getName()+cfstylesheet.getContent();
        }
        return reefchk;
    }
    
    private static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
    
    private void importReef(Reef importreef) {
        for (CfClass cfclass : importreef.getClasslist()) {
            try {
                CfClass checkclass = cfclassService.findByName(cfclass.getName());
                logger.error("CLASS {} already exists.", cfclass.getName());
            } catch (NoResultException ex) {
                //long maxid = cfclassService.findMaxID();
                long oldid = cfclass.getId();
                cfclass.setId(null);
                cfclassService.create(cfclass);
                reorgAttributs(importreef, oldid, cfclass);
            }
        }
        
        for (CfTemplate cftemplate : importreef.getTemplatelist()) {
            try {
                CfTemplate checktemplate = cftemplateService.findByName(cftemplate.getName());
                logger.error("TEMPLATE {} already exists.", cftemplate.getName());
            } catch (NoResultException ex) {
                //long maxid = cftemplateService.findMaxID();
                cftemplate.setId(null);
                cftemplateService.create(cftemplate);
            }
        }
        for (CfJavascript cfjavascript : importreef.getJavascriptlist()) {
            try {
                CfJavascript checkjavascript = cfjavascriptService.findByName(cfjavascript.getName());
                logger.error("JAVASCRIPT {} already exists.", cfjavascript.getName());
            } catch (NoResultException ex) {
                //long maxid = cfjavascriptService.findMaxID();
                cfjavascript.setId(null);
                cfjavascriptService.create(cfjavascript);
            }
        }
        for (CfStylesheet cfstylesheet : importreef.getStylesheetlist()) {
            try {
                CfStylesheet checkstylesheet = cfstylesheetService.findByName(cfstylesheet.getName());
                logger.error("STYLESHEET {} already exists.", cfstylesheet.getName());
            } catch (NoResultException ex) {
                //long maxid = cfstylesheetService.findMaxID();
                cfstylesheet.setId(null);
                cfstylesheetService.create(cfstylesheet);
            }
        }
    }

    private void reorgAttributs(Reef importreef, Long oldid, CfClass newclass) {
        for (CfAttribut cfattribut : importreef.getAttributlist()) {
            if (cfattribut.getClassref().getId() == oldid) {
                cfattribut.setClassref(newclass);
                cfattribut.setId(null);
                cfattributService.create(cfattribut);
            }
        }
    }
}
