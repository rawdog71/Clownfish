/*
 * Copyright 2023 raine.
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

import io.clownfish.clownfish.datamodels.AuthTokenClasscontent;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.dbentities.CfAccessmanager;
import io.clownfish.clownfish.serviceinterface.CfAccessmanagerService;
import java.math.BigInteger;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Component
@Scope("singleton")
public class AccessManagerUtil {
    @Autowired transient AuthTokenListClasscontent authtokenlist;
    @Autowired transient CfAccessmanagerService cfaccessmanagerService;
    
    public boolean checkAccess(String token, Integer type, BigInteger ref) {
        AuthTokenClasscontent classcontent = authtokenlist.getAuthtokens().get(token);
        List<CfAccessmanager> acmlist = cfaccessmanagerService.findByTypeAndRef(type, ref);
        if (acmlist.size() > 0) {
            if (null != classcontent) {
                CfAccessmanager acm = cfaccessmanagerService.findByTypeAndRefAndRefclasscontent(type, ref, BigInteger.valueOf(classcontent.getUser().getId()));
                if (acm.getRefclasscontent().longValue() == classcontent.getUser().getId()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
