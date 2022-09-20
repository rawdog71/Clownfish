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
package io.clownfish.clownfish.odata;


import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.util.List;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 *
 * @author raine
 */
@Component
public class GenericSingletonProcessor implements EntityProcessor {
    @Autowired private CfClassService cfclassservice;
    @Autowired private CfClasscontentService cfclasscontentService;
    @Autowired private CfAttributcontentService cfattributcontentservice;
    @Autowired ContentUtil contentUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired EntityUtil entityUtil;
    
    @Value("${hibernate.use:0}") int useHibernate;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readEntity(ODataRequest odr, ODataResponse odr1, UriInfo uriInfo, ContentType ct) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceSingleton uriResourceSingleton = (UriResourceSingleton) resourcePaths.get(0);
        //EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
    }

    @Override
    public void createEntity(ODataRequest odr, ODataResponse odr1, UriInfo ui, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void updateEntity(ODataRequest odr, ODataResponse odr1, UriInfo ui, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void deleteEntity(ODataRequest odr, ODataResponse odr1, UriInfo ui) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = sm;
    }
    
}
