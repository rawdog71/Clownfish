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
import io.clownfish.clownfish.datamodels.RestKeywordAssetParameter;
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.dbentities.CfAssetkeywordPK;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
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
public class RestKeywordAsset {
    @Autowired transient CfAssetKeywordService cfassetkeywordService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestKeywordAsset.class);

    @PostMapping(value = "/insertkeywordasset", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordAssetParameter restInsertKeywordAsset(@RequestBody RestKeywordAssetParameter iklp) {
        return insertKeywordAsset(iklp);
    }
    
    private RestKeywordAssetParameter insertKeywordAsset(RestKeywordAssetParameter iklp) {
        try {
            String token = iklp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = iklp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfAssetkeyword assetkeyword = cfassetkeywordService.findByAssetRefAndKeywordRef(iklp.getAssetref(), iklp.getKeywordref());
                    if (null != assetkeyword) {
                        LOGGER.warn("Duplicate AssetKeyword");
                        iklp.setReturncode("Duplicate AssetKeyword");
                    } else {
                        CfAssetkeyword newassetkeyword = new CfAssetkeyword();
                        newassetkeyword.setCfAssetkeywordPK(new CfAssetkeywordPK(iklp.getAssetref(), iklp.getKeywordref()));
                        CfAssetkeyword newassetkeyword2 = cfassetkeywordService.create(newassetkeyword);
                        iklp.setAssetref(newassetkeyword2.getCfAssetkeywordPK().getAssetref());
                        iklp.setKeywordref(newassetkeyword2.getCfAssetkeywordPK().getKeywordref());
                        iklp.setReturncode("OK");
                    }
                } else {
                    iklp.setReturncode("Wrong API KEY");
                }
            } else {
                iklp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            iklp.setReturncode("NoResultException");
        }
        return iklp;
    }
    
    @PostMapping(value = "/deletekeywordasset", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordAssetParameter restDeleteKeywordContent(@RequestBody RestKeywordAssetParameter iklp) {
        return deleteKeywordAsset(iklp);
    }
    
    private RestKeywordAssetParameter deleteKeywordAsset(RestKeywordAssetParameter iklp) {
        try {
            String token = iklp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = iklp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfAssetkeyword assetkeyword = cfassetkeywordService.findByAssetRefAndKeywordRef(iklp.getAssetref(), iklp.getKeywordref());
                        cfassetkeywordService.delete(assetkeyword);
                        iklp.setReturncode("OK");
                    } catch (javax.persistence.NoResultException ex) {
                        LOGGER.warn("AssetKeyword not found");
                        iklp.setReturncode("AssetKeyword not found");
                    }
                } else {
                    iklp.setReturncode("Wrong API KEY");
                }
            } else {
                iklp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            iklp.setReturncode("NoResultException");
        }
        return iklp;
    }
}
