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

import io.clownfish.clownfish.beans.JsonFormParameter;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rawdog
 */
public class SAPTemplateBean {
    private List<CfSitesaprfc> sitesaprfclist;
    private HashMap<String, List> saprfcfunctionparamMap;
    private List<JsonFormParameter> postmap;
    private RPY_TABLE_READ rpytableread;
    private Map sitecontentmap;

    public SAPTemplateBean() {
    }
    
    public void init(Map sitecontentmap) {
        this.sitecontentmap = sitecontentmap;
    }

    public Map execute(String catalog, String tablename, String sqlstatement, String namespace) {
        return sitecontentmap;
    }
    
}
