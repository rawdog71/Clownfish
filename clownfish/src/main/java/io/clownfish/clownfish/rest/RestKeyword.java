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
import io.clownfish.clownfish.datamodels.RestKeywordParameter;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
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
public class RestKeyword {
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestKeyword.class);

    @PostMapping(value = "/insertkeyword", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordParameter restInsertKeyword(@RequestBody RestKeywordParameter ikp) {
        return insertKeyword(ikp);
    }
    
    private RestKeywordParameter insertKeyword(RestKeywordParameter ikp) {
        try {
            String token = ikp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ikp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfKeyword keyword = cfkeywordService.findByName(ikp.getKeyword());
                    if (null != keyword) {
                        LOGGER.warn("Duplicate Keyword");
                        ikp.setReturncode("Duplicate Keyword");
                    } else {
                        CfKeyword newkeyword = new CfKeyword();
                        newkeyword.setName(ikp.getKeyword());
                        CfKeyword newkeyword2 = cfkeywordService.create(newkeyword);
                        ikp.setId(newkeyword2.getId());
                        ikp.setReturncode("OK");
                    }
                } else {
                    ikp.setReturncode("Wrong API KEY");
                }
            } else {
                ikp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            ikp.setReturncode("NoResultException");
        }
        return ikp;
    }

    @PostMapping(value = "/updatekeyword", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordParameter restUpdateKeyword(@RequestBody RestKeywordParameter ikp) {
        return updateKeyword(ikp);
    }
    
    private RestKeywordParameter updateKeyword(RestKeywordParameter ikp) {
        try {
            String token = ikp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ikp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfKeyword keyword = cfkeywordService.findById(ikp.getId());
                        keyword.setName(ikp.getKeyword());
                        CfKeyword newkeyword2 = cfkeywordService.edit(keyword);
                        ikp.setReturncode("OK");
                    } catch (javax.persistence.NoResultException ex) {
                        LOGGER.warn("No Keyword");
                        ikp.setReturncode("No Keyword");
                    }
                } else {
                    ikp.setReturncode("Wrong API KEY");
                }
            } else {
                ikp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            ikp.setReturncode("NoResultException");
        }
        return ikp;
    }
}
