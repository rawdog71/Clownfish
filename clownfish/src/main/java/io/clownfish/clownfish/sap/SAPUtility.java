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
import io.clownfish.clownfish.beans.JsonFormParameter;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.sap.models.RfcFunctionParam;
import io.clownfish.clownfish.sap.models.RpyTableRead;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sulzbachr
 */
public class SAPUtility {
    static SAPConnection sapc = null;
    
    final transient static Logger LOGGER = LoggerFactory.getLogger(SAPUtility.class);
    
    public SAPUtility(Object sapc) {
        SAPUtility.sapc = (SAPConnection) sapc;
    }
    
    /*
        getSapExport
        Übergibt die POST Parameter und ruft SAP RFC auf
        Setzt die Ergebnisse in eine Hashmap zur Ausgabe in Freemarker
    */
    public static HashMap<String, HashMap> getSapExport(List<CfSitesaprfc> sitesaprfclist, HashMap<String, List> saprfcfunctionparamMap, List<JsonFormParameter> postmap, RPY_TABLE_READ rpytableread) {
        JCoTable functions_table = null;
        HashMap<String, HashMap> sapexport = new HashMap<>();
        for (CfSitesaprfc cfsitesaprfc : sitesaprfclist) {
            try {
                HashMap<String, Object> sapvalues = new HashMap<>();
                List<RfcFunctionParam> paramlist = saprfcfunctionparamMap.get(cfsitesaprfc.getCfSitesaprfcPK().getRfcfunction());

                // Setze die Import Parameter des SAP RFC mit den Werten aus den POST Parametern
                JCoFunction function = sapc.getDestination().getRepository().getFunction(cfsitesaprfc.getCfSitesaprfcPK().getRfcfunction());
                for (RfcFunctionParam rfcfunctionparam : paramlist) {
                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("I") == 0) {
                        if (null != postmap) {
                            for (JsonFormParameter jfp : postmap) {
                                if (jfp.getName().compareToIgnoreCase(rfcfunctionparam.getParameter()) == 0) {
                                    function.getImportParameterList().setValue(rfcfunctionparam.getParameter(), jfp.getValue());
                                }
                            }
                        }
                    }
                }
                // SAP RFC ausführen
                function.execute(sapc.getDestination());

                HashMap<String, ArrayList> saptables = new HashMap<>();
                for (RfcFunctionParam rfcfunctionparam : paramlist) {    
                    String tablename = rfcfunctionparam.getTabname();
                    String paramname = rfcfunctionparam.getParameter();
                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("E") == 0) {
                        sapvalues.put(rfcfunctionparam.getParameter(), function.getExportParameterList().getString(rfcfunctionparam.getParameter()));
                    }
                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("T") == 0) {
                        ArrayList<HashMap> tablevalues = new ArrayList<>();
                        functions_table = function.getTableParameterList().getTable(paramname);
                        List<RpyTableRead> rpytablereadlist = rpytableread.getRpyTableReadList(tablename);
                        for (int i = 0; i < functions_table.getNumRows(); i++) {
                            HashMap<String, String> sapexportvalues = new HashMap<>();
                            functions_table.setRow(i);
                            for (RpyTableRead rpytablereadentry : rpytablereadlist) {
                                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CHAR) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.NUMC) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.UNIT) == 0)) {
                                    String value = functions_table.getString(rpytablereadentry.getFieldname());
                                    sapexportvalues.put(rpytablereadentry.getFieldname(), value);
                                    continue;
                                }
                                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.DATS) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.TIMS) == 0)) {
                                    Date value = functions_table.getDate(rpytablereadentry.getFieldname());
                                    String datum = "";
                                    if (null != value) {
                                        if (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.DATS) == 0) {
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                            datum = sdf.format(value);
                                        } else {
                                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                            datum = sdf.format(value);
                                        }
                                    }
                                    sapexportvalues.put(rpytablereadentry.getFieldname(), datum);
                                    continue;
                                }
                                if (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.QUAN) == 0) {
                                    double value = functions_table.getDouble(rpytablereadentry.getFieldname());
                                    sapexportvalues.put(rpytablereadentry.getFieldname(), String.valueOf(value));
                                    continue;
                                }
                                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT1) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT2) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT4) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT8) == 0)) {
                                    int value = functions_table.getInt(rpytablereadentry.getFieldname());
                                    sapexportvalues.put(rpytablereadentry.getFieldname(), String.valueOf(value));
                                }
                            }
                            tablevalues.add(sapexportvalues);
                        }
                        saptables.put(paramname, tablevalues);
                    }
                }
                sapvalues.put("table", saptables);
                sapexport.put(cfsitesaprfc.getCfSitesaprfcPK().getRfcfunction(), sapvalues);
            } catch(JCoException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return sapexport;
    }
}
