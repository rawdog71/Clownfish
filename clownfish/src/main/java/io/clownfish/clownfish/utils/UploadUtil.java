/*
 * Copyright 2025 SulzbachR.
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
package io.clownfish.clownfish.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.conn.jco.ConversionException;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import de.destrukt.sapconnection.SAPConnection;
import freemarker.ext.beans.HashAdapter;
import io.clownfish.clownfish.beans.JsonSAPFormParameter;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RFC_READ_TABLE;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.sap.SAPDATATYPE;
import io.clownfish.clownfish.sap.models.RfcFunctionParam;
import io.clownfish.clownfish.sap.models.RpyTableRead;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author SulzbachR
 */

public class UploadUtil {
    @Autowired transient CfDatasourceService cfdatasourceService;
    private JDBCUtil jdbcutil = null;
    private transient RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface = null;
    private HashMap<String, JCoFunction> jcofunctiontable = new HashMap();
    private HashMap<String, List<RpyTableRead>> rpyMap = new HashMap();
    private transient RPY_TABLE_READ rpytableread = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(UploadUtil.class);

    public UploadUtil(CfDatasourceService cfdatasourceService) {
        this.cfdatasourceService = cfdatasourceService;
    }
    
    public void uploadJson(String ruletemplate, String path, String filename) {
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            // 1. Regelstruktur laden
            JsonNode configRoot = mapper.readTree(ruletemplate);
            // 2. Quelldaten laden
            JsonNode dataRoot = mapper.readTree(new File(path + File.separator + filename));
            // targetSystem lesen
            String targetSystem = configRoot.get("targetsystem").asText();
            String targetConnection = configRoot.get("targetconnection").asText();
            
            switch (targetSystem) {
                case "DB"-> {
                    // 3. Daten extrahieren und in DB schreiben
                    processAndWriteDataDB(configRoot, dataRoot, targetConnection);
                }
                case "RFC"-> {
                    // 3. Daten extrahieren und in RFC schreiben
                    processAndWriteDataRFC(configRoot, dataRoot, targetConnection);
                }
            }            
            LOGGER.info("Import abgeschlossen.");
        } catch (IOException e) {
            LOGGER.error("Fehler beim Lesen der JSON-Dateien: " + e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("Datenbankfehler: " + e.getMessage());
        }
    }
    
    /**
     * Verarbeitet die Quelldaten basierend auf der Konfiguration und schreibt in die DB.
     */
    private void processAndWriteDataDB(JsonNode config, JsonNode data, String targetConnection) throws SQLException {
        CfDatasource datasource = cfdatasourceService.findByName(targetConnection);
        String targetName = config.get("targetname").asText(); // Zieltabelle: opheoimport
        JsonNode fieldsConfig = config.get("fields");
        String basePath = config.get("basePath").asText(); 
        String iteratorPath = config.get("iteratorPath").asText(); // "Sendungen/Sendung"
        
        jdbcutil = new JDBCUtil(datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword());

        // Verbindungsaufbau
        try (Connection conn = jdbcutil.getConnection()) {
            conn.setAutoCommit(false); // Transaktionsmanagement
            
            if (basePath.contains("/")) {
                String[] baseparts = basePath.split("/");
                
                // In Ihrem Fall ist der zu iterierende Array-Pfad: Touren
                JsonNode baseArray = data.get(baseparts[0]);

                if (baseArray.isArray()) {
                    for (JsonNode baseitemWrapper : baseArray) {
                        JsonNode baseitemNode = baseitemWrapper.get(baseparts[1]);

                        // Verarbeite alle "Sendungen" innerhalb der Tour
                        processBaseItemDB(conn, targetName, fieldsConfig, baseitemNode, basePath, iteratorPath);
                    }
                }
            } else {
                
            }
            
            conn.commit(); // Alle Änderungen übernehmen

        } // conn wird automatisch geschlossen
    }
    
    /**
     * Verarbeitet die Quelldaten basierend auf der Konfiguration und schreibt in den RFC.
     */
    private void processAndWriteDataRFC(JsonNode config, JsonNode data, String rfcConnection) throws SQLException {
        String rfcName = config.get("targetname").asText(); // Zieltabelle: opheoimport
        JsonNode fieldsConfig = config.get("fields");
        String basePath = config.get("basePath").asText(); 
        String iteratorPath = config.get("iteratorPath").asText(); // "Sendungen/Sendung"
        
        //SAPConnection sapsystemc = new SAPConnection(rfcConnection, "Clownfish_UPLOAD");

        // Verbindungsaufbau
        SAPConnection sapsystemc = new SAPConnection(rfcConnection, "Clownfish_UPLOAD");
        this.rpytableread = new RPY_TABLE_READ(sapsystemc);
        //this.rpytableread.setSapConnection(sapsystemc);
        if (basePath.contains("/")) {
            String[] baseparts = basePath.split("/");

            // In Ihrem Fall ist der zu iterierende Array-Pfad: Touren
            JsonNode baseArray = data.get(baseparts[0]);

            if (baseArray.isArray()) {
                for (JsonNode baseitemWrapper : baseArray) {
                    JsonNode baseitemNode = baseitemWrapper.get(baseparts[1]);

                    // Verarbeite alle "Sendungen" innerhalb der Tour
                    processBaseItemRFC(sapsystemc, rfcName, fieldsConfig, baseitemNode, basePath, iteratorPath);
                }
            }
        } else {

        }
    }
    
    /**
     * Extrahiert Daten für eine einzelne Tour-Einheit (inkl. verschachtelter Sendungen)
     * und führt den DB-Insert aus.
     */
    private void processBaseItemDB(Connection conn, String tableName, JsonNode fieldsConfig, JsonNode baseitemNode, String basepath, String iteratorPath) throws SQLException {
        
        // Feste Tour-Basisdaten, die für jede Sendung kopiert werden
        Map<String, Object> baseData = new HashMap<>();
        
        // 1. Basis-Werte der Tour extrahieren (ohne Sendungen-Pfad)
        // Muss hier separat passieren, da der Pfad "Touren/Tour/..." in der Regeldatei
        // den Knoten relativ zur Wurzel "dataRoot" definiert.
        // Innerhalb der Methode "processTour" muss man sich relativ zum "tourNode" bewegen.
        
        // Vereinfachte Logik: Datenpunkte werden pro Sendung in die Zieltabelle geschrieben
        
        if (iteratorPath.contains("/")) {
            String[] iteratorparts = iteratorPath.split("/");
            
            JsonNode iteratorArray = baseitemNode.get(iteratorparts[0]);
        
            if (iteratorArray != null && iteratorArray.isArray()) {
                for (JsonNode iteratorWrapper : iteratorArray) {
                    JsonNode iteratorNode = iteratorWrapper.get(iteratorparts[1]);

                    // Hier werden alle notwendigen Felder (Tour- UND Sendungs-Daten) gesammelt
                    Map<String, Object> recordData = collectData(fieldsConfig, baseitemNode, iteratorNode, basepath, iteratorPath);

                    // 2. Daten in die Datenbank schreiben
                    insertDataDB(conn, tableName, recordData);
                }
            }
        } else {
            
        }
    }
    
    /**
     * Extrahiert Daten für eine einzelne Tour-Einheit (inkl. verschachtelter Sendungen)
     * und führt den DB-Insert aus.
     */
    private void processBaseItemRFC(SAPConnection sapsystem, String tableName, JsonNode fieldsConfig, JsonNode baseitemNode, String basepath, String iteratorPath) throws SQLException {
        
        // Feste Tour-Basisdaten, die für jede Sendung kopiert werden
        Map<String, Object> baseData = new HashMap<>();
        
        // 1. Basis-Werte der Tour extrahieren (ohne Sendungen-Pfad)
        // Muss hier separat passieren, da der Pfad "Touren/Tour/..." in der Regeldatei
        // den Knoten relativ zur Wurzel "dataRoot" definiert.
        // Innerhalb der Methode "processTour" muss man sich relativ zum "tourNode" bewegen.
        
        // Vereinfachte Logik: Datenpunkte werden pro Sendung in die Zieltabelle geschrieben
        
        if (iteratorPath.contains("/")) {
            String[] iteratorparts = iteratorPath.split("/");
            
            JsonNode iteratorArray = baseitemNode.get(iteratorparts[0]);
        
            if (iteratorArray != null && iteratorArray.isArray()) {
                for (JsonNode iteratorWrapper : iteratorArray) {
                    JsonNode iteratorNode = iteratorWrapper.get(iteratorparts[1]);

                    // Hier werden alle notwendigen Felder (Tour- UND Sendungs-Daten) gesammelt
                    Map<String, Object> recordData = collectData(fieldsConfig, baseitemNode, iteratorNode, basepath, iteratorPath);

                    // 2. Daten in die Datenbank schreiben
                    insertDataRFC(sapsystem, tableName, recordData);
                }
            }
        } else {
            
        }
    }
    
    /**
     * Sammelt die Datenpunkte basierend auf der Konfiguration und den Quelldaten.
     */
    private Map<String, Object> collectData(JsonNode fieldsConfig, JsonNode tourNode, JsonNode sendungNode, String basepath, String iteratorpath) {
        Map<String, Object> record = new HashMap<>();

        if (fieldsConfig.isArray()) {
            for (JsonNode fieldConfig : fieldsConfig) {
                String sourceFieldPath = fieldConfig.get("sourcefield").asText();
                String targetField = fieldConfig.get("targetfield").asText();
                String type = fieldConfig.get("type").asText();
                
                // Der Pfad wird geteilt, um den relativen Pfad zu ermitteln.
                // Beispiel: "Touren/Tour/MandantID" -> "MandantID"
                // Beispiel: "Touren/Tour/Sendungen/Sendung/SendungID" -> "Sendungen/Sendung/SendungID"
                
                // Vereinfachte Pfadlogik, die sich nach dem Knoten "Tour" richtet:
                String relativePath = sourceFieldPath.replace(basepath+"/", "");
                
                // Überprüfen, ob es sich um ein Sendungsfeld handelt
                if (relativePath.startsWith(iteratorpath+"/")) {
                    // Feld aus dem Sendungsknoten holen
                    String valueField = relativePath.replace(iteratorpath+"/", "");
                    JsonNode valueNode = sendungNode.get(valueField);
                    
                    record.put(targetField, convertValue(valueNode, type));
                    
                } else {
                    // Feld aus dem Tourknoten holen
                    JsonNode valueNode = tourNode.get(relativePath);
                    
                    record.put(targetField, convertValue(valueNode, type));
                }
            }
        }
        return record;
    }
    
    /**
     * Konvertiert einen JsonNode in den gewünschten Java-Typ.
     */
    private Object convertValue(JsonNode node, String type) {
        if (node == null || node.isNull()) {
            return null;
        }
        
        // Anpassung der Typen basierend auf der Regelstruktur
        return switch (type.toUpperCase()) {
            case "INT" -> node.asInt();
            case "STRING" -> node.asText();
            // Weitere Typen wie DOUBLE, DATE, etc. müssten hier ergänzt werden
            default -> node.asText(); 
        };
    }

    /**
     * Erstellt eine SQL INSERT-Anweisung und führt sie aus.
     */
    private void insertDataDB(Connection conn, String tableName, 
                                   Map<String, Object> data) throws SQLException {
        
        if (data.isEmpty()) {
            return;
        }
        
        // Erstellung der SQL-Query
        List<String> columns = new ArrayList<>(data.keySet());
        String columnNames = String.join(", ", columns);
        String valuePlaceholders = String.join(", ", java.util.Collections.nCopies(columns.size(), "?"));

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", 
                                    tableName, columnNames, valuePlaceholders);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Setzen der Parameter
            for (int i = 0; i < columns.size(); i++) {
                Object value = data.get(columns.get(i));
                pstmt.setObject(i + 1, value);
            }

            pstmt.executeUpdate();
            
            System.out.println("Datensatz eingefügt: " + data.get("tournummer") + "/" + data.get("sendungnummer"));
        }
    }
    
    /**
     * Erstellt eine SQL INSERT-Anweisung und führt sie aus.
     */
    private void insertDataRFC(SAPConnection sapc, String rfcName, Map<String, Object> data) {
        
        if (data.isEmpty()) {
            return;
        }
        executeAsync(rfcName, data, sapc);
        
    }
    
    public void executeAsync(String rfcFunction, Map parametermap, SAPConnection sapc) {
        rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
        try {
            //LOGGER.info("START SAP execute");
            JCoTable functions_table;
            HashMap<String, HashMap> sapexport = new HashMap<>();
            HashMap<String, List> saprfcfunctionparamMap = new HashMap<>();
            List<RfcFunctionParam> rfcfunctionparamlist = new ArrayList<>();
            rfcfunctionparamlist.addAll(rfc_get_function_interface.getRfcFunctionsParamList(rfcFunction, sapc));
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
                                function.getImportParameterList().setValue(rfcfunctionparam.getParameter(), jfp.getValue());
                            });
                        }
                    }

                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("C") == 0) {
                        JCoTable table = function.getChangingParameterList().getTable(rfcfunctionparam.getParameter()); 
                        insertMapToJCoTable(parametermap, table);
                        function.getChangingParameterList().setValue(rfcfunctionparam.getParameter(), table);
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
                                    rpytablereadlist = getRpytablereadlist(param.trim(), sapc);
                                    setTableValues(functions_table, rpytablereadlist, tablevalues);
                                    saptables.put(paramname, tablevalues);
                                }
                            } else {
                                if (exid.compareToIgnoreCase("u") == 0) {
                                    JCoStructure functions_structure = function.getExportParameterList().getStructure(paramname);
                                    rpytablereadlist = getRpytablereadlist(tablename, sapc);
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
                                    rpytablereadlist = getRpytablereadlist(param.trim(), sapc);
                                    setTableValues(functions_table, rpytablereadlist, tablevalues);
                                    saptables.put(paramname, tablevalues);
                                }
                            } else {
                                functions_table = function.getTableParameterList().getTable(paramname);
                                if (!functions_table.isEmpty()) {
                                    rpytablereadlist = getRpytablereadlist(tablename, sapc);
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
                                    rpytablereadlist = getRpytablereadlist(param.trim(), sapc);
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
        } catch (Exception ex) {
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
    
    private List<RpyTableRead> getRpytablereadlist(String tablename, SAPConnection sapc) {
        List<RpyTableRead> rpytablereadlist;
        if (rpyMap.containsKey(sapc.getDestination().getDestinationID() + "_" + tablename)) {
            rpytablereadlist = rpyMap.get(sapc.getDestination().getDestinationID() + "_" + tablename);
        } else {
            rpytablereadlist = rpytableread.getRpyTableReadList(tablename, sapc);
            rpyMap.put(sapc.getDestination().getDestinationID() + "_" + tablename, rpytablereadlist);
        }
        return rpytablereadlist;
    }
    
    /**
     * Fügt die Daten aus einer Java Map in eine JCoTable ein. Es wird
     * angenommen, dass die Map die Daten für eine einzelne Zeile enthält.
     * @param data
     * @param table
     * @throws java.lang.Exception
     */
    public void insertMapToJCoTable(Map<String, Object> data, JCoTable table) throws Exception {
        if (data == null || data.isEmpty()) {
            // Optionale Behandlung: Map ist leer
            System.out.println("Die Eingabe-Map ist leer, es werden keine Daten eingefügt.");
            return;
        }
        // 1. Eine neue, leere Zeile an das Ende der JCoTable anhängen
        table.appendRow();
        // 2. Map durchlaufen und die Werte in die entsprechenden Spalten kopieren
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // 4. Den Wert in die aktuelle Zeile der JCoTable setzen
            if (value != null) {
                // Konvertierung des Java-Werts in den geeigneten JCo-Datentyp
                // JCo ist intelligent genug, die meisten einfachen Java-Typen 
                // (String, Integer, Double, Boolean) korrekt zu setzen.
                table.setValue(key, value);
            } else {
                // Optional: Behandlung von Null-Werten, z.B. Setzen auf Standardwert oder leeren String
                table.setValue(key, "");
            }
        }
    }
}
