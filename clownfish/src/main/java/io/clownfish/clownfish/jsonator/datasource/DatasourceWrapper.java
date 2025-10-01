/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.datasource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

/**
 *
 * @author SulzbachR
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceWrapper {
    private ArrayList<IDatasource> datasources;

    public ArrayList<IDatasource> getDatasources() {
        return datasources;
    }

    public void setDatasources(ArrayList<IDatasource> datasources) {
        this.datasources = datasources;
    }
}
