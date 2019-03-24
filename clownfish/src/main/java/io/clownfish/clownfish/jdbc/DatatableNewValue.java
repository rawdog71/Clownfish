/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

/**
 *
 * @author sulzbachr
 */
public class DatatableNewValue {
    private String field;
    private String value;

    public DatatableNewValue() {
    }

    public DatatableNewValue(String field, String value) {
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
}
