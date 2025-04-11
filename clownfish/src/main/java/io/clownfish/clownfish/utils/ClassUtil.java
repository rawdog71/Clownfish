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
import io.clownfish.clownfish.datamodels.ODataWizard;
import io.clownfish.clownfish.datamodels.RestContentParameter;
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.serviceinterface.*;
import static j2html.TagCreator.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

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
    @Autowired TemplateUtil templateutil;
    @Autowired JavascriptUtil javascriptutil;
    private @Getter @Setter SiteTreeBean sitetree;
    private @Getter @Setter SiteUtil siteutil;

    private boolean isClassrefMandatory = false;
    final transient Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);
    
    public ClassUtil() {
    }
    
    public void createClass(ClassImport ci) {
        CfClass newclass = new CfClass();
        newclass.setName(ci.getClassname());
        newclass.setSearchrelevant(ci.isSearchenginerelevant());
        newclass.setMaintenance(ci.isBackendmaintenance());
        newclass.setEncrypted(ci.isEncrypted());
        newclass.setLoginclass(ci.isLoginclass());
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
                if (0 == fi.getRelationtype().compareToIgnoreCase("1:n")) {     // 1:n
                    newattribut.setRelationtype(1);
                } else {                                                        // n:m
                    newattribut.setRelationtype(0);
                }
            } else {
                newattribut.setRelationref(null);
            }
            newattribut.setDefault_val(fi.getDefaultval());
            newattribut.setMandatory(fi.isMandatory());
            newattribut.setMin_val(fi.getMinval());
            newattribut.setMax_val(fi.getMaxval());
            newattribut.setDescription(fi.getDescription());
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
                    if (0 == attributcontent.getAttributref().getRelationtype()) {      // n:m
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
                    } else {                                                            // 1:n
                        CfClasscontent selclasscontent = cfclasscontentService.findById(attributcontent.getContentInteger().longValue());
                        Map dummy_attributcontentmap = getattributmap(selclasscontent);
                        attributcontentmap.put(attributcontent.getAttributref().getName(), dummy_attributcontentmap);
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
                    if (0 == attributcontent.getAttributref().getRelationtype()) {      // n:m
                        if (null != attributcontent.getClasscontentlistref()) {
                            contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getClasscontentlistref().getName());
                        }
                    } else {                                                            // 1:n
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
            if (null != attribut) {
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
                        // ToDo: 1:n - n:m logic
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
            } else {
                LOGGER.warn("Attribut " + key + " does not exist anymore!");
            }
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
                    // ToDo: 1:n - n:m logic
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
                    html.append("\t\t\t\t").append("<input type=\"checkbox\" id=\"").append(attr.getName()).append("\" ng-checked=\"{{info['").append(attr.getName()).append("']}}\">").append("\n");
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
                    // ToDo: 1:n - n:m logic
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
            site.setParentref(null);
            site.setTemplateref(template);
            site.setShorturl(siteutil.generateShorturl());
            site.setLoginsite("");
            site.setTestparams("");
            cfSiteService.create(site);
        }
        sitetree.loadTree();
    }
    
    public String generateODataForm(CfClass clazz, List<ODataWizard> wizardlist, boolean generatelistform) {
        List<CfAttribut> attributList = cfattributService.findByClassref(clazz);
        StringBuilder html = new StringBuilder();
        StringBuilder javascript = new StringBuilder();
        CfTemplate template = new CfTemplate();
        CfTemplate dummytemplate = null;
        CfSite site = new CfSite();
        CfJavascript js = new CfJavascript();
        
        boolean created = true;

        html.append("<!DOCTYPE html>").append("\n");
        html.append("<html lang=\"de\" ng-app=\"crud").append(clazz.getName()).append("App\">").append("\n");
        html.append("\t<head>").append("\n");
        html.append("\t\t<meta charset=\"utf-8\">").append("\n");
        html.append("\t\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">").append("\n");
        html.append("\t\t<link rel=\"stylesheet\" href=\"/resources/css/uikit.css\">").append("\n");
        html.append("\t\t<link rel=\"stylesheet\" href=\"/resources/css/cf_crud.css\">").append("\n");
        html.append("\t\t<link href=\"/resources/css/pikaday.css\" rel=\"stylesheet\">").append("\n");
        html.append("\t\t<script src=\"/resources/js/angular.js\"></script>").append("\n");
        html.append("\t\t<script src=\"/js/crud_").append(clazz.getName().toLowerCase()).append(".js\"></script>").append("\n");
        html.append("\t\t<script src=\"/resources/js/pikaday.js\"></script>").append("\n");
        html.append("\t\t<script src=\"/resources/js/luxon.js\"></script>").append("\n");
        html.append("\t</head>").append("\n");
        html.append("\t<body id=\"page-top\" ng-controller=\"Crud").append(clazz.getName()).append("Controller\" data-ng-init=\"init()\">").append("\n");
        
        html.append("\t\t<ul class=\"uk-subnav uk-subnav-pill\" uk-switcher>").append("\n");
	html.append("\t\t\t<li><a href=\"#\">").append(clazz.getName()).append("</a></li>").append("\n");
        if (generatelistform) {
            html.append("\t\t\t<li><a href=\"#\">").append(clazz.getName()).append(" Listen</a></li>").append("\n");
        }
	html.append("\t\t</ul>").append("\n");
	html.append("\t\t<ul class=\"uk-switcher uk-margin\">").append("\n");
	html.append("\t\t\t<li>").append("\n");
        
        html.append("\t\t<div class=\"uk-container-large uk-align-center\">").append("\n");
        html.append("\t\t\t<div class=\"uk-margin-top\">").append("\n");
        html.append("\t\t\t\t<table class=\"uk-table uk-table-small uk-table-striped\">").append("\n");
        html.append("\t\t\t\t\t<caption>").append(clazz.getName()).append(" <a href=\"\" class=\"uk-icon-button\" uk-icon=\"plus\" ng-click=\"add").append(clazz.getName()).append("Modal()\" uk-tooltip=\"").append(clazz.getName()).append(" hinzufügen\"></a></caption>").append("\n");
        html.append("\t\t\t\t\t<thead style=\"position: sticky !important;top: 0;background: white;z-index: 1;\">").append("\n");
        html.append("\t\t\t\t\t\t<tr>").append("\n");
        html.append("\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append(" == 'id', 'descending': order_").append(clazz.getName().toLowerCase()).append(" == '-id'}\">ID</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('id')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('-id')\" uk-icon=\"chevron-down\"></a></th>").append("\n");

        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor() || !created) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "classref":
                    created = false;
                    if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                        created = true;
                    }
                    if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                        created = true;
                    }
                    if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                        created = true;
                    }
                    break;
            }
        }
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                        html.append("\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append(" == '").append(attr.getName()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append(" == '-").append(attr.getName()).append("'}\">").append(StringUtils.capitalise(attr.getName())).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('").append(attr.getName()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('-").append(attr.getName()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        break;
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append(" == '").append(attr.getName()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append(" == '-").append(attr.getName()).append("'}\">").append(StringUtils.capitalise(attr.getName())).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('").append(attr.getName()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('-").append(attr.getName()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append(" == '").append(attr.getName()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append(" == '-").append(attr.getName()).append("'}\">").append(StringUtils.capitalise(attr.getName())).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('").append(attr.getName()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('-").append(attr.getName()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        break;
                    case "boolean":
                        html.append("\t\t\t\t\t\t\t<th><span>").append(StringUtils.capitalise(attr.getName())).append("</span></th>").append("\n");
                        break;
                    case "classref":
                        if (1 == attr.getRelationtype()) {
                            if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                                html.append("\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append(" == '").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append(" == '-").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append("'}\">").append(StringUtils.capitalise(attr.getName())).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('-").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                            }
                            if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                                html.append("\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append(" == '").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append(" == '-").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append("'}\">").append(StringUtils.capitalise(attr.getName())).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('-").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                            }
                            if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                                html.append("\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append(" == '").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append(" == '-").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append("'}\">").append(StringUtils.capitalise(attr.getName())).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("('-").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                            }
                        }
                        break;
                }
            }
        }
        
        html.append("\t\t\t\t\t\t\t<th class=\"uk-text-right\">Aktion</th>").append("\n");
        html.append("\t\t\t\t\t</tr>").append("\n");
        html.append("\t\t\t\t\t<tr>").append("\n");
        html.append("\t\t\t\t\t\t\t<th></th>").append("\n");
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                        html.append("\t\t\t\t\t\t\t<th><input id=\"filter_").append(attr.getName()).append("\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("\"></th>").append("\n");
                        break;
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t<th><input id=\"filter_").append(attr.getName()).append("\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".length != 0}\" type=\"number\" placeholder=\"\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("\"></th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t<th><input id=\"filter_").append(attr.getName()).append("\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("\"></th>").append("\n");
                        break;
                    case "boolean":
                        html.append("\t\t\t\t\t\t\t<th></th>").append("\n");
                        break;
                    case "classref":
                        if (1 == attr.getRelationtype()) {
                            html.append("\t\t\t\t\t\t\t<th><input id=\"filter_").append(attr.getName().toLowerCase()).append("\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("\"></th>").append("\n");
                        }
                        break;
                }
            }
        }
        html.append("\t\t\t\t\t\t\t<th></th>").append("\n");
        html.append("\t\t\t\t\t<tr>").append("\n");
        html.append("\t\t\t\t</thead>").append("\n");
        html.append("\t\t\t\t<tbody>").append("\n");
	html.append("\t\t\t\t\t<tr ng-repeat=\"").append(clazz.getName().toLowerCase()).append(" in ").append(clazz.getName().toUpperCase()).append("LIST | filter: ").append(clazz.getName().toLowerCase()).append("_filter ");
        html.append("| orderBy: order_").append(clazz.getName().toLowerCase()).append("\">").append("\n");
        
	html.append("\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".id}}</td>").append("\n");
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t<td ng-show=\"!").append(clazz.getName().toLowerCase()).append(".editable\" ng-mouseover=\"").append(clazz.getName().toLowerCase()).append(".editable=true\">{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("}}</td>").append("\n");
                        html.append("\t\t\t\t\t\t<td ng-show=\"").append(clazz.getName().toLowerCase()).append(".editable\" ng-mouseleave=\"").append(clazz.getName().toLowerCase()).append(".editable=false\"><input id=\"input-").append(attr.getName()).append("-inst\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("\" ng-model-options=\"{debounce: 1000}\" ng-change=\"update").append(clazz.getName()).append("Instant(").append(clazz.getName().toLowerCase()).append(".id, '").append(attr.getName()).append("', ").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(")\"></td>").append("\n");
                        break;
                    case "boolean":
                        html.append("\t\t\t\t\t\t<td><input id=\"input-").append(attr.getName()).append("-inst\" class=\"uk-checkbox\" type=\"checkbox\" ng-model=\"").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("\" ng-change=\"update").append(clazz.getName()).append("Instant(").append(clazz.getName().toLowerCase()).append(".id, '").append(attr.getName()).append("', ").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(")\"></td>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("}}</td>").append("\n");
                        break;
                    case "classref":
                        if (1 == attr.getRelationtype()) {
                            if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                                html.append("\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append("}}</td>").append("\n");
                            }
                            if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                                html.append("\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append("}}</td>").append("\n");
                            }
                            if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                                html.append("\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append("}}</td>").append("\n");
                            }
                        }
                        break;
                }
            }
        }
	html.append("\t\t\t\t\t\t\t<td class=\"uk-text-right\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<a href=\"\" class=\"uk-icon-button\" ng-click=\"update").append(clazz.getName()).append("Modal(").append(clazz.getName().toLowerCase()).append(".id)\" ng-show=\"!inprogress\" uk-icon=\"pencil\" uk-tooltip=\"").append(clazz.getName()).append(" ändern\"></a>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<a href=\"\" class=\"uk-icon-button\" ng-click=\"delete").append(clazz.getName()).append("Modal(").append(clazz.getName().toLowerCase()).append(".id)\" ng-show=\"!inprogress\" uk-icon=\"trash\" uk-tooltip=\"").append(clazz.getName()).append(" löschen\"></a>").append("\n");
	html.append("\t\t\t\t\t\t\t</td>").append("\n");
	html.append("\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t</tbody>").append("\n");
	html.append("\t\t\t\t</table>").append("\n");
	html.append("\t\t\t</div>").append("\n");
	html.append("\t\t</div>").append("\n");
        html.append("\n");
        
        html.append("\t\t<div id=\"modal-").append(clazz.getName().toLowerCase()).append("-add\" class=\"uk-modal-container uk-flex-top\" uk-modal>").append("\n");
        html.append("\t\t\t<div class=\"uk-modal-dialog uk-modal-header\">").append("\n");
        html.append("\t\t\t\t<button class=\"uk-modal-close-default\" type=\"button\" uk-close></button>").append("\n");
        html.append("\t\t\t\t<h2 class=\"uk-modal-title\">").append(clazz.getName()).append(" hinzufügen</h2>").append("\n");
        html.append("\t\t\t\t<div class=\"uk-overflow-auto\" style=\"max-height: 600px;\">").append("\n");
        html.append("\t\t\t\t\t<form name=\"").append(clazz.getName().toLowerCase()).append("FormAdd\" novalidate>\n");
        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor() || !attr.getExt_mutable()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "string":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\" required>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\">").append(" \n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "hashstring":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-input\" type=\"password\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\"required >").append(" \n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-input\" type=\"password\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\">").append(" \n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "text":
                case "htmltext":
                case "markdown":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<textarea id=\"input-").append(attr.getName()).append("-add\" class=\"uk-textarea\" rows=\"5\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\"required></textarea>").append(" \n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<textarea id=\"input-").append(attr.getName()).append("-add\" class=\"uk-textarea\" rows=\"5\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\"></textarea>").append("\n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "integer":
                case "real":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-input\" type=\"number\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\"required>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-input\" type=\"number\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\">").append(" \n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "datetime":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\"required>").append(" \n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(attr.getName()).append("\">").append(" \n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    html.append("\t\t\t\t\t<script>").append("\n");
                    html.append("\t\t\t\t\t\tvar picker = new Pikaday({ field: document.getElementById('input-").append(attr.getName()).append("-add'), firstDay:1, i18n: { previousMonth: 'Previous Month', nextMonth: 'Next Month', months: ['Januar','Februar','März','April','Mai','Juni','Juli','August','September','Oktober','November','Dezember'], weekdays: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'], weekdaysShort : ['So','Mo','Di','Mi','Do','Fr','Sa']}, showWeekNumber: true, toString: function(date) {").append("\n");
                    html.append("\t\t\t\t\t\t\tvar parts = [date.getFullYear(), ('0'+(date.getMonth()+1)).slice(-2), ('0'+date.getDate()).slice(-2)];").append("\n");
                    html.append("\t\t\t\t\t\treturn parts.join(\"-\");").append("\n");
                    html.append("\t\t\t\t\t\t}});").append("\n");
                    html.append("\t\t\t\t\t</script>").append("\n");
                    break;
                case "boolean":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-checkbox\" type=\"checkbox\" ng-model=\"").append(attr.getName()).append("\">").append("required \n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-add\" class=\"uk-checkbox\" type=\"checkbox\" ng-model=\"").append(attr.getName()).append("\">").append("\n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "media":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    html.append("\t\t\t\t\t<div class=\"uk-overflow-auto uk-height-max-medium\">").append("\n");
                    html.append("\t\t\t\t\t<table class=\"uk-table uk-table-small uk-table-hover uk-table-middle uk-table-divider\" id=\"input-").append(attr.getName()).append("-add\">").append("\n");
                    html.append("\t\t\t\t\t<thead>").append("\n");
                    html.append("\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
                    html.append("\t\t\t\t\t<th class=\"uk-table-shrink\">Preview</th>").append("\n");
                    html.append("\t\t\t\t\t<th class=\"uk-table-small\">Name</th>").append("\n");
                    html.append("\t\t\t\t\t<th class=\"uk-width-small\">Beschreibung</th>").append("\n");
                    html.append("\t\t\t\t\t</tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th><input id=\"filter_media_").append(attr.getName()).append("_name_add\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_media_").append(attr.getName()).append("_name_add.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"name\" ng-model=\"filter_media_").append(attr.getName()).append("_name_add\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th><input id=\"filter_media_").append(attr.getName()).append("_description_add\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_media_").append(attr.getName()).append("_description_add.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"beschreibung\" ng-model=\"filter_media_").append(attr.getName()).append("_description_add\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t</thead>").append("\n");
                    html.append("\t\t\t\t\t<tbody>").append("\n");
                    html.append("\t\t\t\t\t<tr ng-repeat=\"media in MEDIALIST | filter: {name: filter_media_").append(attr.getName()).append("_name_add} | filter: {description: filter_media_").append(attr.getName()).append("_description_add} | filter:filterByMimetypes\">").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t<td><input id=\"input-").append(attr.getName()).append("-add\"").append("class=\"uk-radio\" type=\"radio\" ng-click=\"checkMedia(media.id)\" checked=\"").append(attr.getDefault_val() != null ? "true\"" : "false;\"").append("name=\"radio-").append(attr.getName()).append("\" ng-value=\"media.id\" ng-model=\"media_").append(attr.getName()).append(".").append(attr.getName()).append("\" required></td>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t<td><input class=\"uk-radio\" type=\"radio\" ng-click=\"checkMedia(media.id)\" checked=\"").append(attr.getDefault_val() != null ? "true\"" : "false;\"").append("name=\"radio-").append(attr.getName()).append("\" ng-value=\"media.id\" ng-model=\"media_").append(attr.getName()).append(".").append(attr.getName()).append("\"></td>").append("\n");
                    }
                    html.append("\t\t\t\t\t<td><img class=\"uk-preserve-width\" src=\"GetAsset?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=&mediaid={{media.id}}\" width=\"40\" height=\"40\" alt=\"\"></td>").append("\n");
                    html.append("\t\t\t\t\t<td class=\"uk-table-link\">").append("\n");
                    html.append("\t\t\t\t\t<a class=\"uk-link-reset\" href=\"\">{{media.name}}</a>").append("\n");
                    html.append("\t\t\t\t\t</td>").append("\n");
                    html.append("\t\t\t\t\t<td class=\"uk-text-truncate\">{{media.description}}</td>").append("\n");
                    html.append("\t\t\t\t\t</tr>").append("\n");
                    html.append("\t\t\t\t\t</tbody>").append("\n");
                    html.append("\t\t\t\t\t</table>").append("\n");
                    html.append("\t\t\t\t\t</div>").append("\n");
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "classref":
                    if (1 == attr.getRelationtype()) {
                        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");

                        if(attr.getMandatory()) {
                            html.append("\t\t\t\t\t\t<select id=\"input-").append(attr.getName()).append("-add\" class=\"uk-select\" ng-options=\"").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append(" for ").append(attr.getName().toLowerCase()).append(" in ").append(attr.getName().toUpperCase()).append("LIST track by ").append(attr.getName()).append(".id\" ng-model=\"").append(attr.getName()).append("\" required>").append("\n");
                            html.append("\t\t\t\t\t\t\t<option value=\"\">-- Select ").append(StringUtils.capitalise(attr.getName())).append(" --</option>").append("\n");
                            html.append("\t\t\t\t\t\t</select>").append("\n");html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                        } else {
                            html.append("\t\t\t\t\t\t<select id=\"input-").append(attr.getName()).append("-add\" class=\"uk-select\" ng-options=\"").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append(" for ").append(attr.getName().toLowerCase()).append(" in ").append(attr.getName().toUpperCase()).append("LIST track by ").append(attr.getName()).append(".id\" ng-model=\"").append(attr.getName()).append("\" >").append("\n");
                            html.append("\t\t\t\t\t\t\t<option value=\"\">-- Select ").append(StringUtils.capitalise(attr.getName())).append(" --</option>").append("\n");
                            html.append("\t\t\t\t\t\t</select>").append("\n");
                        }
                        html.append("\t\t\t\t\t</div>").append("\n");
                    } else {
                        if(attr.getMandatory()) {
                            isClassrefMandatory = true;
                        }
                        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"").append(attr.getName()).append("-list-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                        html.append("\t\t\t\t\t\t<div class=\"uk-overflow-auto\" style=\"height: 370px\">").append("\n");
                        html.append("\t\t\t\t\t\t\t<table id=\"").append(attr.getName()).append("-list-add\" class=\"uk-table uk-table-hover uk-table-small uk-table-divider\">").append("\n");
                        html.append("\t\t\t\t\t\t\t\t<thead class=\"table-head\">").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t<tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '").append(odw.getRelationattribut1()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '-").append(odw.getRelationattribut1()).append("'}\">").append(odw.getRelationattribut1()).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('").append(odw.getRelationattribut1()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('-").append(odw.getRelationattribut1()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '").append(odw.getRelationattribut2()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '-").append(odw.getRelationattribut2()).append("'}\">").append(odw.getRelationattribut2()).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('").append(odw.getRelationattribut2()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('-").append(odw.getRelationattribut2()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '").append(odw.getRelationattribut3()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '-").append(odw.getRelationattribut3()).append("'}\">").append(odw.getRelationattribut3()).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('").append(odw.getRelationattribut3()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('-").append(odw.getRelationattribut3()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        }
                        html.append("\t\t\t\t\t\t\t\t\t</tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t<tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append("_").append(odw.getRelationattribut1()).append("\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut1()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(odw.getRelationattribut1()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut1()).append("\"></th>").append("\n");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append("_").append(odw.getRelationattribut2()).append("\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut2()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(odw.getRelationattribut2()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut2()).append("\"></th>").append("\n");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append("_").append(odw.getRelationattribut3()).append("\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut3()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(odw.getRelationattribut3()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut3()).append("\"></th>").append("\n");
                        }
                        html.append("\t\t\t\t\t\t\t\t\t<tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t</thead>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t<tbody>").append("\n");
                        
                        html.append("\t\t\t\t\t\t\t\t\t<tr ng-repeat=\"").append(attr.getName()).append(" in ").append(attr.getName().toUpperCase()).append("LIST ");
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            html.append("| filter: {").append(odw.getRelationattribut1()).append(": filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut1()).append("} ");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            html.append("| filter: {").append(odw.getRelationattribut2()).append(": filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut2()).append("} ");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            html.append("| filter: {").append(odw.getRelationattribut3()).append(": filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut3()).append("} ");
                        }
                        html.append("| orderBy: order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append("\">").append("\n");
                        
                        html.append("\t\t\t\t\t\t\t\t\t\t<td><input class=\"uk-checkbox\" type=\"checkbox\" aria-label=\"Checkbox\" ng-click=\"select").append(clazz.getName()).append("").append(attr.getName()).append("(").append(attr.getName()).append(".id)\"></td>").append("\n");
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<td>{{").append(attr.getName()).append(".").append(odw.getRelationattribut1()).append("}}</td>").append("\n");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<td>{{").append(attr.getName()).append(".").append(odw.getRelationattribut2()).append("}}</td>").append("\n");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<td>{{").append(attr.getName()).append(".").append(odw.getRelationattribut3()).append("}}</td>").append("\n");
                        }
                        html.append("\t\t\t\t\t\t\t\t\t\t</tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t</tbody>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t</table>").append("\n");
                        html.append("\t\t\t\t\t\t\t</div>").append("\n");
                        html.append("\t\t\t\t\t\t</div>").append("\n");
                    }
                    break;
                case "assetref":
                    html.append("\t\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-add\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    html.append("\t\t\t\t\t\t\t<div class=\"uk-overflow-auto uk-height-max-medium\">").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<table class=\"uk-table uk-table-small uk-table-hover uk-table-middle uk-table-divider\" id=\"input-").append(attr.getName()).append("-add\">").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<thead>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\">Preview</th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t<th class=\"uk-table-small\">Name</th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t<th class=\"uk-width-small\">Beschreibung</th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t</tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th><input id=\"filter_media_").append(attr.getName()).append("_name_add\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_media_").append(attr.getName()).append("_name_add.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"name\" ng-model=\"filter_media_").append(attr.getName()).append("_name_add\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th><input id=\"filter_media_").append(attr.getName()).append("_description_add\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_media_").append(attr.getName()).append("_description_add.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"beschreibung\" ng-model=\"filter_media_").append(attr.getName()).append("_description_add\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t</thead>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<tbody>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t<tr ng-repeat=\"media in MEDIALIST | filter: {name: filter_media_").append(attr.getName()).append("_name_add} | filter: {description: filter_media_").append(attr.getName()).append("_description_add} | filter:filterByMimetypes\">").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t<td><input class=\"uk-checkbox\" type=\"checkbox\" aria-label=\"Checkbox\" ng-click=\"select").append(clazz.getName()).append("").append(attr.getName()).append("(media.id)\"></td>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t<td><img class=\"uk-preserve-width\" src=\"GetAsset?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=&mediaid={{media.id}}\" width=\"40\" height=\"40\" alt=\"\"></td>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t<td class=\"uk-table-link\">").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t\t<a class=\"uk-link-reset\" href=\"\">{{media.name}}</a>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t</td>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t\t<td class=\"uk-text-truncate\">{{media.description}}</td>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t</tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t</tbody>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t</table>").append("\n");
                    html.append("\t\t\t\t\t\t\t</div>").append("\n");
                    html.append("\t\t\t\t\t\t</div>").append("\n");
                    break;
            }
        }
        
        html.append("\t\t\t\t\t<div class=\"uk-align-right\">").append("\n");
        if(isClassrefMandatory) {
            html.append("\t\t\t\t\t<button class=\"uk-button uk-button-primary\" type=\"button\" ng-click=\"save").append(clazz.getName()).append("()\" ng-disabled=\"").append(clazz.getName().toLowerCase()).append("FormAdd.$invalid || !isMediaSelected || inprogress || !isAnyCheckboxSelectedAssetRef || !isAnyCheckboxSelected\">Speichern <span ng-show=\"inprogress\" class=\"uk-spinner\" uk-icon=\"icon: cog\"></span></button>").append("\n");
        } else {
            html.append("\t\t\t\t\t<button class=\"uk-button uk-button-primary\" type=\"button\" ng-click=\"save").append(clazz.getName()).append("()\" ng-disabled=\"").append(clazz.getName().toLowerCase()).append("FormAdd.$invalid || !isMediaSelected || !isAnyCheckboxSelectedAssetRef || inprogress\">Speichern <span ng-show=\"inprogress\" class=\"uk-spinner\" uk-icon=\"icon: cog\"></span></button>").append("\n");
        }
        html.append("\t\t\t\t\t<button class=\"uk-button uk-button-secondary uk-modal-close\" type=\"button\" ng-disabled=\"inprogress\">Abbrechen</button>").append("\n");
        html.append("\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t</form>").append("\n");
        html.append("\t\t\t</div>").append("\n");
        html.append("\t\t</div>").append("\n");
	html.append("\t</div>").append("\n");
        
        html.append("\t\t<div id=\"modal-").append(clazz.getName().toLowerCase()).append("-update\" class=\"uk-modal-container uk-flex-top\" uk-modal>").append("\n");
	html.append("\t\t\t<div class=\"uk-modal-dialog uk-modal-header\">").append("\n");
	html.append("\t\t\t\t<button class=\"uk-modal-close-default\" type=\"button\" uk-close></button>").append("\n");
	html.append("\t\t\t\t<h2 class=\"uk-modal-title\">").append(clazz.getName()).append(" ändern</h2>").append("\n");
	html.append("\t\t\t\t<div class=\"uk-overflow-auto\" style=\"max-height: 600px;\">").append("\n");
        html.append("\t\t\t\t\t<form name=\"").append(clazz.getName().toLowerCase()).append("FormUpd\" novalidate>\n");

        isClassrefMandatory = false;

        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor() || !attr.getExt_mutable()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "string":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\" required>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Upd.$touched && ").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Upd.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\">").append("\n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "hashstring":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName().toLowerCase()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-input\" type=\"password\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\" required>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Upd.$touched && ").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Upd.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-input\" type=\"password\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\">").append("\n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "text":
                case "htmltext":
                case "markdown":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<textarea id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-textarea\" rows=\"5\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\" required>{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}</textarea>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Upd.$touched && ").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Upd.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<textarea id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-textarea\" rows=\"5\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\">{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}</textarea>").append("\n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "integer":
                case "real":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-input\" type=\"number\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\" required>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Upd.$touched && ").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Upd.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-input\" type=\"number\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\">").append("\n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "datetime":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\" required>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Upd.$touched && ").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Upd.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-input\" type=\"text\" placeholder=\"").append(StringUtils.capitalise(attr.getName())).append("\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" aria-label=\"").append(StringUtils.capitalise(attr.getName())).append("\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\">").append("\n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    html.append("\t\t\t\t\t<script>").append("\n");
                    html.append("\t\t\t\t\t\tvar picker = new Pikaday({ field: document.getElementById('input-").append(attr.getName()).append("-upd'), firstDay:1, i18n: { previousMonth: 'Previous Month', nextMonth: 'Next Month', months: ['Januar','Februar','März','April','Mai','Juni','Juli','August','September','Oktober','November','Dezember'], weekdays: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'], weekdaysShort : ['So','Mo','Di','Mi','Do','Fr','Sa']}, showWeekNumber: true, toString: function(date) {").append("\n");
                    html.append("\t\t\t\t\t\t\tvar parts = [date.getFullYear(), ('0'+(date.getMonth()+1)).slice(-2), ('0'+date.getDate()).slice(-2)];").append("\n");
                    html.append("\t\t\t\t\t\treturn parts.join(\"-\");").append("\n");
                    html.append("\t\t\t\t\t\t}});").append("\n");
                    html.append("\t\t\t\t\t</script>").append("\n");
                    break;
                case "boolean":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-checkbox\" type=\"checkbox\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\" required>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Upd.$touched && ").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Upd.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t\t<input id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-checkbox\" type=\"checkbox\" value=\"{{").append(clazz.getName()).append(".").append(attr.getName()).append("}}\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\">").append("\n");
                    }
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "media":
                    html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    html.append("\t\t\t\t\t<div class=\"uk-overflow-auto uk-height-max-medium\">").append("\n");
                    html.append("\t\t\t\t\t<table class=\"uk-table uk-table-small uk-table-hover uk-table-middle uk-table-divider\" id=\"input-").append(attr.getName()).append("-upd\">").append("\n");
                    html.append("\t\t\t\t\t<thead>").append("\n");
                    html.append("\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
                    html.append("\t\t\t\t\t<th class=\"uk-table-shrink\">Preview</th>").append("\n");
                    html.append("\t\t\t\t\t<th class=\"uk-table-small\">Name</th>").append("\n");
                    html.append("\t\t\t\t\t<th class=\"uk-width-small\">Beschreibung</th>").append("\n");
                    html.append("\t\t\t\t\t</tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th><input id=\"filter_media_").append(attr.getName()).append("_name_upd\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_media_").append(attr.getName()).append("_name_upd.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"name\" ng-model=\"filter_media_").append(attr.getName()).append("_name_upd\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th><input id=\"filter_media_").append(attr.getName()).append("_description_upd\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_media_").append(attr.getName()).append("_description_upd.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"beschreibung\" ng-model=\"filter_media_").append(attr.getName()).append("_description_upd\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t</thead>").append("\n");
                    html.append("\t\t\t\t\t<tbody>").append("\n");
                    html.append("\t\t\t\t\t<tr ng-repeat=\"media in MEDIALIST | filter: {name: filter_media_").append(attr.getName()).append("_name_upd} | filter: {description: filter_media_").append(attr.getName()).append("_description_upd} | filter:filterByMimetypes\">").append("\n");
                    if(attr.getMandatory()) {
                        html.append("\t\t\t\t\t<td><input id=\"input-").append(attr.getName()).append("-upd\"").append("class=\"uk-radio\" type=\"radio\" ng-click=\"checkMedia(media.id)\" checked=\"").append(attr.getDefault_val() != null ? "true" : "false").append(";\"").append("name=\"radio-").append(attr.getName()).append("\" ng-value=\"media.id\" ng-model=\"media_").append(attr.getName()).append(".").append(attr.getName()).append("\" required></td>").append("\n");
                        html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Add.$touched &&").append(clazz.getName().toLowerCase()).append("FormAdd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Add.$invalid\">THis field is required.</span>\n");
                    } else {
                        html.append("\t\t\t\t\t<td><input class=\"uk-radio\" type=\"radio\" ng-click=\"checkMedia(media.id)\" checked=\"").append(attr.getDefault_val() != null ? "true\"" : "false;\"").append("name=\"radio-").append(attr.getName()).append("\" ng-value=\"media.id\" ng-model=\"media_").append(attr.getName()).append(".").append(attr.getName()).append("\"></td>").append("\n");
                    }
                    html.append("\t\t\t\t\t<td><img class=\"uk-preserve-width\" src=\"GetAsset?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=&mediaid={{media.id}}\" width=\"40\" height=\"40\" alt=\"\"></td>").append("\n");
                    html.append("\t\t\t\t\t<td class=\"uk-table-link\">").append("\n");
                    html.append("\t\t\t\t\t<a class=\"uk-link-reset\" href=\"\">{{media.name}}</a>").append("\n");
                    html.append("\t\t\t\t\t</td>").append("\n");
                    html.append("\t\t\t\t\t<td class=\"uk-text-truncate\">{{media.description}}</td>").append("\n");
                    html.append("\t\t\t\t\t</tr>").append("\n");
                    html.append("\t\t\t\t\t</tbody>").append("\n");
                    html.append("\t\t\t\t\t</table>").append("\n");
                    html.append("\t\t\t\t\t</div>").append("\n");
                    html.append("\t\t\t\t\t</div>").append("\n");
                    break;
                case "classref":
                    if (1 == attr.getRelationtype()) {
                        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                        if(attr.getMandatory()) {
                            html.append("\t\t\t\t\t\t<select id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-select\" ng-options=\"").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append(" for ").append(attr.getName()).append(" in ").append(attr.getName().toUpperCase()).append("LIST track by ").append(attr.getName()).append(".id\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\" required>").append("\n");
                            html.append("\t\t\t\t\t\t\t<option value=\"\">-- Select ").append(StringUtils.capitalise(attr.getName())).append(" --</option>").append("\n");
                            html.append("\t\t\t\t\t\t</select>").append("\n");
                            html.append("\t\t\t\t\t\t<span ng-show=\"").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("Upd.$touched && ").append(clazz.getName().toLowerCase()).append("FormUpd.").append("input"+ attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1) + "Upd.$invalid\">THis field is required.</span>\n");
                        } else {
                            html.append("\t\t\t\t\t\t<select id=\"input-").append(attr.getName()).append("-upd\" class=\"uk-select\" ng-options=\"").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append(" for ").append(attr.getName()).append(" in ").append(attr.getName().toUpperCase()).append("LIST track by ").append(attr.getName()).append(".id\" ng-model=\"").append(clazz.getName()).append(".").append(attr.getName()).append("\" >").append("\n");
                            html.append("\t\t\t\t\t\t\t<option value=\"\">-- Select ").append(StringUtils.capitalise(attr.getName())).append(" --</option>").append("\n");
                            html.append("\t\t\t\t\t\t</select>").append("\n");
                        }
                        html.append("\t\t\t\t\t</div>").append("\n");
                    } else {
                        if(attr.getMandatory()) {
                            isClassrefMandatory = true;
                        } else {
                            isClassrefMandatory = false;
                        }
                        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"").append(attr.getName()).append("-list-update\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                        html.append("\t\t\t\t\t\t<div class=\"uk-overflow-auto\" style=\"height: 370px\">").append("\n");
                        html.append("\t\t\t\t\t\t\t<table id=\"").append(attr.getName()).append("-list-update\" class=\"uk-table uk-table-hover uk-table-small uk-table-divider\">").append("\n");
                        html.append("\t\t\t\t\t\t\t\t<thead class=\"table-head\">").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t<tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '").append(odw.getRelationattribut1().toLowerCase()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '-").append(odw.getRelationattribut1().toLowerCase()).append("'}\">").append(odw.getRelationattribut1().toLowerCase()).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('").append(odw.getRelationattribut1().toLowerCase()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('-").append(odw.getRelationattribut1().toLowerCase()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '").append(odw.getRelationattribut2().toLowerCase()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '-").append(odw.getRelationattribut2().toLowerCase()).append("'}\">").append(odw.getRelationattribut2().toLowerCase()).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('").append(odw.getRelationattribut2().toLowerCase()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('-").append(odw.getRelationattribut2().toLowerCase()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '").append(odw.getRelationattribut3().toLowerCase()).append("', 'descending': order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" == '-").append(odw.getRelationattribut3().toLowerCase()).append("'}\">").append(odw.getRelationattribut3().toLowerCase()).append("</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('").append(odw.getRelationattribut3().toLowerCase()).append("')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("").append(attr.getName()).append("('-").append(odw.getRelationattribut3().toLowerCase()).append("')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
                        }
                        html.append("\t\t\t\t\t\t\t\t\t</tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t<tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append("_").append(odw.getRelationattribut1().toLowerCase()).append("_upd\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(odw.getRelationattribut1().toLowerCase()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append("\"></th>").append("\n");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append("_").append(odw.getRelationattribut2().toLowerCase()).append("_upd\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(odw.getRelationattribut2().toLowerCase()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append("\"></th>").append("\n");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append("_").append(odw.getRelationattribut3().toLowerCase()).append("_upd\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(odw.getRelationattribut3().toLowerCase()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append("\"></th>").append("\n");
                        }
                        html.append("\t\t\t\t\t\t\t\t\t<tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t\t</thead>").append("\n");
                        html.append("\t\t\t\t\t\t\t<tbody>").append("\n");
                        
                        html.append("\t\t\t\t\t\t\t\t<tr ng-repeat=\"").append(attr.getName()).append(" in ").append(attr.getName().toUpperCase()).append("LIST ");
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            html.append("| filter: {").append(odw.getRelationattribut1().toLowerCase()).append(": filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append("} ");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            html.append("| filter: {").append(odw.getRelationattribut2().toLowerCase()).append(": filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append("} ");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            html.append("| filter: {").append(odw.getRelationattribut3().toLowerCase()).append(": filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append("} ");
                        }
                        html.append(" | orderBy: order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append("\">").append("\n");
                        
                        html.append("\t\t\t\t\t\t\t\t\t<td><input class=\"uk-checkbox\" type=\"checkbox\" aria-label=\"Checkbox\" ng-value=\"").append(attr.getName()).append(".id\" ng-checked=\"in").append(StringUtils.capitalise(attr.getName())).append("Selected(").append(attr.getName()).append(".id)\" ng-click=\"select").append(clazz.getName()).append("").append(attr.getName()).append("(").append(attr.getName()).append(".id)\"></td>").append("\n");
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t<td>{{").append(attr.getName()).append(".").append(odw.getRelationattribut1().toLowerCase()).append("}}</td>").append("\n");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t<td>{{").append(attr.getName()).append(".").append(odw.getRelationattribut2().toLowerCase()).append("}}</td>").append("\n");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            html.append("\t\t\t\t\t\t\t\t\t<td>{{").append(attr.getName()).append(".").append(odw.getRelationattribut3().toLowerCase()).append("}}</td>").append("\n");
                        }
                        html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
                        html.append("\t\t\t\t\t\t\t</tbody>").append("\n");
                        html.append("\t\t\t\t\t\t</table>").append("\n");
                        html.append("\t\t\t\t\t</div>").append("\n");
                        html.append("\t\t\t\t</div>").append("\n");
                    }
                    break;
                case "assetref":
                    html.append("\t\t\t\t<div class=\"uk-margin\">").append("\n");
                    html.append("\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-").append(attr.getName()).append("-upd\">").append(StringUtils.capitalise(attr.getName())).append("</label>").append("\n");
                    html.append("\t\t\t\t\t<div class=\"uk-overflow-auto uk-height-max-medium\">").append("\n");
                    html.append("\t\t\t\t\t\t<table class=\"uk-table uk-table-small uk-table-hover uk-table-middle uk-table-divider\" id=\"input-").append(attr.getName()).append("-upd\">").append("\n");
                    html.append("\t\t\t\t\t\t\t<thead>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\">Preview</th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th class=\"uk-table-small\">Name</th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th class=\"uk-width-small\">Beschreibung</th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th><input id=\"filter_media_").append(attr.getName()).append("_name_upd\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_media_").append(attr.getName()).append("_name_upd.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"name\" ng-model=\"filter_media_").append(attr.getName()).append("_name_upd\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<th><input id=\"filter_media_").append(attr.getName()).append("_description_upd\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_media_").append(attr.getName()).append("_description_upd.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"beschreibung\" ng-model=\"filter_media_").append(attr.getName()).append("_description_upd\"></th>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t</thead>").append("\n");
                    html.append("\t\t\t\t\t\t\t<tbody>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t<tr ng-repeat=\"media in MEDIALIST | filter: {name: filter_media_").append(attr.getName()).append("_name_upd} | filter: {description: filter_media_").append(attr.getName()).append("_description_upd} | filter:filterByMimetypes\">").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<td><input class=\"uk-checkbox\" type=\"checkbox\" aria-label=\"Checkbox\" ng-value=\"media.id\" ng-checked=\"in").append(StringUtils.capitalise(attr.getName())).append("Selected(media.id)\" ng-click=\"select").append(clazz.getName()).append("").append(attr.getName()).append("(media.id)\"></td>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<td><img class=\"uk-preserve-width\" src=\"GetAsset?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=&mediaid={{media.id}}\" width=\"40\" height=\"40\" alt=\"\"></td>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<td class=\"uk-table-link\">").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t\t<a class=\"uk-link-reset\" href=\"\">{{media.name}}</a>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t</td>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t\t<td class=\"uk-text-truncate\">{{media.description}}</td>").append("\n");
                    html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
                    html.append("\t\t\t\t\t\t\t</tbody>").append("\n");
                    html.append("\t\t\t\t\t\t</table>").append("\n");
                    html.append("\t\t\t\t\t</div>").append("\n");
                    html.append("\t\t\t\t</div>").append("\n");
                    break;
            }
        }

	html.append("\t\t\t\t<div class=\"uk-align-right\">").append("\n");
        if(isClassrefMandatory) {
            html.append("\t\t\t\t\t<button class=\"uk-button uk-button-primary\" type=\"button\" ng-click=\"update").append(clazz.getName()).append("(").append(clazz.getName()).append(".id)\" ng-disabled=\"").append(clazz.getName().toLowerCase()).append("FormUpd.$invalid || !isMediaSelected || inprogress || !isAnyCheckboxSelectedAssetRef || !isAnyCheckboxSelected\">Speichern <span ng-show=\"inprogress\" class=\"uk-spinner\" uk-icon=\"icon: cog\"></span></button>").append("\n");
        } else {
            html.append("\t\t\t\t\t<button class=\"uk-button uk-button-primary\" type=\"button\" ng-click=\"update").append(clazz.getName()).append("(").append(clazz.getName()).append(".id)\" ng-disabled=\"").append(clazz.getName().toLowerCase()).append("FormUpd.$invalid || !isMediaSelected || !isAnyCheckboxSelectedAssetRef || inprogress\">Speichern <span ng-show=\"inprogress\" class=\"uk-spinner\" uk-icon=\"icon: cog\"></span></button>").append("\n");
        }
    html.append("\t\t\t\t\t<button class=\"uk-button uk-button-secondary uk-modal-close\" type=\"button\" ng-disabled=\"inprogress\">Abbrechen</button>").append("\n");
	html.append("\t\t\t\t</div>").append("\n");
    html.append("\t\t\t</form>").append("\n");
	html.append("\t\t\t</div>").append("\n");
	html.append("\t\t</div>").append("\n");
	html.append("\t</div>").append("\n");
        html.append("\n");
        html.append("\t<div id=\"modal-").append(clazz.getName().toLowerCase()).append("-delete\" class=\"uk-modal-container uk-flex-top\" uk-modal>").append("\n");
        html.append("\t\t<div class=\"uk-modal-dialog uk-modal-header\">").append("\n");
	html.append("\t\t\t<button class=\"uk-modal-close-default\" type=\"button\" uk-close></button>").append("\n");
	html.append("\t\t\t<h2 class=\"uk-modal-title\">").append(clazz.getName()).append(" löschen</h2>").append("\n");
	html.append("\t\t\t<div class=\"uk-overflow-auto\" style=\"max-height: 600px;\">").append("\n");
	html.append("\t\t\t\t<p>{{").append(clazz.getName()).append(".id}}</p>").append("\n");
	html.append("\t\t\t\t<div class=\"uk-align-right\">").append("\n");
	html.append("\t\t\t\t\t<button class=\"uk-button uk-button-danger\" type=\"button\" ng-click=\"delete").append(clazz.getName()).append("(").append(clazz.getName()).append(")\" ng-disabled=\"inprogress\">Löschen <span ng-show=\"inprogress\" class=\"uk-spinner\" uk-icon=\"icon: cog\"></span></button>").append("\n");
	html.append("\t\t\t\t\t<button class=\"uk-button uk-button-secondary uk-modal-close\" type=\"button\" ng-disabled=\"inprogress\">Abbrechen</button>").append("\n");
	html.append("\t\t\t\t</div>").append("\n");
	html.append("\t\t\t</div>").append("\n");
	html.append("\t\t</div>").append("\n");
	html.append("\t</div>").append("\n");

        html.append("\t</li>").append("\n");
	html.append("\t<li>").append("\n");
	html.append("\t<div class=\"uk-container-large uk-align-center\">").append("\n");
	html.append("\t<div class=\"uk-margin-top\">").append("\n");
	html.append("\t<table class=\"uk-table uk-table-small uk-table-striped\">").append("\n");
	html.append("\t<caption>").append(clazz.getName()).append(" Liste <a href=\"\" class=\"uk-icon-button\" uk-icon=\"plus\" ng-click=\"add").append(clazz.getName()).append("ListModal()\" uk-tooltip=\"").append(clazz.getName()).append(" Liste hinzufügen\"></a></caption>").append("\n");
	html.append("\t<thead style=\"position: sticky !important;top: 0;background: white;z-index: 1;\">").append("\n");
	html.append("\t<tr>").append("\n");
	html.append("\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append("_list == 'id', 'descending': order_").append(clazz.getName().toLowerCase()).append("_list == '-id'}\">ID</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("List('id')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("List('-id')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
	html.append("\t<th><span ng-class=\"{'ascending': order_").append(clazz.getName().toLowerCase()).append("_list == 'name', 'descending': order_").append(clazz.getName().toLowerCase()).append("_list == '-name'}\">Name</span> <a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("List('name')\" uk-icon=\"chevron-up\"></a><a href=\"\" class=\"uk-icon\" ng-click=\"sort").append(clazz.getName()).append("List('-name')\" uk-icon=\"chevron-down\"></a></th>").append("\n");
	html.append("\t<th></th>").append("\n");
	html.append("\t<th class=\"uk-text-right\">Aktion</th>").append("\n");
	html.append("\t</tr>").append("\n");
	html.append("\t<tr>").append("\n");
	html.append("\t<th></th>").append("\n");
	html.append("\t<th><input id=\"filter_list_name\" class=\"uk-input uk-form-width-small\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_list.name.length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"Name\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_list.name\"></th>").append("\n");
	html.append("\t<th></th>").append("\n");
	html.append("\t<th></th>").append("\n");
	html.append("\t</tr>").append("\n");
	html.append("\t</thead>").append("\n");
	html.append("\t<tbody>").append("\n");
	html.append("\t<tr ng-repeat=\"").append(clazz.getName().toLowerCase()).append("Item in ").append(clazz.getName().toUpperCase()).append("LISTARRAY | filter: {name: filter_").append(clazz.getName().toLowerCase()).append("_list.name} | orderBy: order_").append(clazz.getName().toLowerCase()).append("_list\">").append("\n");
	html.append("\t<td>{{").append(clazz.getName().toLowerCase()).append("Item.id}}</td>").append("\n");
	html.append("\t<td>{{").append(clazz.getName().toLowerCase()).append("Item.name}}</td>").append("\n");
	html.append("\t<td>").append("\n");
	html.append("\t<table class=\"uk-table uk-table-small uk-table-striped\">").append("\n");
	html.append("\t<thead>").append("\n");
	html.append("\t<tr>").append("\n");
	html.append("\t<th>ID</th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t<th>").append(attr.getName()).append("</th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t<th>").append(attr.getName()).append("</th>").append("\n");
                        break;
                }
            }
        }
        
	html.append("\t</tr>").append("\n");
	html.append("\t</thead>").append("\n");
	html.append("\t<tbody>").append("\n");
	html.append("\t<tr ng-repeat=\"").append(clazz.getName().toLowerCase()).append(" in ").append(clazz.getName().toLowerCase()).append("Item.").append(clazz.getName().toLowerCase()).append("items\">").append("\n");
	html.append("\t<td>{{").append(clazz.getName().toLowerCase()).append(".id}}</td>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("}}</td>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append("}}</td>").append("\n");
                        break;
                }
            }
        }
        
	html.append("\t</tr>").append("\n");
	html.append("\t</tbody>").append("\n");
	html.append("\t</table>").append("\n");
	html.append("\t</td>").append("\n");
	html.append("\t<td class=\"uk-text-right\">").append("\n");
	html.append("\t<a href=\"\" class=\"uk-icon-button\" ng-click=\"update").append(clazz.getName()).append("ItemModal(").append(clazz.getName().toLowerCase()).append("Item.id)\" ng-show=\"!inprogress\" uk-icon=\"pencil\" uk-tooltip=\"").append(clazz.getName()).append(" Liste ändern\"></a>").append("\n");
	html.append("\t<a href=\"\" class=\"uk-icon-button\" ng-click=\"delete").append(clazz.getName()).append("ItemModal(").append(clazz.getName().toLowerCase()).append("Item.id)\" ng-show=\"!inprogress\" uk-icon=\"trash\" uk-tooltip=\"").append(clazz.getName()).append(" Liste löschen\"></a>").append("\n");
	html.append("\t</td>").append("\n");
	html.append("\t</tr>").append("\n");
	html.append("\t</tbody>").append("\n");
	html.append("\t</table>").append("\n");
	html.append("\t</div>").append("\n");
	html.append("\t</div>").append("\n");

	html.append("\t<div id=\"modal-").append(clazz.getName().toLowerCase()).append("-liste-add\" class=\"uk-modal-container uk-flex-top\" uk-modal>").append("\n");
	html.append("\t<div class=\"uk-modal-dialog uk-modal-header\">").append("\n");
	html.append("\t<button class=\"uk-modal-close-default\" type=\"button\" uk-close></button>").append("\n");
	html.append("\t<h2 class=\"uk-modal-title\">").append(clazz.getName()).append(" Liste hinzufügen</h2>").append("\n");
	html.append("\t<div class=\"uk-overflow-auto\" style=\"max-height: 600px;\">").append("\n");
	html.append("\t<div class=\"uk-margin\">").append("\n");
	html.append("\t<label class=\"uk-form-label\" for=\"input-name\">Name</label>").append("\n");
	html.append("\t<input id=\"input-name\" class=\"uk-input\" type=\"text\" placeholder=\"name\" aria-label=\"name\" ng-model=\"").append(clazz.getName().toLowerCase()).append("list_name\">").append("\n");

	html.append("\t<div class=\"uk-child-width-1-2 uk-margin\" uk-grid>").append("\n");
	html.append("\t<div>").append("\n");
	html.append("\t<p class=\"table-caption\">Zugeordnete Einträge</p>").append("\n");
	html.append("\t<div class=\"uk-overflow-auto\" style=\"height: 370px\">").append("\n");
	html.append("\t<table id=\"connected-artikel-group\" class=\"uk-table uk-table-hover uk-table-small uk-table-divider\">").append("\n");
	html.append("\t<thead uk-sticky class=\"table-head\">").append("\n");
	html.append("\t<tr class=\"table-row\">").append("\n");
	html.append("\t<th class=\"uk-table-shrink\"></th>").append("\n");
	html.append("\t<th class=\"uk-table-shrink\">ID</th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t<th class=\"uk-table-expand\">").append(attr.getName()).append("</th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t<th class=\"uk-table-expand\">").append(attr.getName()).append("</th>").append("\n");
                        break;
                }
            }
        }
	html.append("\t</tr>").append("\n");
	html.append("\t<tr class=\"table-row\">").append("\n");
	html.append("\t<th></th>").append("\n");
	html.append("\t<th></th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("").append(attr.getName().toLowerCase()).append("_connected\" class=\"uk-input\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append(".length != 0}\" type=\"number\" placeholder=\"\" aria-label=\"").append(attr.getName()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append("\"></th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("").append(attr.getName().toLowerCase()).append("_connected\" class=\"uk-input\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(attr.getName()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append("\"></th>").append("\n");
                        break;
                }
            }
        }
	html.append("\t</tr>").append("\n");
	html.append("\t</thead>").append("\n");
        
	html.append("\t\t\t\t\t\t\t\t<tbody>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tr ng-repeat=\"").append(clazz.getName().toLowerCase()).append(" in ").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED | filter : ").append(clazz.getName().toLowerCase()).append("connected_filter");
        html.append("\">").append("\n");
        
	html.append("\t\t\t\t\t\t\t\t<td><input class=\"uk-checkbox\" type=\"checkbox\" aria-label=\"Checkbox\" ng-click=\"disconnect").append(clazz.getName()).append("Item(").append(clazz.getName().toLowerCase()).append(".id)\"></td>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".id}}</td>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName().toLowerCase()).append("}}</td>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName().toLowerCase()).append("}}</td>").append("\n");
                        break;
                }
            }
        }
        
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</tbody>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</table>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<p class=\"table-caption\">Nicht zugeordnete Einträge</p>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-overflow-auto\" style=\"height: 370px\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<table id=\"disconnected-artikel-group\" class=\"uk-table uk-table-hover uk-table-small uk-table-divider\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<thead uk-sticky class=\"table-head\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tr class=\"table-row\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\">ID</th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t<th class=\"uk-table-expand\">").append(attr.getName()).append("</th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t<th class=\"uk-table-expand\">").append(attr.getName()).append("</th>").append("\n");
                        break;
                }
            }
        }
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tr class=\"table-row\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th></th>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th></th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("").append(attr.getName().toLowerCase()).append("_disconnected\" class=\"uk-input\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append(".length != 0}\" type=\"number\" placeholder=\"\" aria-label=\"").append(attr.getName()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append("\"></th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("").append(attr.getName().toLowerCase()).append("_disconnected\" class=\"uk-input\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(attr.getName()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append("\"></th>").append("\n");
                        break;
                }
            }
        }
        
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</thead>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tbody>").append("\n");
        html.append("\t\t\t\t\t\t\t\t<tr ng-repeat=\"").append(clazz.getName().toLowerCase()).append(" in ").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED | filter : ").append(clazz.getName().toLowerCase()).append("disconnected_filter");
        html.append("\">").append("\n");
        
	html.append("\t\t\t\t\t\t\t\t<td><input class=\"uk-checkbox\" type=\"checkbox\" aria-label=\"Checkbox\" ng-click=\"connect").append(clazz.getName()).append("Item(").append(clazz.getName().toLowerCase()).append(".id)\"></td>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".id}}</td>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName().toLowerCase()).append("}}</td>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName().toLowerCase()).append("}}</td>").append("\n");
                        break;
                }
            }
        }

	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</tbody>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</table>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");

	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-align-right\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<button class=\"uk-button uk-button-primary\" type=\"button\" ng-click=\"save").append(clazz.getName()).append("Item()\" ng-disabled=\"inprogress || ").append(clazz.getName().toLowerCase()).append("list_name.length == 0\">Speichern <span ng-show=\"inprogress\" class=\"uk-spinner\" uk-icon=\"icon: cog\"></span></button>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<button class=\"uk-button uk-button-secondary\" type=\"button\" ng-click=\"closeModal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-add')\" ng-disabled=\"inprogress\">Abbrechen</button>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");

	html.append("\t\t\t\t\t\t\t\t<div id=\"modal-").append(clazz.getName().toLowerCase()).append("-liste-update\" class=\"uk-modal-container uk-flex-top\" uk-modal>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-modal-dialog uk-modal-header\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<button class=\"uk-modal-close-default\" type=\"button\" uk-close></button>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<h2 class=\"uk-modal-title\">").append(clazz.getName()).append(" Liste ändern</h2>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-overflow-auto\" style=\"max-height: 600px;\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"input-name\">Name</label>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<input id=\"input-name\" class=\"uk-input\" type=\"text\" placeholder=\"name\" aria-label=\"name\" ng-model=\"").append(clazz.getName().toLowerCase()).append("list_name\">").append("\n");

	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-child-width-1-2 uk-margin\" uk-grid>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<p class=\"table-caption\">Zugeordnete Artikel</p>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-overflow-auto\" style=\"height: 370px\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<table id=\"connected-artikel-group\" class=\"uk-table uk-table-hover uk-table-small uk-table-divider\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<thead class=\"table-head\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tr  class=\"table-row\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\">ID</th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-expand\">").append(attr.getName()).append("</th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-expand\">").append(attr.getName()).append("</th>").append("\n");
                        break;
                }
            }
        }
        
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tr class=\"table-row\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th></th>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th></th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("").append(attr.getName().toLowerCase()).append("_connected\" class=\"uk-input\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append(".length != 0}\" type=\"number\" placeholder=\"\" aria-label=\"").append(attr.getName()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append("\"></th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("").append(attr.getName().toLowerCase()).append("_connected\" class=\"uk-input\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(attr.getName()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append("\"></th>").append("\n");
                        break;
                }
            }
        }
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</thead>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tbody>").append("\n");
        html.append("\t\t\t\t\t\t\t\t<tr ng-repeat=\"").append(clazz.getName().toLowerCase()).append(" in ").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED | filter : ").append(clazz.getName().toLowerCase()).append("connected_filter");
        html.append("\">").append("\n");
        
	html.append("\t\t\t\t\t\t\t\t<td><input class=\"uk-checkbox\" type=\"checkbox\" aria-label=\"Checkbox\" ng-click=\"disconnect").append(clazz.getName()).append("Item(").append(clazz.getName().toLowerCase()).append(".id)\"></td>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".id}}</td>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName().toLowerCase()).append("}}</td>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName().toLowerCase()).append("}}</td>").append("\n");
                        break;
                }
            }
        }
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</tbody>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</table>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");

	html.append("\t\t\t\t\t\t\t\t<div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<p class=\"table-caption\">Nicht zugeordnete Artikel</p>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-overflow-auto\" style=\"height: 370px\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<table id=\"disconnected-artikel-group\" class=\"uk-table uk-table-hover uk-table-small uk-table-divider\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<thead class=\"table-head\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tr class=\"table-row\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\"></th>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-shrink\">ID</th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-expand\">").append(attr.getName()).append("</th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t\t<th class=\"uk-table-expand\">").append(attr.getName()).append("</th>").append("\n");
                        break;
                }
            }
        }
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tr class=\"table-row\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th></th>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<th></th>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("").append(attr.getName().toLowerCase()).append("_disconnected\" class=\"uk-input\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append(".length != 0}\" type=\"number\" placeholder=\"\" aria-label=\"").append(attr.getName()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append("\"></th>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t<th><input id=\"filter_").append(clazz.getName().toLowerCase()).append("").append(attr.getName().toLowerCase()).append("_disconnected\" class=\"uk-input\" ng-class=\"{'uk-form-success': filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append(".length != 0}\" type=\"text\" placeholder=\"\" aria-label=\"").append(attr.getName()).append("\" ng-model=\"filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append("\"></th>").append("\n");
                        break;
                }
            }
        }
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</thead>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<tbody>").append("\n");
        html.append("\t\t\t\t\t\t\t\t<tr ng-repeat=\"").append(clazz.getName().toLowerCase()).append(" in ").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED | filter : ").append(clazz.getName().toLowerCase()).append("disconnected_filter");
        html.append("\">").append("\n");
        
	html.append("\t\t\t\t\t\t\t\t<td><input class=\"uk-checkbox\" type=\"checkbox\" aria-label=\"Checkbox\" ng-click=\"connect").append(clazz.getName()).append("Item(").append(clazz.getName().toLowerCase()).append(".id)\"></td>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".id}}</td>").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName().toLowerCase()).append("}}</td>").append("\n");
                        break;
                    case "datetime":
                        html.append("\t\t\t\t\t\t\t\t<td>{{").append(clazz.getName().toLowerCase()).append(".").append(attr.getName().toLowerCase()).append("}}</td>").append("\n");
                        break;
                }
            }
        }
	html.append("\t\t\t\t\t\t\t\t</tr>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</tbody>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</table>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-align-right\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t\t<button class=\"uk-button uk-button-primary\" type=\"button\" ng-click=\"update").append(clazz.getName()).append("Item(IDENTIFIER.id)\" ng-disabled=\"inprogress\">Speichern <span ng-show=\"inprogress\" class=\"uk-spinner\" uk-icon=\"icon: cog\"></span></button>").append("\n");
	html.append("\t\t\t\t\t\t\t\t\t<button class=\"uk-button uk-button-secondary\" type=\"button\" ng-click=\"closeModal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-update')\" ng-disabled=\"inprogress\">Abbrechen</button>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t<div id=\"modal-").append(clazz.getName().toLowerCase()).append("-liste-delete\" class=\"uk-modal-container uk-flex-top\" uk-modal>").append("\n");
	html.append("\t\t\t\t\t\t<div class=\"uk-modal-dialog uk-modal-header\">").append("\n");
	html.append("\t\t\t\t\t\t\t<button class=\"uk-modal-close-default\" type=\"button\" uk-close></button>").append("\n");
	html.append("\t\t\t\t\t\t\t<h2 class=\"uk-modal-title\">").append(clazz.getName()).append(" Liste löschen</h2>").append("\n");
	html.append("\t\t\t\t\t\t\t<div class=\"uk-overflow-auto\" style=\"max-height: 600px;\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t<p>{{IDENTIFIER.id}} - {{IDENTIFIER.name}}</p>").append("\n");
	html.append("\t\t\t\t\t\t\t\t<div class=\"uk-align-right\">").append("\n");
	html.append("\t\t\t\t\t\t\t\t\t<button class=\"uk-button uk-button-danger\" type=\"button\" ng-click=\"delete").append(clazz.getName()).append("Item(IDENTIFIER.id)\" ng-disabled=\"inprogress\">Löschen <span ng-show=\"inprogress\" class=\"uk-spinner\" uk-icon=\"icon: cog\"></span></button>").append("\n");
	html.append("\t\t\t\t\t\t\t\t\t<button class=\"uk-button uk-button-secondary\" type=\"button\" ng-click=\"closeModal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-delete')\" ng-disabled=\"inprogress\">Abbrechen</button>").append("\n");
	html.append("\t\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t\t\t</div>").append("\n");
	html.append("\t\t\t</li>").append("\n");
	html.append("\t\t</ul>").append("\n");
        
        html.append("\t</body>").append("\n");
        html.append("\t<script src=\"/resources/js/uikit.min.js\"></script>").append("\n");
        html.append("\t<script src=\"/resources/js/uikit-icons.min.js\"></script>").append("\n");
        html.append("</html>").append("\n");
        
        if (created) {
            template.setName("crud_" + clazz.getName());
            try {
                dummytemplate = cfTemplateService.findByName(template.getName());
                if (null == dummytemplate) {
                    template.setScriptlanguage(2);
                    template.setCheckedoutby(BigInteger.ZERO);
                    template.setContent(html.toString());
                    cfTemplateService.create(template);
                    templateutil.commit(template);
                } else {
                    dummytemplate.setContent(html.toString());
                    cfTemplateService.edit(dummytemplate);
                    templateutil.commit(dummytemplate);
                }
            } catch (Exception ex) {
                template.setScriptlanguage(2);
                template.setCheckedoutby(BigInteger.ZERO);
                template.setContent(html.toString());
                cfTemplateService.create(template);
                templateutil.commit(template);
            }
        }
        
        javascript.append("var crud").append(clazz.getName()).append(" = angular.module('crud").append(clazz.getName()).append("App', []);").append("\n");
        javascript.append("crud").append(clazz.getName()).append(".controller('Crud").append(clazz.getName()).append("Controller', function($scope, $http) {").append("\n");
        javascript.append("\tvar DateTime = luxon.DateTime;").append("\n");
        javascript.append("\t$scope.loading = false;").append("\n");
        javascript.append("\t$scope.inprogress = false;").append("\n");
        javascript.append("\t$scope.selectedIds = [];").append("\n");
        javascript.append("\t$scope.selectedIdsAssetRef = [];").append("\n");
        javascript.append("\t$scope.isAnyCheckboxSelectedAssetRef = true;").append("\n");

        javascript.append("\t$scope.isAnyCheckboxSelected = true;").append("\n");
        javascript.append("\t$scope.isMediaSelected = true;").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.selectedMimetypes = ['image/jpeg','image/png'];").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.filterByMimetypes = function(media) {").append("\n");
        javascript.append("\t\treturn ($scope.selectedMimetypes.indexOf(media.mimetype) !== -1);").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.MEDIALIST = [];").append("\n");
        javascript.append("\t$scope.").append(clazz.getName().toUpperCase()).append("LIST = [];").append("\n");
        
        javascript.append("\t$scope.filter_").append(clazz.getName().toLowerCase()).append(" = {};").append("\n");
        javascript.append("\t$scope.order_").append(clazz.getName().toLowerCase()).append(" = 'id';").append("\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "media":
                    if(attr.getMandatory()) {
                        if(attr.getDefault_val() != null) {
                            javascript.append("\t$scope.isMediaSelected = true;").append("\n");
                        } else {
                            javascript.append("\t$scope.isMediaSelected = false;").append("\n");
                        }
                    } else {
                        javascript.append("\t$scope.isMediaSelected = true;").append("\n");
                    }
                    javascript.append("\t$scope.media_").append(attr.getName()).append(" = {};").append("\n");
                    javascript.append("\t$scope.media_").append(attr.getName()).append(".").append(attr.getName()).append(" = 0").append("\n");
                    javascript.append("\t$scope.filter_media_").append(attr.getName()).append("_name_add = \"\";").append("\n");
                    javascript.append("\t$scope.filter_media_").append(attr.getName()).append("_description_add = \"\";").append("\n");
                    javascript.append("\t$scope.filter_media_").append(attr.getName()).append("_name_upd = \"\";").append("\n");
                    javascript.append("\t$scope.filter_media_").append(attr.getName()).append("_description_upd = \"\";").append("\n");

                    javascript.append("\n\t$scope.checkMedia = (id) => {");
                    javascript.append("\n\t\t$scope.isMediaSelected = true;").append("\n");
                    javascript.append("\t}\n");
                    break;
                case "classref":
                    if (0 == attr.getRelationtype()) {
                        javascript.append("\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" = {};").append("\n");
                        javascript.append("\t$scope.order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" = 'id';").append("\n");
                    }
                    break;
                case "assetref":
                        javascript.append("\t$scope.filter_media_").append(attr.getName()).append("_name_add = \"\";").append("\n");
                	javascript.append("\t$scope.filter_media_").append(attr.getName()).append("_description_add = \"\";").append("\n");
                        javascript.append("\t$scope.filter_media_").append(attr.getName()).append("_name_upd = \"\";").append("\n");
                	javascript.append("\t$scope.filter_media_").append(attr.getName()).append("_description_upd = \"\";").append("\n");
                    break;
            }
        }
        
        javascript.append("\n");
        javascript.append("\t$scope.").append(clazz.getName().toUpperCase()).append("LISTARRAY = [];").append("\n");
        javascript.append("\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_list = {};").append("\n");
        javascript.append("\t$scope.order_").append(clazz.getName().toLowerCase()).append("_list = 'id';").append("\n");
        javascript.append("\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_connected = {};").append("\n");
        javascript.append("\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_disconnected = {};").append("\n");
        javascript.append("\t$scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED = [];").append("\n");
        javascript.append("\t$scope.").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED = [];").append("\n");
        javascript.append("\n");

        javascript.append("\t$scope.getMediaList = function() {").append("\n");
        javascript.append("\t\t$http.get('/GetAssetList?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=').then(function (res) {").append("\n");
        javascript.append("\t\t\t$scope.MEDIALIST = res.data;").append("\n");
        javascript.append("\t\t});").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "classref":
                    javascript.append("\t$scope.").append(attr.getName().toUpperCase()).append("LIST = [];").append("\n");
                    javascript.append("\t$scope.get").append(StringUtils.capitalise(attr.getName())).append("list = function() {").append("\n");
                    javascript.append("\t\t$http.get('/OData/").append(attr.getRelationref().getName()).append("Set').then(function (res) {").append("\n");
                    javascript.append("\t\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST = res.data.value;").append("\n");
                    javascript.append("\t\t});").append("\n");
                    javascript.append("\t};").append("\n");
                    if (0 == attr.getRelationtype()) {
                        javascript.append("\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED = [];").append("\n");
                        javascript.append("\t$scope.in").append(StringUtils.capitalise(attr.getName())).append("Selected = function(id) {").append("\n");
                        javascript.append("\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED) {").append("\n");
                        javascript.append("\t\t\tif (element.id == id) {").append("\n");
                        javascript.append("\t\t\t\treturn true;").append("\n");
                        javascript.append("\t\t\t}").append("\n");
                        javascript.append("\t\t}").append("\n");
                        javascript.append("\t\treturn false;").append("\n");
                        javascript.append("\t};").append("\n");
                    }
                    break;
                case "assetref":
                    javascript.append("\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED = [];").append("\n");
                    javascript.append("\t$scope.in").append(StringUtils.capitalise(attr.getName())).append("Selected = function(id) {").append("\n");
                    javascript.append("\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED) {").append("\n");
                    javascript.append("\t\t\tif (element == id) {").append("\n");
                    javascript.append("\t\t\t\treturn true;").append("\n");
                    javascript.append("\t\t\t}").append("\n");
                    javascript.append("\t\t}").append("\n");
                    javascript.append("\t\treturn false;").append("\n");
                    javascript.append("\t};").append("\n");
                    break;
            }
        }
        
        javascript.append("\t$scope.init = function() {").append("\n");
        javascript.append("\t\t$scope.get").append(clazz.getName()).append("list();").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor()) {
                continue;
            }
            
            switch (attr.getAttributetype().getName()) {
                case "string":
                    javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(" = \"\";").append("\n");
                    break;
                case "integer":
                case "real":
                    javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(" = \"\";").append("\n");
                    break;
                case "datetime":
                    javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(" = \"\";").append("\n");
                    break;
                case "boolean":
                    javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(" = false;").append("\n");
                    break;
                case "media":
                    javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(" = 0;").append("\n");
                    break;
                case "classref":
                    javascript.append("\t\t$scope.get").append(StringUtils.capitalise(attr.getName())).append("list();").append("\n");
                    javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(" = \"\";").append("\n");
                    if (0 == attr.getRelationtype()) {
                        if ((null != odw.getRelationattribut1()) && (!odw.getRelationattribut1().isBlank())) {
                            javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut1()).append(" = \"\";").append("\n");
                        }
                        if ((null != odw.getRelationattribut2()) && (!odw.getRelationattribut2().isBlank())) {
                            javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut2()).append(" = \"\";").append("\n");
                        }
                        if ((null != odw.getRelationattribut3()) && (!odw.getRelationattribut3().isBlank())) {
                            javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(".").append(odw.getRelationattribut3()).append(" = \"\";").append("\n");
                        }
                    }
                    break;
            }
        }
        javascript.append("\t\t$scope.getMediaList();").append("\n");
        javascript.append("\t\t$scope.get").append(clazz.getName()).append("listArray();").append("\n");
        javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_list.name = \"\";").append("\n");
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                        javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append(" = \"\";").append("\n");
                        javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append(" = \"\";").append("\n");
                        break;
                    case "datetime":
                        javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append(" = \"\";").append("\n");
                        javascript.append("\t\t$scope.filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append(" = \"\";").append("\n");
                        break;
                }
            }
        }
        
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.init").append(clazz.getName()).append(" = function() {").append("\n");
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "string":
                case "text":
                case "htmltext":
                case "markdown":
                case "hashstring":
                    javascript.append("\t\t$scope.").append(attr.getName()).append(" = \"").append(attr.getDefault_val() != null ? attr.getDefault_val() : "").append("\";").append("\n");
                    break;
                case "integer":
                case "real":
                    javascript.append("\t\t$scope.").append(attr.getName()).append(" = ").append(attr.getDefault_val() != null ? attr.getDefault_val() + ";" : "0;").append("\n");
                    break;
                case "datetime":
                    javascript.append("\t\t$scope.").append(attr.getName()).append(" = \"").append(attr.getDefault_val() != null ? attr.getDefault_val() : "").append("\";").append("\n");
                    break;
                case "boolean":
                    javascript.append("\t\t$scope.").append(attr.getName()).append(" = ").append(attr.getDefault_val() != null ? attr.getDefault_val() + ";" : "false;").append("\n");
                    break;
                case "media":
                    javascript.append("\t\t$scope.").append(attr.getName()).append(" = 0").append("\n");
                    break;
                case "classref":
                    if (1 == attr.getRelationtype()) {
                        javascript.append("\t\t$scope.").append(attr.getName()).append(" = null;").append("\n");
                    }
                    break;
            }
        }
        
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.").append(clazz.getName().toLowerCase()).append("_filter = function(entry) {").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "string":
                case "datetime":
                    javascript.append("\tif (entry.").append(attr.getName()).append(" === null) {").append("\n");
                    javascript.append("\t\tentry.").append(attr.getName()).append(" = \"\";").append("\n");
                    javascript.append("\t}").append("\n");
                    break;
                case "integer":
                case "real":
                    javascript.append("\tif (entry.").append(attr.getName()).append(" === null) {").append("\n");
                    javascript.append("\t\tentry.").append(attr.getName()).append(" = \"\";").append("\n");
                    javascript.append("\t} else {").append("\n");
                    javascript.append("\t\tentry.").append(attr.getName()).append(" = entry.").append(attr.getName()).append(".toString();").append("\n");
                    javascript.append("\t}").append("\n");
                    break;
            }
        }
        
        javascript.append("\tif ( ");
        
        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "string":
                case "integer":
                case "real":
                case "datetime":
                    javascript.append("(entry.").append(attr.getName()).append(".toLowerCase().includes($scope.filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".toLowerCase())) && ");
                    break;
                case "classref":
                    if (1 == attr.getRelationtype()) {
                        javascript.append("(entry.").append(attr.getName()).append(".").append(odw.getRelationattribut1()).append(".toLowerCase().includes($scope.filter_").append(clazz.getName().toLowerCase()).append(".").append(attr.getName()).append(".toLowerCase())) && ");
                    }
                    break;
            }
        }
        javascript = javascript.delete(javascript.length()-4, javascript.length());
        javascript.append(") {").append("\n");
        javascript.append("\t    return true;").append("\n");
        javascript.append("\t} else {").append("\n");
        javascript.append("\treturn false;").append("\n");
        javascript.append("\t}").append("\n");
        javascript.append("\t};").append("\n");
        
        javascript.append("\n");
        javascript.append("\t$scope.").append(clazz.getName().toLowerCase()).append("connected_filter = function(entry) {").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "datetime":
                        javascript.append("\tif (entry.").append(attr.getName().toLowerCase()).append(" === null) {").append("\n");
                        javascript.append("\t\tentry.").append(attr.getName().toLowerCase()).append(" = \"\";").append("\n");
                        javascript.append("\t}").append("\n");
                        break;
                    case "integer":
                    case "real":
                        javascript.append("\tif (entry.").append(attr.getName().toLowerCase()).append(" === null) {").append("\n");
                        javascript.append("\t\tentry.").append(attr.getName().toLowerCase()).append(" = \"\";").append("\n");
                        javascript.append("\t} else {").append("\n");
                        javascript.append("\t\tentry.").append(attr.getName().toLowerCase()).append(" = entry.").append(attr.getName().toLowerCase()).append(".toString();").append("\n");
                        javascript.append("\t}").append("\n");
                        break;
                }
            }
        }
        
        javascript.append("\tif ( ");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                    case "datetime":
                        javascript.append("(entry.").append(attr.getName().toLowerCase()).append(".toLowerCase().includes($scope.filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName().toLowerCase()).append(".toLowerCase())) && ");
                        break;
                    case "classref":
                        if (1 == attr.getRelationtype()) {
                            javascript.append("(entry.").append(attr.getName()).append(".").append(odw.getRelationattribut1()).append(".toLowerCase().includes($scope.filter_").append(clazz.getName().toLowerCase()).append("_connected.").append(attr.getName()).append(".toLowerCase())) && ");
                        }
                        break;
                }
            }
        }
        javascript = javascript.delete(javascript.length()-4, javascript.length());
        javascript.append(") {").append("\n");
        javascript.append("\t    return true;").append("\n");
        javascript.append("\t} else {").append("\n");
        javascript.append("\treturn false;").append("\n");
        javascript.append("\t}").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.").append(clazz.getName().toLowerCase()).append("disconnected_filter = function(entry) {").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "datetime":
                        javascript.append("\tif (entry.").append(attr.getName().toLowerCase()).append(" === null) {").append("\n");
                        javascript.append("\t\tentry.").append(attr.getName().toLowerCase()).append(" = \"\";").append("\n");
                        javascript.append("\t}").append("\n");
                        break;
                    case "integer":
                    case "real":
                        javascript.append("\tif (entry.").append(attr.getName().toLowerCase()).append(" === null) {").append("\n");
                        javascript.append("\t\tentry.").append(attr.getName().toLowerCase()).append(" = \"\";").append("\n");
                        javascript.append("\t} else {").append("\n");
                        javascript.append("\t\tentry.").append(attr.getName().toLowerCase()).append(" = entry.").append(attr.getName().toLowerCase()).append(".toString();").append("\n");
                        javascript.append("\t}").append("\n");
                        break;
                }
            }
        }
        
        javascript.append("\tif ( ");
        
        for (ODataWizard odw : wizardlist) {
            if (odw.isTableheader()) {
                CfAttribut attr = odw.getAttribut();
                if (attr.getAutoincrementor()) {
                    continue;
                }
                switch (attr.getAttributetype().getName()) {
                    case "string":
                    case "integer":
                    case "real":
                    case "datetime":
                        javascript.append("(entry.").append(attr.getName().toLowerCase()).append(".toLowerCase().includes($scope.filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName().toLowerCase()).append(".toLowerCase())) && ");
                        break;
                    case "classref":
                        if (1 == attr.getRelationtype()) {
                            javascript.append("(entry.").append(attr.getName()).append(".").append(odw.getRelationattribut1()).append(".toLowerCase().includes($scope.filter_").append(clazz.getName().toLowerCase()).append("_disconnected.").append(attr.getName()).append(".toLowerCase())) && ");
                        }
                        break;
                }
            }
        }
        javascript = javascript.delete(javascript.length()-4, javascript.length());
        javascript.append(") {").append("\n");
        javascript.append("\t    return true;").append("\n");
        javascript.append("\t} else {").append("\n");
        javascript.append("\treturn false;").append("\n");
        javascript.append("\t}").append("\n");
        javascript.append("\t};").append("\n");
        
        
        javascript.append("\n");
        
        javascript.append("\t$scope.init").append(clazz.getName()).append("();").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.sort").append(clazz.getName()).append(" = function(field) {").append("\n");
        javascript.append("\t\t$scope.order_").append(clazz.getName().toLowerCase()).append(" = field;").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        
        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "classref":
                    if (0 == attr.getRelationtype()) {
                        javascript.append("\t$scope.sort").append(clazz.getName()).append("").append(attr.getName()).append(" = function(field) {").append("\n");
                        javascript.append("\t\t$scope.order_").append(clazz.getName().toLowerCase()).append("_").append(attr.getName()).append(" = field;").append("\n");
                        javascript.append("\t};").append("\n");
                    }
                    break;
            }
        }
        
        javascript.append("\t$scope.get").append(clazz.getName()).append("list = function() {").append("\n");
        javascript.append("\t\t$http.get('/OData/").append(clazz.getName()).append("Set').then(function (res) {").append("\n");
        javascript.append("\t\t\t$scope.").append(clazz.getName().toUpperCase()).append("LIST = res.data.value;").append("\n");
        javascript.append("\t\t});").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");

        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "classref":
                    if (0 == attr.getRelationtype()) {
                        if(attr.getMandatory()) {
                            javascript.append("\t\t$scope.isAnyCheckboxSelected = false").append("\n");
                        } else {
                            javascript.append("\t\t$scope.isAnyCheckboxSelected = true").append("\n");
                        }
                        javascript.append("\t$scope.select").append(clazz.getName()).append("").append(attr.getName()).append(" = function(id) {").append("\n");
                        javascript.append("\t\tselect = true;").append("\n");
                        javascript.append("\t\tconst index = $scope.selectedIds.indexOf(id);").append("\n");
                        javascript.append("\t\tif (index === -1) {").append("\n");
                        javascript.append("\t\t\t$scope.selectedIds.push(id);").append("\n");
                        javascript.append("\t\t} else {").append("\n");
                        javascript.append("\t\t$scope.selectedIds.splice(index, 1);").append("\n");
                        javascript.append("\t\t}").append("\n");
                        if(attr.getMandatory()) {
                            javascript.append("\t\t$scope.isAnyCheckboxSelected = $scope.selectedIds.length > 0;").append("\n");
                        }
                        javascript.append("\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED) {").append("\n");
                        javascript.append("\t\t\tif (element.id == id) {").append("\n");
                        javascript.append("\t\t\t\tselect = false;").append("\n");
                        javascript.append("\t\t\t\tconst index = getIndex($scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED, id);").append("\n");
                        javascript.append("\t\t\t\tif (index > -1) {").append("\n");
                        javascript.append("\t\t\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED.splice(index, 1);").append("\n");
                        javascript.append("\t\t\t\t}").append("\n");
                        javascript.append("\t\t\t\tbreak;").append("\n");
                        javascript.append("\t\t\t}").append("\n");
                        javascript.append("\t\t}").append("\n");
                        javascript.append("\t\tif (select) {").append("\n");
                        javascript.append("\t\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST) {").append("\n");
                        javascript.append("\t\t\t\tif (element.id == id) {").append("\n");
                        javascript.append("\t\t\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED.push(element);").append("\n");
                        javascript.append("\t\t\t\t\tbreak;").append("\n");
                        javascript.append("\t\t\t\t}").append("\n");
                        javascript.append("\t\t\t}").append("\n");
                        javascript.append("\t\t}").append("\n");
                        javascript.append("\t}").append("\n");
                    }
                    break;
                case "assetref":
                    if(attr.getMandatory()) {
                        javascript.append("\t\t$scope.isAnyCheckboxSelectedAssetRef = false").append("\n");
                    } else {
                        javascript.append("\t\t$scope.isAnyCheckboxSelectedAssetRef = true").append("\n");
                    }
                    javascript.append("\t$scope.select").append(clazz.getName()).append("").append(attr.getName()).append(" = function(id) {").append("\n");
                    javascript.append("\t\tselect = true;").append("\n");
                    javascript.append("\t\tconst index = $scope.selectedIdsAssetRef.indexOf(id);").append("\n");
                    javascript.append("\t\tif (index === -1) {").append("\n");
                    javascript.append("\t\t\t$scope.selectedIdsAssetRef.push(id);").append("\n");
                    javascript.append("\t\t} else {").append("\n");
                    javascript.append("\t\t$scope.selectedIdsAssetRef.splice(index, 1);").append("\n");
                    javascript.append("\t\t}").append("\n");
                    if(attr.getMandatory()) {
                        javascript.append("\t\t$scope.isAnyCheckboxSelectedAssetRef = $scope.selectedIdsAssetRef.length > 0;").append("\n");
                    }
                    javascript.append("\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED) {").append("\n");
                    javascript.append("\t\t\tif (element == id) {").append("\n");
                    javascript.append("\t\t\t\tselect = false;").append("\n");
                    javascript.append("\t\t\t\tconst index = getAssetIndex($scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED, id);").append("\n");
                    javascript.append("\t\t\t\tif (index > -1) {").append("\n");
                    javascript.append("\t\t\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED.splice(index, 1);").append("\n");
                    javascript.append("\t\t\t\t}").append("\n");
                    javascript.append("\t\t\t\tbreak;").append("\n");
                    javascript.append("\t\t\t}").append("\n");
                    javascript.append("\t\t}").append("\n");
                    javascript.append("\t\tif (select) {").append("\n");
                    javascript.append("\t\t\tfor (const element of $scope.MEDIALIST) {").append("\n");
                    javascript.append("\t\t\t\tif (element.id == id) {").append("\n");
                    javascript.append("\t\t\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED.push(element.id);").append("\n");
                    javascript.append("\t\t\t\t\tbreak;").append("\n");
                    javascript.append("\t\t\t\t}").append("\n");
                    javascript.append("\t\t\t}").append("\n");
                    javascript.append("\t\t}").append("\n");
                    javascript.append("\t}").append("\n");
                    break;
            }
        }
        
        javascript.append("\t$scope.add").append(clazz.getName()).append("Modal = function() {").append("\n");
        javascript.append("\t\t$scope.inprogress = false;").append("\n");
        javascript.append("\t\t$scope.init").append(clazz.getName()).append("();").append("\n");
        
        for (ODataWizard odw : wizardlist) {
            CfAttribut attr = odw.getAttribut();
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "classref":
                    if (0 == attr.getRelationtype()) {
                        javascript.append("\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED = [];").append("\n");
                    }
                    break;
                case "assetref":
                    javascript.append("\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED = [];").append("\n");
                    break;
            }
        }
        javascript.append("\t\tUIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-add').show();").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.save").append(clazz.getName()).append(" = function () {").append("\n");
        javascript.append("\t\t$scope.inprogress = true;").append("\n");
        javascript.append("\t\tvar ").append(clazz.getName()).append(" = new Object();").append("\n");
        javascript.append("\n");
        javascript.append("\t\t").append(clazz.getName()).append(".id = null;").append("\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor() || !attr.getExt_mutable()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "string":
                case "text":
                case "htmltext":
                case "markdown":
                case "hashstring":
                    javascript.append("\t\tif ($scope.").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.").append(attr.getName()).append(";").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "integer":
                    javascript.append("\t\tif ($scope.").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = parseInt($scope.").append(attr.getName()).append(");").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "real":
                    javascript.append("\t\tif ($scope.").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = parseFloat($scope.").append(attr.getName()).append(");").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "datetime":
                    javascript.append("\t\tif ($scope.").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("d = DateTime.fromISO($scope.").append(attr.getName()).append(");").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = d.toFormat('yyyy-MM-dd') + \"T\" + d.toFormat('hh:mm:ss');").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "boolean":
                    javascript.append("\t\tif ($scope.").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.").append(attr.getName()).append(";").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "media":
                    javascript.append("\t\tif ($scope.media_").append(attr.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.media_").append(attr.getName()).append(".").append(attr.getName()).append(";").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "classref":
                    if (1 == attr.getRelationtype()) {
                        javascript.append("\t\tif ($scope.").append(attr.getName()).append(" != null) {").append("\n");
                        javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.").append(attr.getName()).append(";").append("\n");
                        javascript.append("\t\t}").append("\n");
                    } else {
                        javascript.append("\t\tif ($scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED != null) {").append("\n");
                        javascript.append("\t\t\tvar ").append(attr.getName()).append("set_ref = [];").append("\n");
                        javascript.append("\t\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED) {").append("\n");
                        javascript.append("\t\t\t\tvar entry = new Object();").append("\n");
                        javascript.append("\t\t\t\tentry.id = element.id;").append("\n");
                        javascript.append("\t\t\t\t").append(attr.getName()).append("set_ref.push(entry);").append("\n");
                        javascript.append("\t\t\t}").append("\n");
                        javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = ").append(attr.getName()).append("set_ref;").append("\n");
                        javascript.append("\t\t}").append("\n");
                    }
                    break;
                case "assetref":
                    javascript.append("\t\tif ($scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED != null) {").append("\n");
                    javascript.append("\t\t\tvar ").append(attr.getName()).append("set_ref = [];").append("\n");
                    javascript.append("\t\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED) {").append("\n");
                    javascript.append("\t\t\t\t").append(attr.getName()).append("set_ref.push(element);").append("\n");
                    javascript.append("\t\t\t}").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = ").append(attr.getName()).append("set_ref;").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
            }
        }
        
        javascript.append("\t\tvar jsonString = JSON.stringify(").append(clazz.getName()).append(");").append("\n");
        javascript.append("\t\t$http.post('/OData/").append(clazz.getName()).append("', jsonString).then(function (res) {").append("\n");
        javascript.append("\t\t\tif (res.status === 201) {").append("\n");
        javascript.append("\t\t\t\t$scope.get").append(clazz.getName()).append("list();").append("\n");
        javascript.append("\t\t\t\t$scope.inprogress = false;").append("\n");
        javascript.append("\t\t\t\tUIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-add').hide();").append("\n");
        javascript.append("\t\t\t}").append("\n");
        javascript.append("\t\t}, function (res) {").append("\n");
        javascript.append("\t\t\tconsole.log(\"ERROR\");").append("\n");
        javascript.append("\t\t});").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.update").append(clazz.getName()).append("Modal = function(id) {").append("\n");
        javascript.append("\t\t$scope.inprogress = true;").append("\n");
        javascript.append("\t\tUIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-update').show();").append("\n");
        javascript.append("\t\t$http.get('/OData/").append(clazz.getName()).append("Set?$filter=id eq ' + id).then(function (res) {").append("\n");
        javascript.append("\t\t\t$scope.").append(clazz.getName()).append(" = res.data.value[0];").append("\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "hashstring":
                    javascript.append("\t\t\t$scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" = \"\";").append("\n");
                    break;
                case "classref":
                    if (0 == attr.getRelationtype()) {
                        javascript.append("\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED = [];").append("\n");
                        javascript.append("\t\t\tfor (const element of $scope.").append(clazz.getName()).append(".").append(attr.getName()).append(") {").append("\n");
                        javascript.append("\t\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED.push(element);").append("\n");
                        javascript.append("\t\t\t\t$scope.selectedIds.push(element.id)").append("\n");
                        javascript.append("\t\t\t}").append("\n");
                    }
                    break;
                case "assetref":
                    javascript.append("\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED = [];").append("\n");
                    javascript.append("\t\t\tfor (const element of $scope.").append(clazz.getName()).append(".").append(attr.getName()).append(") {").append("\n");
                    javascript.append("\t\t\t\t$scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED.push(element);").append("\n");
                    javascript.append("\t\t\t\t$scope.selectedIdsAssetRef.push(element)").append("\n");
                    javascript.append("\t\t\t}").append("\n");
                    break;
            }
        }
        javascript.append("\t\t\tif($scope.selectedIds.length > 0) {").append("\n");
        javascript.append("\t\t\t\t$scope.isAnyCheckboxSelected = true;").append("\n");
        javascript.append("\t\t\t}").append("\n");
        javascript.append("\t\t\tif($scope.selectedIdsAssetRef.length > 0) {").append("\n");
        javascript.append("\t\t\t\t$scope.isAnyCheckboxSelectedAssetRef = true;").append("\n");
        javascript.append("\t\t\t}").append("\n");

        javascript.append("\t\t\t$scope.inprogress = false;").append("\n");
        javascript.append("\t\t});").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.update").append(clazz.getName()).append("Instant = function(id, field, value) {").append("\n");
	javascript.append("\t\t$http.get('/OData/").append(clazz.getName()).append("Set?$filter=id eq ' + id).then(function (res) {").append("\n");
	javascript.append("\t\t\t$scope.").append(clazz.getName()).append(" = res.data.value[0];").append("\n");
        javascript.append("\t\t\t$scope.").append(clazz.getName()).append("[field] = value;").append("\n");
        javascript.append("\t\t\t$scope.update").append(clazz.getName()).append("(id);").append("\n");
	javascript.append("\t\t});").append("\n");
	javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.update").append(clazz.getName()).append(" = function (id) {").append("\n");
        javascript.append("\t\t$scope.inprogress = true;").append("\n");
        javascript.append("\t\tvar ").append(clazz.getName()).append(" = new Object();").append("\n");
        javascript.append("\n");
        javascript.append("\t\t").append(clazz.getName()).append(".id = id;").append("\n");
        
        for (CfAttribut attr : attributList) {
            if (attr.getAutoincrementor() || !attr.getExt_mutable()) {
                continue;
            }
            switch (attr.getAttributetype().getName()) {
                case "string":
                case "text":
                case "htmltext":
                case "markdown":
                    javascript.append("\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.").append(clazz.getName()).append(".").append(attr.getName()).append(";").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "hashstring":
                    javascript.append("\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" !== '') {").append("\n");
                    javascript.append("\t\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.").append(clazz.getName()).append(".").append(attr.getName()).append(";").append("\n");
                    javascript.append("\t\t\t}").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "integer":
                    javascript.append("\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = parseInt($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(");").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "real":
                    javascript.append("\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = parseFloat($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(");").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "datetime":
                    javascript.append("\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("d = DateTime.fromISO($scope.").append(attr.getName()).append(");").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = d.toFormat('yyyy-MM-dd') + \"T\" + d.toFormat('hh:mm:ss');").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "boolean":
                    javascript.append("\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.").append(clazz.getName()).append(".").append(attr.getName()).append(";").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "media":
                    javascript.append("\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.").append(clazz.getName()).append(".").append(attr.getName()).append(";").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
                case "classref":
                    if (1 == attr.getRelationtype()) {
                        javascript.append("\t\tif ($scope.").append(clazz.getName()).append(".").append(attr.getName()).append(" != null) {").append("\n");
                        javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = $scope.").append(clazz.getName()).append(".").append(attr.getName()).append(";").append("\n");
                        javascript.append("\t\t}").append("\n");
                    } else {
                        javascript.append("\t\tif ($scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED != null) {").append("\n");
                        javascript.append("\t\t\tvar ").append(attr.getName()).append("set_ref = [];").append("\n");
                        javascript.append("\t\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED) {").append("\n");
                        javascript.append("\t\t\t\tvar entry = new Object();").append("\n");
                        javascript.append("\t\t\t\tentry.id = element.id;").append("\n");
                        javascript.append("\t\t\t\t").append(attr.getName()).append("set_ref.push(entry);").append("\n");
                        javascript.append("\t\t\t}").append("\n");
                        javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = ").append(attr.getName()).append("set_ref;").append("\n");
                        javascript.append("\t\t}").append("\n");
                    }
                    break;
                case "assetref":
                    javascript.append("\t\tif ($scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED != null) {").append("\n");
                    javascript.append("\t\t\tvar ").append(attr.getName()).append("set_ref = [];").append("\n");
                    javascript.append("\t\t\tfor (const element of $scope.").append(attr.getName().toUpperCase()).append("LIST_SELECTED) {").append("\n");
                    javascript.append("\t\t\t\t").append(attr.getName()).append("set_ref.push(element);").append("\n");
                    javascript.append("\t\t\t}").append("\n");
                    javascript.append("\t\t\t").append(clazz.getName()).append(".").append(attr.getName()).append(" = ").append(attr.getName()).append("set_ref;").append("\n");
                    javascript.append("\t\t}").append("\n");
                    break;
            }
        }
        
        javascript.append("\t\tvar jsonString = JSON.stringify(").append(clazz.getName()).append(");").append("\n");
        javascript.append("\t\t$http.patch(\"/OData/").append(clazz.getName()).append(buildIdentifier(attributList, clazz.getName())).append("\", jsonString).then(function (res) {").append("\n");
        javascript.append("\t\t\tif (res.status === 200) {").append("\n");
        javascript.append("\t\t\t\t$scope.get").append(clazz.getName()).append("list();").append("\n");
        javascript.append("\t\t\t\t$scope.inprogress = false;").append("\n");
        javascript.append("\t\t\t\tUIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-update').hide();").append("\n");
        javascript.append("\t\t\t}").append("\n");
        javascript.append("\t\t}, function (res) {").append("\n");
        javascript.append("\t\t\tconsole.log(\"ERROR\");").append("\n");
        javascript.append("\t\t});").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.delete").append(clazz.getName()).append("Modal = function(id) {").append("\n");
        javascript.append("\t\t$scope.inprogress = true;").append("\n");
        javascript.append("\t\tUIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-delete').show();").append("\n");
        javascript.append("\t\t$http.get('/OData/").append(clazz.getName()).append("Set?$filter=id eq ' + id).then(function (res) {").append("\n");
        javascript.append("\t\t\t$scope.").append(clazz.getName()).append(" = res.data.value[0];").append("\n");
        javascript.append("\t\t\t$scope.inprogress = false;").append("\n");
        javascript.append("\t\t});").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.delete").append(clazz.getName()).append(" = function (").append(clazz.getName()).append(") {").append("\n");
        javascript.append("\t\t$scope.inprogress = true;").append("\n");
        javascript.append("\t\t$http.delete(\"/OData/").append(clazz.getName()).append(buildIdentifier(attributList, clazz.getName())).append("\").then(function (res) {").append("\n");
        javascript.append("\t\tif (res.status === 200) {").append("\n");
        javascript.append("\t\t\t$scope.get").append(clazz.getName()).append("list();").append("\n");
        javascript.append("\t\t\t$scope.get").append(clazz.getName()).append("listArray();").append("\n");
        javascript.append("\t\t\t$scope.inprogress = false;").append("\n");
        javascript.append("\t\t\tUIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-delete').hide();").append("\n");
        javascript.append("\t\t}").append("\n");
        javascript.append("\t\t}, function (res) {").append("\n");
        javascript.append("\t\t\tconsole.log(\"ERROR\");").append("\n");
        javascript.append("\t\t});").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.closeModal = function(modalelement) {").append("\n");
        javascript.append("\t\t$scope.inprogress = false;").append("\n");
        javascript.append("\t\tUIkit.modal(modalelement).hide();").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.get").append(clazz.getName()).append("listArray = function() {").append("\n");
        javascript.append("\t\t$http.get('/OData/").append(clazz.getName()).append("Lists').then(function (res) {").append("\n");
        javascript.append("\t\t\t$scope.").append(clazz.getName().toUpperCase()).append("LISTARRAY = res.data.value;").append("\n");
        javascript.append("\t\t\tfor (const element of $scope.").append(clazz.getName().toUpperCase()).append("LISTARRAY) {").append("\n");
        javascript.append("\t\t\t\t$http.get('/OData/'+element.name+'List').then(function (res) {").append("\n");
        javascript.append("\t\t\t\t\tif (res.status === 200) {").append("\n");
        javascript.append("\t\t\t\t\t\telement.").append(clazz.getName().toLowerCase()).append("items = res.data.value;").append("\n");
        javascript.append("\t\t\t\t\t}").append("\n");
        javascript.append("\t\t\t\t})").append("\n");
        javascript.append("\t\t\t}").append("\n");
        javascript.append("\t\t});").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.add").append(clazz.getName()).append("ListModal = function() {").append("\n");
        javascript.append("\t\t$scope.inprogress = false;").append("\n");
        javascript.append("\t\t$scope.init").append(clazz.getName()).append("();").append("\n");
        javascript.append("\t\t$scope.").append(clazz.getName().toLowerCase()).append("list_name = \"\";").append("\n");
        javascript.append("\t\t$scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED = [];").append("\n");
        javascript.append("\t\t$scope.").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED = [];").append("\n");
        javascript.append("\t\tfor (const element of $scope.").append(clazz.getName().toUpperCase()).append("LIST) {").append("\n");
        javascript.append("\t\t\t$scope.").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED.push(element);").append("\n");
        javascript.append("\t\t}").append("\n");
        javascript.append("\t\tUIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-add').show();").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\t$scope.connect").append(clazz.getName()).append("Item = function (id) {").append("\n");
        javascript.append("\t\tfor (const element of $scope.").append(clazz.getName().toUpperCase()).append("LIST) {").append("\n");
        javascript.append("\t\t\tif (element.id == id) {").append("\n");
        javascript.append("\t\t\t\t$scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED.push(element);").append("\n");
        javascript.append("\t\t\t\tbreak;").append("\n");
        javascript.append("\t\t\t}").append("\n");
        javascript.append("\t\t}").append("\n");
        javascript.append("\t\tconst index = getIndex($scope.").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED, id);").append("\n");
        javascript.append("\t\tif (index > -1) {").append("\n");
        javascript.append("\t\t\t$scope.").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED.splice(index, 1);").append("\n");
        javascript.append("\t\t}").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("$scope.disconnect").append(clazz.getName()).append("Item = function (id) {").append("\n");
        javascript.append("for (const element of $scope.").append(clazz.getName().toUpperCase()).append("LIST) {").append("\n");
        javascript.append("if (element.id == id) {").append("\n");
        javascript.append("$scope.").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED.push(element);").append("\n");
        javascript.append("break;").append("\n");
        javascript.append("}").append("\n");
        javascript.append("}").append("\n");
        javascript.append("const index = getIndex($scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED, id);").append("\n");
        javascript.append("if (index > -1) {").append("\n");
        javascript.append("$scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED.splice(index, 1);").append("\n");
        javascript.append("}").append("\n");
        javascript.append("};").append("\n");

        javascript.append("$scope.save").append(clazz.getName()).append("Item = function() {").append("\n");
        javascript.append("$scope.inprogress = true;").append("\n");
        javascript.append("var identifier = new Object();").append("\n");
        javascript.append("identifier.id = null;").append("\n");
        javascript.append("identifier.name = $scope.").append(clazz.getName().toLowerCase()).append("list_name;").append("\n");
        javascript.append("var ").append(clazz.getName().toLowerCase()).append("set_ref = [];").append("\n");
        javascript.append("for (const element of $scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED) {").append("\n");
        javascript.append("").append(clazz.getName().toLowerCase()).append("set_ref.push(element.id);").append("\n");
        javascript.append("}").append("\n");
        javascript.append("identifier.listset = ").append(clazz.getName().toLowerCase()).append("set_ref;").append("\n");
        javascript.append("var jsonString = JSON.stringify(identifier);").append("\n");
        javascript.append("$http.post('/OData/").append(clazz.getName()).append("Lists', jsonString).then(function (res) {").append("\n");
        javascript.append("if (res.status === 201) {").append("\n");
        javascript.append("$scope.get").append(clazz.getName()).append("listArray();").append("\n");
        javascript.append("$scope.inprogress = false;").append("\n");
        javascript.append("UIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-add').hide();").append("\n");
        javascript.append("}").append("\n");
        javascript.append("}, function (res) {").append("\n");
        javascript.append("console.log(\"ERROR\");").append("\n");
        javascript.append("});").append("\n");
        javascript.append("};").append("\n");

        javascript.append("$scope.update").append(clazz.getName()).append("ItemModal = function (id) {").append("\n");
        javascript.append("$scope.inprogress = true;").append("\n");
        javascript.append("UIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-update').show();").append("\n");
        javascript.append("$scope.").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED = [];").append("\n");
        javascript.append("$http.get('/OData/").append(clazz.getName()).append("Lists?$filter=id eq ' + id ).then(function (res) {").append("\n");
        javascript.append("$scope.IDENTIFIER = res.data.value[0];").append("\n");
        javascript.append("$scope.").append(clazz.getName().toLowerCase()).append("list_id = $scope.IDENTIFIER.id;").append("\n");
        javascript.append("$scope.").append(clazz.getName().toLowerCase()).append("list_name = $scope.IDENTIFIER.name;").append("\n");

        javascript.append("$http.get('/OData/' + $scope.").append(clazz.getName().toLowerCase()).append("list_name + 'List').then(function (res2) {").append("\n");
        javascript.append("$scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED = res2.data.value;").append("\n");
        javascript.append("for (const element of $scope.").append(clazz.getName().toUpperCase()).append("LIST) {").append("\n");
        javascript.append("found = false;").append("\n");
        javascript.append("for (const element2 of $scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED) {").append("\n");
        javascript.append("if (element2.id === element.id) {").append("\n");
        javascript.append("found = true;").append("\n");
        javascript.append("break;").append("\n");
        javascript.append("}").append("\n");
        javascript.append("}").append("\n");
        javascript.append("if (!found) {").append("\n");
        javascript.append("$scope.").append(clazz.getName().toUpperCase()).append("LIST_DISCONNECTED.push(element);").append("\n");
        javascript.append("}").append("\n");
        javascript.append("}").append("\n");
        javascript.append("});").append("\n");
        javascript.append("$scope.inprogress = false;").append("\n");
        javascript.append("});").append("\n");
        javascript.append("};").append("\n");

        javascript.append("$scope.update").append(clazz.getName()).append("Item = function(id) {").append("\n");
        javascript.append("$scope.inprogress = true;").append("\n");
        javascript.append("var identifier = new Object();").append("\n");
        javascript.append("identifier.id = id;").append("\n");
        javascript.append("identifier.name = $scope.").append(clazz.getName().toLowerCase()).append("list_name;").append("\n");
        javascript.append("var ").append(clazz.getName().toLowerCase()).append("set_ref = [];").append("\n");
        javascript.append("for (const element of $scope.").append(clazz.getName().toUpperCase()).append("LIST_CONNECTED) {").append("\n");
        javascript.append("").append(clazz.getName().toLowerCase()).append("set_ref.push(element.id);").append("\n");
        javascript.append("}").append("\n");
        javascript.append("identifier.listset = ").append(clazz.getName().toLowerCase()).append("set_ref;").append("\n");
        javascript.append("var jsonString = JSON.stringify(identifier);").append("\n");
        javascript.append("$http.patch('/OData/").append(clazz.getName()).append("Lists(' + id + ')', jsonString).then(function (res_patch) {").append("\n");
        javascript.append("if (res_patch.status === 200) {").append("\n");
        javascript.append("$scope.get").append(clazz.getName()).append("listArray();").append("\n");
        javascript.append("$scope.inprogress = false;").append("\n");
        javascript.append("UIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-update').hide();").append("\n");
        javascript.append("}").append("\n");
        javascript.append("}, function (res) {").append("\n");
        javascript.append("console.log(\"ERROR\");").append("\n");
        javascript.append("});").append("\n");
        javascript.append("};").append("\n");

        javascript.append("$scope.delete").append(clazz.getName()).append("ItemModal = function (id) {").append("\n");
        javascript.append("$scope.inprogress = true;").append("\n");
        javascript.append("UIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-delete').show();").append("\n");
        javascript.append("$http.get('/OData/").append(clazz.getName()).append("Lists?$filter=id eq ' + id).then(function (res) {").append("\n");
        javascript.append("$scope.IDENTIFIER = res.data.value[0];").append("\n");
        javascript.append("$scope.inprogress = false;").append("\n");
        javascript.append("});").append("\n");
        javascript.append("};").append("\n");

        javascript.append("$scope.delete").append(clazz.getName()).append("Item = function (id) {").append("\n");
        javascript.append("$scope.inprogress = true;").append("\n");
        javascript.append("$http.delete('/OData/").append(clazz.getName()).append("Lists(' + id + ')').then(function (res) {").append("\n");
        javascript.append("if (res.status === 200) {").append("\n");
        javascript.append("$scope.get").append(clazz.getName()).append("listArray();").append("\n");
        javascript.append("$scope.inprogress = false;").append("\n");
        javascript.append("UIkit.modal('#modal-").append(clazz.getName().toLowerCase()).append("-liste-delete').hide();").append("\n");
        javascript.append("}").append("\n");
        javascript.append("}, function (res) {").append("\n");
        javascript.append("console.log(\"ERROR\");").append("\n");
        javascript.append("});").append("\n");
        javascript.append("};").append("\n");
        
        javascript.append("\n");
        javascript.append("\tgetIndex = function(array, id) {").append("\n");
        javascript.append("\t\tidx = 0;").append("\n");
        javascript.append("\t\tfor (const element of array) {").append("\n");
        javascript.append("\t\t\tif (element.id == id) {").append("\n");
        javascript.append("\t\t\t\treturn idx;").append("\n");
        javascript.append("\t\t\t}").append("\n");
        javascript.append("\t\t\tidx++;").append("\n");
        javascript.append("\t\t}").append("\n");
        javascript.append("\t\treturn -1;").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("\n");
        javascript.append("\tgetAssetIndex = function(array, id) {").append("\n");
        javascript.append("\t\tidx = 0;").append("\n");
        javascript.append("\t\tfor (const element of array) {").append("\n");
        javascript.append("\t\t\tif (element == id) {").append("\n");
        javascript.append("\t\t\t\treturn idx;").append("\n");
        javascript.append("\t\t\t}").append("\n");
        javascript.append("\t\t\tidx++;").append("\n");
        javascript.append("\t\t}").append("\n");
        javascript.append("\t\treturn -1;").append("\n");
        javascript.append("\t};").append("\n");
        javascript.append("});").append("\n");
        
        if (created) {
            js.setName("crud_" + clazz.getName().toLowerCase());
            try {
                CfJavascript dummyjs = cfJavaScriptService.findByName(js.getName());
                if (null == dummyjs) {
                    js.setCheckedoutby(BigInteger.ZERO);
                    js.setContent(javascript.toString());
                    cfJavaScriptService.create(js);
                    javascriptutil.commit(js);
                    javascriptutil.writeStaticJS(js.getName(), js.getContent(), "js");
                } else {
                    dummyjs.setContent(javascript.toString());
                    cfJavaScriptService.edit(dummyjs);
                    javascriptutil.commit(dummyjs);
                    javascriptutil.writeStaticJS(dummyjs.getName(), dummyjs.getContent(), "js");
                }
            } catch (Exception ex) {
                js.setCheckedoutby(BigInteger.ZERO);
                js.setContent(javascript.toString());
                cfJavaScriptService.create(js);
                javascriptutil.commit(js);
                javascriptutil.writeStaticJS(js.getName(), js.getContent(), "js");
            }

            site.setName("crud_" + clazz.getName().toLowerCase());
            CfSite dummysite = cfSiteService.findByName(site.getName());
            if (null == dummysite) {
                site.setCharacterencoding("UTF-8");
                site.setHitcounter(BigInteger.ZERO);
                site.setTitle("");
                site.setContenttype("text/html");
                site.setSearchrelevant(false);
                site.setHtmlcompression(0);
                site.setGzip(0);
                site.setLocale("de");
                site.setDescription("Automatic generation");
                site.setAliaspath(site.getName());
                CfSite parent = cfSiteService.findByName("crud");
                if (null != parent) {
                    site.setParentref(parent);
                } else {
                    site.setParentref(null);
                }
                if (null != template.getContent()) {
                    site.setTemplateref(template);
                } else {
                    site.setTemplateref(dummytemplate);
                }
                site.setShorturl(siteutil.generateShorturl());
                site.setLoginsite("");
                site.setTestparams("");
                cfSiteService.create(site);
            }
            sitetree.loadTree();
            return "OData Form template generated";
        } else {
            return "OData Form template NOT generated - Relation refs not set";
        }
    }

    public void generateLogin(CfClass clazz, String idField, String passwordField, String authField, String adminMail) {
        List<CfAttribut> attributList = cfattributService.findByClassref(clazz);

        StringBuilder html = new StringBuilder();
        CfTemplate template = new CfTemplate();
        CfTemplate dummytemplate = null;
        CfSite site = new CfSite();

        html.append("<!doctype html>").append("\n");
        html.append("<html ng-app=\"dummyApp\">").append("\n");
        html.append("\t<head>").append("\n");
        html.append("\t\t<!-- CSS FILES -->").append("\n");
        html.append("\t\t<link rel=\"stylesheet\" href=\"/resources/css/bootstrap5.css\">").append("\n");
        html.append("\t\t<link rel=\"stylesheet\" href=\"/resources/css/uikit.css\">").append("\n");
        html.append("\t\t<link rel=\"stylesheet\" href=\"/resources/css/toaster.css\">").append("\n");

        html.append("\t\t<script src=\"/resources/js/angular.js\"></script>").append("\n");
        html.append("\t\t<script src=\"/resources/js/angular-sanitize.js\"></script>").append("\n");
        html.append("\t\t<script src=\"/resources/js/ui-bootstrap.js\"></script>").append("\n");
        html.append("\t\t<script src=\"/resources/js/ui-bootstrap-tpls.js\"></script>").append("\n");
        html.append("\t\t<script src=\"/resources/js/toaster.js\"></script>").append("\n");


        html.append("\t\t<style> .width-100 { width: 100%;} .upper-first {text-transform: capitalize;} .width-50 {width: 50%;} .flex-middle {display: flex;justify-content: center;} .password-forgot {font-size: 14px;text-decoration: underline;color: blue;cursor: pointer;} .password-info-text {font-size: 12px;color: #eb2828;margin: 0 !important;}</style>\n").append("\n");


        html.append("\t\t<body ng-controller=\"DummyComponent\">").append("\n");
        html.append("\t\t\t<div class=\"uk-container uk-container-small\">").append("\n");
        html.append("\t\t\t\t<p class=\"flex-middle\">Diese Seite ist zugriffsbeschränkt. Bitte loggen Sie sich ein.</p>").append("\n");
        html.append("\t\t\t</div>").append("\n");
        html.append("\t\t\t<div ng-if=\"!forgotPassword && !createUserB && !resetPasswordEmail && !deleteEmail\" class=\"flex-middle uk-container uk-container-small uk-margin uk-form-stacked\">").append("\n");
        html.append("\t\t\t\t<div class=\"width-50\">").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"form-stacked-email\">E-Mail</label>").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: user\"></span>").append("\n");
        html.append("\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-class=\"{'uk-form-danger': warning}\" id=\"loginEmail\" ng-model=\"email\" type=\"text\" placeholder=\"\">").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"form-stacked-password\">Password</label>").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: unlock\"></span>").append("\n");
        html.append("\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-class=\"{'uk-form-danger': warning}\" id=\"loginPassword\" ng-model=\"password\" type=\"password\" placeholder=\"\" onkeydown = \"if (event.keyCode == 13) document.getElementById('login-button').click()\">").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t<p class=\"width-100 password-forgot\" ng-click=\"clickForgotPassword()\">Passwort vergessen?</p>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t<p class=\"width-100 password-forgot\" ng-click=\"changeDelete()\">Account löschen</p>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\" style=\"margin-bottom: 0;\">").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-form-controls uk-flex\">").append("\n");
        html.append("\t\t\t\t\t\t\t<button class=\"width-100 uk-button uk-button-primary\" id=\"login-button\" ng-click=\"login('${metainfo.referrer}')\">Login</button>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t<div class=\"uk-form-controls uk-flex\">").append("\n");
        html.append("\t\t\t\t\t\t<button class=\"width-100 uk-button uk-button-primary\" ng-click=\"changeCreate()\">Benutzer erstellen</button>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t</div>").append("\n");
        html.append("\t\t\t</div>").append("\n");
        html.append("\t\t\t</div>").append("\n");

        // Ändern <div class="width-50">
        html.append("\t\t\t\t<div ng-if=\"forgotPassword && !createUserB && !resetPasswordEmail\" class=\"flex-middle uk-container uk-container-small uk-margin uk-form-stacked\">").append("\n");
        html.append("\t\t\t\t\t<div class=\"width-50\">").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"form-stacked-email\">E-Mail</label>").append("\n");
        html.append("\t\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: user\"></span>").append("\n");
        html.append("\t\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-class=\"{'uk-form-danger': warning}\" id=\"resetEmail\" ng-model=\"email\" type=\"text\" placeholder=\"\" onkeydown = \"if (event.keyCode == 13) document.getElementById('reset-button').click()\">").append("\n");
        html.append("\t\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t\t<div class=\"uk-form-controls uk-flex\">").append("\n");
        html.append("\t\t\t\t\t\t\t\t<button class=\"width-100 uk-button uk-button-primary\" id=\"reset-button\" ng-click=\"resetPassword()\">Zurücksetzen</button>").append("\n");
        html.append("\t\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t<div ng-if=\"deleteEmail\" class=\"flex-middle uk-container uk-container-small uk-margin uk-form-stacked\">").append("\n");
        html.append("\t\t\t\t\t<div class=\"width-50\">").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"form-stacked-email\">E-Mail</label>").append("\n");
        html.append("\t\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: user\"></span>").append("\n");
        html.append("\t\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-class=\"{'uk-form-danger': warning}\" id=\"deleteEmail\" ng-model=\"email\" type=\"text\" placeholder=\"\">").append("\n");
        html.append("\t\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t\t<div class=\"uk-form-controls uk-flex\">").append("\n");
        html.append("\t\t\t\t\t\t\t\t<button class=\"width-100 uk-button uk-button-primary\" ng-click=\"deleteAccount()\">Löschen</button>").append("\n");
        html.append("\t\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t</div>").append("\n");

        html.append("\t\t\t<div ng-if=\"resetPasswordEmail\" class=\"flex-middle uk-container uk-container-small uk-margin uk-form-stacked\">").append("\n");
        html.append("\t\t\t\t<div class=\"width-50\">").append("\n");
        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"form-stacked-email\">Neues Passwort</label>").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: user\"></span>").append("\n");
        html.append("\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-class=\"{'uk-form-danger': warning}\" id=\"newPassword\" type=\"password\" placeholder=\"\">").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<label class=\"uk-form-label\" for=\"form-stacked-email\">Neues Passwort wiederholen</label>").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: user\"></span>").append("\n");
        html.append("\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-class=\"{'uk-form-danger': warning}\" id=\"newPasswordRepeat\" type=\"password\" placeholder=\"\" onkeydown = \"if (event.keyCode == 13) document.getElementById('new-button').click()\">").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-form-controls uk-flex\">").append("\n");
        html.append("\t\t\t\t\t\t\t<button id=\"new-button\" class=\"width-100 uk-button uk-button-primary\" ng-click=\"setNewPassword()\">Neues Passwort setzen</button>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t</div>").append("\n");
        html.append("\t\t\t</div>").append("\n");

        html.append("\t\t\t<div ng-if=\"createUserB\" class=\"flex-middle uk-container uk-container-small uk-margin uk-form-stacked\">").append("\n");
        html.append("\t\t\t\t<div class=\"width-50\">").append("\n");

        for (CfAttribut cfa : attributList) {
            if(!Objects.equals(cfa.getName(), authField) && !Objects.equals(cfa.getName(), passwordField) && !Objects.equals(cfa.getName(), "id") && cfa.getExt_mutable()) {
                html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
                html.append("\t\t\t\t\t\t<label class=\"uk-form-label upper-first\" for=\"form-stacked-email\">").append(cfa.getName()).append("</label>\n");
                html.append("\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
                html.append("\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: user\"></span>").append("\n");
                html.append("\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-class=\"{'uk-form-danger': warning}\" id=\"create").append(cfa.getName()).append("\" ng-model=\"").append(cfa.getName()).append("\" type=\"text\" placeholder=\"\">").append("\n");
                html.append("\t\t\t\t\t\t</div>").append("\n");
                html.append("\t\t\t\t\t</div>").append("\n");
            }
        }

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<label class=\"uk-form-label upper-first\" for=\"form-stacked-email\">").append(passwordField).append("</label>\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: user\"></span>").append("\n");
        html.append("\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-change=\"checkPassword()\" ng-class=\"{'uk-form-danger': warning}\" id=\"create").append(passwordField).append("\" ng-model=\"").append(passwordField).append("\" type=\"password\" placeholder=\"\">").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<label class=\"uk-form-label upper-first\" for=\"form-stacked-email\">").append(passwordField).append(" wiederholen</label>\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-inline width-100 uk-form-width-large uk-form-controls\">").append("\n");
        html.append("\t\t\t\t\t\t\t<span class=\"uk-form-icon\" uk-icon=\"icon: user\"></span>").append("\n");
        html.append("\t\t\t\t\t\t\t<input class=\"uk-input width-100\" ng-class=\"{'uk-form-danger': warning}\" id=\"create").append(passwordField).append("Repeat\" ng-model=\"").append(passwordField).append("Again\" type=\"password\" placeholder=\"\" onkeydown = \"if (event.keyCode == 13) document.getElementById('createUserButton').click()\">").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");

        html.append("\t\t\t\t\t<div class=\"uk-margin\">").append("\n");
        html.append("\t\t\t\t\t\t<div class=\"uk-form-controls uk-flex\">").append("\n");
        html.append("\t\t\t\t\t\t\t<button class=\"width-100 uk-button uk-button-primary\" disabled=\"true\" id=\"createUserButton\" ng-click=\"createUser()\">Erstellen</button>").append("\n");
        html.append("\t\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t\t</div>").append("\n");
        html.append("\t\t\t\t</div>").append("\n");
        html.append("\t\t\t</div>").append("\n");
        html.append("\t\t</body>").append("\n");

        html.append("\t\t<!-- JS FILES -->").append("\n");
        html.append("\t\t<script src=\"/resources/js/uikit.min.js\"></script>").append("\n");
        html.append("\t\t<script src=\"/resources/js/uikit-icons.min.js\"></script>").append("\n");

        // Javascript Login Logik
        html.append("\t\t<script>").append("\n");
        html.append("\t\t\tvar dummyapp = angular.module('dummyApp', ['ngSanitize', 'ui.bootstrap', 'toastr']);").append("\n");
        html.append("\t\t\tdummyapp.controller(\"DummyComponent\", function ($scope, $uibModal, $http, toastr) {").append("\n");

        html.append("\t\t\t\tconst className = \"").append(clazz.getName()).append("\";\n");
        html.append("\t\t\t\tconst passwordField = \"").append(passwordField).append("\";\n");
        html.append("\t\t\t\tconst idField = \"").append(idField).append("\";\n");
        html.append("\t\t\t\tconst authfield = \"").append(authField).append("\";\n");
        html.append("\t\t\t\tconst adminMail = \"").append(adminMail).append("\";").append("\n");
        html.append("\t\t\t\tconst loginSite = \"").append("login_").append(clazz.getName().toLowerCase()).append("\";\n");
        html.append("\t\t\t\tconst passwordCheck = false;").append("\n\n");

        html.append("\t\t\t\t$scope.forgotPassword = false;").append("\n");
        html.append("\t\t\t\t$scope.createUserB = false;").append("\n");
        html.append("\t\t\t\t$scope.deleteEmail = false;").append("\n");
        html.append("\t\t\t\t$scope.email = \"\"").append("\n");
        html.append("\t\t\t\t$scope.password = \"\"").append("\n");
        html.append("\t\t\t\t$scope.password_again = \"\"").append("\n");
        html.append("\t\t\t\t$scope.resetPasswordEmail = false;").append("\n");
        html.append("\t\t\t\t$scope.classDataList = []").append("\n\n");

        html.append("\t\t\t\tconst queryString = window.location.search;").append("\n");
        html.append("\t\t\t\tconst urlParams = new URLSearchParams(queryString);").append("\n");
        html.append("\t\t\t\tconst token = urlParams.get('token');").append("\n\n");

        html.append("\t\t\t\tif(token) {").append("\n");
        html.append("\t\t\t\t\t$scope.resetPasswordEmail = true;").append("\n");
        html.append("\t\t\t\t}").append("\n\n");

        html.append("\t\t\t\t$scope.clickForgotPassword = () => {").append("\n");
        html.append("\t\t\t\t\t$scope.forgotPassword = !$scope.forgotPassword").append("\n");
        html.append("\t\t\t\t}").append("\n\n");

        html.append("\t\t\t\t$scope.changeCreate = () => {").append("\n");
        html.append("\t\t\t\t\t$scope.createUserB = !$scope.createUserB;").append("\n");
        html.append("\t\t\t\t}").append("\n\n");

        html.append("\t\t\t\t$scope.changeDelete = () => {").append("\n");
        html.append("\t\t\t\t\t$scope.deleteEmail = !$scope.deleteEmail;").append("\n");
        html.append("\t\t\t\t}").append("\n\n");

        //SetNewPassword
        html.append("\t\t\t\t$scope.setNewPassword = () => {").append("\n");
        html.append("\t\t\t\t\tvar newPassword = document.getElementById(\"newPassword\").value;").append("\n");
        html.append("\t\t\t\t\tvar newPasswordRepeat = document.getElementById(\"newPasswordRepeat\").value;").append("\n");
        html.append("\t\t\t\t\tif(newPassword == newPasswordRepeat) {").append("\n");
        html.append("\t\t\t\t\t\t$scope.warning = false;").append("\n");
        html.append("\t\t\t\t\t\tif(isTokenValid(token)) {").append("\n");
        html.append("\t\t\t\t\t\t\tvar data = decodeToken(token)").append("\n");
        html.append("\t\t\t\t\t\t\tvar userToChange = $scope.classDataList.find(user => user.").append(idField).append(" == data.").append(idField).append(");\n");
        html.append("\t\t\t\t\t\t\tvar updateData = {").append("\n");

        for (CfAttribut cfa : attributList) {
            if(!Objects.equals(cfa.getName(), passwordField)) {
                html.append("\t\t\t\t\t\t\t\t").append(cfa.getName()).append(": userToChange.").append(cfa.getName()).append(",\n");
            }
        }

        html.append("\t\t\t\t\t\t\t\t").append(passwordField).append(": newPassword").append("\n");
        html.append("\t\t\t\t\t\t\t}").append("\n");
        StringBuilder patchParameters = new StringBuilder("\"OData/\"+className+\"(");
        for(CfAttribut cfa: attributList) {
            if(cfa.getIdentity() && !Objects.equals(cfa.getAttributetype().getName(), "string")) {
                patchParameters.append(cfa.getName()).append("=\"").append(" + userToChange.").append(cfa.getName()).append("+ \",");
            } else if (cfa.getIdentity() && Objects.equals(cfa.getAttributetype().getName(), "string")) {
                patchParameters.append(cfa.getName()).append("=\'\"").append(" + userToChange.").append(cfa.getName()).append("+ \"\',");
            }
        }

        html.append("\t\t\t\t\t\t\tvar parameters = ").append(patchParameters).append("\"\n");

        html.append("\t\t\t\t\t\t\t$http.patch((").append("parameters.slice(0, -1) + \")\"").append("), updateData).then(res => {\n");
        html.append("\t\t\t\t\t\t\t\tif(res.status == 201 || res.status == 200) {").append("\n");
        html.append("\t\t\t\t\t\t\t\t\ttoastr.success('Passwort erfolgreich geändert', 'Erfolgreich');").append("\n");
        html.append("\t\t\t\t\t\t\t\t\topenLogin();").append("\n");
        html.append("\t\t\t\t\t\t\t\t} else {").append("\n");
        html.append("\t\t\t\t\t\t\t\t\ttoastr.error('Passwort konnte nicht erneuert werden.', 'Fehler');").append("\n");
        html.append("\t\t\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t\t\t})").append("\n");
        html.append("\t\t\t\t\t\t} else {").append("\n");
        html.append("\t\t\t\t\t\t\ttoastr.error('Der Token ist abgelaufen', 'Fehler')").append("\n");
        html.append("\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t} else {").append("\n");
        html.append("\t\t\t\t\t\t$scope.warning = true;").append("\n");
        html.append("\t\t\t\t\t\ttoastr.error('Die Passwörter stimmen nicht überein', 'Fehler')").append("\n");
        html.append("\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t}").append("\n\n");
        html.append("\t\t\t\t$scope.createUser = () => {").append("\n");
        for (CfAttribut cfa : attributList) {
            if(!Objects.equals(cfa.getName(), authField) && !Objects.equals(cfa.getName(), passwordField)&& !Objects.equals(cfa.getName(), "id") && cfa.getExt_mutable()) {
                html.append("\t\t\t\t\t").append("var ").append(cfa.getName()).append("Text = document.getElementById(\"create").append(cfa.getName()).append("\").value;\n");
            }
        }

        html.append("\t\t\t\t\t").append("var ").append(passwordField).append("Text = document.getElementById(\"create").append(passwordField).append("\").value;\n");
        html.append("\t\t\t\t\t").append("var ").append(passwordField).append("TextRepeat = document.getElementById(\"create").append(passwordField).append("Repeat\").value;\n");

        html.append("\t\t\t\t\tvar data = {").append("\n");
        html.append("\t\t\t\t\t\tid: null,").append("\n");
        html.append("\t\t\t\t\t\t").append(authField).append(": false,\n");
        for (CfAttribut cfa : attributList) {
            if(!Objects.equals(cfa.getName(), authField) && !Objects.equals(cfa.getName(), "id") && cfa.getExt_mutable()) {
                html.append("\t\t\t\t\t\t").append(cfa.getName()).append(": ").append(cfa.getName()).append("Text,\n");
            }
        }
        for (CfAttribut cfa : attributList) {
            if(!Objects.equals(cfa.getName(), authField) && !Objects.equals(cfa.getName(), "id") && !cfa.getExt_mutable()) {
                if (!cfa.getDefault_val().isEmpty()) {
                    switch (cfa.getAttributetypeString()) {
                        case "boolean":
                        case "integer":
                            html.append("\t\t\t\t\t\t").append(cfa.getName()).append(": ").append(cfa.getDefault_val()).append(",\n");
                            break;
                        default:
                            html.append("\t\t\t\t\t\t").append(cfa.getName()).append(": '").append(cfa.getDefault_val()).append("',\n");
                    }
                } else {
                    if (0 == cfa.getRelationtype()) {
                        html.append("\t\t\t\t\t\t").append(cfa.getName()).append(": ").append("[],\n");
                    } else {
                        html.append("\t\t\t\t\t\t").append(cfa.getName()).append(": ").append("null,\n");
                    }
                }
            }
        }
        html.append("\t\t\t\t\t};").append("\n");
        html.append("\t\t\t\t\tif(").append(passwordField).append("Text == ").append(passwordField).append("TextRepeat) {").append("\n");
        html.append("\t\t\t\t\t\t$scope.warning = false;").append("\n");
        html.append("\t\t\t\t\t\t$http.post(\"OData/\" +className , data).then(res => {").append("\n");
        html.append("\t\t\t\t\t\tif(res.status == 201) {").append("\n");
        html.append("\t\t\t\t\t\t\ttoastr.success('Benutzer erfolgreich erstellt. eine E-Mail zum Bestätigen wurde an Sie verschickt. Anschließend wird ein Administrator die Freischaltung vornehmen.', 'Erfolgreich');").append("\n");
        //html.append("\t\t\t\t\t\t\t$http.get(\"cf_sendemail?cf_job=true&to=\"+adminMail+\"&subject=Bestätigung für einen neuen Benutzer&body=Der Benutzer: \" + ").append(idField).append("Text + \" muss für die Seite: ${metainfo.referrer} bestätigt werden.\");\n");
        html.append("\t\t\t\t\t\t\t$http.get(\"SendConfirmMail?class=\" + className + \"&id=\" + res.data.id + \"&password=\" + passwordText + \"&pwfield=password&emailfield=email&sitefield=${metainfo.referrer}\");").append("\n");
        html.append("\t\t\t\t\t\t\tgetInformation()").append("\n");
        html.append("\t\t\t\t\t\t\topenLogin()").append("\n");
        html.append("\t\t\t\t\t\t} else if(res.status == 406) {").append("\n");
        html.append("\t\t\t\t\t\t\ttoastr.error('Der Account existiert schon. Bitte bei der IT melden.', 'Fehler');").append("\n");
        html.append("\t\t\t\t\t\t} else {").append("\n");
        html.append("\t\t\t\t\t\t\ttoastr.error('Es kam zu einem Fehler beim Erstellen', 'Fehler');").append("\n");
        html.append("\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t\t}, err => {").append("\n");
        html.append("\t\t\t\t\t\t toastr.error('Es kam zu einem Fehler beim Erstellen', 'Fehler');").append("\n");
        html.append("\t\t\t\t\t\t})").append("\n");
        html.append("\t\t\t\t\t } else {").append("\n");
        html.append("\t\t\t\t\t\t$scope.warning = true;").append("\n");
        html.append("\t\t\t\t\t\t$scope.showPasswordInfo = true;").append("\n");
        html.append("\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t}").append("\n\n");
        //ResetPassword
        html.append("\t\t\t\t$scope.resetPassword = () => {").append("\n");
        html.append("\t\t\t\t\tvar token = createToken();").append("\n");
        html.append("\t\t\t\t\tvar resetLink = \"http://\" + window.location.hostname + \"/\"+loginSite+\"?token=\" + token;").append("\n");
        html.append("\t\t\t\t\tvar email = document.getElementById(\"resetEmail\").value;").append("\n");
        html.append("\t\t\t\t\t$http.get(\"cf_sendemail?cf_job=true&to=\"+email+\"&subject=Passwort zurücksetzen&body=\"+ resetLink +\"\").then(res => {").append("\n");
        html.append("\t\t\t\t\t\ttoastr.success('Sie erhalten in kürze eine Email.', 'Erfolgreich')").append("\n");
        html.append("\t\t\t\t\t\topenLogin()").append("\n");
        html.append("\t\t\t\t\t});").append("\n");
        html.append("\t\t\t\t};").append("\n\n");


        html.append("\t\t\t\t$scope.deleteAccount = () => {").append("\n");
        html.append("\t\t\t\t\tvar email = document.getElementById(\"deleteEmail\").value;").append("\n");
        html.append("\t\t\t\t\t$http.get(\"SendConfirmDeleteMail?class=\"+className+\"&email=\"+ email +\"\").then(res => {").append("\n");
        html.append("\t\t\t\t\t\ttoastr.success('Sie erhalten in kürze eine Email.', 'Erfolgreich')").append("\n");
        html.append("\t\t\t\t\t\topenLogin()").append("\n");
        html.append("\t\t\t\t\t});").append("\n");
        html.append("\t\t\t\t};").append("\n\n");

        //Login
        html.append("\t\t\t\t$scope.login = function(referrer) {").append("\n");
        html.append("\t\t\t\t\t$http.get(\"Auth\", {params: { \"class\": className, \"idField\": idField, \"pwField\": passwordField, \"id\": document.getElementById(\"loginEmail\").value, \"clearPw\": document.getElementById(\"loginPassword\").value, \"authfield\": authfield}}).then(function (res) {").append("\n");
        html.append("\t\t\t\t\t\tif (res.status == 200) {").append("\n");
        html.append("\t\t\t\t\t\t\tif (res.data.status == true) {").append("\n");
        html.append("\t\t\t\t\t\t\t\t$scope.warning = false;").append("\n");
        html.append("\t\t\t\t\t\t\t\tdocument.cookie = \"cf_login_token=\"+res.data.token;").append("\n");
        html.append("\t\t\t\t\t\t\t\tdocument.cookie = \"cf_token=\"+res.data.token;").append("\n");
        html.append("\t\t\t\t\t\t\t\t\twindow.location = referrer;").append("\n");
        html.append("\t\t\t\t\t\t\t} else {").append("\n");
        html.append("\t\t\t\t\t\t\t\t$scope.warning = true;").append("\n");
        html.append("\t\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t\t} else {").append("\n");
        html.append("\t\t\t\t\t\t\t$scope.warning = true;").append("\n");
        html.append("\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t}, function (res) {").append("\n");
        html.append("\t\t\t\t\t\tconsole.log(\"ERROR\");").append("\n");
        html.append("\t\t\t\t\t});").append("\n");
        html.append("\t\t\t\t}").append("\n\n");

        //CheckPassword
        html.append("\t\t\t\t$scope.checkPassword = () => {").append("\n");
        html.append("\t\t\t\t\tif(passwordCheck) {").append("\n");
        html.append("\t\t\t\t\t\tvar password = document.getElementById('createPassword').value;").append("\n");
        html.append("\t\t\t\t\t\tvar passwordAgain = document.getElementById('createPasswordAgain').value;").append("\n");
        html.append("\t\t\t\t\t\tconst criteria = {").append("\n");
        html.append("\t\t\t\t\t\t\tlength: {").append("\n");
        html.append("\t\t\t\t\t\t\t\tisValid: password.length >= 8,").append("\n");
        html.append("\t\t\t\t\t\t\t\tmessage: \"Mindestens 8 Zeichen lang.\"").append("\n");
        html.append("\t\t\t\t\t\t\t},").append("\n");
        html.append("\t\t\t\t\t\t\tlowercase: {").append("\n");
        html.append("\t\t\t\t\t\t\t\tisValid: /[a-z]/.test(password),").append("\n");
        html.append("\t\t\t\t\t\t\t\tmessage: \"Mindestens ein kleiner Buchstabe.\"").append("\n");
        html.append("\t\t\t\t\t\t\t},").append("\n");
        html.append("\t\t\t\t\t\t\tuppercase: {").append("\n");
        html.append("\t\t\t\t\t\t\t\tisValid: /[A-Z]/.test(password),").append("\n");
        html.append("\t\t\t\t\t\t\t\tmessage: \"Mindestens ein großer Buchstabe.\"").append("\n");
        html.append("\t\t\t\t\t\t\t},").append("\n");
        html.append("\t\t\t\t\t\t\tdigit: {").append("\n");
        html.append("\t\t\t\t\t\t\t\tisValid: /\\d/.test(password),").append("\n");
        html.append("\t\t\t\t\t\t\t\tmessage: \"Mindestens eine Nummer.\"").append("\n");
        html.append("\t\t\t\t\t\t\t},").append("\n");
        html.append("\t\t\t\t\t\t\tspecialChar: {").append("\n");
        html.append("\t\t\t\t\t\t\t\tisValid: /[!@#$%^&*(){}[],.?\":{}|<>]/.test(password),").append("\n");
        html.append("\t\t\t\t\t\t\t\tmessage: \"Mindestens ein Sonderzeichen.\"").append("\n");
        html.append("\t\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t\t};").append("\n");
        html.append("\t\t\t\t\t\t$scope.missingCriteria = [];").append("\n");
        html.append("\t\t\t\t\t\tfor (const [key, value] of Object.entries(criteria)) {").append("\n");
        html.append("\t\t\t\t\t\t\tif (!value.isValid) {").append("\n");
        html.append("\t\t\t\t\t\t\t\t$scope.missingCriteria.push(value.message);").append("\n");
        html.append("\t\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t\tif($scope.missingCriteria.length == 0 && password == passwordAgain) {").append("\n");
        html.append("\t\t\t\t\t\t\tdocument.getElementById(\"createUserButton\").disabled = false").append("\n");
        html.append("\t\t\t\t\t\t} else { ").append("\n");
        html.append("\t\t\t\t\t\t\tdocument.getElementById(\"createUserButton\").disabled = true").append("\n");
        html.append("\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t} else {").append("\n");
        html.append("\t\t\t\t\t\tdocument.getElementById(\"createUserButton\").disabled = false").append("\n");
        html.append("\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t}").append("\n\n");

        html.append("\t\t\t\t/* Helper Functions */").append("\n");
        html.append("\t\t\t\tfunction createToken() {").append("\n");
        html.append("\t\t\t\t\tconst now = new Date();").append("\n");
        html.append("\t\t\t\t\tconst expiresAt = new Date(now.getTime() + 20 * 60000);").append("\n");
        html.append("\t\t\t\t\tconst token = btoa(JSON.stringify({ expiresAt: expiresAt.toISOString(), email: document.getElementById(\"resetEmail\").value }));").append("\n");
        html.append("\t\t\t\t\treturn token;").append("\n");
        html.append("\t\t\t\t}").append("\n\n");

        html.append("\t\t\t\tfunction decodeToken(token) {").append("\n");
        html.append("\t\t\t\t\tconst decoded = atob(token);").append("\n");
        html.append("\t\t\t\t\tconst payload = JSON.parse(decoded);").append("\n");
        html.append("\t\t\t\t\treturn payload;").append("\n");
        html.append("\t\t\t\t}").append("\n\n");

        html.append("\t\t\t\tfunction isTokenValid(token) {").append("\n");
        html.append("\t\t\t\t\tconst payload = decodeToken(token);").append("\n");
        html.append("\t\t\t\t\tconst expiresAt = new Date(payload.expiresAt);").append("\n");
        html.append("\t\t\t\t\tconst now = new Date();").append("\n");
        html.append("\t\t\t\t\treturn now < expiresAt;").append("\n");
        html.append("\t\t\t\t}").append("\n");

        html.append("\t\t\t\tfunction createUpdateData(userToChange, newPassword) {").append("\n");
        html.append("\t\t\t\t\tlet updateData = {};").append("\n");
        html.append("\t\t\t\t\tfor (let key in userToChange) {").append("\n");
        html.append("\t\t\t\t\t\tif (userToChange.hasOwnProperty(key)) {").append("\n");
        html.append("\t\t\t\t\t\t\tupdateData[key] = userToChange[key];").append("\n");
        html.append("\t\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\t}").append("\n");
        html.append("\t\t\t\t\tupdateData.password = newPassword;").append("\n");
        html.append("\t\t\t\t\treturn updateData;").append("\n");
        html.append("\t\t\t\t}").append("\n");

        html.append("\t\t\t\tfunction openLogin() {").append("\n");
        html.append("\t\t\t\t\t$scope.forgotPassword = false;").append("\n");
        html.append("\t\t\t\t\t$scope.resetPasswordEmail = false;").append("\n");
        html.append("\t\t\t\t\t$scope.createUserB = false;").append("\n");
        html.append("\t\t\t\t\t$scope.deleteEmail = false;").append("\n");
        html.append("\t\t\t\t};").append("\n");

        html.append("\t\t\t\tasync function getInformation() {").append("\n");
        html.append("\t\t\t\t\tawait $http.get(\"/OData/\" + ").append("className).then(res => $scope.classDataList = res.data.value);\n");
        html.append("\t\t\t\t}").append("\n");
        html.append("\t\t\t\tgetInformation();").append("\n");

        html.append("\t\t\t});").append("\n\n");
        html.append("\t\t</script>").append("\n");

        template.setName("login_" + clazz.getName());
        try {
            dummytemplate = cfTemplateService.findByName(template.getName());
            if (null == dummytemplate) {
                template.setScriptlanguage(0);
                template.setCheckedoutby(BigInteger.ZERO);
                template.setContent(html.toString());
                cfTemplateService.create(template);
                templateutil.commit(template);
            } else {
                dummytemplate.setContent(html.toString());
                cfTemplateService.edit(dummytemplate);
                templateutil.commit(dummytemplate);
            }
        } catch (Exception ex) {
            template.setScriptlanguage(2);
            template.setCheckedoutby(BigInteger.ZERO);
            template.setContent(html.toString());
            cfTemplateService.create(template);
            templateutil.commit(template);
        }

        site.setName("login_" + clazz.getName().toLowerCase());
        CfSite dummysite = cfSiteService.findByName(site.getName());
        if (null == dummysite) {
            site.setCharacterencoding("UTF-8");
            site.setHitcounter(BigInteger.ZERO);
            site.setTitle("");
            site.setContenttype("text/html");
            site.setSearchrelevant(false);
            site.setHtmlcompression(0);
            site.setGzip(0);
            site.setLocale("de");
            site.setDescription("Automatic generation");
            site.setAliaspath(site.getName());
            CfSite parent = cfSiteService.findByName("logins");
            if (null != parent) {
                site.setParentref(parent);
            } else {
                site.setParentref(null);
            }
            if (null != template.getContent()) {
                site.setTemplateref(template);
            } else {
                site.setTemplateref(dummytemplate);
            }
            site.setShorturl(siteutil.generateShorturl());
            site.setLoginsite("");
            site.setTestparams("");
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
    
    private String buildIdentifier(List<CfAttribut> attributlist, String classname) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (CfAttribut at : attributlist) {
            if (at.getIdentity()) {
                switch (at.getAttributetypeString()) {
                    case "integer":
                        sb.append(at.getName());
                        sb.append("=\" + ");
                        sb.append(classname);
                        sb.append(".");
                        sb.append(at.getName());
                        sb.append(" + \",");
                        break;
                    case "string":
                        sb.append(at.getName());
                        sb.append("='\" + ");
                        sb.append(classname);
                        sb.append(".");
                        sb.append(at.getName());
                        sb.append(" + \"',");
                        break;
                }
            }
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")");
        return sb.toString();
    }
}
