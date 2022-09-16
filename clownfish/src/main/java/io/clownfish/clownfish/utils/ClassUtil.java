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
import io.clownfish.clownfish.beans.JavaList;
import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.compiler.JVMLanguages;
import io.clownfish.clownfish.datamodels.ClassImport;
import io.clownfish.clownfish.datamodels.FieldImport;
import io.clownfish.clownfish.datamodels.RestContentParameter;
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.serviceinterface.*;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import static j2html.TagCreator.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
    
    public void createClass(ClassImport ci) {
        CfClass newclass = new CfClass();
        newclass.setName(ci.getClassname());
        newclass.setSearchrelevant(ci.isSearchenginerelevant());
        newclass.setMaintenance(ci.isBackendmaintenance());
        newclass.setEncrypted(ci.isEncrypted());
        newclass.setTemplateref(null);
        newclass = cfclassService.create(newclass);
        
        for (FieldImport fi : ci.getFields()) {
            CfAttribut newattribut = new CfAttribut();
            newattribut.setClassref(newclass);
            newattribut.setName(fi.getFieldname());
            newattribut.setIdentity(fi.isFieldisidentity());
            newattribut.setAutoincrementor(fi.isFieldisautoinc());
            newattribut.setIsindex(fi.isFieldisindex());
            newattribut.setAttributetype(cfattributetypeService.findByName(fi.getFieldtype()));
            if (0 == fi.getFieldtype().compareToIgnoreCase("classref")) {
                newattribut.setRelationref(cfclassService.findByName(fi.getClassref()));
                if (0 == fi.getRelationtype().compareToIgnoreCase("1:n")) {
                    newattribut.setRelationtype(1);
                } else {
                    newattribut.setRelationtype(0);
                }
            } else {
                newattribut.setRelationref(null);
            }
            
            cfattributService.create(newattribut);
        }
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
                    if (0 == attributcontent.getAttributref().getRelationtype()) {
                        if (null != attributcontent.getClasscontentlistref()) {
                            contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getClasscontentlistref().getName());
                        }
                    } else {
                        if (null != attributcontent.getContentInteger()) {
                            contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentInteger().toString());
                        }
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
        html.append("<html lang=\"en\" ng-app=\"webformApp\">").append("\n\n");
        html.append(head(
                meta().attr("charset", "UTF-8"),
                meta().attr("http-equiv", "X-UA-Compatible").attr("content", "IE=edge"),
                meta().attr("name", "viewport").attr("content", "width=device-width, initial-scale=1.0"),
                script().withSrc("resources/js/angular.js"),
                link().withHref("resources/css/bootstrap5.css").withRel("stylesheet"),
                script().withSrc("resources/js/bootstrap5.js"),
                script().withSrc("resources/js/User_Webform.js"),
                script().withSrc("resources/js/axios.js"),
                title("Webform")).renderFormatted()).append("\n");

        html.append("<body ng-controller=\"WebformCtrl\">").append("\n");
        html.append("\t").append(h1(clazz.getName()).withId("classname").withClass("text-center mt-3")).append("\n");
        
        html.append("\t").append(("<div class=\"mx-5\">")).append("\n");
        html.append("\t\t").append(("<div class=\"d-flex flex-row-reverse\">")).append("\n");
        html.append("\t\t\t").append(("<button class=\"btn btn-primary\" data-bs-toggle=\"modal\" data-bs-target=\"#exampleModal\"><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-plus-lg\" viewBox=\"0 0 16 16\">\n" +
"                <path fill-rule=\"evenodd\" d=\"M8 2a.5.5 0 0 1 .5.5v5h5a.5.5 0 0 1 0 1h-5v5a.5.5 0 0 1-1 0v-5h-5a.5.5 0 0 1 0-1h5v-5A.5.5 0 0 1 8 2Z\"/>\n" +
"              </svg> Hinzufügen</button>")).append("\n");
        html.append("\t\t").append(("</div>")).append("\n");
        
        html.append("\t").append(("<table class=\"table\">")).append("\n");
        html.append("\t\t").append(("<thead>")).append("\n");
        html.append("\t\t\t").append(("<tr>")).append("\n");
        html.append("\t\t\t\t").append("<th scope=\"col\">#</th>\n");
        html.append("\t\t\t\t").append("<th scope=\"col\">Contentname</th>\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            html.append("\t").append("<th scope=\"col\">").append(attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("</th>\n");
        }
        html.append("\t\t\t\t").append("<th class=\"text-end\" scope=\"col\">Aktionen</th>\n");
        html.append("\t\t\t").append(("</tr>")).append("\n");
        html.append("\t\t").append(("</thead>")).append("\n");
        
        html.append("\t\t").append(("<tbody>")).append("\n");
        html.append("\t\t\t").append(("<tr ng-repeat=\"info in contentList track by $index\">")).append("\n");
        html.append("\t\t\t\t").append("<th scope=\"row\">{{$index}}</th>\n");
        html.append("\t\t\t\t").append("<td> {{info.content.name}} </td>").append("\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            html.append("\t\t\t\t").append("<td> {{info.keyvals[0][\"").append(attr.getName()).append("\"]}}").append("</td>\n");
        }
        
        html.append("\t\t\t\t").append(("<td class=\"text-end\">")).append("\n");
        html.append("\t\t\t\t\t").
                append(("<button class=\"btn btn-primary\" ng-click=\"edit($index)\" data-bs-toggle=\"modal\" data-bs-target=\"#editModal\">\n" +
"                            <div class=\"d-flex align-items-center\">\n" +
"                                <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-pencil-square\" viewBox=\"0 0 16 16\">\n" +
"                                    <path d=\"M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z\"/>\n" +
"                                    <path fill-rule=\"evenodd\" d=\"M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z\"/>\n" +
"                                </svg>\n" +
"                                <p class=\"m-0 ms-1\">Editieren</p>\n" +
"                            </div>\n" +
"                        </button>")).append("\n");
        
        html.append("\t\t\t\t\t").
                append(("<button class=\"btn btn-danger\" ng-click=\"deleteI($index)\">\n" +
"                            <div class=\"d-flex align-items-center\">\n" +
"                                <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-trash\" viewBox=\"0 0 16 16\">\n" +
"                                    <path d=\"M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z\"/>\n" +
"                                    <path fill-rule=\"evenodd\" d=\"M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z\"/>\n" +
"                                </svg>\n" +
"                                <p class=\"m-0 ms-1\">Löschen</p>\n" +
"                            </div>\n" +
"                        </button>")).append("\n");
        html.append("\t\t\t\t").append(("</td>")).append("\n");
        html.append("\t\t\t").append(("</tr>")).append("\n");
        html.append("\t\t").append(("</tbody>")).append("\n");
        html.append("\t").append(("</table>")).append("\n");
        html.append("\t").append(("</div>")).append("\n");
        
        html.append("\t").append(("<div class=\"modal fade\" id=\"exampleModal\" tabindex=\"-1\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">")).append("\n");
        html.append("\t\t").append(("<div class=\"modal-dialog\">")).append("\n");
        html.append("\t\t\t").append(("<div class=\"modal-content\">")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-header\">")).append("\n");
        html.append("\t\t\t\t\t").append(h5(clazz.getName()).withId("exampleModalLabel").withClass("modal-title")).append("\n");
        html.append("\t\t\t\t\t").append(("<button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        
        html.append("\t\t\t\t").append(("<div class=\"modal-body\">")).append("\n");
        html.append("\t\t\t\t\t").append(("<form id=\"forms\" class=\"row g-3\">")).append("\n");
        
        html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-12\">")).append("\n");
        html.append("\t\t\t\t\t\t\t").append(label("Contentname").withFor("contentname").withClass("form-label")).append("\n");
        html.append("\t\t\t\t\t\t\t").append(input().withId("contentname").withType("text").withClass("form-control")).append("\n");
        html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "boolean":
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(attr.getName()).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("checkbox").withId(attr.getName())).append("\n");
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "string":
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(input().withType("text").withId(attr.getName()).withClass("form-control")).append("\n");
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "text":
                case "htmltext":
                case "markdown":
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(textarea().withId(attr.getName()).withClass("form-control")).append("\n");
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "hashstring":
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(input().withType("password").withId(attr.getName()).withClass("form-control")).append("\n");
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "integer":
                case "real":
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(input().withType("number").withId(attr.getName()).withClass("form-control"));
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "assetref":
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append("<select class=\"form-select\" id=\"").append(attr.getName()).append("\">").append("\n");
                    html.append("\t\t\t\t\t\t\t\t").append(("<option ng-repeat=\"name in libNames\" value=\"{{name.id}}\">{{name.name}}</option>")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(("</select>")).append("\n");
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "media":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append("<select class=\"form-select\" id=\"").append(attr.getName()).append("\">").append("\n");
                    html.append("\t\t").append("<option selected value=\"NOVALUE\">Klicken um zu ändern</option>").append("\n");
                    html.append("\t\t\t\t").append(("<option ng-repeat=\"media in mediaList\" value=\"{{media.asset.id}}\">{{media.asset.id}}. {{media.asset.name}}</option>")).append("\n");
                    html.append("\t\t\t\t").append(("</select>")).append("\n");
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "classref":
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append("<select class=\"form-select\" id=\"").append(attr.getName()).append("\">").append("\n");
                    html.append("\t\t\t\t\t\t\t\t").append(("<option ng-repeat=\"name in classNames\" value=\"{{name.id}}\">{{name.name}}</option>")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(("</select>")).append("\n");
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "datetime":
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(input().withType("date").withId(attr.getName()).withValue("{{getTodaysDate()}}").withClass("form-control"));
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
            }
        }
        
        html.append("\t\t\t\t\t").append(("</form>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        
        html.append("\t\t\t\t").append(("<div class=\"modal-footer\">")).append("\n");
        html.append("\t\t\t\t").append(("<button class=\"btn btn-primary w-100\" data-bs-dismiss=\"modal\" ng-click=\"add()\">Hinzufügen</button>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        
        
        html.append("\t\t\t\t").append(("<div class=\"modal fade\" id=\"editModal\" tabindex=\"-1\" aria-labelledby=\"editModalLabel\" aria-hidden=\"true\">")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-dialog\">")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-content\" ng-repeat=\"info in recordEdit\">")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-header\">")).append("\n");
        html.append("\t\t\t\t\t").append(h5(clazz.getName()).withId("exampleModalLabel").withClass("modal-title")).append("\n");
        html.append("\t\t\t\t").append(("<button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-body\">")).append("\n");
        html.append("\t\t\t\t").append(("<form id=\"forms2\" class=\"row g-3\">")).append("\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            
            switch (attr.getAttributetype().getName()) {
                case "boolean":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(attr.getName()).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t").append(("<input type=\"checkbox\" id=\"" + attr.getName() + "\" ng-checked=\"{{info['" + attr.getName() + "']}}\">")).append("\n");
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "string":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("text").withId(attr.getName()).withClass("form-control").withValue("{{info['" + attr.getName() + "']}}")).append("\n");
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "text":
                case "htmltext":
                case "markdown":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(textarea().withId(attr.getName()).withClass("form-control").withText("{{info['" + attr.getName() + "']}}")).append("\n");
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "hashstring":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("password").withId(attr.getName()).withClass("form-control")).append("\n");
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "integer":
                case "real":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("number").withId(attr.getName()).withClass("form-control").withValue("{{info['" + attr.getName() + "']}}"));
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "assetref":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append("<select class=\"form-select\" id=\"").append(attr.getName()).append("\">").append("\n");
                    html.append("\t\t").append("<option ng-selected=\"{{info['").append(attr.getName()).append("'] === 'undefined'}}\" value=\"NOVALUE\">Klicken um zu ändern</option>").append("\n");
                    html.append("\t\t\t\t").append("<option ng-repeat=\"name in libNames\" value=\"{{name.id}}\" ng-selected=\"{{info['").append(attr.getName()).append("'] === name.name}}\">{{name.name}}</option>").append("\n");
                    html.append("\t\t\t\t").append(("</select>")).append("\n");
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case "media":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append("<select class=\"form-select\" id=\"").append(attr.getName()).append("\">").append("\n");
                    html.append("\t\t").append("<option ng-selected=\"{{info['").append(attr.getName()).append("'] === 'undefined'}}\" value=\"NOVALUE\">Klicken um zu ändern</option>").append("\n");
                    html.append("\t\t\t\t").append("<option ng-repeat=\"media in mediaList\" value=\"{{media.asset.id}}\" ng-selected=\"{{media.asset.id === info['").append(attr.getName()).append("']}}\">{{media.asset.id}}. {{media.asset.name}}</option>").append("\n");
                    html.append("\t\t\t\t").append("</select>").append("\n");
                    html.append("\t\t\t\t").append("</div>").append("\n");
                    break;
                case "classref":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append("<select class=\"form-select\" id=\"").append(attr.getName()).append("\">").append("\n");
                    html.append("\t\t").append("<option ng-selected=\"{{info['").append(attr.getName()).append("'] === 'undefined'}}\" value=\"NOVALUE\">Klicken um zu ändern</option>").append("\n");
                    html.append("\t\t\t\t").append("<option ng-repeat=\"name in classNames\" value=\"{{name.id}}\" ng-selected=\"{{info['").append(attr.getName()).append("'] === name.name}}\">{{name.name}}</option>").append("\n");
                    html.append("\t\t\t\t").append("</select>").append("\n");
                    html.append("\t\t\t\t").append("</div>").append("\n");
                    break;
                case "datetime":
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("date").withId(attr.getName()).withClass("form-control").withValue("{{formatDate(info['" + attr.getName() + "'])}}"));
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
            }
        }
        html.append("\t\t\t\t").append(("</form>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-footer\">")).append("\n");
        html.append("\t\t\t\t").append(("<button class=\"btn btn-primary w-100\" data-bs-dismiss=\"modal\" ng-click=\"update(info['contentname'])\">Editieren</button>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</body>")).append("\n");
        html.append("\t\t\t\t").append(("</html>")).append("\n");
        
        template.setName(clazz.getName() + "_Webform");
        try {
            CfTemplate dummytemplate = cfTemplateService.findByName(template.getName());

            if (null == dummytemplate) {
                template.setScriptlanguage(2);
                template.setCheckedoutby(BigInteger.ZERO);
                template.setContent(html.toString());
                cfTemplateService.create(template);
            } else {
                dummytemplate.setContent(html.toString());
                cfTemplateService.edit(dummytemplate);
            }
        } catch (Exception ex) {
            template.setScriptlanguage(2);
            template.setCheckedoutby(BigInteger.ZERO);
            template.setContent(html.toString());
            cfTemplateService.create(template);
        }

        site.setName(clazz.getName() + "_Webform");
        try {
            CfSite dummysite = cfSiteService.findByName(site.getName());
        } catch (Exception ex) {
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
            cfSiteService.create(site);
        }
        sitetree.loadTree();
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
