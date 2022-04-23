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

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSiteassetlist;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitekeywordlist;
import io.clownfish.clownfish.dbentities.CfSitelist;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSiteassetlistService;
import io.clownfish.clownfish.serviceinterface.CfSitekeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Component
public class SiteUtil {
    @Autowired CfSiteService cfsiteService;
    @Autowired CfSitelistService cfsitelistService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfClassService cfclassService;
    @Autowired CfListService cflistService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired CfAttributcontentService cfattributcontentService;
    @Autowired CfAssetService cfassetService;
    @Autowired CfAssetlistService cfassetlistService;
    @Autowired CfSiteassetlistService cfsiteassetlistService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfSitekeywordlistService cfsitekeywordlistService;
    @Autowired CfKeywordlistService cfkeywordlistService;
    @Autowired CfKeywordlistcontentService cfkeywordlistcontentService;
    @Autowired ClassUtil classutil;
    @Autowired HibernateUtil hibernateutil;
    @Autowired private PropertyUtil propertyUtil;
    final transient Logger LOGGER = LoggerFactory.getLogger(SiteUtil.class);
    
    @Value("${hibernate.use:0}") int useHibernate;
    
    public SiteUtil() {
    }
    
    public Map getSitelist_list(CfSite cfsite, Map sitecontentmap) {
        List<CfSitelist> sitelist_list = new ArrayList<>();
        sitelist_list.addAll(cfsitelistService.findBySiteref(cfsite.getId()));
        if (!sitelist_list.isEmpty()) {
            for (CfSitelist sitelist : sitelist_list) {
                CfList cflist = cflistService.findById(sitelist.getCfSitelistPK().getListref());
                Map listcontentmap = new LinkedHashMap();

                List<CfListcontent> contentlist = cflistcontentService.findByListref(cflist.getId());
                for (CfListcontent listcontent : contentlist) {
                    CfClasscontent classcontent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
                    cfclassService.findById(classcontent.getClassref().getId());
                    List<CfAttributcontent> attributcontentlist = new ArrayList<>();
                    attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
                    if (0 == useHibernate) {
                        listcontentmap.put(classcontent.getName(), classutil.getattributmap(classcontent));
                    } else {
                        listcontentmap.put(classcontent.getName(), hibernateutil.getContent(classcontent.getClassref().getName(), classcontent.getId()));
                    }
                }
                sitecontentmap.put(cflist.getName(), listcontentmap);
            }
        }
        return sitecontentmap;
    }
    
    public Map getSitelist_list(List<CfList> sitelist, Map sitecontentmap) {
        if (!sitelist.isEmpty()) {
            for (CfList cflist : sitelist) {
                Map listcontentmap = new LinkedHashMap();

                List<CfListcontent> contentlist = cflistcontentService.findByListref(cflist.getId());
                for (CfListcontent listcontent : contentlist) {
                    CfClasscontent classcontent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
                    cfclassService.findById(classcontent.getClassref().getId());
                    List<CfAttributcontent> attributcontentlist = new ArrayList<>();
                    attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
                    if (0 == useHibernate) {
                        listcontentmap.put(classcontent.getName(), classutil.getattributmap(classcontent));
                    } else {
                        listcontentmap.put(classcontent.getName(), hibernateutil.getContent(classcontent.getClassref().getName(), classcontent.getId()));
                    }
                }
                sitecontentmap.put(cflist.getName(), listcontentmap);
            }
        }
        return sitecontentmap;
    }
    
    public Map getSitecontentmapList(List<CfSitecontent> sitecontentlist) {
        Map sitecontentmapdummy = new LinkedHashMap();
        for (CfSitecontent sitecontent : sitecontentlist) {
            CfClasscontent classcontent = cfclasscontentService.findById(sitecontent.getCfSitecontentPK().getClasscontentref());
            if (null != classcontent) {
                List<CfAttributcontent> attributcontentlist = new ArrayList<>();
                attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
                if (0 == useHibernate) {
                    sitecontentmapdummy.put(classcontent.getName(), classutil.getattributmap(classcontent));
                } else {
                    sitecontentmapdummy.put(classcontent.getName(), hibernateutil.getContent(classcontent.getClassref().getName(), classcontent.getId()));
                }
            } else {
                LOGGER.warn("CLASSCONTENT NOT FOUND (deleted or on scrapyard): " + sitecontent.getCfSitecontentPK().getClasscontentref());
            }
        }
        return sitecontentmapdummy;
    }
    
    public Map getClasscontentmapList(List<CfClasscontent> classcontentlist) {
        Map sitecontentmapdummy = new LinkedHashMap();
        for (CfClasscontent classcontent : classcontentlist) {
            if (null != classcontent) {
                List<CfAttributcontent> attributcontentlist = new ArrayList<>();
                attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
                if (0 == useHibernate) {
                    sitecontentmapdummy.put(classcontent.getName(), classutil.getattributmap(classcontent));
                } else {
                    sitecontentmapdummy.put(classcontent.getName(), hibernateutil.getContent(classcontent.getClassref().getName(), classcontent.getId()));
                }
                
                if (classcontent.getClassref().isEncrypted()) {
                    HashMap contentmap = (HashMap) sitecontentmapdummy.get(classcontent.getName());
                    for (Object key : contentmap.keySet()) {
                        if (null != getAttributValue(attributcontentlist, key.toString())) {
                            HashMap am = (HashMap) sitecontentmapdummy.get(classcontent.getName());
                            am.put(key, getAttributValue(attributcontentlist, key.toString()));
                        }
                    }
                }
                
            } else {
                LOGGER.warn("CLASSCONTENT NOT FOUND (deleted or on scrapyard): " + classcontent.getId());
            }
        }
        return sitecontentmapdummy;
    }
    
    public Map getSiteAssetlibrary(CfSite cfsite, Map sitecontentmap) {
        List<CfSiteassetlist> siteassetlibrary = new ArrayList<>();
        siteassetlibrary.addAll(cfsiteassetlistService.findBySiteref(cfsite.getId()));
        
        HashMap<String, ArrayList> assetlibraryMap = new HashMap<>();
        for (CfSiteassetlist siteassetlist : siteassetlibrary) {
            CfAssetlist cfassetlist = cfassetlistService.findById(siteassetlist.getCfSiteassetlistPK().getAssetlistref());
            List<CfAssetlistcontent> assetlist = new ArrayList<>();
            assetlist.addAll(cfassetlistcontentService.findByAssetlistref(cfassetlist.getId()));
            ArrayList<CfAsset> dummyassetlist = new ArrayList<>();
            for (CfAssetlistcontent assetcontent : assetlist) {
                CfAsset asset = cfassetService.findById(assetcontent.getCfAssetlistcontentPK().getAssetref());
                if (null != asset) {
                    dummyassetlist.add(asset);
                } else {
                    LOGGER.warn("ASSET NOT FOUND (deleted or on scrapyard): " + assetcontent.getCfAssetlistcontentPK().getAssetref());
                }
            }
            assetlibraryMap.put(cfassetlist.getName(), dummyassetlist);
        }
        sitecontentmap.put("AssetLibrary", assetlibraryMap);
        return sitecontentmap;
    }
    

    public Map getAssetlibrary(List<CfAssetlist> assetlibrary_list, Map sitecontentmap) {
        HashMap<String, ArrayList> assetlibraryMap = new HashMap<>();
        for (CfAssetlist cfassetlist : assetlibrary_list) {
            List<CfAssetlistcontent> assetlist = new ArrayList<>();
            assetlist.addAll(cfassetlistcontentService.findByAssetlistref(cfassetlist.getId()));
            ArrayList<CfAsset> dummyassetlist = new ArrayList<>();
            for (CfAssetlistcontent assetcontent : assetlist) {
                CfAsset asset = cfassetService.findById(assetcontent.getCfAssetlistcontentPK().getAssetref());
                if (null != asset) {
                    dummyassetlist.add(asset);
                } else {
                    LOGGER.warn("ASSET NOT FOUND (deleted or on scrapyard): " + assetcontent.getCfAssetlistcontentPK().getAssetref());
                }
            }
            assetlibraryMap.put(cfassetlist.getName(), dummyassetlist);
        }
        sitecontentmap.put("AssetLibrary", assetlibraryMap);
        return sitecontentmap;
    }
    
    public Map getSiteKeywordlibrary(CfSite cfsite, Map sitecontentmap) {
        List<CfSitekeywordlist> sitekeywordlibrary = new ArrayList<>();
        sitekeywordlibrary.addAll(cfsitekeywordlistService.findBySiteref(cfsite.getId()));
        
        HashMap<String, ArrayList> keywordlibraryMap = new HashMap<>();
        for (CfSitekeywordlist sitekeywordlist : sitekeywordlibrary) {
            CfKeywordlist cfkeywordlist = cfkeywordlistService.findById(sitekeywordlist.getCfSitekeywordlistPK().getKeywordlistref());
            List<CfKeywordlistcontent> keywordlist = new ArrayList<>();
            keywordlist.addAll(cfkeywordlistcontentService.findByKeywordlistref(cfkeywordlist.getId()));
            ArrayList<CfKeyword> dummykeywordlist = new ArrayList<>();
            for (CfKeywordlistcontent keywordcontent : keywordlist) {
                CfKeyword keyword = cfkeywordService.findById(keywordcontent.getCfKeywordlistcontentPK().getKeywordref());
                dummykeywordlist.add(keyword);
            }
            keywordlibraryMap.put(cfkeywordlist.getName(), dummykeywordlist);
        }
        sitecontentmap.put("KeywordLibrary", keywordlibraryMap);
        return sitecontentmap;
    }
    
    public Map getSiteKeywordlibrary(List<CfKeywordlist> keywordlibrary_list, Map sitecontentmap) {
        HashMap<String, ArrayList> keywordlibraryMap = new HashMap<>();
        for (CfKeywordlist cfkeywordlist : keywordlibrary_list) {
            List<CfKeywordlistcontent> keywordlist = new ArrayList<>();
            keywordlist.addAll(cfkeywordlistcontentService.findByKeywordlistref(cfkeywordlist.getId()));
            ArrayList<CfKeyword> dummykeywordlist = new ArrayList<>();
            for (CfKeywordlistcontent keywordcontent : keywordlist) {
                CfKeyword keyword = cfkeywordService.findById(keywordcontent.getCfKeywordlistcontentPK().getKeywordref());
                dummykeywordlist.add(keyword);
            }
            keywordlibraryMap.put(cfkeywordlist.getName(), dummykeywordlist);
        }
        sitecontentmap.put("KeywordLibrary", keywordlibraryMap);
        return sitecontentmap;
    }
    
    private String getAttributValue(List<CfAttributcontent> attributcontentlist, String key) {
        for (CfAttributcontent ac : attributcontentlist) {
            if ((0 == ac.getAttributref().getName().compareToIgnoreCase(key)) && (!ac.getAttributref().getIdentity()) && (0 == ac.getAttributref().getAttributetype().getName().compareToIgnoreCase("string"))) {
                return EncryptUtil.decrypt(ac.getContentString(), propertyUtil.getPropertyValue("aes_key")) ;
            }
        }
        return null;
    }
    
    public String generateShorturl() {
        String shorturl = "";
        boolean notfound = true;
        while (notfound) {
            for (int i = 1; i <= 5; i++) {
                shorturl += getRandomChar();
            }
            try {
                cfsiteService.findByShorturl(shorturl);
                notfound = true;
            } catch (Exception ex) {
                notfound = false;
            }
        }
        return shorturl;
    }
    
    private char getRandomChar() {
        Random rand = new Random();
        int i = rand.nextInt(62);
        int j = 0;
        if (i < 10) {
                j = i + 48;
        } else if (i > 9 && i <= 35) {
                j = i + 55;
        } else {
                j = i + 61;
        }
        return (char) j;
    }
}
