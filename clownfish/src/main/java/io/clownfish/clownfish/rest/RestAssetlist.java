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
import io.clownfish.clownfish.datamodels.RestAssetlistParameter;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
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
public class RestAssetlist {
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfAssetlistcontentService cfassetlistcontentService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAssetlist.class);
    
    @PostMapping(value = "/getassetlists", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestAssetlistParameter restGetAssetlists(@RequestBody RestAssetlistParameter idp) {
        return getAssetlists(idp);
    }
    
    private RestAssetlistParameter getAssetlists(RestAssetlistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    idp.setAssetlist(cfassetlistService.findAll());
                    idp.setReturncode("OK");

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

    @PostMapping(value = "/insertassetlist", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestAssetlistParameter restInsertAssetlist(@RequestBody RestAssetlistParameter idp) {
        return insertDatalist(idp);
    }
    
    private RestAssetlistParameter insertDatalist(RestAssetlistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfAssetlist list = cfassetlistService.findByName(idp.getListname().trim().replaceAll("\\s+", "_"));
                    if (null != list) {
                        idp.setReturncode("Duplicate Assetlistcontent");
                    } else {
                        CfAssetlist newlist = new CfAssetlist();
                        newlist.setName(idp.getListname().trim().replaceAll("\\s+", "_"));

                        CfAssetlist newlist2 = cfassetlistService.create(newlist);
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
    
    @PostMapping(value = "/deleteassetlist", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestAssetlistParameter restDeleteAssetlist(@RequestBody RestAssetlistParameter idp) {
        return deleteAssetlist(idp);
    }
    
    private RestAssetlistParameter deleteAssetlist(RestAssetlistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfAssetlist list = cfassetlistService.findByName(idp.getListname());
                        List<CfAssetlistcontent> listcontentList = cfassetlistcontentService.findByAssetlistref(list.getId());
                        for (CfAssetlistcontent listcontent : listcontentList) {
                            cfassetlistcontentService.delete(listcontent);
                        }
                        cfassetlistService.delete(list);
                        idp.setReturncode("OK");
                    } catch (javax.persistence.NoResultException ex) {
                        idp.setReturncode("Assetlist not found");
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
