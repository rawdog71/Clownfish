/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.conditions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

/**
 *
 * @author SulzbachR
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionsWrapper {
    private ArrayList<ICondition> conditions;

    public ArrayList<ICondition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<ICondition> conditions) {
        this.conditions = conditions;
    }
}
