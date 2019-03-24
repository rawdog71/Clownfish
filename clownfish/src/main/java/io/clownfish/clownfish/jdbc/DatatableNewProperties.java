/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

import java.util.ArrayList;

/**
 *
 * @author sulzbachr
 */
public class DatatableNewProperties {
    private String tablename;
    private ArrayList<DatatableNewValue> valuelist;

    public DatatableNewProperties() {
        valuelist = new ArrayList<>();
    }

    public DatatableNewProperties(String tablename, ArrayList<DatatableNewValue> valuelist) {
        this.tablename = tablename;
        valuelist = new ArrayList<>();
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
}
