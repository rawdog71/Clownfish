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
package io.clownfish.clownfish.templatebeans;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class NetworkTemplateBean implements Serializable {
    final transient Logger LOGGER = LoggerFactory.getLogger(NetworkTemplateBean.class);

    public NetworkTemplateBean() {
    }
    
    public boolean getNetworkStatus(String ipaddress) {
        try{
            InetAddress address = InetAddress.getByName(ipaddress);
            boolean reachable = address.isReachable(1000);
            return reachable;
        } catch (IOException e){
            LOGGER.warn(e.getMessage());
            return false;
        }
    }
}
