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

import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.utils.PasswordUtil;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

/**
 *
 * @author raine
 */
public class AuthToken {
    private @Getter @Setter String token;
    private @Getter @Setter DateTime validuntil;
    private @Getter @Setter CfUser user;

    public AuthToken() {
    }

    public AuthToken(String token, DateTime validuntil, CfUser user) {
        this.token = token;
        this.validuntil = validuntil;
        this.user = user;
    }
    
    public static String generateToken(String password, String salt) {
        return PasswordUtil.generateSecurePassword(password + new DateTime().toString(), salt);
    }
}
