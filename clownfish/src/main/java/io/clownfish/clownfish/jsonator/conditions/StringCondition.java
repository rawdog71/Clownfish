/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.conditions;

/**
 *
 * @author SulzbachR
 */
public class StringCondition implements ICondition {
    private String type;
    private String reference;
    private String condition;

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @Override
    public String getType() {
        return type;
    }
    
}
