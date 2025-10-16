/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.clownfish.clownfish.jsonator.conditions.ConditionsWrapper;
import io.clownfish.clownfish.jsonator.conditions.ICondition;
import io.clownfish.clownfish.jsonator.datasource.DatasourceWrapper;
import io.clownfish.clownfish.jsonator.datasource.IDatasource;
import io.clownfish.clownfish.jsonator.datasource.auth.AccessToken;
import io.clownfish.clownfish.jsonator.datasource.auth.BearerToken;
import io.clownfish.clownfish.jsonator.listconditions.ListCondition;
import io.clownfish.clownfish.jsonator.listconditions.ListConditionParser;
import io.clownfish.clownfish.jsonator.metajson.IMetaJson;
import io.clownfish.clownfish.jsonator.metajson.MetaJsonWrapper;
import io.clownfish.clownfish.jsonator.tools.Replacer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SulzbachR
 */
public class JsonMapper {
    private JsonNode paramnode;
    
    public JsonMapper(JsonNode rootNode) {
        this.paramnode = rootNode;
    }
    
    private static boolean static_refresh;
    
    public String map(String mappingcontent, boolean refresh) {
        static_refresh = refresh;
        ObjectMapper outputMapper = new ObjectMapper();
        // Ein Haupt-Objekt erstellen
        ObjectNode mainNode = outputMapper.createObjectNode();
        ObjectNode outputNode = outputMapper.createObjectNode();
        HashMap<String, JsonNode> jsonlists = new HashMap<>();
        HashMap<String, ArrayNode> jsonarray = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        
        if (null != paramnode) {
            mainNode.set("PARAMETER", paramnode);
        }
        
        // Hole die Datenquellen
        ArrayList<IDatasource> datasources = new ArrayList();
        try {
            // Die Hauptklasse, die die Liste der Datenquellen enthält
            DatasourceWrapper wrapper = objectMapper.readValue(mappingcontent, DatasourceWrapper.class);
            // Hole die Liste der Datenquellen
            datasources = wrapper.getDatasources();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Hole die Conditions
        ArrayList<ICondition> conditions = new ArrayList();
        try {
            // Die Hauptklasse, die die Liste der Datenquellen enthält
            ConditionsWrapper condwrapper = objectMapper.readValue(mappingcontent, ConditionsWrapper.class);
            // Hole die Liste der Datenquellen
            conditions = condwrapper.getConditions();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ObjectMapper objectMapper2 = new ObjectMapper();
        ArrayList<IMetaJson> mappings = new ArrayList();
        try {
            // Die Hauptklasse, die die Liste der Datenquellen enthält
            MetaJsonWrapper wrapper = objectMapper2.readValue(mappingcontent, MetaJsonWrapper.class);
            // Hole die Liste der Mappingdaten
            mappings = wrapper.getMetajson();
            // Iteriere durch die Liste und zeige die Klassentypen an
            for (IMetaJson mapping : mappings) {
                mapping.setConditions(conditions);
                static_refresh = mapping.init(static_refresh);
                /*
                System.out.println("TAG: " + mapping.getTag());
                System.out.println("Parent: " + mapping.getParent());
                System.out.println("Contenttype: " + mapping.getContenttype());
                System.out.println("Contentvalue: " + mapping.getContentvalue());
                System.out.println("Contentsource: " + mapping.getContentsource());
                System.out.println("Contenttable: " + mapping.getContenttype());
                System.out.println("Contentfield: " + mapping.getContentfield());
                System.out.println("Condition: " + mapping.getCondition());
                System.out.println("Instanziierte Klasse: " + mapping.getClass().getName());
                System.out.println("---");
                */
                
                if (!mapping.getContentsource().isEmpty()) {
                    Optional<IDatasource> result = datasources.stream()
                        .filter(datasource -> mapping.getContentsource().equals(datasource.getName()))
                        .findFirst();

                    // Prüfen, ob ein Ergebnis gefunden wurde
                    if (result.isPresent()) {
                        //System.out.println("Gefundene Datenquelle: " + result.get().getName());
                        
                        switch (mapping.getContentvalue()) {
                            case "API" -> {
                                switch (mapping.getContenttype()) {
                                    case "list" -> {
                                        // Listen Verarbeitung
                                        JsonNode value = getJsonNodeVal(mapping, result, mainNode, refresh);
                                        jsonlists.put(mapping.getTag(), value);
                                    }
                                    case "string", "number", "boolean" -> {
                                        // Single Verarbeitung
                                        if (mapping.getParent().isEmpty()) {
                                            String value = getStringValAPI(mapping, result, mainNode, 0, refresh);
                                            //System.out.println("VALUE: " + value);
                                            mainNode.put(mapping.getTag(), value);
                                            if (mapping.getOutput()) {
                                                outputNode.put(mapping.getTag(), value);
                                            }
                                        } else {
                                            //System.out.println("PARENT: " + mapping.getParent());
                                            // Ein Array für die Liste erstellen
                                            Optional<IMetaJson> parent = mappings.stream().filter(map -> mapping.getParent().equals(map.getTag())).findFirst();
                                            if (0 == parent.get().getContentvalue().compareToIgnoreCase("STRUCT")) {
                                                // Parent als Struktur Verarbeitung
                                                //System.out.println("PARENT: " + mapping.getParent());
                                                
                                                if (!parent.get().getParent().isEmpty()) {                                         
                                                    JsonNode parentNodeUp = mainNode.findPath(parent.get().getParent());
                                                    switch (parentNodeUp.getNodeType()) {
                                                        case OBJECT -> {
                                                            JsonNode parentNode = mainNode.findPath(mapping.getParent());
                                                            ObjectNode objectNode = (ObjectNode) parentNode;
                                                            String value = getStringValAPI(mapping, result, mainNode, 0, refresh);
                                                            objectNode.put(mapping.getTag(), value);

                                                            if (mapping.getOutput()) {
                                                                JsonNode parentoutNode = outputNode.findPath(mapping.getParent());
                                                                ObjectNode objectoutNode = (ObjectNode) parentoutNode;
                                                                //String outvalue = getStringValAPI(mapping, result, mainNode, 0);
                                                                objectoutNode.put(mapping.getTag(), value);
                                                            }
                                                        }
                                                        case ARRAY -> {
                                                            for (int i=0; i<parentNodeUp.size(); i++) {
                                                                JsonNode parentNode = jsonarray.get(parent.get().getParent()).get(i);
                                                                ObjectNode objectNode = (ObjectNode) parentNode.get(mapping.getParent());
                                                                String value = getStringValAPI(mapping, result, mainNode, i, refresh);
                                                                objectNode.put(mapping.getTag(), value);

                                                                if (mapping.getOutput()) {
                                                                    JsonNode parentoutNode = jsonarray.get(parent.get().getParent()).get(i);
                                                                    ObjectNode objectoutNode = (ObjectNode) parentoutNode.get(mapping.getParent());
                                                                    //String outvalue = getStringValAPI(mapping, result, mainNode, 0);
                                                                    objectoutNode.put(mapping.getTag(), value);
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    JsonNode parentNode = mainNode.findPath(mapping.getParent());
                                                    ObjectNode objectNode = (ObjectNode) parentNode;
                                                    String value = getStringValAPI(mapping, result, mainNode, 0, refresh);
                                                    objectNode.put(mapping.getTag(), value);

                                                    if (mapping.getOutput()) {
                                                        JsonNode parentoutNode = outputNode.findPath(mapping.getParent());
                                                        ObjectNode objectoutNode = (ObjectNode) parentoutNode;
                                                        //String outvalue = getStringValAPI(mapping, result, mainNode, 0);
                                                        objectoutNode.put(mapping.getTag(), value);
                                                    }
                                                }
                                            } else {
                                                ArrayNode listNode;
                                                if (jsonarray.containsKey(mapping.getParent())) {
                                                    listNode = jsonarray.get(mapping.getParent());
                                                } else {
                                                    listNode = objectMapper.createArrayNode();
                                                    jsonarray.put(mapping.getParent(), listNode);
                                                }
                                                int i = 0;
                                                for (JsonNode jn : jsonlists.get(mapping.getParent())) {
                                                    if (listNode.size()>0) {
                                                        ObjectNode listentry = (ObjectNode)listNode.get(i);
                                                        if (null != listentry) {
                                                            listentry.put(mapping.getTag(), jn.at(mapping.getContentfield()));
                                                            if (listentry.get(mapping.getTag()).isMissingNode()) {
                                                                listentry.put(mapping.getTag(), mapping.getNullval());
                                                            }
                                                            listNode.set(i, listentry);
                                                        } else {
                                                            listentry = objectMapper.createObjectNode();
                                                            listentry.put(mapping.getTag(), jn.at(mapping.getContentfield()));
                                                            listNode.add(listentry);
                                                        }
                                                    } else {
                                                        ObjectNode listentry = objectMapper.createObjectNode();
                                                        listentry.put(mapping.getTag(), jn.at(mapping.getContentfield()));
                                                        listNode.add(listentry);
                                                    }
                                                    i++;
                                                }
                                                mainNode.set(mapping.getParent(), listNode);
                                                if (mapping.getOutput()) {
                                                    outputNode.set(mapping.getParent(), listNode);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            case "DB" -> {
                                switch (mapping.getContenttype()) {
                                    case "list" -> {
                                        // Listen Verarbeitung
                                        JsonNode value = getJsonNodeVal(mapping, result, mainNode, refresh);
                                        jsonlists.put(mapping.getTag(), value);
                                    }
                                    case "string", "number", "boolean" -> {
                                        // Single Verarbeitung
                                        if (mapping.getParent().isEmpty()) {
                                            String value = getStringValDB(mapping, result, mainNode, refresh);
                                            //System.out.println("VALUE: " + value);
                                            mainNode.put(mapping.getTag(), value);
                                            if (mapping.getOutput()) {
                                                outputNode.put(mapping.getTag(), value);
                                            }
                                        } else {
                                            //System.out.println("PARENT: " + mapping.getParent());
                                            // Ein Array für die Liste erstellen
                                            Optional<IMetaJson> parent = mappings.stream().filter(map -> mapping.getParent().equals(map.getTag())).findFirst();
                                            if (0 == parent.get().getContentvalue().compareToIgnoreCase("STRUCT")) {
                                                // Parent als Struktur Verarbeitung
                                                //System.out.println("PARENT: " + mapping.getParent());
                                                JsonNode parentNode = mainNode.findPath(mapping.getParent());
                                                ObjectNode objectNode = (ObjectNode) parentNode;
                                                String value = getStringValDB(mapping, result, mainNode, refresh);
                                                objectNode.put(mapping.getTag(), value);
                                                
                                                if (mapping.getOutput()) {
                                                    JsonNode parentoutNode = outputNode.findPath(mapping.getParent());
                                                    ObjectNode objectoutNode = (ObjectNode) parentoutNode;
                                                    String outvalue = getStringValDB(mapping, result, mainNode, refresh);
                                                    objectoutNode.put(mapping.getTag(), outvalue);
                                                }
                                            } else {
                                                ArrayNode listNode;
                                                if (jsonarray.containsKey(mapping.getParent())) {
                                                    listNode = jsonarray.get(mapping.getParent());
                                                } else {
                                                    listNode = objectMapper.createArrayNode();
                                                    jsonarray.put(mapping.getParent(), listNode);
                                                }
                                                int i = 0;
                                                for (JsonNode jn : jsonlists.get(mapping.getParent())) {
                                                    if (listNode.size()>0) {
                                                        ObjectNode listentry = (ObjectNode)listNode.get(i);
                                                        if (null != listentry) {
                                                            listentry.put(mapping.getTag(), jn.at(mapping.getContentfield()));
                                                            listNode.set(i, listentry);
                                                        } else {
                                                            listentry = objectMapper.createObjectNode();
                                                            listentry.put(mapping.getTag(), jn.at(mapping.getContentfield()));
                                                            listNode.add(listentry);
                                                        }
                                                    } else {
                                                        ObjectNode listentry = objectMapper.createObjectNode();
                                                        listentry.put(mapping.getTag(), jn.at(mapping.getContentfield()));
                                                        listNode.add(listentry);
                                                    }
                                                    i++;
                                                }
                                                mainNode.set(mapping.getParent(), listNode);
                                                if (mapping.getOutput()) {
                                                    outputNode.set(mapping.getParent(), listNode);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Keine Datenquelle mit dem Namen '" + mapping.getContentsource() + "' gefunden.");
                    }
                } else {
                    switch (mapping.getContentvalue()) {
                        case "PARAM" -> {
                            if (mapping.getParent().isEmpty()) {
                                // Kein Parent 
                                String value = mapping.getValue(mapping.getContenttype(), null, "");
                                //System.out.println("VALUE: " + value);
                                // Einfache Properties hinzufügen
                                mainNode.put(mapping.getTag(), mainNode.at(value).asText());
                                if (mapping.getOutput()) {
                                    outputNode.put(mapping.getTag(), mainNode.at(value).asText());
                                }
                            } else {
                                JsonNode parentNode = mainNode.findPath(mapping.getParent());
                                ObjectNode objectNode = (ObjectNode) parentNode;
                                String value = mapping.getValue(mapping.getContenttype(), null, "");
                                objectNode.put(mapping.getTag(), mainNode.at(value).asText());
                                if (mapping.getOutput()) {
                                    JsonNode parentoutNode = outputNode.findPath(mapping.getParent());
                                    ObjectNode objectoutNode = (ObjectNode) parentoutNode;
                                    String outvalue = mapping.getValue(mapping.getContenttype(), null, "");
                                    objectoutNode.put(mapping.getTag(), mainNode.at(outvalue).asText());
                                }
                            }
                        }
                        case "FIX" -> {
                            if (mapping.getParent().isEmpty()) {
                                // Kein Parent 
                                String value = mapping.getValue(mapping.getContenttype(), null, "");
                                //System.out.println("VALUE: " + value);
                                // Einfache Properties hinzufügen
                                mainNode.put(mapping.getTag(), value);
                                if (mapping.getOutput()) {
                                    outputNode.put(mapping.getTag(), value);
                                }
                            } else {
                                JsonNode parentNode = mainNode.findPath(mapping.getParent());
                                ObjectNode objectNode = (ObjectNode) parentNode;
                                String value = mapping.getValue(mapping.getContenttype(), null, "");
                                objectNode.put(mapping.getTag(), value);
                                if (mapping.getOutput()) {
                                    JsonNode parentoutNode = outputNode.findPath(mapping.getParent());
                                    ObjectNode objectoutNode = (ObjectNode) parentoutNode;
                                    String outvalue = mapping.getValue(mapping.getContenttype(), null, "");
                                    objectoutNode.put(mapping.getTag(), outvalue);
                                }
                            }
                        }
                        case "STRUCT" -> {
                            //System.out.println("VALUE: " + value);
                            // Einfache Properties hinzufügen
                            if (mapping.getParent().isEmpty()) {
                                // Kein Parent
                                mainNode.putObject(mapping.getTag());
                                if (mapping.getOutput()) {
                                    outputNode.putObject(mapping.getTag());
                                }
                            } else {
                                JsonNode parentNode = mainNode.findPath(mapping.getParent());
                                switch (parentNode.getNodeType()) {
                                    case OBJECT -> {
                                        ObjectNode objectNode = (ObjectNode) parentNode;
                                        objectNode.putObject(mapping.getTag());
                                        /*
                                        parentNode = outputNode.findPath(mapping.getParent());
                                        objectNode = (ObjectNode) parentNode;
                                        objectNode.putObject(mapping.getTag());
                                        */
                                    }
                                    case ARRAY -> {
                                        ArrayNode arrayNode = (ArrayNode) parentNode;
                                        for (int i = 0; i<arrayNode.size(); i++) {
                                            JsonNode jn = arrayNode.get(i);
                                            ((ObjectNode)jn).putObject(mapping.getTag());
                                        }                                   
                                        /*
                                        parentNode = outputNode.findPath(mapping.getParent());
                                        arrayNode = (ArrayNode) parentNode;
                                        arrayNode.add(mapping.getTag());
                                        */
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Die JSON-Struktur ausgeben
            String jsonOutput = outputMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputNode);
            //System.out.println(jsonOutput);
            return jsonOutput;
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }
    
    private String getStringValAPI(IMetaJson mapping, Optional<IDatasource> result, ObjectNode mainNode, int index, boolean refresh) {
        String condition = Replacer.replaceVariables((String) mapping.getCondition(), mainNode);
        condition = Replacer.processSubstringPattern(condition);
        
        JsonNode jn = mapping.getJson(result.get().getConnection(), condition, "",  "", result.get().getMethod(), refresh);
        JsonNode checkNode = jn.at(mapping.getContenttable());
        String value;
        if (checkNode.isArray()) {
            value = mapping.getValue(mapping.getContenttype(), jn, mapping.getContenttable()+"/"+index+mapping.getContentfield());
        } else {
            value = mapping.getValue(mapping.getContenttype(), jn, mapping.getContenttable()+mapping.getContentfield());
        }
        if (null != value) {
            return value;
        } else {
            return mapping.getNullval();
        }
    }
    
    private String getStringValDB(IMetaJson mapping, Optional<IDatasource> result, ObjectNode mainNode, boolean refresh) {
        String condition = Replacer.replaceVariables((String) mapping.getCondition(), mainNode);
        condition = Replacer.processSubstringPattern(condition);
        
        JsonNode jn = mapping.getJson(result.get().getConnection(), condition, "", result.get().getUser(), result.get().getPassword(), refresh);
        
        ObjectMapper mapper = new ObjectMapper();
        
        // This TypeReference is crucial for telling Jackson to deserialize into a List of Maps.
        TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {};
        
        List<Map<String, Object>> list = null;
        try {
            list = mapper.readValue(jn.toString(), typeRef);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(JsonMapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (null != list) {
            // Get the first item from the list, which is a Map.
            Map<String, Object> firstItem = list.get(0);

            // Access the value of BOX_ID_REF from the map.
            String value = (String) firstItem.get(mapping.getContentfield());
            return value;
        } else {
            return null;
        }
        
    }
    
    private JsonNode getJsonNodeVal(IMetaJson mapping, Optional<IDatasource> result, ObjectNode mainNode, boolean refresh) {
        String condition = Replacer.replaceVariables((String) mapping.getCondition(), mainNode);
        condition = Replacer.processSubstringPattern(condition);
        if (result.get().getAuth().getUrl().isEmpty()) {
            JsonNode jn = null;
            List<ListCondition> listconditions = null;
            switch (mapping.getContentvalue()) {
                case "API" -> {
                    jn = mapping.getJson(result.get().getConnection(), condition, "", "", result.get().getMethod(), refresh);
                }
                case "DB" -> {
                    jn = mapping.getJson(result.get().getConnection(), condition, "", result.get().getUser(), result.get().getPassword(), refresh);
                }
            }
            if (!mapping.getListcondition().isEmpty()) {
                System.out.println("LISTCONDTION:" + mapping.getListcondition());
                listconditions = ListConditionParser.parseConditions(mapping.getListcondition());
            }
            if (!mapping.getContenttable().isEmpty()) {
                JsonNode checkNode = jn.at(mapping.getContenttable());
                JsonNode value = null;
                if (checkNode.isArray()) {
                    value = mapping.getNode(jn, mapping.getContenttable());
                } else {
                    System.out.println("NODE: " + mapping.getContenttable() + " is not a list");
                }
                if (null != listconditions) {
                    value = ListConditionParser.filterNodes(value, listconditions);
                }
                return value;
            } else {
                return jn;
            }
        } else {
            AccessToken accesstoken = null;
            switch (result.get().getAuth().getType()) {
                case "Bearer" -> { 
                    BearerToken bt = new BearerToken();
                    accesstoken = bt.getAccessToken(result.get().getAuth().getUrl());
                }
            }
            List<ListCondition> listconditions = null;
            
            JsonNode jn = mapping.getJson(result.get().getConnection(), condition, "", accesstoken.getAccesstoken(), result.get().getMethod(), refresh);
            JsonNode checkNode = jn.at(mapping.getContenttable());
            JsonNode value = null;
            if (!mapping.getListcondition().isEmpty()) {
                //System.out.println("LISTCONDTION:" + mapping.getListcondition());
                listconditions = ListConditionParser.parseConditions(mapping.getListcondition());
            }
            
            if (checkNode.isArray()) {
                value = mapping.getNode(jn, mapping.getContenttable());
                if (null != listconditions) {
                    value = ListConditionParser.filterNodes(value, listconditions);
                }
            } else {
                System.out.println("NODE: " + mapping.getContenttable() + " is not a list");
            }
            return value;
        }        
    }

    /*
    private String nullcheck(JsonNode jn, IMetaJson mapping) {
        if (null != jn.at(mapping.getContentfield()) {
            return jn.at(mapping.getContentfield());
        }
    }
*/
}
