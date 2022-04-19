/*
 * Copyright 2019 sulzbachr.
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
package io.clownfish.clownfish.utils;

import com.google.gson.Gson;
import io.clownfish.clownfish.Main;
import io.clownfish.clownfish.beans.JavaList;
import io.clownfish.clownfish.beans.JavascriptList;
import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.compiler.JVMLanguages;
import io.clownfish.clownfish.datamodels.RestContentParameter;
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.serviceinterface.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import j2html.Config;
import static j2html.TagCreator.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.language;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author sulzbachr
 */
@Scope("singleton")
@Component
public class ClassUtil implements Serializable {
    @Autowired CfAttributService cfattributService;
    @Autowired CfAttributetypeService cfattributetypeService;
    @Autowired CfAttributcontentService cfattributcontentService;
    @Autowired CfClassService cfclassService;
    @Autowired CfListService cflistService;
    @Autowired CfAssetlistService cfassetlistService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    @Autowired CfAssetService cfassetService;
    @Autowired CfJavaService cfjavaService;
    @Autowired CfJavaversionService cfjavaversionService;
    @Autowired CfTemplateService cfTemplateService;
    @Autowired CfSiteService cfSiteService;
    @Autowired CfJavascriptService cfJavaScriptService;
    @Autowired JavaUtil javaUtility;
    @Autowired MarkdownUtil markdownUtil;
    @Autowired FolderUtil folderUtil;
    @Autowired JavaList javalist;
    private @Getter @Setter SiteTreeBean sitetree;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);
    
    public ClassUtil() {
    }
    
    public Map getattributmap (CfClasscontent classcontent) {
        List<CfAttributcontent> attributcontentlist = new ArrayList<>();
        attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
        
        Map attributcontentmap = new LinkedHashMap();

        for (CfAttributcontent attributcontent : attributcontentlist) {
            CfAttribut cfattribut = cfattributService.findById(attributcontent.getAttributref().getId());
            CfAttributetype cfattributtype = cfattributetypeService.findById(cfattribut.getAttributetype().getId());
            switch (cfattributtype.getName()) {
                case "boolean":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentBoolean());
                    break;
                case "string":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentString());
                    break;
                case "hashstring":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentString());
                    break;    
                case "integer":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentInteger());
                    break;
                case "real":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentReal());
                    break;
                case "htmltext":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentText());
                    break;
                case "datetime":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentDate());
                    break;
                case "media":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentInteger());
                    break;
                case "text":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentText());
                    break;    
                case "markdown":
                    markdownUtil.initOptions();
                    if (null != attributcontent.getContentText()) {
                        attributcontentmap.put(cfattribut.getName(), markdownUtil.parseMarkdown(attributcontent.getContentText(), markdownUtil.getMarkdownOptions()));
                    } else {
                        attributcontentmap.put(cfattribut.getName(), markdownUtil.parseMarkdown("", markdownUtil.getMarkdownOptions()));
                    }
                    break;
                case "classref":
                    if (null != attributcontent.getClasscontentlistref()) {
                        Map listcontentmap = new LinkedHashMap();
                        List<CfListcontent> selectedcontent = cflistcontentService.findByListref(attributcontent.getClasscontentlistref().getId());
                        List<CfClasscontent> selectedListcontent = new ArrayList<>();
                        selectedListcontent.clear();
                        if (!selectedcontent.isEmpty()) {
                            selectedcontent.stream().map((listcontent) -> cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref())).forEach((selectedContent) -> {
                                if (null != selectedContent) {
                                    selectedListcontent.add(selectedContent);
                                }
                            });
                        }
                        selectedListcontent.stream().forEach((cc) -> {
                            Map dummy_attributcontentmap = new LinkedHashMap();
                            dummy_attributcontentmap = getattributmap(cc);
                            listcontentmap.put(cc.getName(), dummy_attributcontentmap);
                        });
                        attributcontentmap.put(attributcontent.getAttributref().getName(), listcontentmap);
                    }
                    break;
                case "assetref":
                    if (null != attributcontent.getAssetcontentlistref()) {
                        Map assetlistcontentmap = new LinkedHashMap();
                        List<CfAssetlistcontent> selectedlistassets = cfassetlistcontentService.findByAssetlistref(attributcontent.getAssetcontentlistref().getId());
                        List<CfAsset> selectedAssets = new ArrayList<>();
                        selectedAssets.clear();
                        if (!selectedlistassets.isEmpty()) {
                            selectedlistassets.stream().map((listcontent) -> cfassetService.findById(listcontent.getCfAssetlistcontentPK().getAssetref())).forEach((selectedContent) -> {
                                if (null != selectedContent) {
                                    selectedAssets.add(selectedContent);
                                }
                            });
                        }                        
                        selectedAssets.stream().forEach((asset) -> {
                            assetlistcontentmap.put(asset.getName(), asset);
                        });
                        attributcontentmap.put(attributcontent.getAttributref().getName(), assetlistcontentmap);
                    }
                    break;    
            }
        }
        /* add keywords  */
        List<CfClasscontentkeyword> contentkeywordlist;
        contentkeywordlist = cfclasscontentkeywordService.findByClassContentRef(classcontent.getId());
        if (!contentkeywordlist.isEmpty()) {
            ArrayList listcontentmap = new ArrayList();
            contentkeywordlist.stream().forEach((contentkeyword) -> {
                listcontentmap.add(cfkeywordService.findById(contentkeyword.getCfClasscontentkeywordPK().getKeywordref()));
            });
            attributcontentmap.put("keywords", listcontentmap);
        }
        
        return attributcontentmap;
    }
    
    public String jsonExport(CfClasscontent classcontent, List<CfAttributcontent> attributcontentlist) {
        RestContentParameter contentparameter = new RestContentParameter();
        contentparameter.setClassname(classcontent.getClassref().getName());
        contentparameter.setContentname(classcontent.getName());
        for (CfAttributcontent attributcontent : attributcontentlist) {
            switch (attributcontent.getAttributref().getAttributetype().getName()) {
                case "boolean":
                    if (null != attributcontent.getContentBoolean()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentBoolean().toString());
                    }
                    break;
                case "string":
                    if (null != attributcontent.getContentString()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentString());
                    }
                    break;
                case "hashstring":
                    if (null != attributcontent.getContentString()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentString());
                    }
                    break;    
                case "integer":
                    if (null != attributcontent.getContentInteger()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentInteger().toString());
                    }
                    break;
                case "real":
                    if (null != attributcontent.getContentReal()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentReal().toString());
                    }
                    break;
                case "htmltext":
                    if (null != attributcontent.getContentText()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    }
                    break;    
                case "text":
                    if (null != attributcontent.getContentText()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    }
                    break;
                case "markdown":
                    if (null != attributcontent.getContentText()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    }
                    break;    
                case "datetime":
                    if (null != attributcontent.getContentDate()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentDate().toString());
                    }
                    break;
                case "media":
                    if (null != attributcontent.getContentInteger()) {
                        CfAsset asset = cfassetService.findById(attributcontent.getContentInteger().longValue());
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), asset.getName());
                    }
                    break;
                case "classref":
                    if (null != attributcontent.getClasscontentlistref()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getClasscontentlistref().getName());
                    }
                    break;
                case "assetref":
                    if (null != attributcontent.getAssetcontentlistref()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getAssetcontentlistref().getName());
                    }
                    break;    
            }
        }
        Gson gson = new Gson();
        return gson.toJson(contentparameter);
    }
    
    public List<CfAttributcontent> jsonImport(String jsoncontent) {
        ArrayList<CfAttributcontent> attributcontentlist = new ArrayList<>();
        Gson gson = new Gson();
        RestContentParameter contentparameter = gson.fromJson(jsoncontent, RestContentParameter.class);
        CfClass clazz = cfclassService.findByName(contentparameter.getClassname());
        List<CfAttribut> attributlist = cfattributService.findByClassref(clazz);
        for (String key : contentparameter.getAttributmap().keySet()) {
            CfAttribut attribut = getAttributFromAttributlist(attributlist, key);
            CfAttributcontent attributcontent = new CfAttributcontent();
            attributcontent.setAttributref(attribut);
            switch (attribut.getAttributetype().getName()) {
                case "boolean":
                    attributcontent.setContentBoolean(true);
                    break;
                case "string":
                case "hashstring":    
                    attributcontent.setContentString(contentparameter.getAttributmap().get(key));
                    break;
                case "integer":
                    attributcontent.setContentInteger(BigInteger.valueOf(Long.parseLong(contentparameter.getAttributmap().get(key))));
                    break;
                case "real":
                    attributcontent.setContentReal(Double.parseDouble(contentparameter.getAttributmap().get(key)));
                    break;
                case "htmltext":
                case "text":
                case "markdown":
                    attributcontent.setContentText(contentparameter.getAttributmap().get(key));
                    break;
                case "datetime":
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.GERMAN);
                    Date date;
                    try {
                        date = formatter.parse(contentparameter.getAttributmap().get(key));
                        attributcontent.setContentDate(date);
                    } catch (ParseException ex) {
                        java.util.logging.Logger.getLogger(ClassUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case "media":
                    CfAsset asset = cfassetService.findByName(contentparameter.getAttributmap().get(key));
                    attributcontent.setContentInteger(BigInteger.valueOf(asset.getId()));
                    break;
                case "classref":
                    try {
                        CfList list = cflistService.findByName(contentparameter.getAttributmap().get(key));
                        attributcontent.setClasscontentlistref(list);
                    } catch (Exception ex) {
                        attributcontent.setClasscontentlistref(null);
                    }
                    break;
                case "assetref":
                    try {
                        CfAssetlist assetlist = cfassetlistService.findByName(contentparameter.getAttributmap().get(key));
                        attributcontent.setAssetcontentlistref(assetlist);
                    } catch (Exception ex) {
                        attributcontent.setAssetcontentlistref(null);
                    }
                    break;
            }
            attributcontentlist.add(attributcontent);
        }
        return attributcontentlist;
    }
    
    public void generateJVMClass(CfClass clazz, JVMLanguages language) {
        List<CfAttribut> attributllist =  cfattributService.findByClassref(clazz);
        String output = "";
        StringBuilder sb = new StringBuilder();
        switch (language) {
            case JAVA:
                sb.append("package io.clownfish.java;\n\n");
                sb.append("import java.util.Date;\n");
                sb.append("import java.util.Map;\n");
                sb.append("import java.util.HashMap;\n\n");
                sb.append("public class ").append(clazz.getName()).append("Class {\n");
                sb.append("\tprivate HashMap<String, Object> ").append(clazz.getName().toLowerCase()).append(";\n\n");
                
                sb.append("\tpublic void set").append(clazz.getName()).append("(Map<String, Object> ").append(clazz.getName().toLowerCase()).append(") {\n");

                sb.append("\t\tthis.").append(clazz.getName().toLowerCase()).append(" = new HashMap<String, Object>(").append(clazz.getName().toLowerCase()).append(");\n");
                
                sb.append("\t}\n\n");
                for (CfAttribut attribut : attributllist) {
                    String type = getAttributeJVMType(attribut, language);
                    sb.append("\tpublic ").append(type).append(" get").append(attribut.getName().toUpperCase().charAt(0)).append(attribut.getName().substring(1)).append("() {\n");
                    sb.append("\t\treturn (").append(type).append(") ").append(clazz.getName().toLowerCase()).append(".get(\"").append(attribut.getName()).append("\");\n");
                    sb.append("\t}\n\n");
                    /*
                    sb.append("\tpublic void set").append(attribut.getName().toUpperCase().charAt(0)).append(attribut.getName().substring(1)).append("(").append(type).append(" ").append(attribut.getName()).append(") {\n");
                    sb.append("\t\tthis.").append(clazz.getName().toLowerCase()).append(".put(\"").append(attribut.getName()).append("\", ").append(attribut.getName()).append(");\n");
                    sb.append("\t}\n\n");
                    */
                }
                sb.append("}\n");
                
                try {
                    CfJava java = cfjavaService.findByName(clazz.getName()+"Class");
                    try {
                        long maxversion = cfjavaversionService.findMaxVersion(java.getId());
                        javaUtility.setCurrentVersion(maxversion + 1);
                        byte[] joutput = CompressionUtils.compress(sb.toString().getBytes(StandardCharsets.UTF_8));

                        javalist.writeVersion(java.getId(), javaUtility.getCurrentVersion(), joutput);
                        java.setContent(sb.toString());
                        cfjavaService.edit(java);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                } catch (javax.persistence.NoResultException nrex) {
                    try {
                        CfJava newjava = new CfJava();
                        newjava.setName(clazz.getName()+"Class");
                        newjava.setLanguage(language.getId());
                        newjava.setContent(sb.toString());
                        cfjavaService.create(newjava);

                        byte[] joutput = CompressionUtils.compress(sb.toString().getBytes(StandardCharsets.UTF_8));
                        javalist.writeVersion(newjava.getId(), 1, joutput);
                        javaUtility.setCurrentVersion(1);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
                
                break;
            case KOTLIN:
                sb.append("package io.clownfish.kotlin;\n\n");
                sb.append("import java.util.Date;\n");
                sb.append("import kotlin.collections.Map;\n");
                sb.append("import kotlin.collections.HashMap;\n\n");
                sb.append("public class ").append(clazz.getName()).append("ClassKotlin {\n");
                sb.append("\tvar ").append(clazz.getName().toLowerCase()).append(" : Map<String, Any>? = null\n");
                sb.append("\t\tset(value) {\n");
                sb.append("\t\t\tfield = HashMap<String, Any>(value)\n");
                sb.append("\t\t}\n\n");
                for (CfAttribut attribut : attributllist) {
                    String type = getAttributeJVMType(attribut, language);
                    sb.append("\tfun get").append(attribut.getName().toUpperCase().charAt(0)).append(attribut.getName().substring(1)).append("() : ").append(type).append(" {\n");
                    sb.append("\t\treturn this.").append(clazz.getName().toLowerCase()).append("!!.get(\"").append(attribut.getName().toLowerCase()).append("\") as ").append(type).append("\n");
                    sb.append("\t}\n\n");
                }
                sb.append("}\n");
                
                try {
                    CfJava java = cfjavaService.findByName(clazz.getName()+"ClassKotlin");
                    try {
                        long maxversion = cfjavaversionService.findMaxVersion(java.getId());
                        javaUtility.setCurrentVersion(maxversion + 1);
                        byte[] joutput = CompressionUtils.compress(sb.toString().getBytes(StandardCharsets.UTF_8));

                        javalist.writeVersion(java.getId(), javaUtility.getCurrentVersion(), joutput);
                        java.setContent(sb.toString());
                        cfjavaService.edit(java);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                } catch (javax.persistence.NoResultException nrex) {
                    try {
                        CfJava newjava = new CfJava();
                        newjava.setName(clazz.getName()+"ClassKotlin");
                        newjava.setLanguage(language.getId());
                        newjava.setContent(sb.toString());
                        cfjavaService.create(newjava);

                        byte[] joutput = CompressionUtils.compress(sb.toString().getBytes(StandardCharsets.UTF_8));
                        javalist.writeVersion(newjava.getId(), 1, joutput);
                        javaUtility.setCurrentVersion(1);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
                
                break;
            case GROOVY:
                sb.append("package io.clownfish.groovy;\n\n");
                sb.append("import java.util.Date;\n");
                sb.append("import java.util.Map;\n");
                sb.append("import java.util.HashMap;\n\n");
                sb.append("public class ").append(clazz.getName()).append("ClassGroovy {\n");
                sb.append("\tprivate HashMap<String, Object> ").append(clazz.getName().toLowerCase()).append(";\n\n");
                
                sb.append("\tdef void set").append(clazz.getName()).append("(Map<String, Object> ").append(clazz.getName().toLowerCase()).append(") {\n");

                sb.append("\t\tthis.").append(clazz.getName().toLowerCase()).append(" = new HashMap<String, Object>(").append(clazz.getName().toLowerCase()).append(");\n");
                
                sb.append("\t}\n\n");
                for (CfAttribut attribut : attributllist) {
                    String type = getAttributeJVMType(attribut, language);
                    sb.append("\tdef ").append(type).append(" get").append(attribut.getName().toUpperCase().charAt(0)).append(attribut.getName().substring(1)).append("() {\n");
                    sb.append("\t\treturn (").append(type).append(") ").append(clazz.getName().toLowerCase()).append(".get(\"").append(attribut.getName()).append("\");\n");
                    sb.append("\t}\n\n");
                    /*
                    sb.append("\tpublic void set").append(attribut.getName().toUpperCase().charAt(0)).append(attribut.getName().substring(1)).append("(").append(type).append(" ").append(attribut.getName()).append(") {\n");
                    sb.append("\t\tthis.").append(clazz.getName().toLowerCase()).append(".put(\"").append(attribut.getName()).append("\", ").append(attribut.getName()).append(");\n");
                    sb.append("\t}\n\n");
                    */
                }
                sb.append("}\n");
                
                try {
                    CfJava java = cfjavaService.findByName(clazz.getName()+"ClassGroovy");
                    try {
                        long maxversion = cfjavaversionService.findMaxVersion(java.getId());
                        javaUtility.setCurrentVersion(maxversion + 1);
                        byte[] joutput = CompressionUtils.compress(sb.toString().getBytes(StandardCharsets.UTF_8));

                        javalist.writeVersion(java.getId(), javaUtility.getCurrentVersion(), joutput);
                        java.setContent(sb.toString());
                        cfjavaService.edit(java);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                } catch (javax.persistence.NoResultException nrex) {
                    try {
                        CfJava newjava = new CfJava();
                        newjava.setName(clazz.getName()+"ClassGroovy");
                        newjava.setLanguage(language.getId());
                        newjava.setContent(sb.toString());
                        cfjavaService.create(newjava);

                        byte[] joutput = CompressionUtils.compress(sb.toString().getBytes(StandardCharsets.UTF_8));
                        javalist.writeVersion(newjava.getId(), 1, joutput);
                        javaUtility.setCurrentVersion(1);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
                
                break;
            case SCALA:
                sb.append("package io.clownfish.scala\n\n");
                sb.append("import java.util.Date\n");
                sb.append("import java.util.Map\n");
                sb.append("import java.util.HashMap\n\n");
                sb.append("class ").append(clazz.getName()).append("ClassScala(var ").append(clazz.getName().toLowerCase()).append(": HashMap[String, Object])\n");
                sb.append("{\n");
                sb.append("\tdef this() = this(").append(clazz.getName().toLowerCase()).append(" = new HashMap[String, Object]())\n\n");
                sb.append("\tdef set").append(clazz.getName()).append("(new").append(clazz.getName().toLowerCase()).append(": Map[String, Object]) = { ").append(clazz.getName().toLowerCase()).append(" = new HashMap[String, Object](new").append(clazz.getName().toLowerCase()).append(") }\n\n");
                for (CfAttribut attribut : attributllist) {
                    String type = getAttributeJVMType(attribut, language);
                    sb.append("\tdef get").append(attribut.getName().toUpperCase().charAt(0)).append(attribut.getName().substring(1)).append("(): ").append(type).append(" = ").append(clazz.getName().toLowerCase()).append(".get(\"").append(attribut.getName()).append("\").asInstanceOf[").append(type).append("]\n\n");
                }
                sb.append("}\n");
                
                try {
                    CfJava java = cfjavaService.findByName(clazz.getName()+"ClassScala");
                    try {
                        long maxversion = cfjavaversionService.findMaxVersion(java.getId());
                        javaUtility.setCurrentVersion(maxversion + 1);
                        byte[] joutput = CompressionUtils.compress(sb.toString().getBytes(StandardCharsets.UTF_8));

                        javalist.writeVersion(java.getId(), javaUtility.getCurrentVersion(), joutput);
                        java.setContent(sb.toString());
                        cfjavaService.edit(java);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                } catch (javax.persistence.NoResultException nrex) {
                    try {
                        CfJava newjava = new CfJava();
                        newjava.setName(clazz.getName()+"ClassScala");
                        newjava.setLanguage(language.getId());
                        newjava.setContent(sb.toString());
                        cfjavaService.create(newjava);

                        byte[] joutput = CompressionUtils.compress(sb.toString().getBytes(StandardCharsets.UTF_8));
                        javalist.writeVersion(newjava.getId(), 1, joutput);
                        javaUtility.setCurrentVersion(1);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
                
                break;
        }
    }

    public void generateHTMLForm(CfClass clazz) {
        List<CfAttribut> attributList = cfattributService.findByClassref(clazz);
        StringBuilder html = new StringBuilder();
        CfTemplate template = new CfTemplate();
        CfSite site = new CfSite();
        CfJavascript js = new CfJavascript();

        html.append("<!DOCTYPE html>").append("\n");
        html.append("<html lang=\"en\">").append("\n\n");
        html.append(head(
                meta().attr("charset", "UTF-8"),
                meta().attr("http-equiv", "X-UA-Compatible").attr("content", "IE=edge"),
                meta().attr("name", "viewport").attr("content", "width=device-width, initial-scale=1.0"),
                script().withSrc("/resources/js/axios.js"),
                script().withSrc("/resources/js/User_Webform.js"),
                title("Document")).renderFormatted()).append("\n");

        html.append("<body>").append("\n");
        html.append("\t").append(h1(clazz.getName()).withId("classname")).append("\n");

        html.append("\t").append(label("Content name").withFor("contentname")).append("\n");
        html.append("\t").append(input().withId("contentname").withType("text")).append(br()).append("\n");
        html.append("\t").append(("<form action=\"\" id=\"forms\">")).append("\n");

        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "boolean":
                    html.append("\t\t").append(label(attr.getName()).withFor(attr.getName())).append("\n");
                    html.append("\t\t").append(input().withType("checkbox").withId(attr.getName())).append(br()).append("\n");
                    break;
                case "string":
                case "htmltext":
                case "markdown":
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName())).append("\n");
                    html.append("\t\t").append(input().withType("text").withId(attr.getName())).append(br()).append("\n");
                    break;
                case "hashstring":
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName())).append("\n");
                    html.append("\t\t").append(input().withType("password").withId(attr.getName())).append(br()).append("\n");
                    break;
                case "integer":
                case "real":
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName())).append("\n");
                    html.append("\t\t").append(input().withType("number").withId(attr.getName())).append(br());
                    break;
                case "assetref":
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName())).append(br()).append("\n");
                    html.append("\t\t").append(input().withType("number").withId(attr.getName()).withMax(String.valueOf(
                            cfassetlistService.findAll().stream().max(Comparator.comparing(CfAssetlist::getId)).get().getId()))).append(br()).append("\n");
                case "media":
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName())).append("\n");
                    html.append("\t\t").append(input().withType("number").withId(attr.getName()).withMax(String.valueOf(
                            cfassetService.findAll().stream().max(Comparator.comparing(CfAsset::getId)).get().getId()))).append(br()).append("\n");
                    break;
                case "classref":
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName())).append("\n");
                    html.append("\t\t").append(input().withType("number").withId(attr.getName()).withMax(String.valueOf(
                            cfclassService.findAll().stream().max(Comparator.comparing(CfClass::getId)).get().getId()))).append(br()).append("\n");
                    break;
                case "datetime":
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName())).append("\n");
                    html.append("\t\t").append(input().withType("date").withId(attr.getName()).withMax(String.valueOf(
                            cfclassService.findAll().stream().max(Comparator.comparing(CfClass::getId)).get().getId()))).append(br()).append("\n");
                    break;
            }
        }

        html.append("\t</form>").append("\n");
        html.append("\t").append(button("Add").attr("onclick", "add()")).append("\n");
        html.append("\t").append(button("Update").attr("onclick", "update()")).append("\n");
        html.append("\t").append(button("Delete").attr("onclick", "deleteI()")).append("\n");
        html.append("</body>").append("\n");
        html.append("</html>");

        template.setName(clazz.getName() + "_Webform");
        template.setScriptlanguage(2);
        template.setCheckedoutby(BigInteger.ZERO);
        template.setContent(html.toString());
        //if (cfTemplateService.findByName(template.getName()) == null) {
            cfTemplateService.create(template);
//        } else {
//            FacesMessage message = new FacesMessage("Template \"" + template.getName() + "\" already exists! Aborting...");
//            FacesContext.getCurrentInstance().addMessage(null, message);
//            return;
//        }

        js.setContent(
                "//Add content \n" +
                "function add() {\n" +
                "\tvar attributmap = {\n" +
                "\t\tapikey: \"+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=\",\n" +
                "\t\tclassname: document.getElementById('classname').innerText,\n" +
                "\t\tattributmap: getInputInformation(),\n" +
                "\t\tcontentname: document.getElementById('contentname').value,\n" +
                "\t};\n" +
                "\n" +
                "\taxios.post('http://localhost:9000/insertcontent',\n" +
                "\t\t\tattributmap\n" +
                "\t\t)\n" +
                "\t\t.then(function(response) {\n" +
                "\t\t\tconsole.log(response);\n" +
                "\t\t})\n" +
                "\t\t.catch(function(error) {\n" +
                "\t\t\tconsole.log(error);\n" +
                "\t\t});\n" +
                "\n" +
                "\tconsole.log(\"Add\");\n" +
                "}\n" +
                "\n" +
                "//Update Content\n" +
                "function update() {\n" +
                "\tvar attributmap = {\n" +
                "\t\tapikey: \"+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=\",\n" +
                "\t\tclassname: document.getElementById('classname').innerText,\n" +
                "\t\tcontentname: document.getElementById('contentname').value,\n" +
                "\t\tattributmap: getInputInformation(),\n" +
                "\t};\n" +
                "\n" +
                "\taxios.post('http://localhost:9000/updatecontent',\n" +
                "\t\t\tattributmap\n" +
                "\t\t)\n" +
                "\t\t.then(function(response) {\n" +
                "\t\t\tconsole.log(response);\n" +
                "\t\t})\n" +
                "\t\t.catch(function(error) {\n" +
                "\t\t\tconsole.log(error);\n" +
                "\t\t});\n" +
                "\n" +
                "\tconsole.log(\"Update\");\n" +
                "}\n" +
                "\n" +
                "//Delete content\n" +
                "function deleteI() {\n" +
                "\t// \n" +
                "\tvar attributmap = {\n" +
                "\t\tapikey: \"+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=\",\n" +
                "\t\tclassname: document.getElementById('classname').innerText,\n" +
                "\t\tcontentname: document.getElementById('contentname').value,\n" +
                "\t};\n" +
                "\n" +
                "\taxios.post('http://localhost:9000/deletecontent',\n" +
                "\t\t\tattributmap\n" +
                "\t\t)\n" +
                "\t\t.then(function(response) {\n" +
                "\t\t\tconsole.log(response);\n" +
                "\t\t})\n" +
                "\t\t.catch(function(error) {\n" +
                "\t\t\tconsole.log(error);\n" +
                "\t\t});\n" +
                "\n" +
                "\tconsole.log(\"Delete\");\n" +
                "}\n" +
                "\n" +
                "//Give a List of the whole content\n" +
                "function read() {\n" +
                "\n" +
                "}\n" +
                "\n" +
                "function getInputInformation() {\n" +
                "        var formEl = document.forms.tester;\n" +
                "        var kvpairs = [];\n" +
                "        var form = document.forms.forms;\n" +
                "\n" +
                "        for (var i = 0; i < form.elements.length; i++) {\n" +
                "            console.log(form.elements)\n" +
                "            var e = form.elements[i];\n" +
                "            var x = {};\n" +
                "            if (e.type == \"checkbox\") {\n" +
                "                x[e.id] = e.checked;\n" +
                "            } else if (e.type == \"date\") {\n" +
                "                var today = new Date();\n" +
                "                var time = today.getHours() + \":\" + today.getMinutes() < 10 ? 0 + today.getMinutes().toString(): today.getMinutes()  + \":\" + today.getSeconds() < 10 ? 0 + today.getSeconds().toString(): today.getSeconds();\n" +
                "\n" +
                "                x[e.id] = e.value + \" \" + time;\n" +
                "            } else {\n" +
                "                x[e.id] = e.value;\n" +
                "            }\n" +
                "\n" +
                "            kvpairs.push(x);\n" +
                "        }\n" +
                "        var attributemap = Object.assign({}, ...kvpairs);\n" +
                "\n" +
                "        console.log(attributemap);\n" +
                "        return attributemap;\n" +
                "}\n" +
                "\n" +
                "read();");
        js.setName(clazz.getName() + "_Webform");
        js.setCheckedoutby(BigInteger.ZERO);
        //if (cfJavaScriptService.findByName(js.getName()) == null) {
        cfJavaScriptService.create(js);

        FileOutputStream fileStream = null;
        try {
            fileStream = new FileOutputStream(new File(folderUtil.getJs_folder()+ File.separator + js.getName() + ".js"));
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            try {
                writer.write(js.getContent());
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create the destination file", e);
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            try {
                if (null != fileStream) {
                    fileStream.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
//        } else {
//            FacesMessage message = new FacesMessage("JavaScript \"" + js.getName() + "\" already exists! Aborting...");
//            FacesContext.getCurrentInstance().addMessage(null, message);
//            return;
//        }

        site.setName(clazz.getName() + "_Webform");
        site.setCharacterencoding("UTF-8");
        site.setHitcounter(BigInteger.ZERO);
        site.setTitle("");
        site.setContenttype("text/html");
        site.setSearchrelevant(false);
        site.setHtmlcompression(0);
        site.setGzip(0);
        site.setLocale("");
        site.setDescription("");
        site.setAliaspath(site.getName());
        site.setParentref(BigInteger.ZERO);
        site.setTemplateref(BigInteger.valueOf(template.getId()));
        site.setJavascriptref(BigInteger.valueOf(js.getId()));
        //if (cfSiteService.findByName(site.getName()) == null) {
            cfSiteService.create(site);
            sitetree.loadTree();
//        } else {
//            FacesMessage message = new FacesMessage("Site \"" + site.getName() + "\" already exists! Aborting...");
//            FacesContext.getCurrentInstance().addMessage(null, message);
//        }
    }
    
    private String getAttributeJVMType(CfAttribut attribut, JVMLanguages language) {
        switch (language) {
            case JAVA:
                switch (attribut.getAttributetype().getName()) {
                    case "boolean":
                        return "boolean";
                    case "string":
                    case "htmltext":
                    case "hashstring":
                    case "markdown":
                    case "text":
                        return "String";
                    case "integer":
                    case "media":
                    case "classref":
                    case "assetref":
                        return "long";
                    case "real":
                        return "double";
                    case "datetime":
                        return "Date";
                    default:
                        return "";
                }
            case KOTLIN:
            case GROOVY:
                switch (attribut.getAttributetype().getName()) {
                    case "boolean":
                        return "boolean";
                    case "string":
                    case "htmltext":
                    case "hashstring":
                    case "markdown":
                    case "text":
                        return "String";
                    case "integer":
                    case "media":
                    case "classref":
                    case "assetref":
                        return "Long";
                    case "real":
                        return "Double";
                    case "datetime":
                        return "Date";
                    default:
                        return "";
                }
            case SCALA:
                switch (attribut.getAttributetype().getName()) {
                    case "boolean":
                        return "Boolean";
                    case "string":
                    case "htmltext":
                    case "hashstring":
                    case "markdown":
                    case "text":
                        return "String";
                    case "integer":
                    case "media":
                    case "classref":
                    case "assetref":
                        return "Long";
                    case "real":
                        return "Double";
                    case "datetime":
                        return "Date";
                    default:
                        return "";
                }
            default:
                return "";
        }
    }
    
    public CfAttribut getAttributFromAttributlist(List<CfAttribut> attributlist, String name) {
        for (CfAttribut attribut : attributlist) {
            if (0 == attribut.getName().compareToIgnoreCase(name)) {
                return attribut;
            }
        }
        return null;
    }
}
