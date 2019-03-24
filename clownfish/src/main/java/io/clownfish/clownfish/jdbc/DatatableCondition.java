/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

/**
 *
 * @author sulzbachr
 */
public class DatatableCondition {
    private String field;
    private String operand;
    private String value;

    public DatatableCondition() {
    }

    public DatatableCondition(String field, String operand, String value) {
        this.field = field;
        this.operand = operand;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
