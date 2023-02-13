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
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfContentversion;
import io.clownfish.clownfish.dbentities.CfContentversionPK;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.lucene.ContentIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListService;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    @Autowired FolderUtil folderUtil;
    @Autowired IndexService indexService;
    @Autowired ContentIndexer contentIndexer;
    @Autowired transient CfContentversionService cfcontentversionService;
    @Autowired ClassUtil classUtil;
    @Autowired private PropertyUtil propertyUtil;
    private @Getter @Setter long currentVersion;
    private @Getter @Setter String content = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentUtil.class);
    
    public AttributDef getAttributContent(long attributtypeid, CfAttributcontent attributcontent) {
        CfAttributetype knattributtype = cfattributetypeService.findById(attributtypeid);
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
                    return new AttributDef(attributcontent.getContentText(), "markdown");
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
    
    public ArrayList getContentOutputKeyval(List<CfAttributcontent> attributcontentList) {
        ArrayList<HashMap> output = new ArrayList<>();
        HashMap<String, String> dummyoutputmap = new HashMap<>();
        attributcontentList.stream().forEach((attributcontent) -> {
            CfAttribut knattribut = cfattributService.findById(attributcontent.getAttributref().getId());
            long attributtypeid = knattribut.getAttributetype().getId();
            AttributDef attributdef = getAttributContent(attributtypeid, attributcontent);
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
    
    public ArrayList getContentMap(Map content) {
        HashMap<String, String> contentMap = new HashMap<>(content);
        ArrayList contenList = new ArrayList<>();
        contenList.add(contentMap);
        return contenList;
    }
    
    public ArrayList getContentMapDecrypted(Map content, CfClass classref) {
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
}
