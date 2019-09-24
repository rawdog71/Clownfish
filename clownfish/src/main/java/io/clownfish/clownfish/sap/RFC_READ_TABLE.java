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
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sulzbachr
 */
public class RFC_READ_TABLE {
    static SAPConnection sapc = null;
    JCoTable functions_table = null;
    JCoTable options_table = null;
    
    private static HashMap<String, String> structurenameProxy = null;
    
    final transient Logger logger = LoggerFactory.getLogger(RFC_READ_TABLE.class);

    public RFC_READ_TABLE(Object sapc) {
        RFC_READ_TABLE.sapc = (SAPConnection) sapc;
        if (null == structurenameProxy) {
            structurenameProxy = new HashMap<>();
        }
    }
    
    public String getTableStructureName(String tablename, String bedingung, int index) {
        String hash = new StringBuilder(tablename + "-" + bedingung + "-" + index).toString();
        if (structurenameProxy.containsKey(hash)) {
            return structurenameProxy.get(hash);
        } else {
            String value = "";
            try {
                JCoFunction function = sapc.getDestination().getRepository().getFunction("RFC_READ_TABLE");
                function.getImportParameterList().setValue("QUERY_TABLE", tablename);
                function.getImportParameterList().setValue("DELIMITER", ";");
                function.getImportParameterList().setValue("NO_DATA", "");
                function.getImportParameterList().setValue("ROWSKIPS", 0);
                function.getImportParameterList().setValue("ROWCOUNT", 0);

                options_table = function.getTableParameterList().getTable("OPTIONS");
                options_table.appendRow();
                options_table.setValue("TEXT", bedingung);

                function.execute(sapc.getDestination());
                functions_table = function.getTableParameterList().getTable("DATA");
                for (int i = 0; i < functions_table.getNumRows(); i++) {
                    functions_table.setRow(i);
                    String row = functions_table.getString("WA");
                    String[] entries = row.split(";");
                    value = entries[index];
                }
                structurenameProxy.put(hash, value);
                return value;
            } catch(JCoException ex) {
                logger.error(ex.getMessage());
                return null;
            }
        }
    }
}
