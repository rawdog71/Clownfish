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
package io.clownfish.clownfish;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Component
public class ContainerInitializer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Value("${server.port:9000}")
    int serverPort;

    /**
     * Customize the embedded tomcat server port
     * @param factory
     */
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.setContextPath("");
        factory.setPort(serverPort);
    }
}
