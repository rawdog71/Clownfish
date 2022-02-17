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
import io.clownfish.clownfish.datamodels.RestAssetlistcontentParameter;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAssetlistcontentPK;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.FolderUtil;
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
public class RestAssetlistcontent {
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfAssetlistcontentService cfassetlistcontentService;
    @Autowired FolderUtil folderUtil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired HibernateUtil hibernateutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAssetlistcontent.class);

    @PostMapping("/insertassetlistcontent")
    public RestAssetlistcontentParameter restInsertAssetlistcontent(@RequestBody RestAssetlistcontentParameter ilcp) {
        return insertAssetlistcontent(ilcp);
    }
    
    private RestAssetlistcontentParameter insertAssetlistcontent(RestAssetlistcontentParameter ilcp) {
        try {
            String token = ilcp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ilcp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfAssetlist assetlist = cfassetlistService.findByName(ilcp.getListname());
                        CfAsset asset = cfassetService.findById(ilcp.getAssetref());

                        CfAssetlistcontent listcontent = new CfAssetlistcontent();
                        CfAssetlistcontentPK cfassetlistcontentPK = new CfAssetlistcontentPK();
                        cfassetlistcontentPK.setAssetlistref(assetlist.getId());
                        cfassetlistcontentPK.setAssetref(asset.getId());
                        listcontent.setCfAssetlistcontentPK(cfassetlistcontentPK);
                        cfassetlistcontentService.create(listcontent);
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
    
    @PostMapping("/deleteassetlistcontent")
    public RestAssetlistcontentParameter restDeleteAssetlistcontent(@RequestBody RestAssetlistcontentParameter ilcp) {
        return deleteAssetlistcontent(ilcp);
    }
    
    private RestAssetlistcontentParameter deleteAssetlistcontent(RestAssetlistcontentParameter ilcp) {
        try {
            String token = ilcp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ilcp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        ilcp.setReturncode("");
                        CfAssetlist assetlist = cfassetlistService.findByName(ilcp.getListname());
                        CfAsset asset = cfassetService.findById(ilcp.getAssetref());
                        List<CfAssetlistcontent> listcontentList = cfassetlistcontentService.findByAssetlistref(assetlist.getId());

                        for (CfAssetlistcontent listcontent : listcontentList) {
                            if (listcontent.getCfAssetlistcontentPK().getAssetref()== asset.getId()) {
                                cfassetlistcontentService.delete(listcontent);
                            }
                        }
                        ilcp.setReturncode("OK");
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
