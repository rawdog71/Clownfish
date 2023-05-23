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

import static io.clownfish.clownfish.constants.ClownfishConst.AccessTypes.TYPE_CLASS;
import io.clownfish.clownfish.datamodels.AuthTokenClasscontent;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.datamodels.RestDatalistParameter;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.AccessManagerUtil;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import java.math.BigInteger;
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
public class RestDatalist {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    @Autowired transient AuthTokenListClasscontent contentauthtokenlist;
    @Autowired AccessManagerUtil accessmanager;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestDatalist.class);
    
    @PostMapping(value = "/getdatalists", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestDatalistParameter restGetDatalists(@RequestBody RestDatalistParameter idp) {
        return getDatalists(idp);
    }
    
    private RestDatalistParameter getDatalists(RestDatalistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    if ((null == idp.getClassname()) || (idp.getClassname().isBlank())) {
                        // !ToDo: #95 check AccessManager
                        AuthTokenClasscontent classcontent = contentauthtokenlist.getAuthtokens().get(token);
                        if (null != classcontent) {
                            idp.setList(cflistService.findNotInList(BigInteger.valueOf(classcontent.getUser().getId())));
                        } else {
                            idp.setList(cflistService.findNotInList(BigInteger.valueOf(0L)));
                        }
                        idp.setReturncode("OK");
                    } else {
                        // !ToDo: #95 check AccessManager
                        CfClass clazz = cfclassService.findByName(idp.getClassname());
                        if (accessmanager.checkAccess(token, TYPE_CLASS.getValue(), BigInteger.valueOf(clazz.getId()))) {
                            idp.setList(cflistService.findByClassref(clazz));
                            idp.setReturncode("OK");
                        }
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

    @PostMapping(value = "/insertdatalist", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestDatalistParameter restInsertDatalist(@RequestBody RestDatalistParameter idp) {
        return insertDatalist(idp);
    }
    
    private RestDatalistParameter insertDatalist(RestDatalistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfList list = cflistService.findByName(idp.getListname().trim().replaceAll("\\s+", "_"));
                        idp.setReturncode("Duplicate Datalistcontent");
                    } catch (javax.persistence.NoResultException ex) {
                        CfClass clazz = cfclassService.findByName(idp.getClassname());

                        CfList newlist = new CfList();
                        newlist.setName(idp.getListname().trim().replaceAll("\\s+", "_"));
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
    
    @PostMapping(value = "/deletedatalist", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestDatalistParameter restDeleteDatalist(@RequestBody RestDatalistParameter idp) {
        return deleteDatalist(idp);
    }
    
    private RestDatalistParameter deleteDatalist(RestDatalistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
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
