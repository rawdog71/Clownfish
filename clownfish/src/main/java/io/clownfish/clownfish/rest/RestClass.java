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
import io.clownfish.clownfish.datamodels.ClassImport;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.utils.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SulzbachR
 */
@RestController
public class RestClass {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient ClassUtil classutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClass.class);

    @PostMapping("/insertclass")
    public void restInsertContent(@RequestHeader("token") String token, @RequestBody ClassImport ci) {
        insertClass(token, ci);
    }
    
    private void insertClass(String token, ClassImport ci) {
        try {
            if (authtokenlist.checkValidToken(token)) {
                //String apikey = icp.getApikey();
                //if (apikeyutil.checkApiKey(apikey, "RestService")) {
                try {
                    CfClass clazz = cfclassService.findByName(ci.getClassname());
                    LOGGER.error("Class exists");
                } catch (Exception ex) {
                    classutil.createClass(ci);
                }
                //} else {
                //    icp.setReturncode("Wrong API KEY");
                //}
            } else {
                //icp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            //icp.setReturncode("NoResultException");
        }
    }
}
