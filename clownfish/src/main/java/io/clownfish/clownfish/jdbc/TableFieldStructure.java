/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

import java.util.ArrayList;

/**
 *
 * @author sulzbachr
 */
public class TableFieldStructure {
    private ArrayList<TableField> tableFieldsList;
    private String default_order;

    public TableFieldStructure() {
    }

    public TableFieldStructure(ArrayList<TableField> tableFieldsList, String default_order) {
        this.tableFieldsList = tableFieldsList;
        this.default_order = default_order;
    }

    public ArrayList<TableField> getTableFieldsList() {
        return tableFieldsList;
    }

    public void setTableFieldsList(ArrayList<TableField> tableFieldsList) {
        this.tableFieldsList = tableFieldsList;
    }

    public String getDefault_order() {
        return default_order;
    }

    public void setDefault_order(String default_order) {
        this.default_order = default_order;
    }
    
    
}
