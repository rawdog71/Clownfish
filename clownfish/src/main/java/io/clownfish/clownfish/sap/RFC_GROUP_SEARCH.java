/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.sap;

import KNSAPTools.SAPConnection;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import io.clownfish.clownfish.sap.models.RfcGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sulzbachr
 */
public class RFC_GROUP_SEARCH {
    static SAPConnection sapc = null;
    JCoTable groups_table = null;
    
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
            Logger.getLogger(RFC_GROUP_SEARCH.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
