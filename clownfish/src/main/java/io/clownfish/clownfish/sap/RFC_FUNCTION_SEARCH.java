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
package io.clownfish.clownfish.sap;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.sap.models.RfcFunction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sulzbachr
 */
public class RFC_FUNCTION_SEARCH {
    static SAPConnection sapc = null;
    JCoTable functions_table = null;
    
    public RFC_FUNCTION_SEARCH(SAPConnection sapc) {
       RFC_FUNCTION_SEARCH.sapc = sapc;
    }
    
    public List<RfcFunction> getRfcFunctionsList(String groupname) {
        try {
            JCoFunction function = sapc.getDestination().getRepository().getFunction("RFC_FUNCTION_SEARCH");
            function.getImportParameterList().setValue("FUNCNAME", "*");
            function.getImportParameterList().setValue("GROUPNAME", groupname);
            function.getImportParameterList().setValue("LANGUAGE", "DE");
            function.execute(sapc.getDestination());
            functions_table = function.getTableParameterList().getTable("FUNCTIONS");
            List<RfcFunction> functionsList = new ArrayList<>();
            for (int i = 0; i < functions_table.getNumRows(); i++) {
                functions_table.setRow(i);

                RfcFunction rfcfunction = new RfcFunction(
                    functions_table.getString("FUNCNAME"),
                    functions_table.getString("GROUPNAME"),
                    functions_table.getString("APPL"),
                    functions_table.getString("HOST"),
                    functions_table.getString("STEXT")
                );
                functionsList.add(rfcfunction);
            }
            return functionsList;
        } catch(Exception ex) {
            //Logger.getLogger(RFC_FUNCTION_SEARCH.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
