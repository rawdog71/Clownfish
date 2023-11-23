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

import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.RestListcontentParameter;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfListcontentPK;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SulzbachR
 */
@RestController
public class RestListcontent {
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired HibernateUtil hibernateutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestListcontent.class);

    @PostMapping(value = "/insertlistcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestListcontentParameter restInsertDatalist(@RequestBody RestListcontentParameter ilcp) {
        return insertListcontent(ilcp);
    }
    
    private RestListcontentParameter insertListcontent(RestListcontentParameter ilcp) {
        try {
            String token = ilcp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ilcp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfList list = cflistService.findByName(ilcp.getListname());
                        CfClasscontent classcontent = cfclasscontentService.findByName(ilcp.getClasscontentname());

                        CfListcontent listcontent = new CfListcontent();
                        CfListcontentPK cfListcontentPK = new CfListcontentPK();
                        cfListcontentPK.setListref(list.getId());
                        cfListcontentPK.setClasscontentref(classcontent.getId());
                        listcontent.setCfListcontentPK(cfListcontentPK);
                        cflistcontentService.create(listcontent);
                        hibernateutil.updateRelation(list);
                        ilcp.setReturncode("OK");
                    } catch (Exception ex) {
                        ilcp.setReturncode("Exception");
                    }
                } else {
                    ilcp.setReturncode("Wrong API KEY");
                }
            } else {
                ilcp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ilcp.setReturncode("NoResultException");
        }
        return ilcp;
    }
    
    @PostMapping(value = "/deletelistcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestListcontentParameter restDeleteDatalist(@RequestBody RestListcontentParameter ilcp) {
        return deleteListcontent(ilcp);
    }
    
    private RestListcontentParameter deleteListcontent(RestListcontentParameter ilcp) {
        try {
            String token = ilcp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ilcp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        ilcp.setReturncode("");
                        CfList list = cflistService.findByName(ilcp.getListname());
                        CfClasscontent classcontent = cfclasscontentService.findByName(ilcp.getClasscontentname());
                        List<CfListcontent> listcontentList = cflistcontentService.findByListref(list.getId());

                        for (CfListcontent listcontent : listcontentList) {
                            if (listcontent.getCfListcontentPK().getClasscontentref() == classcontent.getId()) {
                                cflistcontentService.delete(listcontent);
                                ilcp.setReturncode("OK");
                            }
                        }
                    } catch (javax.persistence.EntityExistsException ex) {
                        ilcp.setReturncode("EntityExistsException");
                    }
                } else {
                    ilcp.setReturncode("Wrong API KEY");
                }
            } else {
                ilcp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ilcp.setReturncode("NoResultException");
        }
        return ilcp;
    }
}
