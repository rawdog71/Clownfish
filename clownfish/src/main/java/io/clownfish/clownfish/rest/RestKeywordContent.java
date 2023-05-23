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
import io.clownfish.clownfish.datamodels.RestKeywordContentParameter;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfClasscontentkeywordPK;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
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
public class RestKeywordContent {
    @Autowired transient CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestKeywordContent.class);

    @PostMapping(value = "/insertkeywordcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordContentParameter restInsertKeywordContent(@RequestBody RestKeywordContentParameter iklp) {
        return insertKeywordContent(iklp);
    }
    
    private RestKeywordContentParameter insertKeywordContent(RestKeywordContentParameter iklp) {
        try {
            String token = iklp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = iklp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfClasscontentkeyword contentkeyword = cfclasscontentkeywordService.findByClasscontentRefAndKeywordRef(iklp.getContentref(), iklp.getKeywordref());
                        LOGGER.warn("Duplicate ContentKeyword");
                        iklp.setReturncode("Duplicate ContentKeyword");
                    } catch (javax.persistence.NoResultException ex) {
                        CfClasscontentkeyword newcontentkeyword = new CfClasscontentkeyword();
                        newcontentkeyword.setCfClasscontentkeywordPK(new CfClasscontentkeywordPK(iklp.getContentref(), iklp.getKeywordref()));
                        CfClasscontentkeyword newcontentkeyword2 = cfclasscontentkeywordService.create(newcontentkeyword);
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
    
    @PostMapping(value = "/deletekeywordcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordContentParameter restDeleteKeywordContent(@RequestBody RestKeywordContentParameter iklp) {
        return deleteKeywordContent(iklp);
    }
    
    private RestKeywordContentParameter deleteKeywordContent(RestKeywordContentParameter iklp) {
        try {
            String token = iklp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = iklp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfClasscontentkeyword contentkeyword = cfclasscontentkeywordService.findByClasscontentRefAndKeywordRef(iklp.getContentref(), iklp.getKeywordref());
                        cfclasscontentkeywordService.delete(contentkeyword);
                        iklp.setReturncode("OK");
                    } catch (javax.persistence.NoResultException ex) {
                        LOGGER.warn("ContentKeyword not found");
                        iklp.setReturncode("ContentKeyword not found");
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
