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
import io.clownfish.clownfish.compiler.JVMLanguages;
import io.clownfish.clownfish.datamodels.RestContentParameter;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.serviceinterface.CfJavaversionService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
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
    @Autowired JavaUtil javaUtility;
    @Autowired MarkdownUtil markdownUtil;
    @Autowired JavaList javalist;
    
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
