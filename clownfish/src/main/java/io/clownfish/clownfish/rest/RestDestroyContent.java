/*
 * Copyright 2020 SulzbachR.
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
package io.clownfish.clownfish.rest;

import io.clownfish.clownfish.datamodels.UpdateContentParameter;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SulzbachR
 */
@RestController
public class RestDestroyContent {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAttributetypeService cfattributetypeService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfSitecontentService cfsitecontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired ContentUtil contentUtil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired HibernateUtil hibernateUtil;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestDestroyContent.class);

    @PostMapping("/destroycontent")
    public UpdateContentParameter restDestroyContent(@RequestBody UpdateContentParameter ucp) {
        return destroyContent(ucp);
    }
    
    private UpdateContentParameter destroyContent(UpdateContentParameter ucp) {
        try {
            String apikey = ucp.getApikey();
            if (apikeyutil.checkApiKey(apikey, "DestroyContent")) {
                CfClass clazz = cfclassService.findByName(ucp.getClassname());
                try {
                    CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname());
                    // Delete corresponding attributcontent entries
                    List<CfAttributcontent> attributcontentlistdummy = cfattributcontentService.findByClasscontentref(classcontent);
                    for (CfAttributcontent attributcontent : attributcontentlistdummy) {
                        cfattributcontentService.delete(attributcontent);
                    }

                    // Delete corresponding listcontent entries
                    List<CfListcontent> selectedcontent = cflistcontentService.findByClasscontentref(classcontent.getId());
                    for (CfListcontent listcontent : selectedcontent) {
                        cflistcontentService.delete(listcontent);
                    }

                    // Delete corresponding keywordcontent entries
                    List<CfClasscontentkeyword> keywordcontentdummy = cfclasscontentkeywordService.findByClassContentRef(classcontent.getId());
                    for (CfClasscontentkeyword keywordcontent : keywordcontentdummy) {
                        cfclasscontentkeywordService.delete(keywordcontent);
                    }

                    // Delete corresponding sitecontent entries
                    List<CfSitecontent> sitecontentdummy = cfsitecontentService.findByClasscontentref(classcontent.getId());
                    for (CfSitecontent sitecontent : sitecontentdummy) {
                        cfsitecontentService.delete(sitecontent);
                    }

                    cfclasscontentService.delete(classcontent);
                    try {
                        hibernateUtil.deleteContent(classcontent);
                    } catch (javax.persistence.NoResultException ex) {
                        LOGGER.warn(ex.getMessage());
                    }
                    
                    ucp.setReturncode("OK");
                } catch (javax.persistence.NoResultException ex) {
                    ucp.setReturncode("Classcontent not found");
                }
            } else {
                ucp.setReturncode("Wrong API KEY");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }
}
