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
import io.clownfish.clownfish.datamodels.RestKeywordListParameter;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
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
public class RestKeywordList {
    @Autowired transient CfKeywordlistService cfkeywordlistService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestKeywordList.class);

    @PostMapping(value = "/insertkeywordlist", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordListParameter restInsertKeyword(@RequestBody RestKeywordListParameter iklp) {
        return insertKeywordList(iklp);
    }
    
    private RestKeywordListParameter insertKeywordList(RestKeywordListParameter iklp) {
        try {
            String token = iklp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = iklp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfKeywordlist keywordlist = cfkeywordlistService.findByName(iklp.getKeywordlist().trim().replaceAll("\\s+", "_"));
                        LOGGER.warn("Duplicate Keywordlist");
                        iklp.setReturncode("Duplicate Keywordlist");
                    } catch (javax.persistence.NoResultException ex) {
                        CfKeywordlist newkeywordlist = new CfKeywordlist();
                        newkeywordlist.setName(iklp.getKeywordlist().trim().replaceAll("\\s+", "_"));
                        CfKeywordlist newkeywordlist2 = cfkeywordlistService.create(newkeywordlist);
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
}
