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

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.sap.models.RfcFunctionParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sulzbachr
 */
public class RFC_GET_FUNCTION_INTERFACE {
    static SAPConnection sapc = null;
    JCoTable functions_table = null;
    private static final HashMap<String, List<RfcFunctionParam>> rfcfunctionsparamlist = new HashMap<>();
    
    final transient Logger LOGGER = LoggerFactory.getLogger(RFC_GET_FUNCTION_INTERFACE.class);

    public RFC_GET_FUNCTION_INTERFACE(Object sapc) {
        RFC_GET_FUNCTION_INTERFACE.sapc = (SAPConnection) sapc;
    }
    
    public void setSapConnection(Object sapc) {
        sapc = (SAPConnection) sapc;
    }
    
    public void init() {
        rfcfunctionsparamlist.clear();
    }
    
    public List<RfcFunctionParam> getRfcFunctionsParamList(String funcname, SAPConnection sapc) {
        if (rfcfunctionsparamlist.containsKey(funcname)) {
            return rfcfunctionsparamlist.get(funcname);
        } else {
            try {
                sapc.getDestination().getRepository().clear();
                JCoFunction function = sapc.getDestination().getRepository().getFunction("RFC_GET_FUNCTION_INTERFACE");
                function.getImportParameterList().setValue("FUNCNAME", funcname);
                function.getImportParameterList().setValue("NONE_UNICODE_LENGTH", " ");
                function.getImportParameterList().setValue("LANGUAGE", "DE");
                function.execute(sapc.getDestination());
                functions_table = function.getTableParameterList().getTable("PARAMS");
                List<RfcFunctionParam> functionsList = new ArrayList<>();
                for (int i = 0; i < functions_table.getNumRows(); i++) {
                    functions_table.setRow(i);

                    RfcFunctionParam rfcfunctionparam = new RfcFunctionParam(
                        functions_table.getString("PARAMCLASS"),
                        functions_table.getString("PARAMETER"),
                        functions_table.getString("TABNAME"),
                        functions_table.getString("FIELDNAME"),
                        functions_table.getString("EXID"),
                        functions_table.getInt("POSITION"),
                        functions_table.getInt("OFFSET"),
                        functions_table.getInt("INTLENGTH"),
                        functions_table.getInt("DECIMALS"),
                        functions_table.getString("DEFAULT"),
                        functions_table.getString("PARAMTEXT"),
                        functions_table.getString("OPTIONAL")
                    );
                    functionsList.add(rfcfunctionparam);
                }
                rfcfunctionsparamlist.put(funcname, functionsList);
                return functionsList;
            } catch (JCoException ex) {
                LOGGER.error(ex.getMessage());
                return null;
            }
        }
    }
}
