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
package io.clownfish.clownfish.endpoints;

import io.clownfish.clownfish.Clownfish;
import io.clownfish.clownfish.utils.PropertyUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author raine
 */
@RestController
public class EndpointError {
    @Autowired private PropertyUtil propertyUtil;
    @Autowired private Clownfish clownfish;
    
    /**
     * Call of the "error" site
     * Fetches the error site from the system property "site_error" and calls universalGet 
     * @param request
     * @param response
     */
    @RequestMapping("/error")
    public void error(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        String error_site = propertyUtil.getPropertyValue("site_error");
        if (null == error_site) {
            error_site = "error";
        }
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, error_site);
        clownfish.universalGet(error_site, request, response);
    }
}
