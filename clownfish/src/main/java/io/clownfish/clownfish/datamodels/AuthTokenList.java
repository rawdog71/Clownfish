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

import io.clownfish.clownfish.utils.PropertyUtil;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Component
public class AuthTokenList {
    private @Getter @Setter HashMap<String, AuthToken> authtokens;
    @Autowired transient PropertyUtil propertyUtil;

    public AuthTokenList() {
        authtokens = new HashMap<>();
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
}
