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

import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitelist;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import java.util.ArrayList;
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
    @Autowired ClassUtil classutil;
    
    public SiteUtil() {
    }
    
    public void getSitelist_list(CfSite cfsite, Map sitecontentmap) {
        List<CfSitelist> sitelist_list = new ArrayList<>();
        sitelist_list.addAll(cfsitelistService.findBySiteref(cfsite.getId()));
        for (CfSitelist sitelist : sitelist_list) {
            CfList cflist = cflistService.findById(sitelist.getCfSitelistPK().getListref());
            Map listcontentmap = new LinkedHashMap();

            List<CfListcontent> contentlist = cflistcontentService.findByListref(cflist.getId());
            for (CfListcontent listcontent : contentlist) {
                CfClasscontent classcontent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
                CfClass cfclass = cfclassService.findById(classcontent.getClassref().getId());
                List<CfAttributcontent> attributcontentlist = new ArrayList<>();
                attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
                listcontentmap.put(classcontent.getName(), classutil.getattributmap(classcontent));
            }
            sitecontentmap.put(cflist.getName(), listcontentmap);
        }
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
}
