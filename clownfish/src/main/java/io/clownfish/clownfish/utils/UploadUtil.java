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
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author SulzbachR
 */

public class UploadUtil {
    @Autowired transient CfDatasourceService cfdatasourceService;
    private JDBCUtil jdbcutil = null;

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
            System.out.println("Import abgeschlossen.");

        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der JSON-Dateien: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Datenbankfehler: " + e.getMessage());
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

        /*
        // Verbindungsaufbau
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false); // Transaktionsmanagement
            
            if (basePath.contains("/")) {
                String[] baseparts = basePath.split("/");
                
                // In Ihrem Fall ist der zu iterierende Array-Pfad: Touren
                JsonNode baseArray = data.get(baseparts[0]);

                if (baseArray.isArray()) {
                    for (JsonNode baseitemWrapper : baseArray) {
                        JsonNode baseitemNode = baseitemWrapper.get(baseparts[1]);

                        // Verarbeite alle "Sendungen" innerhalb der Tour
                        processBaseItemRFC(conn, rfcName, fieldsConfig, baseitemNode, basePath, iteratorPath);
                    }
                }
            } else {
                
            }
            
            conn.commit(); // Alle Änderungen übernehmen

        } // conn wird automatisch geschlossen
        */
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
    private void processBaseItemRFC(Connection conn, String tableName, JsonNode fieldsConfig, JsonNode baseitemNode, String basepath, String iteratorPath) throws SQLException {
        
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
                    insertDataRFC(conn, tableName, recordData);
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
    private void insertDataRFC(Connection conn, String tableName, 
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
}
