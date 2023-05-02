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
import io.clownfish.clownfish.datamodels.RestKeywordListContentParameter;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontent;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontentPK;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistcontentService;
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
public class RestKeywordListContent {
    @Autowired transient CfKeywordlistcontentService cfkeywordlistcontentService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestKeywordListContent.class);

    @PostMapping(value = "/insertkeywordlistcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordListContentParameter restInsertKeywordContent(@RequestBody RestKeywordListContentParameter iklp) {
        return insertKeywordListContent(iklp);
    }
    
    private RestKeywordListContentParameter insertKeywordListContent(RestKeywordListContentParameter iklp) {
        try {
            String token = iklp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = iklp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfKeywordlistcontent keywordlistcontent = cfkeywordlistcontentService.findByKeywordrefAndKeywordlistref(iklp.getKeywordref(), iklp.getKeywordlistref());
                        LOGGER.warn("Duplicate Keywordlistcontent");
                        iklp.setReturncode("Duplicate Keywordlistcontent");
                    } catch (javax.persistence.NoResultException ex) {
                        CfKeywordlistcontent newkeywordlistcontent = new CfKeywordlistcontent();
                        newkeywordlistcontent.setCfKeywordlistcontentPK(new CfKeywordlistcontentPK(iklp.getKeywordlistref(), iklp.getKeywordref()));
                        CfKeywordlistcontent newkeywordliscontent2 = cfkeywordlistcontentService.create(newkeywordlistcontent);
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
    
    @PostMapping(value = "/deletekeywordlistcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestKeywordListContentParameter restDeleteKeywordContent(@RequestBody RestKeywordListContentParameter iklp) {
        return deleteKeywordListContent(iklp);
    }
    
    private RestKeywordListContentParameter deleteKeywordListContent(RestKeywordListContentParameter iklp) {
        try {
            String token = iklp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = iklp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfKeywordlistcontent keywordlistcontent = cfkeywordlistcontentService.findByKeywordrefAndKeywordlistref(iklp.getKeywordref(), iklp.getKeywordlistref());
                        cfkeywordlistcontentService.delete(keywordlistcontent);
                        iklp.setReturncode("OK");
                    } catch (javax.persistence.NoResultException ex) {
                        LOGGER.warn("Keywordlistcontent not found");
                        iklp.setReturncode("Keywordlistcontent not found");
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
