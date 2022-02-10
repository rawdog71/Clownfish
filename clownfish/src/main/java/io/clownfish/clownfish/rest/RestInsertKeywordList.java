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

import io.clownfish.clownfish.datamodels.KeywordListParameter;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
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
public class RestInsertKeywordList {
    @Autowired transient CfKeywordlistService cfkeywordlistService;
    @Autowired ApiKeyUtil apikeyutil;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestInsertKeywordList.class);

    @PostMapping("/insertkeywordlist")
    public KeywordListParameter restInsertKeyword(@RequestBody KeywordListParameter iklp) {
        return insertKeywordList(iklp);
    }
    
    private KeywordListParameter insertKeywordList(KeywordListParameter iklp) {
        try {
            String apikey = iklp.getApikey();
            if (apikeyutil.checkApiKey(apikey, "GetKeywordLibraries")) {
                try {
                    CfKeywordlist keywordlist = cfkeywordlistService.findByName(iklp.getKeywordlist());
                    LOGGER.warn("Duplicate Keywordlist");
                    iklp.setReturncode("Duplicate Keywordlist");
                } catch (javax.persistence.NoResultException ex) {
                    CfKeywordlist newkeywordlist = new CfKeywordlist();
                    newkeywordlist.setName(iklp.getKeywordlist());
                    CfKeywordlist newkeywordlist2 = cfkeywordlistService.create(newkeywordlist);
                    iklp.setReturncode("OK");
                }
            } else {
                iklp.setReturncode("Wrong API KEY");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            iklp.setReturncode("NoResultException");
        }
        return iklp;
    }
}
