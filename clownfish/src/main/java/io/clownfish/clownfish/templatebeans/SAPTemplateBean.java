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

import com.sap.conn.jco.ConversionException;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.beans.JsonFormParameter;
import io.clownfish.clownfish.datamodels.WebserviceCache;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RFC_READ_TABLE;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.sap.SAPDATATYPE;
import io.clownfish.clownfish.sap.models.RfcFunctionParam;
import io.clownfish.clownfish.sap.models.RpyTableRead;
import io.clownfish.clownfish.utils.ClownfishUtil;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope("request")
@Component
public class SAPTemplateBean implements Serializable {
    private List<JsonFormParameter> postmap;
    private transient RPY_TABLE_READ rpytableread = null;
    private transient @Getter @Setter Map contentmap;
    static SAPConnection sapc = null;
    private transient RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface = null;
    private HashMap<String, JCoFunction> jcofunctiontable = new HashMap();
    private HashMap<String, List<RpyTableRead>> rpyMap = new HashMap();
    private static @Getter @Setter Map contentCache;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(SAPTemplateBean.class);

    public SAPTemplateBean() {
        contentmap = new HashMap<>();
        if (null == contentCache) {
            contentCache = new HashMap<>();
        }
    }
    
    public void init(Object sapc, List<CfSitesaprfc> sitesaprfclist, RPY_TABLE_READ rpytableread, List<JsonFormParameter> postmap) {
        this.sapc = (SAPConnection) sapc;
        this.rpytableread = rpytableread;
        this.postmap = postmap;
        rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
        jcofunctiontable = new HashMap();
        rpyMap = new HashMap();
        contentmap.clear();
    }
    
    public Map executeAsync(String rfcFunction) {
        return executeAsync(rfcFunction, null);
    }
    
    public Map executeAsync(String rfcFunction, Map parametermap) {
        try {
            //LOGGER.info("START SAP execute");
            JCoTable functions_table;
            HashMap<String, HashMap> sapexport = new HashMap<>();
            HashMap<String, List> saprfcfunctionparamMap = new HashMap<>();
            List<RfcFunctionParam> rfcfunctionparamlist = new ArrayList<>();
            rfcfunctionparamlist.addAll(rfc_get_function_interface.getRfcFunctionsParamList(rfcFunction));
            saprfcfunctionparamMap.put(rfcFunction, rfcfunctionparamlist);

            List<JsonFormParameter> postmap_async = ClownfishUtil.getJsonFormParameterList(parametermap);
            
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
                                function.getImportParameterList().setValue(rfcfunctionparam.getParameter(), jfp.getValue());
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
                                    if (exid.compareToIgnoreCase("c") == 0) {
                                        sapvalues.put(rfcfunctionparam.getParameter(), function.getExportParameterList().getChar(rfcfunctionparam.getParameter()));
                                    } else {
                                        sapvalues.put(rfcfunctionparam.getParameter(), function.getExportParameterList().getString(rfcfunctionparam.getParameter()));
                                    }
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
                            /*
                            functions_table = function.getTableParameterList().getTable(paramname);
                            if (!functions_table.isEmpty()) {
                                rpytablereadlist = getRpytablereadlist(tablename);
                                setTableValues(functions_table, rpytablereadlist, tablevalues);
                                saptables.put(paramname, tablevalues);
                            }
                            */
                            break;
                        case "c":
                            String param = new RFC_READ_TABLE(sapc).getTableStructureName("DD40L", "TYPENAME = '" + tablename + "'", 3);
                            functions_table = function.getChangingParameterList().getTable(paramname);
                            try {
                                if (!functions_table.isEmpty()) {
                                    rpytablereadlist = getRpytablereadlist(param.trim());
                                    //rpytablereadlist = rpytableread.getRpyTableReadList(param);
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
    
    public Map executeAsync(String rfcFunction, int seconds) {
        return executeAsync(rfcFunction, null, seconds);
    }
    
    public Map executeAsync(String rfcFunction, Map parametermap, int seconds) {
        String hash = "";
        if (null != parametermap) {
            hash = parametermap.toString();
        }
        if (contentCache.containsKey(rfcFunction + "_" + hash)) {
            if (DateTime.now().isBefore(((WebserviceCache)contentCache.get(rfcFunction + "_" + hash)).getValiduntil())) {
                return ((WebserviceCache)contentCache.get(rfcFunction + "_" + hash)).getContentmap();
            } else {
                Map cm = new HashMap<>();
                cm.putAll(executeAsync(rfcFunction, parametermap));
                ((WebserviceCache)contentCache.get(rfcFunction + "_" + hash)).setContentmap(cm);
                ((WebserviceCache)contentCache.get(rfcFunction + "_" + hash)).setValiduntil(DateTime.now().plusSeconds(seconds));
                
                return cm;
            }
        } else {
            Map cm = new HashMap<>();
            cm.putAll(executeAsync(rfcFunction, parametermap));

            WebserviceCache webservicecache = new WebserviceCache();
            webservicecache.setContentmap(cm);
            webservicecache.setValiduntil(DateTime.now().plusSeconds(seconds));
            contentCache.put(rfcFunction + "_" + hash, webservicecache);

            return cm;
        }
    }

    public Map execute(String rfcFunction) {
        JCoTable functions_table;
        HashMap<String, HashMap> sapexport = new HashMap<>();
        HashMap<String, List> saprfcfunctionparamMap = new HashMap<>();
        List<RfcFunctionParam> rfcfunctionparamlist = new ArrayList<>();
        rfcfunctionparamlist.addAll(rfc_get_function_interface.getRfcFunctionsParamList(rfcFunction));
        saprfcfunctionparamMap.put(rfcFunction, rfcfunctionparamlist);
        
        try {
            HashMap<String, Object> sapvalues = new HashMap<>();
            List<RfcFunctionParam> paramlist = saprfcfunctionparamMap.get(rfcFunction);

            // Setze die Import Parameter des SAP RFC mit den Werten aus den POST Parametern
            JCoFunction function;
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
                    if (null != postmap) {
                        postmap.stream().filter((jfp) -> (jfp.getName().compareToIgnoreCase(rfcfunctionparam.getParameter()) == 0)).forEach((jfp) -> {
                            function.getImportParameterList().setValue(rfcfunctionparam.getParameter(), jfp.getValue());
                        });
                    }
                }
            }
            // SAP RFC ausführen
            function.execute(sapc.getDestination());
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
                List<RpyTableRead> rpytablereadlist = null;
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
                            sapvalues.put(rfcfunctionparam.getParameter(), function.getExportParameterList().getString(rfcfunctionparam.getParameter()));
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
                        /*
                        functions_table = function.getTableParameterList().getTable(paramname);
                        if (!functions_table.isEmpty()) {
                            rpytablereadlist = getRpytablereadlist(tablename);
                            setTableValues(functions_table, rpytablereadlist, tablevalues);
                            saptables.put(paramname, tablevalues);
                        }
                        setTableValues(functions_table, rpytablereadlist, tablevalues);
                        saptables.put(paramname, tablevalues);
                        */
                        break;
                    case "c":
                        String param = new RFC_READ_TABLE(sapc).getTableStructureName("DD40L", "TYPENAME = '" + tablename + "'", 3);
                        functions_table = function.getChangingParameterList().getTable(paramname);
                        //rpytablereadlist = rpytableread.getRpyTableReadList(param);
                        if (!functions_table.isEmpty()) {
                            rpytablereadlist = getRpytablereadlist(param);
                            setTableValues(functions_table, rpytablereadlist, tablevalues);
                            saptables.put(paramname, tablevalues);
                        }
                        setTableValues(functions_table, rpytablereadlist, tablevalues);
                        saptables.put(paramname, tablevalues);
                    break;
                }
            }
            sapvalues.put("table", saptables);
            sapexport.put(rfcFunction, sapvalues);
        } catch(JCoException ex) {
            LOGGER.error(ex.getMessage());
        }
        contentmap.put("sap", sapexport);
        return contentmap;
    }
    
    public Map execute(String rfcFunction, int seconds) {
        if (contentCache.containsKey(rfcFunction)) {
            if (DateTime.now().isBefore(((WebserviceCache)contentCache.get(rfcFunction)).getValiduntil())) {
                return ((WebserviceCache)contentCache.get(rfcFunction)).getContentmap();
            } else {
                contentmap.putAll(execute(rfcFunction));
                ((WebserviceCache)contentCache.get(rfcFunction)).setContentmap(contentmap);
                ((WebserviceCache)contentCache.get(rfcFunction)).setValiduntil(DateTime.now().plusSeconds(seconds));
                
                return contentmap;
            }
        } else {
            contentmap.putAll(execute(rfcFunction));

            WebserviceCache webservicecache = new WebserviceCache();
            webservicecache.setContentmap(contentmap);
            webservicecache.setValiduntil(DateTime.now().plusSeconds(seconds));
            contentCache.put(rfcFunction, webservicecache);

            return contentmap;
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
            rpytablereadlist = rpytableread.getRpyTableReadList(tablename);
            rpyMap.put(tablename, rpytablereadlist);
        }
        return rpytablereadlist;
    }
}
