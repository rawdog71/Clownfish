/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

import java.util.ArrayList;

/**
 *
 * @author sulzbachr
 */
public class DatatableUpdateProperties {
    private String tablename;
    private ArrayList<DatatableNewValue> valuelist;
    private ArrayList<DatatableCondition> conditionlist;

    public DatatableUpdateProperties() {
        valuelist = new ArrayList<>();
        conditionlist = new ArrayList<>();
    }

    public DatatableUpdateProperties(String tablename) {
        this.tablename = tablename;
        valuelist = new ArrayList<>();
        conditionlist = new ArrayList<>();
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public ArrayList<DatatableNewValue> getValuelist() {
        return valuelist;
    }

    public void setValuelist(ArrayList<DatatableNewValue> valuelist) {
        this.valuelist = valuelist;
    }

    public ArrayList<DatatableCondition> getConditionlist() {
        return conditionlist;
    }

    public void setConditionlist(ArrayList<DatatableCondition> conditionlist) {
        this.conditionlist = conditionlist;
    }
}
