/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.metajson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.clownfish.clownfish.jsonator.conditions.ICondition;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author SulzbachR
 * Interface for API values
    {
        "tag": "productSerie",
        "parent": "",
        "contenttype": "string",
        "contentvalue": "API",
        "contentsource": "PRODIS",
        "contenttable": "HEAD",
        "contentfield": "VTEXT",
        "condition": "#PRODIS",
        "output": true
    }
 */
public class MetaApi implements IMetaJson {
    private String tag;
    private String parent;
    private String contenttype;
    private String contentvalue;
    private String contentsource;
    private String contenttable;
    private String contentfield;
    private String condition;
    private String listcondition;
    private String nullval;
    private boolean output;
    private ArrayList<ICondition> conditions;
    private static boolean static_refresh = true;
    
    private static HashMap<String, JsonNode> jsonmap = new HashMap<>();

    @Override
    public boolean init(boolean refresh) {
        if (refresh)
            static_refresh = true;
        return false;
    }
    
    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getParent() {
        return parent;
    }

    @Override
    public String getContenttype() {
        return contenttype;
    }

    @Override
    public String getContentvalue() {
        return contentvalue;
    }

    @Override
    public String getContentsource() {
        return contentsource;
    }

    @Override
    public String getContentfield() {
        return contentfield;
    }

    @Override
    public String getContenttable() {
        return contenttable;
    }

    @Override
    public Object getCondition() {
        if (condition.startsWith("#")) {
            return conditions.stream().filter(cond -> condition.substring(1).equals(cond.getReference())).findFirst().get().getCondition();
        } else {
            return condition;
        }
    }

    @Override
    public String getValue(String type, JsonNode jn, String xpath) {
        JsonNode valueNode = jn.at(xpath);
        // Prüfe, ob der Knoten existiert, bevor du darauf zugreifst
        if (!valueNode.isMissingNode()) {
            return valueNode.asText();
        } else {
            return null;
        }
    }
    
    @Override
    public JsonNode getJson(String url, String condition, String conditiontype, String authtoken, String method, boolean refresh) {
        if (static_refresh) {
            static_refresh = refresh;
        }
        if (0 == method.compareToIgnoreCase("GET")) {
            if (jsonmap.containsKey(url+"_"+condition) && !static_refresh) {
                return jsonmap.get(url+"_"+condition);
            } else {
                static_refresh = false;
                String finalQuery = "";
                String[] params = condition.split("&");
                String encodedValue;
                for (String param : params) {
                    String[] values = param.split("=");
                    encodedValue = values[0] + "=" + URLEncoder.encode(values[1], StandardCharsets.UTF_8);
                    String correctedValue = encodedValue.replace("+", "%20");
                    finalQuery += correctedValue + "&";
                }
                String fullUrl = url + "?" + finalQuery.substring(0, finalQuery.lastIndexOf("&"));

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .build();
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String jsonBody = response.body();
                        // Jackson ObjectMapper erstellen
                        ObjectMapper mapper = new ObjectMapper();
                        // Den JSON-String als JsonNode parsen
                        JsonNode rootNode = mapper.readTree(jsonBody);
                        jsonmap.put(url+"_"+condition, rootNode);
                        return rootNode;
                    } else {
                        System.out.println("Fehler beim Aufruf des Webservices. Statuscode: " + response.statusCode());
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else {
            if (jsonmap.containsKey(url+"_"+condition)) {
                return jsonmap.get(url+"_"+condition);
            } else {
                HttpClient client = HttpClient.newHttpClient();

                // HttpRequest erstellen
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + authtoken)
                        .POST(HttpRequest.BodyPublishers.ofString(condition))
                        .build();
                try {
                    // Anfrage senden und Antwort erhalten
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                            String jsonBody = response.body();
                            // Jackson ObjectMapper erstellen
                            ObjectMapper mapper = new ObjectMapper();
                            // Den JSON-String als JsonNode parsen
                            JsonNode rootNode = mapper.readTree(jsonBody);
                            jsonmap.put(url+"_"+condition, rootNode);
                            return rootNode;
                        } else {
                            System.out.println("Fehler beim Aufruf des Webservices. Statuscode: " + response.statusCode());
                            return null;
                        }

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

    @Override
    public JsonNode getNode(JsonNode jn, String xpath) {
        JsonNode node = jn.at(xpath);
        return node;
    }

    @Override
    public void setConditions(ArrayList<ICondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean getOutput() {
        return output;
    }

    @Override
    public String getListcondition() {
        return listcondition;
    }

    @Override
    public String getNullval() {
        return nullval;
    }
}
