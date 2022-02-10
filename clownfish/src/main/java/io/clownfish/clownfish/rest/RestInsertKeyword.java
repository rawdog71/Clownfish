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

import io.clownfish.clownfish.datamodels.KeywordParameter;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
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
public class RestInsertKeyword {
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired ApiKeyUtil apikeyutil;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestInsertKeyword.class);

    @PostMapping("/insertkeyword")
    public KeywordParameter restInsertKeyword(@RequestBody KeywordParameter ikp) {
        return insertKeyword(ikp);
    }
    
    private KeywordParameter insertKeyword(KeywordParameter ikp) {
        try {
            String apikey = ikp.getApikey();
            if (apikeyutil.checkApiKey(apikey, "GetKeywords")) {
                try {
                    CfKeyword keyword = cfkeywordService.findByName(ikp.getKeyword());
                    LOGGER.warn("Duplicate Keyword");
                    ikp.setReturncode("Duplicate Keyword");
                } catch (javax.persistence.NoResultException ex) {
                    CfKeyword newkeyword = new CfKeyword();
                    newkeyword.setName(ikp.getKeyword());
                    CfKeyword newkeyword2 = cfkeywordService.create(newkeyword);
                    ikp.setReturncode("OK");
                }
            } else {
                ikp.setReturncode("Wrong API KEY");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            ikp.setReturncode("NoResultException");
        }
        return ikp;
    }
}
