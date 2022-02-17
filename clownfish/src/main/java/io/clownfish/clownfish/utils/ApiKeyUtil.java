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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.dbentities.CfWebservice;
import io.clownfish.clownfish.dbentities.CfWebserviceauth;
import io.clownfish.clownfish.serviceinterface.CfWebserviceService;
import io.clownfish.clownfish.serviceinterface.CfWebserviceauthService;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@Scope("singleton")
@Component
public class ApiKeyUtil implements Serializable {
    @Autowired CfWebserviceauthService cfwebserviceauthService;
    @Autowired CfWebserviceService cfwebserviceService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyUtil.class);

    public ApiKeyUtil() {
    }
    
    public boolean checkApiKey(String apikey, String webservicename) {
        try {
            CfWebserviceauth webserviceauth = cfwebserviceauthService.findByHash(apikey);
            return webserviceauth.getCfWebserviceauthPK().getWebserviceRef().getName().compareToIgnoreCase(webservicename) == 0;
        } catch (Exception ex) {
            return false;
        }
    }
    
    public String getRestApikey(CfUser userref) {
        String apikey = "";
        for (CfWebserviceauth auth : cfwebserviceauthService.findByUserRef(userref)) {
            CfWebservice webservice = cfwebserviceService.findById(auth.getCfWebserviceauthPK().getWebserviceRef().getId());
            if (0 == webservice.getName().compareToIgnoreCase("RestService")) {
                try {
                    apikey = URLEncoder.encode(auth.getHash(), StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
        return apikey;
    }
}
