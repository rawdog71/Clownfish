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
import io.clownfish.clownfish.datamodels.RestAssetParameter;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import javax.servlet.MultipartConfigElement;
import javax.servlet.annotation.MultipartConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SulzbachR
 */
@RestController
@MultipartConfig
public class RestAsset {
    @Autowired transient CfAssetService cfassetService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestKeyword.class);
    
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }

    /*
    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        //multipartResolver.setMaxUploadSize(1000000);
        return multipartResolver;
    }
    */

    @PostMapping("/updateasset")
    public RestAssetParameter restUpdateAsset(@RequestBody RestAssetParameter ikp) {
        return updateAsset(ikp);
    }
    
    private RestAssetParameter updateAsset(RestAssetParameter ikp) {
        try {
            String token = ikp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ikp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfAsset asset = cfassetService.findById(ikp.getId());
                        asset.setDescription(ikp.getDescription());
                        asset.setPublicuse(ikp.isPublicuse());
                        CfAsset newasset2 = cfassetService.edit(asset);
                        ikp.setReturncode("OK");
                    } catch (javax.persistence.NoResultException ex) {
                        LOGGER.warn("No Asset");
                        ikp.setReturncode("No Asset");
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