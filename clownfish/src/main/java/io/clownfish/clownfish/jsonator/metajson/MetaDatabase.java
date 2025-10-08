/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.metajson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.clownfish.clownfish.jsonator.conditions.ICondition;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author SulzbachR
 */
public class MetaDatabase implements IMetaJson {
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
    
    private static HashMap<String, JsonNode> jsonmap = new HashMap<>();

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
    public String getCondition() {
        if (condition.startsWith("#")) {
            return conditions.stream().filter(cond -> condition.substring(1).equals(cond.getReference())).findFirst().get().getCondition();
        } else {
            return condition;
        }
    }

    @Override
    public String getValue(String type, JsonNode jn, String xpath) {
        return "";
    }

    @Override
    public JsonNode getJson(String url, String condition, String conditiontype, String authtoken, String method) {
        if (jsonmap.containsKey(url+"_"+condition)) {
            return jsonmap.get(url+"_"+condition);
        } else {
            String user = authtoken;
            String password = method;
            String sql = condition;

            // Laden des jTDS-Treibers (nicht immer notwendig, aber gute Praxis)
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            List<Map<String, Object>> resultList = new ArrayList<>();

            try (Connection con = DriverManager.getConnection(url, user, password);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>(columns);
                    for (int i = 1; i <= columns; ++i) {
                        // Spaltenname und Wert in die Map einfügen
                        row.put(md.getColumnName(i), rs.getObject(i));
                    }
                    resultList.add(row);
                }

                // Umwandlung der Liste in einen JSON-String mit Jackson
                ObjectMapper mapper = new ObjectMapper();
                mapper.findAndRegisterModules();
                String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultList);

                // Den JSON-String als JsonNode parsen
                JsonNode rootNode = mapper.readTree(jsonOutput);

                //System.out.println(jsonOutput);
                jsonmap.put(url+"_"+condition, rootNode);
                return rootNode;
            } catch (SQLException | com.fasterxml.jackson.core.JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public JsonNode getNode(JsonNode jn, String xpath) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
