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
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.serviceinterface.*;
import io.clownfish.clownfish.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
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
    private @Getter @Setter List<CfJava> javalist;
    
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfTemplateService cftemplateService;
    @Autowired transient CfStylesheetService cfstylesheetService;
    @Autowired transient CfJavascriptService cfjavascriptService;
    @Autowired transient CfJavaService cfjavaService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient ClassList cl;
    @Autowired transient TemplateList tl;
    @Autowired transient JavascriptList jl;
    @Autowired transient StylesheetList sl;
    @Autowired transient JavaList javaList;
    
    @Autowired TemplateUtil templateUtil;
    @Autowired StylesheetUtil stylesheetUtil;
    @Autowired JavascriptUtil javascriptUtil;
    @Autowired JavaUtil javaUtil;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ReefBean.class);
    
    @PostConstruct
    public void init() {
        LOGGER.info("INIT REEF START");
        classlist = cfclassService.findAll();
        templatelist = cftemplateService.findAll();
        stylesheetlist = cfstylesheetService.findAll();
        javascriptlist = cfjavascriptService.findAll();
        javalist = cfjavaService.findAll();
        LOGGER.info("INIT REEF END");
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
            String checksum = getMD5Checksum(reefcheck(reef));
            reef.setChecksum(checksum);
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
                LOGGER.error(ex.getMessage());
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        
    }
    
    public void handleFileUpload(FileUploadEvent event) throws IOException {
        //String filename = event.getFile().getFileName().toLowerCase();
        //LOGGER.info("FILE: " + filename);
        InputStream inputStream;
        inputStream = event.getFile().getInputStream();
        StringBuilder json = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")))) {
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
        }
        //LOGGER.info("UPLOAD: " + json);
        Gson gson = new Gson();
        Reef newreef = gson.fromJson(json.toString(), Reef.class);
        String checksum_comp = newreef.getChecksum();
        String checksum = getMD5Checksum(reefcheck(newreef));
        if (0 == checksum_comp.compareToIgnoreCase(checksum)) {
            LOGGER.info("CHECKSUM OK");
            importReef(newreef);
        } else {
            LOGGER.info("CHECKSUM NOT OK");
        }
        LOGGER.info(newreef.getName());
    }
    
    private String reefcheck(Reef chkreef) {
        String reefchk = "";
        reefchk += chkreef.getName();
        if (null != chkreef.getClasslist()) {
            for (CfClass cfclass : chkreef.getClasslist()) {
                reefchk += cfclass.getName();
            }
        }
        if (null != chkreef.getAttributlist()) {
            for (CfAttribut cfattribut : chkreef.getAttributlist()) {
                reefchk += cfattribut.getName()+cfattribut.getAttributetype().getName();
            }
        }
        if (null != chkreef.getTemplatelist()) {
            for (CfTemplate cftemplate : chkreef.getTemplatelist()) {
                reefchk += cftemplate.getName()+cftemplate.getContent()+cftemplate.getScriptLanguageTxt();
            }
        }
        if (null != chkreef.getJavascriptlist()) {
            for (CfJavascript cfjavascript : chkreef.getJavascriptlist()) {
                reefchk += cfjavascript.getName()+cfjavascript.getContent();
            }
        }
        if (null != chkreef.getJavalist()) {
            for (CfJava cfjava : chkreef.getJavalist()) {
                reefchk += cfjava.getName()+cfjava.getContent();
            }
        }
        if (null != chkreef.getStylesheetlist()) {
            for (CfStylesheet cfstylesheet : chkreef.getStylesheetlist()) {
                reefchk += cfstylesheet.getName()+cfstylesheet.getContent();
            }
        }
        return reefchk;
    }
    
    private static String getMD5Checksum(String value) {
        return DigestUtils.md5Hex(value);
    }

    private void importReef(Reef importreef) {
        if (null != importreef.getClasslist()) {
            for (CfClass cfclass : importreef.getClasslist()) {
                try {
                    CfClass checkclass = cfclassService.findByName(cfclass.getName());
                    LOGGER.error("CLASS {} already exists.", cfclass.getName());
                } catch (NoResultException ex) {
                    if (null != cfclass.getTemplateref()) {     // check if class has preview template
                        try {
                            CfTemplate checktemplate = cftemplateService.findByName(cfclass.getTemplateref().getName());
                            cfclass.setTemplateref(checktemplate);
                        } catch (NoResultException ex2) {
                            // if preview template does not exist, create it
                            CfTemplate newtemplate = new CfTemplate();
                            newtemplate.setCheckedoutby(cfclass.getTemplateref().getCheckedoutby());
                            newtemplate.setContent(cfclass.getTemplateref().getContent());
                            newtemplate.setName(cfclass.getTemplateref().getName());
                            newtemplate.setScriptlanguage(cfclass.getTemplateref().getScriptlanguage());
                            newtemplate.setType(cfclass.getTemplateref().getType());
                            cfclass.setTemplateref(cftemplateService.create(newtemplate));
                        }
                    }
                    cfclass.setId(null);
                    cfclassService.create(cfclass);
                }
            }
            cl.onRefreshAll();
        }
        
        if (null != importreef.getAttributlist()) {
            for (CfAttribut cfattribut : importreef.getAttributlist()) {
                CfClass newclass = cfclassService.findByName(cfattribut.getClassref().getName());
                cfattribut.setClassref(newclass);
                if (null != cfattribut.getRelationref()) {
                    CfClass newrefclass = cfclassService.findByName(cfattribut.getRelationref().getName());
                    cfattribut.setRelationref(newrefclass);
                }
                cfattribut.setId(null);
                cfattributService.create(cfattribut);
            }
            cl.onRefreshAll();
        }
        
        if (null != importreef.getTemplatelist()) {
            for (CfTemplate cftemplate : importreef.getTemplatelist()) {
                try {
                    CfTemplate checktemplate = cftemplateService.findByName(cftemplate.getName());
                    LOGGER.error("TEMPLATE {} already exists.", cftemplate.getName());
                } catch (NoResultException ex) {
                    cftemplate.setId(null);
                    cftemplate = cftemplateService.create(cftemplate);

                    templateUtil.setTemplateContent(cftemplate.getContent());

                    String content = templateUtil.getTemplateContent();
                    byte[] output = null;
                    try {
                        output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException ex1) {
                        LOGGER.error(ex1.getMessage());
                    } catch (IOException ex1) {
                        LOGGER.error(ex1.getMessage());
                    }

                    templateUtil.setCurrentVersion(1);
                    templateUtil.writeVersion(cftemplate.getId(), templateUtil.getCurrentVersion(), output, 0);
                }
            }
            tl.refresh();
        }
        
        if (null != importreef.getJavascriptlist()) {
            for (CfJavascript cfjavascript : importreef.getJavascriptlist()) {
                try {
                    CfJavascript checkjavascript = cfjavascriptService.findByName(cfjavascript.getName());
                    LOGGER.error("JAVASCRIPT {} already exists.", cfjavascript.getName());
                } catch (NoResultException ex) {
                    cfjavascript.setId(null);
                    cfjavascript = cfjavascriptService.create(cfjavascript);

                    javascriptUtil.setJavascriptContent(cfjavascript.getContent());

                    String content = javascriptUtil.getJavascriptContent();
                    byte[] output = null;
                    try {
                        output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException ex1) {
                        LOGGER.error(ex1.getMessage());
                    } catch (IOException ex1) {
                        LOGGER.error(ex1.getMessage());
                    }

                    javascriptUtil.setCurrentVersion(1);
                    javascriptUtil.writeVersion(cfjavascript.getId(), javascriptUtil.getCurrentVersion(), output, 0);
                }
            }
            jl.refresh();
        }

        if (null != importreef.getJavalist()) {
            for (CfJava cfjava : importreef.getJavalist())
            {
                try
                {
                    CfJava checkjava = cfjavaService.findByName(cfjava.getName());
                    LOGGER.error("JAVA {} already exists.", cfjava.getName());
                }
                catch (NoResultException ex)
                {
                    cfjava.setId(null);
                    cfjava = cfjavaService.create(cfjava);

                    javaUtil.setJavaContent(cfjava.getContent());

                    String content = javaUtil.getJavaContent();
                    byte[] output = null;
                    try
                    {
                        output = CompressionUtils.compress(content.getBytes(StandardCharsets.UTF_8));
                    }
                    catch (IOException ex1)
                    {
                        LOGGER.error(ex1.getMessage());
                    }

                    javaUtil.setCurrentVersion(1);
                    javaUtil.writeVersion(cfjava.getId(), javaUtil.getCurrentVersion(), output, 0);
                }
            }
            javaList.refresh();
        }

        if (null != importreef.getStylesheetlist()) {
            for (CfStylesheet cfstylesheet : importreef.getStylesheetlist()) {
                try {
                    CfStylesheet checkstylesheet = cfstylesheetService.findByName(cfstylesheet.getName());
                    LOGGER.error("STYLESHEET {} already exists.", cfstylesheet.getName());
                } catch (NoResultException ex) {
                    cfstylesheet.setId(null);
                    cfstylesheet = cfstylesheetService.create(cfstylesheet);

                    stylesheetUtil.setStyelsheetContent(cfstylesheet.getContent());

                    String content = stylesheetUtil.getStyelsheetContent();
                    byte[] output = null;
                    try {
                        output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException ex1) {
                        LOGGER.error(ex1.getMessage());
                    } catch (IOException ex1) {
                        LOGGER.error(ex1.getMessage());
                    }

                    stylesheetUtil.setCurrentVersion(1);
                    stylesheetUtil.writeVersion(cfstylesheet.getId(), stylesheetUtil.getCurrentVersion(), output, 0);
                }
            }
            sl.refresh();
        }
    }
}
