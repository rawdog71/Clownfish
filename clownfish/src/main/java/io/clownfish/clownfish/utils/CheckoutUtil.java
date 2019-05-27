/*
 * Copyright 2019 sulzbachr.
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

import io.clownfish.clownfish.beans.LoginBean;
import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Component
public class CheckoutUtil {
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    
    public void getCheckoutAccess(BigInteger co, LoginBean loginbean) {
        if (null != co) {
            if (co.longValue() > 0) {
                if (co.longValue() == loginbean.getCfuser().getId()) {
                    checkedout = true;
                    access = true;
                } else {
                    checkedout = false;
                    access = false;
                }
            } else {
                checkedout = false;
                access = true;
            }
        } else {
            checkedout = false;
            access = true;
        }
    }
}
