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
import io.clownfish.clownfish.serviceinterface.CfSiteassetlistService;
import io.clownfish.clownfish.serviceinterface.CfSitekeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Component
public class SiteUtil {
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
                    listcontentmap.put(classcontent.getName(), classutil.getattributmap(classcontent));
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
            List<CfAttributcontent> attributcontentlist = new ArrayList<>();
            attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
            sitecontentmapdummy.put(classcontent.getName(), classutil.getattributmap(classcontent));
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
                dummyassetlist.add(asset);
            }
            assetlibraryMap.put(cfassetlist.getName(), dummyassetlist);
            //sitecontentmap.put(, listcontentmap);
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
}
