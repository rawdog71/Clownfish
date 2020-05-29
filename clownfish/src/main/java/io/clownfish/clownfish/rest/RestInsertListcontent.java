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

import io.clownfish.clownfish.datamodels.InsertListcontentParameter;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfListcontentPK;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.FolderUtil;
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
public class RestInsertListcontent {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired FolderUtil folderUtil;
    @Autowired ApiKeyUtil apikeyutil;
    private static final Logger logger = LoggerFactory.getLogger(RestInsertListcontent.class);

    @PostMapping("/insertlistcontent")
    public InsertListcontentParameter restInsertDatalist(@RequestBody InsertListcontentParameter ilcp) {
        return insertListcontent(ilcp);
    }
    
    private InsertListcontentParameter insertListcontent(InsertListcontentParameter ilcp) {
        try {
            String apikey = ilcp.getApikey();
            if (apikeyutil.checkApiKey(apikey, "InsertListcontent")) {
                try {
                    CfList list = cflistService.findByName(ilcp.getListname());
                    CfClasscontent classcontent = cfclasscontentService.findByName(ilcp.getClasscontentname());
                    
                    CfListcontent listcontent = new CfListcontent();
                    CfListcontentPK cfListcontentPK = new CfListcontentPK();
                    cfListcontentPK.setListref(list.getId());
                    cfListcontentPK.setClasscontentref(classcontent.getId());
                    listcontent.setCfListcontentPK(cfListcontentPK);
                    cflistcontentService.create(listcontent);
                    ilcp.setReturncode("OK");
                } catch (javax.persistence.EntityExistsException ex) {
                    ilcp.setReturncode("EntityExistsException");
                }
            } else {
                ilcp.setReturncode("Wrong API KEY");
            }
        } catch (javax.persistence.NoResultException ex) {
            ilcp.setReturncode("NoResultException");
        }
        return ilcp;
    }
}
