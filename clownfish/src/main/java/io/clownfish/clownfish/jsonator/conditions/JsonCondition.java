/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.conditions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SulzbachR
 */
public class JsonCondition implements ICondition {
    private String type;
    private String reference;
    private Object condition;

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public String getCondition() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(condition);
            
            return jsonString;
        } catch (JsonProcessingException ex) {
            Logger.getLogger(JsonCondition.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getType() {
        return type;
    }
    
}
