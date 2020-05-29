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

import io.clownfish.clownfish.datamodels.InsertDatalistParameter;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfListService;
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
public class RestInsertDatalist {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfListService cflistService;
    @Autowired FolderUtil folderUtil;
    @Autowired ApiKeyUtil apikeyutil;
    private static final Logger logger = LoggerFactory.getLogger(RestInsertDatalist.class);

    @PostMapping("/insertdatalist")
    public InsertDatalistParameter restInsertDatalist(@RequestBody InsertDatalistParameter idp) {
        return insertDatalist(idp);
    }
    
    private InsertDatalistParameter insertDatalist(InsertDatalistParameter idp) {
        try {
            String apikey = idp.getApikey();
            if (apikeyutil.checkApiKey(apikey, "InsertDatalist")) {
                try {
                    CfList list = cflistService.findByName(idp.getListname());
                    idp.setReturncode("Duplicate Datalistcontent");
                } catch (javax.persistence.NoResultException ex) {
                    CfClass clazz = cfclassService.findByName(idp.getClassname());
                    
                    CfList newlist = new CfList();
                    newlist.setName(idp.getListname());
                    newlist.setClassref(clazz);
                    
                    CfList newlist2 = cflistService.create(newlist);
                    idp.setListid(newlist2.getId());
                    idp.setReturncode("OK");
                }
            } else {
                idp.setReturncode("Wrong API KEY");
            }
        } catch (javax.persistence.NoResultException ex) {
            idp.setReturncode("NoResultException");
        }
        return idp;
    }
}
