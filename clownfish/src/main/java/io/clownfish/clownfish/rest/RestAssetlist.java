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
public class RestAssetlist {
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfAssetlistcontentService cfassetlistcontentService;
    @Autowired FolderUtil folderUtil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAssetlist.class);

    @PostMapping("/insertassetlist")
    public RestAssetlistParameter restInsertAssetlist(@RequestBody RestAssetlistParameter idp) {
        return insertDatalist(idp);
    }
    
    private RestAssetlistParameter insertDatalist(RestAssetlistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "GetAssetLibraries")) {
                    try {
                        CfAssetlist list = cfassetlistService.findByName(idp.getListname());
                        idp.setReturncode("Duplicate Assetlistcontent");
                    } catch (javax.persistence.NoResultException ex) {
                        CfAssetlist newlist = new CfAssetlist();
                        newlist.setName(idp.getListname());

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
    
    @PostMapping("/deleteassetlist")
    public RestAssetlistParameter restDeleteAssetlist(@RequestBody RestAssetlistParameter idp) {
        return deleteAssetlist(idp);
    }
    
    private RestAssetlistParameter deleteAssetlist(RestAssetlistParameter idp) {
        try {
            String token = idp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = idp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "GetAssetLibraries")) {
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
