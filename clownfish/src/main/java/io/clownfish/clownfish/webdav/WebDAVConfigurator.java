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
package io.clownfish.clownfish.webdav;

import io.milton.http.fs.NullSecurityManager;
import io.milton.servlet.DefaultMiltonConfigurator;

/**
 *
 * @author SulzbachR
 */
public class WebDAVConfigurator extends DefaultMiltonConfigurator {
    private NullSecurityManager securityManager;
    /**
     * Instantiates a new My milton configurator.
     */
    public WebDAVConfigurator() {
        this.securityManager = new NullSecurityManager();
    }
    
    @Override
    protected void build() {
        builder.setSecurityManager(securityManager);
        super.build();
    }
}
