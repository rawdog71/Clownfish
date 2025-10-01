/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.metajson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

/**
 *
 * @author SulzbachR
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaJsonWrapper {
    private ArrayList<IMetaJson> metajson;

    public ArrayList<IMetaJson> getMetajson() {
        return metajson;
    }

    public void setMetajson(ArrayList<IMetaJson> metajson) {
        this.metajson = metajson;
    }
}
