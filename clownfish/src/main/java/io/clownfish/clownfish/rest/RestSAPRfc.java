/*
 * Copyright 2023 SulzbachR.
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

import com.sap.conn.jco.JCoFunction;
import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.datamodels.RestSAPRfcParameter;
import io.clownfish.clownfish.datamodels.RestSAPSystemRfcParameter;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.sap.SAPUtility;
import io.clownfish.clownfish.sap.models.RpyTableRead;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SulzbachR
 */
@RestController
public class RestSAPRfc {
    static SAPConnection sapc = null;
    static SAPConnection sapsystemc = null;
    private transient RPY_TABLE_READ rpytableread = null;
    private transient @Getter @Setter Map contentmap;
    private transient RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface = null;
    private @Getter @Setter RFC_GROUP_SEARCH rfcgroupsearch = null;
    private @Getter @Setter RFC_FUNCTION_SEARCH rfcfunctionsearch = null;
    private HashMap<String, JCoFunction> jcofunctiontable = new HashMap();
    private HashMap<String, List<RpyTableRead>> rpyMap = new HashMap();
    private boolean sapSupport = false;
    private PropertyUtil propertyUtil;
    @Autowired PropertyList propertylist;
    @Value("${sapconnection.file}") String SAPCONNECTION;
    
    
    /*
    this.sapc = (SAPConnection) sapc;
        this.rpytableread = rpytableread;
        this.postmap = postmap;
        rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
        */
    
    @PostMapping(value = "/saprfc", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestSAPRfcParameter restGetSAPRfc(@RequestBody RestSAPRfcParameter rsrp) {
        if (null == propertyUtil) {
            propertyUtil = new PropertyUtil(propertylist);
        }
        sapSupport = propertyUtil.getPropertyBoolean("sap_support", sapSupport);
        if (sapSupport) {
            if (null == sapc) {
                sapc = new SAPConnection(SAPCONNECTION, "Clownfish_REST");
                rpytableread = new RPY_TABLE_READ(sapc);
                rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
                rfcgroupsearch = new RFC_GROUP_SEARCH(sapc);
                rfcfunctionsearch = new RFC_FUNCTION_SEARCH(sapc);
            }
            SAPUtility su = new SAPUtility(sapc);
            rsrp.setResult(su.executeAsync(rsrp.getRfcFunction(), rsrp.getParametermap(), rfc_get_function_interface, jcofunctiontable));
        }
        return rsrp;
    }
    
    @PostMapping(value = "/sapsystemrfc", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestSAPSystemRfcParameter restGetSAPRfc(@RequestBody RestSAPSystemRfcParameter rsrp) {
        if (null == propertyUtil) {
            propertyUtil = new PropertyUtil(propertylist);
        }
        sapSupport = propertyUtil.getPropertyBoolean("sap_support", sapSupport);
        if (sapSupport) {
            sapsystemc = new SAPConnection(rsrp.getRfcSystem(), "Clownfish_RESTSYSTEM");
            rpytableread = new RPY_TABLE_READ(sapsystemc);
            rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapsystemc);
            rfcgroupsearch = new RFC_GROUP_SEARCH(sapsystemc);
            rfcfunctionsearch = new RFC_FUNCTION_SEARCH(sapsystemc);
            SAPUtility su = new SAPUtility(sapsystemc);
            su.setSapConnection(sapsystemc);
            rsrp.setResult(su.executeAsync(rsrp.getRfcFunction(), rsrp.getParametermap(), rfc_get_function_interface, jcofunctiontable));
        }
        return rsrp;
    }
    
    @PostMapping(value = "/saprfcmeta", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestSAPRfcParameter restGetSAPRfcMeta(@RequestBody RestSAPRfcParameter rsrp) {
        if (null == propertyUtil) {
            propertyUtil = new PropertyUtil(propertylist);
        }
        sapSupport = propertyUtil.getPropertyBoolean("sap_support", sapSupport);
        if (sapSupport) {
            if (null == sapc) {
                sapc = new SAPConnection(SAPCONNECTION, "Clownfish_REST");
                rpytableread = new RPY_TABLE_READ(sapc);
                rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
                rfcgroupsearch = new RFC_GROUP_SEARCH(sapc);
                rfcfunctionsearch = new RFC_FUNCTION_SEARCH(sapc);
            }
            SAPUtility su = new SAPUtility(sapc);
            rsrp.setResult(su.getMetadata(rsrp.getRfcFunction(), rsrp.getParametermap(), rfc_get_function_interface));
        }
        return rsrp;
    }
    
    @PostMapping(value = "/sapsystemrfcmeta", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestSAPSystemRfcParameter restGetSAPRfcMeta(@RequestBody RestSAPSystemRfcParameter rsrp) {
        if (null == propertyUtil) {
            propertyUtil = new PropertyUtil(propertylist);
        }
        sapSupport = propertyUtil.getPropertyBoolean("sap_support", sapSupport);
        if (sapSupport) {
            sapsystemc = new SAPConnection(rsrp.getRfcSystem(), "Clownfish_RESTSYSTEM");
            rpytableread = new RPY_TABLE_READ(sapsystemc);
            rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapsystemc);
            rfcgroupsearch = new RFC_GROUP_SEARCH(sapsystemc);
            rfcfunctionsearch = new RFC_FUNCTION_SEARCH(sapsystemc);
            SAPUtility su = new SAPUtility(sapsystemc);
            rsrp.setResult(su.getMetadata(rsrp.getRfcFunction(), rsrp.getParametermap(), rfc_get_function_interface));
        }
        return rsrp;
    }
}
