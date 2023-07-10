/*
 * Copyright 2020 sulzbachr.
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

import io.clownfish.clownfish.datamodels.AttributDef;
import io.clownfish.clownfish.datamodels.ContentDataOutput;
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.lucene.ContentIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.DataFormatException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;

/**
 *
 * @author sulzbachr
 */
@Component
public class ContentUtil implements IVersioningInterface {
    @Autowired transient CfAttributetypeService cfattributetypeService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfClasscontentKeywordService cfcontentkeywordService;
    @Autowired transient CfAttributService cfattributservice;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfAssetlistcontentService cfassetlistcontentService;
    @Autowired FolderUtil folderUtil;
    @Autowired MarkdownUtil markdownUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired IndexService indexService;
    @Autowired ContentIndexer contentIndexer;
    @Autowired transient CfContentversionService cfcontentversionService;
    @Autowired ClassUtil classUtil;
    @Autowired private PropertyUtil propertyUtil;
    private @Getter @Setter long currentVersion;
    private @Getter @Setter String content = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentUtil.class);

    @Value("${hibernate.use:0}") int useHibernate;

    public void init(MarkdownUtil markdownUtil, String site, List urlParams) {
        this.markdownUtil = markdownUtil;
        this.markdownUtil.setSite(site);
        this.markdownUtil.setUrlParams(urlParams);
        this.markdownUtil.setInit(true);
    }

    public AttributDef getAttributContent(long attributtypeid, CfAttributcontent attributcontent, CfAttributetype knattributtype) {
        //CfAttributetype knattributtype = cfattributetypeService.findById(attributtypeid);
        switch (knattributtype.getName()) {
            case "boolean":
                if (null != attributcontent.getContentBoolean()) {
                    return new AttributDef(attributcontent.getContentBoolean().toString(), "boolean");
                } else {
                    return new AttributDef(null, "boolean");
                }
            case "string":
                if (null != attributcontent.getContentString()) {
                    return new AttributDef(attributcontent.getContentString(), "string");
                } else {
                    return new AttributDef(null, "string");
                }
            case "hashstring":
                if (null != attributcontent.getContentString()) {
                    return new AttributDef(attributcontent.getContentString(), "hashstring");
                } else {
                    return new AttributDef(null, "hashstring");
                }
            case "integer":
                if (null != attributcontent.getContentInteger()) {
                    return new AttributDef(attributcontent.getContentInteger().toString(), "integer");
                } else {
                    return new AttributDef(null, "integer");
                }
            case "real":
                if (null != attributcontent.getContentReal()) {
                    return new AttributDef(attributcontent.getContentReal().toString(), "real");
                } else {
                    return new AttributDef(null, "real");
                }
            case "htmltext":
                if (null != attributcontent.getContentText()) {
                    return new AttributDef(attributcontent.getContentText(), "htmltext");
                } else {
                    return new AttributDef(null, "htmltext");
                }
            case "markdown":
                if (null != attributcontent.getContentText()) {
                    return new AttributDef(markdownUtil.parseMarkdown(attributcontent.getContentText(), markdownUtil.getMarkdownOptions()), "markdown");
                } else {
                    return new AttributDef(null, "markdown");
                }
            case "datetime":
                if (null != attributcontent.getContentDate()) {
                    return new AttributDef(attributcontent.getContentDate().toString(), "datetime");
                } else {
                    return new AttributDef(null, "datetime");
                }
            case "media":
                if (null != attributcontent.getContentInteger()) {
                    return new AttributDef(attributcontent.getContentInteger().toString(), "media");
                } else {
                    return new AttributDef(null, "media");
                }
            case "text":
                if (null != attributcontent.getContentText()) {
                    return new AttributDef(attributcontent.getContentText(), "text");
                } else {
                    return new AttributDef(null, "text");
                }
            case "classref":
                if (0 == attributcontent.getAttributref().getRelationtype()) {  // n:m
                    if (null != attributcontent.getClasscontentlistref()) {
                        return new AttributDef(attributcontent.getClasscontentlistref().getName(), "classref");
                    } else {                                                    // 1:n
                        return new AttributDef(null, "classref");
                    }
                } else {
                    if (null != attributcontent.getContentInteger()) {
                        return new AttributDef(attributcontent.getContentInteger().toString(), "classref");
                    } else {
                        return new AttributDef(null, "classref");
                    }
                }
            case "assetref":
                if (null != attributcontent.getAssetcontentlistref()) {
                    return new AttributDef(attributcontent.getAssetcontentlistref().getName(), "assetref");
                } else {
                    return new AttributDef(null, "assetref");
                }    
            default:
                return null;
        }
    }
    
    public CfAttributcontent setAttributValue(CfAttributcontent selectedAttribut, String editContent) {
        if (null == editContent) {
            editContent = "";
        }
        try {
            switch (selectedAttribut.getAttributref().getAttributetype().getName()) {
                case "boolean":
                    selectedAttribut.setContentBoolean(Boolean.valueOf(editContent));
                    break;
                case "string":
                    if (editContent.length() > 255) {
                        editContent = editContent.substring(0, 255);
                    }
                    if (selectedAttribut.getAttributref().getIdentity() == true) {
                        List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(selectedAttribut.getClasscontentref().getClassref());
                        boolean found = false;
                        for (CfClasscontent classcontent : classcontentlist2) {
                            try {
                                CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(selectedAttribut.getAttributref(), classcontent);
                                if (attributcontent.getContentString().compareToIgnoreCase(editContent) == 0) {
                                    found = true;
                                }
                            } catch (javax.persistence.NoResultException | NullPointerException ex) {
                                LOGGER.error(ex.getMessage());
                            }
                        }
                        if (!found) {                        
                            selectedAttribut.setContentString(editContent);
                        }
                    } else {
                        if (selectedAttribut.getClasscontentref().getClassref().isEncrypted()) {
                            selectedAttribut.setContentString(EncryptUtil.encrypt(editContent, propertyUtil.getPropertyValue("aes_key")));
                        } else {
                            selectedAttribut.setContentString(editContent);
                        }
                    }
                    break;
                case "hashstring":
                    String salt = PasswordUtil.getSalt(30);
                    selectedAttribut.setContentString(PasswordUtil.generateSecurePassword(editContent, salt));
                    selectedAttribut.setSalt(salt);
                    break;    
                case "integer":
                    if (!editContent.isBlank()) {
                        selectedAttribut.setContentInteger(BigInteger.valueOf(Long.parseLong(editContent)));
                    }
                    break;
                case "real":
                    if (!editContent.isBlank()) {
                        selectedAttribut.setContentReal(Double.parseDouble(editContent));
                    }
                    break;
                case "htmltext":
                    if (selectedAttribut.getClasscontentref().getClassref().isEncrypted()) {
                        selectedAttribut.setContentText(EncryptUtil.encrypt(editContent, propertyUtil.getPropertyValue("aes_key")));
                    } else {
                        selectedAttribut.setContentText(editContent);
                    }
                    break;    
                case "text":
                    if (selectedAttribut.getClasscontentref().getClassref().isEncrypted()) {
                        selectedAttribut.setContentText(EncryptUtil.encrypt(editContent, propertyUtil.getPropertyValue("aes_key")));
                    } else {
                        selectedAttribut.setContentText(editContent);
                    }
                    break;
                case "markdown":
                    if (selectedAttribut.getClasscontentref().getClassref().isEncrypted()) {
                        selectedAttribut.setContentText(EncryptUtil.encrypt(editContent, propertyUtil.getPropertyValue("aes_key")));
                    } else {
                        selectedAttribut.setContentText(editContent);
                    }
                    break;
                case "datetime":
                    Date datum;
                    DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yyyy").withZone(DateTimeZone.forID("Europe/Berlin"));
                    try {
                        datum = DateTime.parse(editContent, fmt).toDate();
                        selectedAttribut.setContentDate(datum);
                    } catch (IllegalArgumentException ex) {
                        try {
                            fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(DateTimeZone.forID("Europe/Berlin"));
                            datum = DateTime.parse(editContent, fmt).toDate();
                            selectedAttribut.setContentDate(datum);
                        } catch (IllegalArgumentException ex2) {
                            fmt = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss").withZone(DateTimeZone.forID("Europe/Berlin"));
                            try {
                                datum = DateTime.parse(editContent, fmt).toDate();
                                selectedAttribut.setContentDate(datum);
                            } catch (IllegalArgumentException ex3) {
                                datum = null;
                                selectedAttribut.setContentDate(datum);
                            }
                        }
                    }
                    break;
                case "media":
                    if (null != editContent) {
                        try {
                            CfAsset asset = cfassetService.findById(Long.parseLong(editContent));
                            selectedAttribut.setContentInteger(BigInteger.valueOf(asset.getId()));
                        } catch (Exception ex) {
                            try {
                                CfAsset asset = cfassetService.findByName(editContent);
                                selectedAttribut.setContentInteger(BigInteger.valueOf(asset.getId()));
                            } catch (Exception ex1) {
                                selectedAttribut.setContentInteger(null);
                                LOGGER.error("INSERTCONTENT: Media " + editContent + " not found!");
                            }
                        }
                    } else {
                        selectedAttribut.setContentInteger(null);
                    }
                    break;    
                case "classref":
                    if (null != editContent) {
                        if (0 == selectedAttribut.getAttributref().getRelationtype()) {                 // n:m relation
                            try {
                                CfList list_ref = cflistService.findById(Long.parseLong(editContent));
                                selectedAttribut.setClasscontentlistref(list_ref);
                            } catch (Exception ex) {
                                try {
                                    CfList list_ref = cflistService.findByName(editContent);
                                    selectedAttribut.setClasscontentlistref(list_ref);
                                } catch (Exception ex1) {
                                    selectedAttribut.setClasscontentlistref(null);
                                }
                            }
                        } else {                                                                        // 1:n relation
                            try {
                                CfClasscontent classcontent = cfclasscontentService.findById(Long.parseLong(editContent));
                                selectedAttribut.setContentInteger(BigInteger.valueOf(classcontent.getId()));
                            } catch (Exception ex) {
                                try {
                                    CfClasscontent classcontent = cfclasscontentService.findByName(editContent);
                                    selectedAttribut.setContentInteger(BigInteger.valueOf(classcontent.getId()));
                                } catch (Exception ex1) {
                                    selectedAttribut.setContentInteger(null);
                                }
                            }
                        }
                    } else {
                        selectedAttribut.setClasscontentlistref(null);
                        selectedAttribut.setContentInteger(null);
                    }
                    break;
                case "assetref":
                    if (null != editContent) {
                        try {
                            CfAssetlist assetlist_ref = cfassetlistService.findById(Long.parseLong(editContent));
                            selectedAttribut.setAssetcontentlistref(assetlist_ref);
                        } catch (Exception ex) {
                            try {
                                CfAssetlist assetlist_ref = cfassetlistService.findByName(editContent);
                                selectedAttribut.setAssetcontentlistref(assetlist_ref);
                            } catch (Exception ex1) {
                                selectedAttribut.setAssetcontentlistref(null);
                            }
                        }
                    } else {
                        selectedAttribut.setAssetcontentlistref(null);
                    }
                    break;    
            }
            selectedAttribut.setIndexed(false);
            return selectedAttribut;
        } catch (NullPointerException ex) {
            LOGGER.warn(ex.getMessage());
            return selectedAttribut;
        }
    }
    
    public ArrayList getContentOutputKeyvalList(List<CfAttributcontent> attributcontentList) {
        ArrayList<HashMap> output = new ArrayList<>();
        HashMap<String, String> dummyoutputmap = new HashMap<>();
        attributcontentList.stream().forEach((attributcontent) -> {
            CfAttribut knattribut = cfattributService.findById(attributcontent.getAttributref().getId());
            long attributtypeid = knattribut.getAttributetype().getId();
            AttributDef attributdef = getAttributContent(attributtypeid, attributcontent, knattribut.getAttributetype());
            if (attributdef.getType().compareToIgnoreCase("hashstring") != 0) {
                if (((knattribut.getClassref().isEncrypted()) && (!knattribut.getIdentity())) && (isEncryptable(knattribut))) {
                    dummyoutputmap.put(knattribut.getName(), EncryptUtil.decrypt(attributdef.getValue(), propertyUtil.getPropertyValue("aes_key")));
                } else {
                    dummyoutputmap.put(knattribut.getName(), attributdef.getValue());
                }
            }
        });
        output.add(dummyoutputmap);
        return output;
    }
    
    public HashMap getContentOutputKeyval(List<CfAttributcontent> attributcontentList) {
        HashMap<String, String> dummyoutputmap = new HashMap<>();
        attributcontentList.stream().forEach((attributcontent) -> {
            CfAttribut knattribut = cfattributService.findById(attributcontent.getAttributref().getId());
            long attributtypeid = knattribut.getAttributetype().getId();
            AttributDef attributdef = getAttributContent(attributtypeid, attributcontent, knattribut.getAttributetype());
            if (attributdef.getType().compareToIgnoreCase("hashstring") != 0) {
                if (((knattribut.getClassref().isEncrypted()) && (!knattribut.getIdentity())) && (isEncryptable(knattribut))) {
                    dummyoutputmap.put(knattribut.getName(), EncryptUtil.decrypt(attributdef.getValue(), propertyUtil.getPropertyValue("aes_key")));
                } else {
                    dummyoutputmap.put(knattribut.getName(), attributdef.getValue());
                }
            }
        });
        return dummyoutputmap;
    }
    
    public ArrayList getContentOutputKeywords(CfClasscontent classcontent, boolean toLower) {
        ArrayList<String> keywords = new ArrayList<>();
        List<CfClasscontentkeyword> keywordlist = cfclasscontentkeywordService.findByClassContentRef(classcontent.getId());
        if (!keywordlist.isEmpty()) {
            for (CfClasscontentkeyword cck : keywordlist) {
                if (toLower) {
                    keywords.add(cfkeywordService.findById(cck.getCfClasscontentkeywordPK().getKeywordref()).getName().toLowerCase());
                } else {
                    keywords.add(cfkeywordService.findById(cck.getCfClasscontentkeywordPK().getKeywordref()).getName());
                }
            }
        }
        return keywords;
    }
    
    public void indexContent() {
        // Index the changed content and merge the Index files
        if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getMedia_folder().isEmpty())) {
            Thread contentindexer_thread = new Thread(contentIndexer);
            contentindexer_thread.start();
            LOGGER.info("CONTENTINDEXER RUN");
        }
    }

    @Override
    public String getVersion(long ref, long version) {
        try {
            CfContentversion contentversion = cfcontentversionService.findByPK(ref, version);
            byte[] decompress = CompressionUtils.decompress(contentversion.getContent());
            return new String(decompress, StandardCharsets.UTF_8);
        } catch (IOException | DataFormatException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public void writeVersion(long ref, long version, byte[] content, long userid) {
        CfContentversionPK contentversionpk = new CfContentversionPK();
        contentversionpk.setContentref(ref);
        contentversionpk.setVersion(version);

        CfContentversion cfcontentversion = new CfContentversion();
        cfcontentversion.setCfContentversionPK(contentversionpk);
        cfcontentversion.setContent(content);
        cfcontentversion.setTstamp(new Date());
        cfcontentversion.setCommitedby(BigInteger.valueOf(userid));
        cfcontentversionService.create(cfcontentversion);
    }

    @Override
    public long getCurrentVersionNumber(String name) {
        CfClasscontent cfcontent = cfclasscontentService.findByName(name);
        return cfcontentversionService.findMaxVersion((cfcontent).getId());
    }
    
    @Override
    public boolean hasDifference(Object object) {
        boolean diff = false;
        try {
            currentVersion = (long) cfcontentversionService.findMaxVersion(((CfClasscontent)object).getId());
        } catch (NullPointerException ex) {
            currentVersion = 0;
        }
        if (currentVersion > 0) {
            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref((CfClasscontent)object);
            String currentContent = classUtil.jsonExport(((CfClasscontent)object), attributcontentlist);
            String contentVersion = getVersion(((CfClasscontent)object).getId(), currentVersion);
            diff = 0 != currentContent.compareToIgnoreCase(contentVersion);
        } else {
            diff = true;
        }
        return diff;
    }
    
    public ArrayList getContentKeywords(CfClasscontent content, boolean toLower) {
        ArrayList<String> keywords = new ArrayList<>();
        List<CfClasscontentkeyword> keywordlist = cfcontentkeywordService.findByClassContentRef(content.getId());
        if (!keywordlist.isEmpty()) {
            for (CfClasscontentkeyword ak : keywordlist) {
                if (toLower) {
                    keywords.add(cfkeywordService.findById(ak.getCfClasscontentkeywordPK().getKeywordref()).getName().toLowerCase());
                } else {
                    keywords.add(cfkeywordService.findById(ak.getCfClasscontentkeywordPK().getKeywordref()).getName());
                }
            }
        }
        return keywords;
    }
    
    public ArrayList getContentMapList(Map content) {
        HashMap<String, String> contentMap = new HashMap<>(content);
        ArrayList contenList = new ArrayList<>();
        contenList.add(contentMap);
        return contenList;
    }
    
    public HashMap getContentMap(Map content) {
        HashMap<String, String> contentMap = new HashMap<>(content);
        return contentMap;
    }
    
    public ArrayList getContentMapListDecrypted(Map content, CfClass classref) {
        List<CfAttribut> attributlist = cfattributService.findByClassref(classref);
        HashMap<String, String> contentMap = new HashMap<>(content);
        for (CfAttribut attribut : attributlist) {
            if ((isEncryptable(attribut)) && (!attribut.getIdentity())) {
                contentMap.put(attribut.getName(), EncryptUtil.decrypt(contentMap.get(attribut.getName()), propertyUtil.getPropertyValue("aes_key")));
            }
        }
        ArrayList contenList = new ArrayList<>();
        contenList.add(contentMap);
        return contenList;
    }
    
    public HashMap getContentMapDecrypted(Map content, CfClass classref) {
        List<CfAttribut> attributlist = cfattributService.findByClassref(classref);
        HashMap<String, String> contentMap = new HashMap<>(content);
        for (CfAttribut attribut : attributlist) {
            if ((isEncryptable(attribut)) && (!attribut.getIdentity())) {
                contentMap.put(attribut.getName(), EncryptUtil.decrypt(contentMap.get(attribut.getName()), propertyUtil.getPropertyValue("aes_key")));
            }
        }
        return contentMap;
    }
    
    public String toString(CfAttributcontent attributcontent) {
        switch (attributcontent.getAttributref().getAttributetype().getId().intValue()) {
            case 1: // boolean
                if (null != attributcontent.getContentBoolean()) {
                    return attributcontent.getContentBoolean().toString();
                } else {
                    return "";
                }    
            case 2: // string
                if (null != attributcontent.getContentString()) {
                    if ((!attributcontent.getAttributref().getClassref().isEncrypted()) || (attributcontent.getAttributref().getIdentity())) {
                        return attributcontent.getContentString();
                    } else {
                        return EncryptUtil.decrypt(attributcontent.getContentString(), propertyUtil.getPropertyValue("aes_key"));
                    }
                } else {
                    return "";
                }
            case 3: // integer
                if (null != attributcontent.getContentInteger()) {
                    return attributcontent.getContentInteger().toString();
                } else {
                    return "";
                }
            case 4: // real
                if (null != attributcontent.getContentReal()) {
                    return attributcontent.getContentReal().toString();
                } else {
                    return "";
                }    
            case 5: // htmltext (formatted)
                if (null != attributcontent.getContentText()) {
                    if ((!attributcontent.getAttributref().getClassref().isEncrypted()) || (attributcontent.getAttributref().getIdentity())) {
                        return attributcontent.getContentText();
                    } else {
                        return EncryptUtil.decrypt(attributcontent.getContentText(), propertyUtil.getPropertyValue("aes_key"));
                    }
                } else {
                    return "";
                }
            case 6: // datetime
                if (null != attributcontent.getContentDate()) {
                    DateTime dt = new DateTime(attributcontent.getContentDate());
                    DateTimeFormatter dtf1 = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy").withLocale(Locale.GERMANY);
                    
                    dt.toString(dtf1);
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy");
                    
                    return dt.toString(dtf);
                } else {
                    return "";
                }
            case 7: // hashstring (crypted with salt - for passwords)
                if (null != attributcontent.getContentString()) {
                    return attributcontent.getContentString();
                } else {
                    return "";
                }
            case 8: // media (id to asset)
                if (null != attributcontent.getContentInteger()) {
                    return attributcontent.getContentInteger().toString();
                } else {
                    return "";
                }
            case 9: // text (unformatted)
                if (null != attributcontent.getContentText()) {
                    if ((!attributcontent.getAttributref().getClassref().isEncrypted()) || (attributcontent.getAttributref().getIdentity())) {
                        return attributcontent.getContentText();
                    } else {
                        return EncryptUtil.decrypt(attributcontent.getContentText(), propertyUtil.getPropertyValue("aes_key"));
                    }
                } else {
                    return "";
                }
            case 10: // text (markdown formatted)
                if (null != attributcontent.getContentText()) {
                    if ((!attributcontent.getAttributref().getClassref().isEncrypted()) || (attributcontent.getAttributref().getIdentity())) {
                        return attributcontent.getContentText();
                    } else {
                        return EncryptUtil.decrypt(attributcontent.getContentText(), propertyUtil.getPropertyValue("aes_key"));
                    }
                } else {
                    return "";
                } 
            case 11: // classref
                if (0 == attributcontent.getAttributref().getRelationtype()) {  // n:m
                    if (null != attributcontent.getClasscontentlistref()) {
                        return attributcontent.getClasscontentlistref().getName();
                    } else {                                                    // 1:n
                        return "";
                    }
                } else {
                    if (null != attributcontent.getContentInteger()) {
                        return cfclasscontentService.findById(attributcontent.getContentInteger().longValue()).getName(); //attributcontent.getClasscontentlistref().getName();
                    } else {
                        return "";
                    }
                }
            case 12: // assetref
                if (null != attributcontent.getAssetcontentlistref()) {
                    return attributcontent.getAssetcontentlistref().getName();
                } else {
                    return "";
                }     
        }
        return "?";
    }
    
    private boolean isEncryptable(CfAttribut attribut) {
        switch (attribut.getAttributetype().getName()) {
            case "string":
            case "text":
            case "htmltext":
            case "markdown":
                return true;
            default:
                return false;
        }
    }

    @Override
    public String getUniqueName(String name) {
        int i = 1;
        boolean found = false;
        do {
            try {
                cfclasscontentService.findByName(name+"("+i+")");
                i++;
            } catch(Exception ex) {
                found = true;
            }
        } while (!found);
        return name+"("+i+")";
    }

    public boolean setClassrefVals(HashMap hm, CfClass clazz, List<HashMap<String, String>> filter_list) {
        boolean found = true;
        for (Object key : hm.keySet()) {
            try {
                CfAttribut attr = cfattributservice.findByNameAndClassref((String) key, clazz);
                if (0 == attr.getAttributetype().getName().compareToIgnoreCase("classref")) {
                    if (0 == attr.getRelationtype()) {          // n:m
                        CfList contentlist = cflistService.findByClassrefAndName(attr.getRelationref(), (String) hm.get(key));
                        List<CfListcontent> listcontent = cflistcontentService.findByListref(contentlist.getId());
                        List<Map<String, String>> result = new ArrayList<>();
                        for (CfListcontent contentitem : listcontent) {
                            Map output = hibernateUtil.getContent(attr.getRelationref().getName(), contentitem.getCfListcontentPK().getClasscontentref(), attr.getName(), filter_list);
                            if (!output.isEmpty()) {
                                CfClasscontent cfclasscontent = cfclasscontentService.findById((long)output.get("cf_contentref"));
                                if (null != cfclasscontent) {
                                    if (!cfclasscontent.isScrapped()) {
                                        ContentDataOutput contentdataoutput = new ContentDataOutput();
                                        contentdataoutput.setContent(cfclasscontent);
                                        if (cfclasscontent.getClassref().isEncrypted()) {
                                            contentdataoutput.setKeyvals(getContentMapListDecrypted(output, cfclasscontent.getClassref()));
                                            contentdataoutput.setKeyval(getContentMapDecrypted(output, cfclasscontent.getClassref()));
                                        } else {
                                            contentdataoutput.setKeyvals(getContentMapList(output));
                                            contentdataoutput.setKeyval(getContentMap(output));
                                        }
                                        setClassrefVals(contentdataoutput.getKeyvals().get(0), clazz, filter_list);
                                        setAssetrefVals(contentdataoutput.getKeyvals().get(0), clazz);
                                        try {
                                            contentdataoutput.setDifference(hasDifference(cfclasscontent));
                                            contentdataoutput.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                                        } catch (Exception ex) {

                                        }
                                        result.add(contentdataoutput.getKeyvals().get(0));
                                    }
                                }
                            }
                        }
                        hm.put(attr.getName(), result);
                        if (result.isEmpty()) found = false;
                    } else {                                    // 1:n
                        if (hm.get(key) instanceof String) {
                            Map output = hibernateUtil.getContent(attr.getRelationref().getName(), Long.parseLong((String)hm.get(key)), attr.getName(), filter_list);
                            hm.put(attr.getName(), output);
                            if (output.isEmpty()) found = false;
                        } else {
                            Map output = hibernateUtil.getContent(attr.getRelationref().getName(), (long) hm.get(key), attr.getName(), filter_list);
                            hm.put(attr.getName(), output);
                            if (output.isEmpty()) found = false;
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        return found;
    }

    public void setAssetrefVals(HashMap hm, CfClass clazz) {
        for (Object key : hm.keySet()) {
            try {
                CfAttribut attr = cfattributservice.findByNameAndClassref((String) key, clazz);
                if (0 == attr.getAttributetype().getName().compareToIgnoreCase("assetref")) {
                    CfAssetlist assetlist = cfassetlistService.findByName((String)hm.get(key));
                    List<CfAssetlistcontent> assetcontentlist = cfassetlistcontentService.findByAssetlistref(assetlist.getId());

                    List<Long> result = new ArrayList<>();
                    for (CfAssetlistcontent assetitem : assetcontentlist) {
                        result.add(assetitem.getCfAssetlistcontentPK().getAssetref());
                    }
                    hm.put(attr.getName(), result);
                }
            } catch (Exception ex) {

            }
        }
    }

    public Map<String, String> getSingle(CfClass clazz, String attributname, Object attributvalue) {
        if (0 == useHibernate) {
            Map<String, String> result = new HashMap<>();
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
            for (CfClasscontent cc : classcontentList) {
                if (!cc.isScrapped()) {
                    HashMap<String, String> attributmap = new HashMap<>();
                    List<CfAttributcontent> aclist = cfattributcontentService.findByClasscontentref(cc);
                    if (checkCompare(aclist, cc, attributname, attributvalue)) {
                        List keyvals = getContentOutputKeyvalList(aclist);

                        result.putAll((Map)keyvals.get(0));
                    }
                }
            }
            return result;
        } else {
            Map<String, String> result = new HashMap<>();
            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            HashMap searchmap = new HashMap<>();
            searchmap.put(attributname+"_1", ":eq:" + (String) attributvalue.toString());
            Query query = hibernateUtil.getQuery(session_tables, searchmap, clazz.getName(), null);
            if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
                LOGGER.info("Query: " + query.getQueryString());
            }
            try {
                List<Map> contentliste = (List<Map>) query.getResultList();

                session_tables.close();
                for (Map content : contentliste) {
                    CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                    if (null != cfclasscontent) {
                        if (!cfclasscontent.isScrapped()) {
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(cfclasscontent);
                            if (cfclasscontent.getClassref().isEncrypted()) {
                                contentdataoutput.setKeyvals(getContentMapListDecrypted(content, cfclasscontent.getClassref()));
                                contentdataoutput.setKeyval(getContentMapDecrypted(content, cfclasscontent.getClassref()));
                            } else {
                                contentdataoutput.setKeyvals(getContentMapList(content));
                                contentdataoutput.setKeyval(getContentMap(content));
                            }
                            setClassrefVals(contentdataoutput.getKeyvals().get(0), clazz, null);
                            setAssetrefVals(contentdataoutput.getKeyvals().get(0), clazz);
                            try {
                                contentdataoutput.setDifference(hasDifference(cfclasscontent));
                                contentdataoutput.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                            } catch (Exception ex) {

                            }
                            result.putAll(contentdataoutput.getKeyvals().get(0));
                        }
                    }
                }
            } catch (NoResultException ex) {
                session_tables.close();
            }

            return result;
        }
    }

    public List<Map<String, String>> getList(CfClass clazz, String attributname, Object attributvalue) {
        if (0 == useHibernate) {
            List<Map<String, String>> result = new ArrayList<>();
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
            for (CfClasscontent cc : classcontentList) {
                if (!cc.isScrapped()) {
                    HashMap<String, String> attributmap = new HashMap<>();
                    if (!attributname.isEmpty()) {
                        List<CfAttributcontent> aclist = cfattributcontentService.findByClasscontentref(cc);
                        if (checkCompare(aclist, cc, attributname, attributvalue)) {
                            List keyvals = getContentOutputKeyvalList(aclist);
                            result.add((Map)keyvals.get(0));
                        }
                    } else {
                        List<CfAttributcontent> aclist = cfattributcontentService.findByClasscontentref(cc);
                        List keyvals = getContentOutputKeyvalList(aclist);
                        result.add((Map)keyvals.get(0));
                    }
                }
            }
            return result;
        } else {
            List<Map<String, String>> result = new ArrayList<>();
            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            HashMap searchmap = new HashMap<>();
            if (null != attributvalue) {
                searchmap.put(attributname+"_1", (String) attributvalue.toString());
            }
            Query query = hibernateUtil.getQuery(session_tables, searchmap, clazz.getName(), null);
            if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
                LOGGER.info("Query: " + query.getQueryString());
            }
            try {
                List<Map> contentliste = (List<Map>) query.getResultList();

                session_tables.close();
                for (Map content : contentliste) {
                    CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                    if (null != cfclasscontent) {
                        if (!cfclasscontent.isScrapped()) {
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(cfclasscontent);
                            if (cfclasscontent.getClassref().isEncrypted()) {
                                contentdataoutput.setKeyvals(getContentMapListDecrypted(content, cfclasscontent.getClassref()));
                                contentdataoutput.setKeyval(getContentMapDecrypted(content, cfclasscontent.getClassref()));
                            } else {
                                contentdataoutput.setKeyvals(getContentMapList(content));
                                contentdataoutput.setKeyval(getContentMap(content));
                            }
                            setClassrefVals(contentdataoutput.getKeyvals().get(0), clazz, null);
                            setAssetrefVals(contentdataoutput.getKeyvals().get(0), clazz);
                            try {
                                contentdataoutput.setDifference(hasDifference(cfclasscontent));
                                contentdataoutput.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                            } catch (Exception ex) {

                            }
                            result.add(contentdataoutput.getKeyvals().get(0));
                        }
                    }
                }
            } catch (NoResultException ex) {
                session_tables.close();
            }

            return result;
        }
    }

    public List<Map<String, String>> getList(CfClass clazz, List<HashMap<String, String>> filter_list) {
        if (0 == useHibernate) {
            List<Map<String, String>> result = new ArrayList<>();
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
            for (CfClasscontent cc : classcontentList) {
                if (!cc.isScrapped()) {
                    HashMap<String, String> attributmap = new HashMap<>();

                    for (HashMap<String, String> filter : filter_list) {
                        String field = filter.get("field");
                        String op = filter.get("op");
                        String value1 = filter.get("value1");
                        String value2 = filter.get("value2");

                        CfAttribut attribut = cfattributservice.findByNameAndClassref(field, clazz);
                        if (!field.isEmpty()) {
                            List<CfAttributcontent> aclist = cfattributcontentService.findByClasscontentref(cc);
                            if (checkCompare(aclist, cc, field, value1)) {
                                List keyvals = getContentOutputKeyvalList(aclist);
                                result.add((Map)keyvals.get(0));
                            }
                        } else {
                            List<CfAttributcontent> aclist = cfattributcontentService.findByClasscontentref(cc);
                            List keyvals = getContentOutputKeyvalList(aclist);
                            result.add((Map)keyvals.get(0));
                        }

                    }
                }
            }
            return result;
        } else {
            List<Map<String, String>> result = new ArrayList<>();
            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            HashMap searchmap = new HashMap<>();
            for (HashMap<String, String> filter : filter_list) {
                String field = filter.get("field");
                String op = filter.get("op");
                String value1 = filter.get("value1");
                String value2 = filter.get("value2");
                if ((null != field) && (!field.contains("."))) {
                    if ((!value2.isEmpty()) && (0 == op.compareToIgnoreCase("bt"))) {
                        searchmap.put(field+"_1", ":" + op + ":" + value1 + ":" + value2);
                    } else {
                        searchmap.put(field+"_1", ":" + op + ":" + value1);
                    }
                }
            }
            Query query = hibernateUtil.getQuery(session_tables, searchmap, clazz.getName(), null);
            if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
                LOGGER.info("Query: " + query.getQueryString());
            }
            try {
                List<Map> contentliste = (List<Map>) query.getResultList();

                session_tables.close();
                for (Map content : contentliste) {
                    CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                    if (null != cfclasscontent) {
                        if (!cfclasscontent.isScrapped()) {
                            boolean found = false;
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(cfclasscontent);
                            if (cfclasscontent.getClassref().isEncrypted()) {
                                contentdataoutput.setKeyvals(getContentMapListDecrypted(content, cfclasscontent.getClassref()));
                                contentdataoutput.setKeyval(getContentMapDecrypted(content, cfclasscontent.getClassref()));
                            } else {
                                contentdataoutput.setKeyvals(getContentMapList(content));
                                contentdataoutput.setKeyval(getContentMap(content));
                            }
                            found = setClassrefVals(contentdataoutput.getKeyvals().get(0), clazz, filter_list);
                            setAssetrefVals(contentdataoutput.getKeyvals().get(0), clazz);
                            try {
                                contentdataoutput.setDifference(hasDifference(cfclasscontent));
                                contentdataoutput.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                            } catch (Exception ex) {

                            }
                            if (found)
                                result.add(contentdataoutput.getKeyvals().get(0));
                        }
                    }
                }
            } catch (NoResultException ex) {
                session_tables.close();
            }

            return result;
        }
    }

    private boolean checkCompare(List<CfAttributcontent> aclist, CfClasscontent cc, String attributname, Object attributvalue) {
        boolean found = false;
        for (CfAttributcontent ac : aclist) {
            if ((!found) && (0 == ac.getAttributref().getName().compareToIgnoreCase(attributname))) {
                switch (ac.getAttributref().getAttributetype().getName()) {
                    case "string":
                    case "text":
                    case "htmltext":
                    case "markdown":
                        if ((ac.getClasscontentref().getClassref().isEncrypted()) && (!ac.getAttributref().getIdentity())) {
                            if (0 == EncryptUtil.decrypt(ac.getContentString(), propertyUtil.getPropertyValue("aes_key")).compareTo((String) attributvalue)) {
                                found = true;
                            }
                        } else {
                            if ((null != ac.getContentString()) && (0 == ac.getContentString().compareTo((String) attributvalue))) {
                                found = true;
                            }
                        }
                        break;
                    case "boolean":
                        if ((null != ac.getContentBoolean()) && (ac.getContentBoolean() == (boolean) attributvalue)) {
                            found = true;
                        }
                        break;
                    case "integer":
                        if ((null != ac.getContentInteger()) && (ac.getContentInteger().longValue() == (long) attributvalue)) {
                            found = true;
                        }
                        break;
                    case "real":
                        if ((null != ac.getContentReal()) && (ac.getContentReal().floatValue()  == (float) attributvalue)) {
                            found = true;
                        }
                        break;
                }
            }
        }
        return found;
    }
}
