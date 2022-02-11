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
import io.clownfish.clownfish.datamodels.RestDatalistParameter;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.FolderUtil;
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
public class RestDatalist {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired FolderUtil folderUtil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestDatalist.class);
    
    @PostMapping("/getdatalists")
    public RestDatalistParameter restGetDatalists(@RequestBody RestDatalistParameter idp) {
        return getDatalists(idp);
    }
    
    private RestDatalistParameter getDatalists(RestDatalistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "GetDatalist")) {
                    if ((null == idp.getClassname()) || (idp.getClassname().isBlank())) {
                        idp.setList(cflistService.findAll());
                        idp.setReturncode("OK");
                    } else {
                        idp.setList(cflistService.findByClassref(cfclassService.findByName(idp.getClassname())));
                        idp.setReturncode("OK");
                    }
                } else {
                    idp.setReturncode("Wrong API KEY");
                }
            } else {
                idp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            idp.setReturncode("NoResultException");
        }
        return idp;
    }

    @PostMapping("/insertdatalist")
    public RestDatalistParameter restInsertDatalist(@RequestBody RestDatalistParameter idp) {
        return insertDatalist(idp);
    }
    
    private RestDatalistParameter insertDatalist(RestDatalistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
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
            } else {
                idp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            idp.setReturncode("NoResultException");
        }
        return idp;
    }
    
    @PostMapping("/deletedatalist")
    public RestDatalistParameter restDeleteDatalist(@RequestBody RestDatalistParameter idp) {
        return deleteDatalist(idp);
    }
    
    private RestDatalistParameter deleteDatalist(RestDatalistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "InsertDatalist")) {
                    try {
                        CfList list = cflistService.findByName(idp.getListname());
                        List<CfListcontent> listcontentList = cflistcontentService.findByListref(list.getId());
                        for (CfListcontent listcontent : listcontentList) {
                            cflistcontentService.delete(listcontent);
                        }
                        cflistService.delete(list);
                        idp.setReturncode("OK");
                    } catch (javax.persistence.NoResultException ex) {
                        idp.setReturncode("Datalist not found");
                    }
                } else {
                    idp.setReturncode("Wrong API KEY");
                }
            } else {
                idp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            idp.setReturncode("NoResultException");
        }
        return idp;
    }
}
