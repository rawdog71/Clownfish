/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.metajson;

import com.fasterxml.jackson.databind.JsonNode;
import io.clownfish.clownfish.jsonator.conditions.ICondition;
import java.util.ArrayList;

/**
 *
 * @author SulzbachR
 * Interface for STRUCT values
    {
        "tag": "generalInformation",
        "parent": "",
        "contenttype": "",
        "contentvalue": "STRUCT",
        "contentsource": "",
        "contenttable": "",
        "contentfield": "",
        "condition": "",
        "output": true
    }
 */
public class MetaStruct implements IMetaJson {
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

    @Override
    public boolean init(boolean refresh) {
        //if (refresh)
            //static_refresh = true;
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
    public String getCondition() {
        return condition;
    }

    @Override
    public String getValue(String type, JsonNode jn, String xpath) {
        return contentfield;
    }

    @Override
    public JsonNode getJson(String url, String condition, String conditiontype, String authtoken, String method, boolean refresh) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
