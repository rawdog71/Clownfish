/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.clownfish.clownfish.jsonator.metajson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import io.clownfish.clownfish.jsonator.conditions.ICondition;
import java.util.ArrayList;

/**
 *
 * @author SulzbachR
 */

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "contentvalue",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MetaParam.class, name = "PARAM"),
    @JsonSubTypes.Type(value = MetaStruct.class, name = "STRUCT"),
    @JsonSubTypes.Type(value = MetaFix.class, name = "FIX"),
    @JsonSubTypes.Type(value = MetaApi.class, name = "API"),
    @JsonSubTypes.Type(value = MetaDatabase.class, name = "DB"),
    @JsonSubTypes.Type(value = MetaFile.class, name = "FILE")
})
public interface IMetaJson {
    public boolean init(boolean refresh);
    public String getTag();
    public String getParent();
    public String getContenttype();
    public String getContentvalue();
    public String getContentsource();
    public String getContenttable();
    public String getContentfield();
    public Object getCondition();
    public String getListcondition();
    public String getNullval();
    public boolean getOutput();
    public String getValue(String type, JsonNode jn, String xpath);
    public JsonNode getNode(JsonNode jn, String xpath);
    public JsonNode getJson(String url, String condition, String conditiontype, String authtoken, String method, boolean refresh); 
    public void setConditions(ArrayList<ICondition> conditions);
}
