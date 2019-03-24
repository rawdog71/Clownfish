/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

/**
 *
 * @author sulzbachr
 */
public class TableField {
    private String name;
    private String type;
    private boolean primaryKey;
    private int size;
    private int decimaldigits;
    private String nullable;

    public TableField() {
    }

    public TableField(String name, String type, boolean primaryKey, int size, int decimaldigits, String nullable) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.size = size;
        this.decimaldigits = decimaldigits;
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDecimaldigits() {
        return decimaldigits;
    }

    public void setDecimaldigits(int decimaldigits) {
        this.decimaldigits = decimaldigits;
    }

    public String getNullable() {
        return nullable;
    }

    public void setNullable(String nullable) {
        this.nullable = nullable;
    }

}
