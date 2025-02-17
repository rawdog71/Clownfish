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

import com.sap.conn.jco.ConversionException;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.beans.JsonSAPFormParameter;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.sap.models.RfcFunctionParam;
import io.clownfish.clownfish.sap.models.RpyTableRead;
import io.clownfish.clownfish.utils.ClownfishUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sulzbachr
 */
public class SAPUtility {
    static SAPConnection sapc = null;
    private HashMap<String, List<RpyTableRead>> rpyMap = new HashMap();
    private transient RPY_TABLE_READ rpytableread = null;

    final transient static Logger LOGGER = LoggerFactory.getLogger(SAPUtility.class);

    public SAPUtility(Object sapc) {
        SAPUtility.sapc = (SAPConnection) sapc;
        rpytableread = new RPY_TABLE_READ((SAPConnection) sapc);
    }

    public void setSapConnection(Object sapc) {
        sapc = (SAPConnection) sapc;
    }

    /*
        getSapExport
        Übergibt die POST Parameter und ruft SAP RFC auf
        Setzt die Ergebnisse in eine Hashmap zur Ausgabe in Freemarker
    */
    @Deprecated
    public static HashMap<String, HashMap> getSapExport(List<CfSitesaprfc> sitesaprfclist, HashMap<String, List> saprfcfunctionparamMap, List<JsonSAPFormParameter> postmap, RPY_TABLE_READ rpytableread) {
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
                            for (JsonSAPFormParameter jfp : postmap) {
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
                        List<RpyTableRead> rpytablereadlist = rpytableread.getRpyTableReadList(tablename, sapc);
                        for (int i = 0; i < functions_table.getNumRows(); i++) {
                            HashMap<String, String> sapexportvalues = new HashMap<>();
                            functions_table.setRow(i);
                            for (RpyTableRead rpytablereadentry : rpytablereadlist) {
                                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CHAR) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CLNT) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.NUMC) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.DEC) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CURR) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CUKY) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.RAW) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.LANG) == 0) ||
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

    public Map executeAsync(String rfcFunction, Map parametermap, RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface, HashMap<String, JCoFunction> jcofunctiontable) {
        Map contentmap = new HashMap<>();
        try {
            //LOGGER.info("START SAP execute");
            JCoTable functions_table;
            HashMap<String, HashMap> sapexport = new HashMap<>();
            HashMap<String, List> saprfcfunctionparamMap = new HashMap<>();
            List<RfcFunctionParam> rfcfunctionparamlist = new ArrayList<>();
            rfc_get_function_interface.setSapConnection(sapc);
            rfcfunctionparamlist.addAll(rfc_get_function_interface.getRfcFunctionsParamList(rfcFunction));
            saprfcfunctionparamMap.put(rfcFunction, rfcfunctionparamlist);

            List<JsonSAPFormParameter> postmap_async = ClownfishUtil.getJsonSAPFormParameterList(parametermap);

            HashMap<String, Object> sapvalues = new HashMap<>();
            List<RfcFunctionParam> paramlist = saprfcfunctionparamMap.get(rfcFunction);
            JCoFunction function;
            try {
                // Setze die Import Parameter des SAP RFC mit den Werten aus den POST Parametern
                if (jcofunctiontable.containsKey(rfcFunction)) {
                    function = jcofunctiontable.get(rfcFunction);
                } else {
                    function = sapc.getDestination().getRepository().getFunction(rfcFunction);
                    jcofunctiontable.put(rfcFunction, function);
                }
                try {
                    if (null != function.getTableParameterList()) {
                        function.getTableParameterList().clear();
                    }
                    if (null != function.getChangingParameterList()) {
                        function.getChangingParameterList().clear();
                    }
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage());
                }
                for (RfcFunctionParam rfcfunctionparam : paramlist) {
                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("I") == 0) {
                        if (null != postmap_async) {
                            postmap_async.stream().filter((jfp) -> (jfp.getName().compareToIgnoreCase(rfcfunctionparam.getParameter()) == 0)).forEach((jfp) -> {
                                function.getImportParameterList().setValue(rfcfunctionparam.getParameter(), (String)jfp.getValue());
                            });
                        }
                    }

                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("C") == 0) {
                        if (null != postmap_async) {
                            postmap_async.stream().filter((jfp) -> (jfp.getName().compareToIgnoreCase(rfcfunctionparam.getParameter()) == 0)).forEach((jfp) -> {
                                ArrayList<LinkedHashMap<String, String>> val = (ArrayList<LinkedHashMap<String, String>>)jfp.getValue();
                                JCoTable table = function.getChangingParameterList().getTable("I_ZPRUDRUCK");
                                for (int i = 0; i < val.size(); i++) {
                                    table.appendRow();
                                    val.get(i).forEach(table::setValue);
                                }
                                function.getChangingParameterList().setValue(rfcfunctionparam.getParameter(), table);
                            });
                        }
                    }
                }
                // SAP RFC ausführen
                //LOGGER.info("START SAP RFC execute");

                function.execute(sapc.getDestination());
                //LOGGER.info("STOP SAP RFC execute");
                HashMap<String, ArrayList> saptables = new HashMap<>();
                for (RfcFunctionParam rfcfunctionparam : paramlist) {
                    String paramclass = rfcfunctionparam.getParamclass().toLowerCase();
                    if (paramclass.compareToIgnoreCase("i") == 0) {
                        continue;
                    }
                    String tablename = rfcfunctionparam.getTabname();
                    String paramname = rfcfunctionparam.getParameter();
                    String exid = rfcfunctionparam.getExid();

                    ArrayList<HashMap> tablevalues = new ArrayList<>();
                    tablevalues.clear();
                    List<RpyTableRead> rpytablereadlist;
                    switch (paramclass) {
                        case "e":
                            if (exid.compareToIgnoreCase("h") == 0) {
                                String param = new RFC_READ_TABLE(sapc).getTableStructureName("DD40L", "TYPENAME = '" + tablename + "'", 3);
                                functions_table = function.getExportParameterList().getTable(paramname.trim());
                                if (!functions_table.isEmpty()) {
                                    rpytablereadlist = getRpytablereadlist(param.trim());
                                    setTableValues(functions_table, rpytablereadlist, tablevalues);
                                    saptables.put(paramname, tablevalues);
                                }
                            } else {
                                if (exid.compareToIgnoreCase("u") == 0) {
                                    JCoStructure functions_structure = function.getExportParameterList().getStructure(paramname);
                                    rpytablereadlist = getRpytablereadlist(tablename);
                                    setStructureValues(functions_structure, rpytablereadlist, tablevalues);
                                    saptables.put(paramname, tablevalues);
                                } else {
                                    sapvalues.put(rfcfunctionparam.getParameter(), function.getExportParameterList().getString(rfcfunctionparam.getParameter()));
                                }
                            }
                            break;
                        case "t":
                            if (exid.compareToIgnoreCase("h") == 0) {
                                String param = new RFC_READ_TABLE(sapc).getTableStructureName("DD40L", "TYPENAME = '" + tablename + "'", 3);
                                functions_table = function.getTableParameterList().getTable(paramname);
                                if (!functions_table.isEmpty()) {
                                    rpytablereadlist = getRpytablereadlist(param.trim());
                                    setTableValues(functions_table, rpytablereadlist, tablevalues);
                                    saptables.put(paramname, tablevalues);
                                }
                            } else {
                                functions_table = function.getTableParameterList().getTable(paramname);
                                if (!functions_table.isEmpty()) {
                                    rpytablereadlist = getRpytablereadlist(tablename);
                                    setTableValues(functions_table, rpytablereadlist, tablevalues);
                                    saptables.put(paramname, tablevalues);
                                }
                            }
                            break;
                        case "c":
                            String param = new RFC_READ_TABLE(sapc).getTableStructureName("DD40L", "TYPENAME = '" + tablename + "'", 3);
                            functions_table = function.getChangingParameterList().getTable(paramname);
                            try {
                                if (!functions_table.isEmpty()) {
                                    rpytablereadlist = getRpytablereadlist(param.trim());
                                    setTableValues(functions_table, rpytablereadlist, tablevalues);
                                    saptables.put(paramname, tablevalues);
                                }
                            } catch(ConversionException ex) {
                                LOGGER.error(ex.getMessage());
                            }
                        break;
                    }
                }
                sapvalues.put("table", saptables);
                sapexport.put(rfcFunction.replaceFirst("/", "").replaceAll("/", "_"), sapvalues);
            } catch(JCoException ex) {
                LOGGER.error(ex.getMessage());
            }
            contentmap.put("sap", sapexport);
            //LOGGER.info("STOP SAP execute");
            return contentmap;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return contentmap;
        }
    }

    public Map getMetadata(String rfcFunction, Map parametermap, RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface) {
        HashMap<String, List> saprfcfunctionparamMap = new HashMap<>();
        try {
            List<RfcFunctionParam> rfcfunctionparamlist = new ArrayList<>();
            rfcfunctionparamlist.addAll(rfc_get_function_interface.getRfcFunctionsParamList(rfcFunction));
            saprfcfunctionparamMap.put(rfcFunction, rfcfunctionparamlist);

            return saprfcfunctionparamMap;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return saprfcfunctionparamMap;
        }
    }

    private void setTableValues(JCoTable functions_table, List<RpyTableRead> rpytablereadlist, ArrayList<HashMap> tablevalues) {
        try {
            for (int i = 0; i < functions_table.getNumRows(); i++) {
                HashMap<String, String> sapexportvalues = new HashMap<>();
                functions_table.setRow(i);
                for (RpyTableRead rpytablereadentry : rpytablereadlist) {
                    if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CHAR) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CLNT) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.NUMC) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.DEC) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CURR) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CUKY) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.RAW) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.LANG) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.ACCP) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.UNIT) == 0)) {
                        String value = functions_table.getString(rpytablereadentry.getFieldname());
                        sapexportvalues.put(rpytablereadentry.getFieldname().replaceFirst("/", "").replaceAll("/", "_"), value);
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
                        sapexportvalues.put(rpytablereadentry.getFieldname().replaceFirst("/", "").replaceAll("/", "_"), datum);
                        continue;
                    }
                    if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.QUAN) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.FLTP) == 0)) {
                        double value = functions_table.getDouble(rpytablereadentry.getFieldname());
                        sapexportvalues.put(rpytablereadentry.getFieldname().replaceFirst("/", "").replaceAll("/", "_"), String.valueOf(value));
                        continue;
                    }
                    if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT1) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT2) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT4) == 0) ||
                        (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT8) == 0)) {
                        int value = functions_table.getInt(rpytablereadentry.getFieldname());
                        sapexportvalues.put(rpytablereadentry.getFieldname().replaceFirst("/", "").replaceAll("/", "_"), String.valueOf(value));
                        continue;
                    }
                    if (!rpytablereadentry.getDatatype().isBlank())
                        System.out.println("SAP_FIELD = " + rpytablereadentry.getFieldname() + " - SAP_DATA_TYPE = " + rpytablereadentry.getDatatype());
                }
                tablevalues.add(sapexportvalues);
            }
        } catch(Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }


    private void setStructureValues(JCoStructure functions_table, List<RpyTableRead> rpytablereadlist, ArrayList<HashMap> tablevalues) {
        try {
            tablevalues.clear();
            HashMap<String, String> sapexportvalues = new HashMap<>();
            for (RpyTableRead rpytablereadentry : rpytablereadlist) {
                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CHAR) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CLNT) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.NUMC) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.DEC) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CURR) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CUKY) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.RAW) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.LANG) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.ACCP) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.UNIT) == 0)) {
                    String value = functions_table.getString(rpytablereadentry.getFieldname());
                    sapexportvalues.put(rpytablereadentry.getFieldname().replaceFirst("/", "").replaceAll("/", "_"), value);
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
                    sapexportvalues.put(rpytablereadentry.getFieldname().replaceFirst("/", "").replaceAll("/", "_"), datum);
                    continue;
                }
                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.QUAN) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.FLTP) == 0)) {
                    double value = functions_table.getDouble(rpytablereadentry.getFieldname());
                    sapexportvalues.put(rpytablereadentry.getFieldname().replaceFirst("/", "").replaceAll("/", "_"), String.valueOf(value));
                    continue;
                }
                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT1) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT2) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT4) == 0) ||
                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT8) == 0)) {
                    int value = functions_table.getInt(rpytablereadentry.getFieldname());
                    sapexportvalues.put(rpytablereadentry.getFieldname().replaceFirst("/", "").replaceAll("/", "_"), String.valueOf(value));
                    continue;
                }
                if (!rpytablereadentry.getDatatype().isBlank())
                    System.out.println("SAP_FIELD = " + rpytablereadentry.getFieldname() + " - SAP_DATA_TYPE = " + rpytablereadentry.getDatatype());
                }
                tablevalues.add(sapexportvalues);
        } catch(Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    private List<RpyTableRead> getRpytablereadlist(String tablename) {
        List<RpyTableRead> rpytablereadlist;
        if (rpyMap.containsKey(tablename)) {
            rpytablereadlist = rpyMap.get(tablename);
        } else {
            rpytableread.setSapConnection(sapc);
            rpytablereadlist = rpytableread.getRpyTableReadList(tablename, sapc);
            rpyMap.put(tablename, rpytablereadlist);
        }
        return rpytablereadlist;
    }
}
