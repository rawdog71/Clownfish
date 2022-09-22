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
package io.clownfish.clownfish.rest;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author raine
 */
@RestController
@RequestMapping(RestOData.URI)
public class RestOData {
    protected static final String URI = "/OData";

    @Autowired
    CsdlEdmProvider edmProvider;

    @Autowired
    EntityCollectionProcessor processor;
    
    @Autowired
    EntityProcessor singletonprocessor;
    
    @Autowired
    PrimitiveProcessor primitiveprocessor;

    @RequestMapping({"*", "*/*"})
    public void process(HttpServletRequest request, HttpServletResponse response) {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(edmProvider, new ArrayList<>());
        ODataHttpHandler handler = odata.createHandler(edm);
        handler.register(processor);
        handler.register(singletonprocessor);
        handler.register(primitiveprocessor);
        handler.process(new HttpServletRequestWrapper(request) {
            @Override
            public String getServletPath() {
                return RestOData.URI;
            }
        }, response);
    }
}
