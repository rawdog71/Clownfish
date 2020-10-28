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
import io.clownfish.clownfish.sap.models.RfcGroup;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sulzbachr
 */
public class RFC_GROUP_SEARCH {
    static SAPConnection sapc = null;
    JCoTable groups_table = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(RFC_GROUP_SEARCH.class);
    
    public RFC_GROUP_SEARCH(SAPConnection sapc) {
        RFC_GROUP_SEARCH.sapc = sapc;
    }
    
    public List<RfcGroup> getRfcGroupList() {
        try {
            JCoFunction function = sapc.getDestination().getRepository().getFunction("RFC_GROUP_SEARCH");
            function.getImportParameterList().setValue("GROUPNAME", "z*");
            function.getImportParameterList().setValue("LANGUAGE", "DE");
            function.execute(sapc.getDestination());
            groups_table = function.getTableParameterList().getTable("GROUPS");
            List<RfcGroup> groupsList = new ArrayList<>();
            for (int i = 0; i < groups_table.getNumRows(); i++) {
                groups_table.setRow(i);

                RfcGroup rfcgroup = new RfcGroup(
                    groups_table.getString("GROUPNAME"),
                    groups_table.getString("STEXT")
                );
                groupsList.add(rfcgroup);
            }
            return groupsList;
        } catch(Exception ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
}
