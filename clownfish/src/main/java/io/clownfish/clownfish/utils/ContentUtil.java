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
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
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
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
public class ContentUtil {
    @Autowired transient CfAttributetypeService cfattributetypeService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired FolderUtil folderUtil;
    @Autowired IndexService indexService;
    @Autowired ContentIndexer contentIndexer;
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
                if (null != attributcontent.getClasscontentref()) {
                    return new AttributDef(attributcontent.getClasscontentref().getName(), "classref");
                } else {
                    return new AttributDef(null, "classref");
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
                        selectedAttribut.setContentString(editContent);
                    }
                    break;
                case "hashstring":
                    String salt = PasswordUtil.getSalt(30);
                    selectedAttribut.setContentString(PasswordUtil.generateSecurePassword(editContent, salt));
                    selectedAttribut.setSalt(salt);
                    break;    
                case "integer":
                    selectedAttribut.setContentInteger(BigInteger.valueOf(Long.parseLong(editContent)));
                    break;
                case "real":
                    selectedAttribut.setContentReal(Double.parseDouble(editContent));
                    break;
                case "htmltext":
                    selectedAttribut.setContentText(editContent);
                    break;    
                case "text":
                    selectedAttribut.setContentText(editContent);
                    break;
                case "markdown":
                    selectedAttribut.setContentText(editContent);
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
                            datum = DateTime.parse(editContent, fmt).toDate();
                            selectedAttribut.setContentDate(datum);
                        }
                    }
                    break;
                case "media":
                    if (null != editContent) {
                        try {
                            CfAsset asset = cfassetService.findByName(editContent);
                            selectedAttribut.setContentInteger(BigInteger.valueOf(asset.getId()));
                        } catch (Exception ex) {
                            selectedAttribut.setContentInteger(null);
                            LOGGER.error("INSERTCONTENT: Media " + editContent + " not found!");
                        }
                    } else {
                        selectedAttribut.setContentInteger(null);
                    }
                    break;    
                case "classref":
                    if (null != editContent) {
                        CfList list_ref = cflistService.findById(Long.parseLong(editContent));
                        selectedAttribut.setClasscontentlistref(list_ref);
                    }
                    break;
                case "assetref":
                    if (null != editContent) {
                        CfAssetlist assetlist_ref = cfassetlistService.findById(Long.parseLong(editContent));
                        selectedAttribut.setAssetcontentlistref(assetlist_ref);
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
                dummyoutputmap.put(knattribut.getName(), attributdef.getValue());
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
}
