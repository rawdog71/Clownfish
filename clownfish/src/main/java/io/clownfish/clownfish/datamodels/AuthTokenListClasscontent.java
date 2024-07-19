/*
 * Copyright 2022 raine.
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
package io.clownfish.clownfish.datamodels;

import io.clownfish.clownfish.beans.ContentList;
import io.clownfish.clownfish.beans.ScrapyardList;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Component
public class AuthTokenListClasscontent {
    private @Getter @Setter HashMap<String, AuthTokenClasscontent> authtokens;
    @Autowired transient PropertyUtil propertyUtil;
    @Autowired transient ContentList contentlist;
    @Autowired transient ScrapyardList scrapyardlist;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAttributService cfattributService;
    private @Getter @Setter boolean confirmation = false;

    public AuthTokenListClasscontent() {
        authtokens = new HashMap<>();
        Runnable gcRunnable = () -> {
            deleteTokens();
        };
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(gcRunnable, 0, 1, TimeUnit.MINUTES);
    }
    
    public boolean checkValidToken(String token) {
        if (propertyUtil.getPropertyBoolean("check_authtoken", true)) {
            if (authtokens.containsKey(token)) {
                return authtokens.get(token).getValiduntil().isAfterNow();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private void deleteTokens() {
        for (String token : authtokens.keySet()) {
            if (authtokens.get(token).getValiduntil().isBeforeNow()) {
                if (confirmation) {
                    // kill also the user in db
                    CfClasscontent user = authtokens.get(token).getUser();
                    CfClasscontent usercheck = cfclasscontentService.findById(user.getId());
                    CfAttribut confirmed_attr = cfattributService.findByNameAndClassref("confirmed", user.getClassref());
                    CfAttributcontent confirmed = cfattributcontentService.findByAttributrefAndClasscontentref(confirmed_attr, usercheck);
                    if (!confirmed.getContentBoolean()) {
                        contentlist.deleteContent(authtokens.get(token).getUser());
                        scrapyardlist.destroyContent(authtokens.get(token).getUser());
                    }
                }
                authtokens.remove(token);
            }
        }
    }
}
