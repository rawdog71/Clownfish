/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.sap;

import KNSAPTools.SAPConnection;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import io.clownfish.clownfish.sap.models.RpyTableRead;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sulzbachr
 */
public class RPY_TABLE_READ {
    static SAPConnection sapc = null;
    JCoTable functions_table = null;

    public RPY_TABLE_READ(Object sapc) {
        RPY_TABLE_READ.sapc = (SAPConnection) sapc;
    }
    
    public List<RpyTableRead> getRpyTableReadList(String tablename) {
        try {
            JCoFunction function = sapc.getDestination().getRepository().getFunction("RPY_TABLE_READ");
            function.getImportParameterList().setValue("ACTIVATION_TYPE_I", "M");
            function.getImportParameterList().setValue("TABLE_NAME", tablename);
            function.getImportParameterList().setValue("LANGUAGE", "DE");
            function.getImportParameterList().setValue("WITH_DOCU", " ");
            function.getImportParameterList().setValue("DOCUTYPE", "U");
            function.execute(sapc.getDestination());
            functions_table = function.getTableParameterList().getTable("TABL_FIELDS");
            List<RpyTableRead> tablefieldList = new ArrayList<>();
            for (int i = 0; i < functions_table.getNumRows(); i++) {
                functions_table.setRow(i);

                RpyTableRead tablefield = new RpyTableRead(
                    functions_table.getString("TABLNAME"),
                    functions_table.getString("FIELDNAME"),
                    functions_table.getString("DTELNAME"),
                    functions_table.getString("CHECKTABLE"),
                    functions_table.getString("KEYFLAG"),
                    functions_table.getInt("POSITION"),
                    functions_table.getString("REFTABLE"),
                    functions_table.getString("REFFIELD"),
                    functions_table.getString("INCLNAME"),
                    functions_table.getString("NOTNULL"),
                    functions_table.getString("DOMANAME"),
                    functions_table.getString("PARAMID"),
                    functions_table.getString("LOGFLAG"),
                    functions_table.getInt("HEADLEN"),
                    functions_table.getInt("SCRLEN_S"),
                    functions_table.getInt("SCRLEN_M"),
                    functions_table.getInt("SCRLEN_L"),
                    functions_table.getString("DATATYPE"),
                    functions_table.getInt("LENGTH"),
                    functions_table.getInt("OUTPUTLEN"),
                    functions_table.getInt("DECIMALS"),
                    functions_table.getString("LOWERCASE"),
                    functions_table.getString("SIGNFLAG"),
                    functions_table.getString("LANGFLAG"),
                    functions_table.getString("VALUETAB"),
                    functions_table.getString("CONVEXIT"),
                    functions_table.getString("DDTEXT"),
                    functions_table.getString("REPTEXT"),
                    functions_table.getString("SCRTEXT_S"),
                    functions_table.getString("SCRTEXT_M"),
                    functions_table.getString("SCRTEXT_L"),
                    functions_table.getString("VALUEEXIST"),
                    functions_table.getString("ADMINFIELD"),
                    functions_table.getInt("INTLENGTH"),
                    functions_table.getString("INTTYPE")
                );
                tablefieldList.add(tablefield);
            }
            return tablefieldList;
        } catch(JCoException ex) {
            Logger.getLogger(RFC_GET_FUNCTION_INTERFACE.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
