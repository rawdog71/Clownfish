/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

import java.util.ArrayList;

/**
 *
 * @author sulzbachr
 */
public class DatatableDeleteProperties {
    private String tablename;
    private ArrayList<DatatableDeleteValue> valuelist;

    public DatatableDeleteProperties() {
        valuelist = new ArrayList<>();
    }

    public DatatableDeleteProperties(String tablename, ArrayList<DatatableDeleteValue> valuelist) {
        this.tablename = tablename;
        valuelist = new ArrayList<>();
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public ArrayList<DatatableDeleteValue> getValuelist() {
        return valuelist;
    }

    public void setValuelist(ArrayList<DatatableDeleteValue> valuelist) {
        this.valuelist = valuelist;
    }
}
