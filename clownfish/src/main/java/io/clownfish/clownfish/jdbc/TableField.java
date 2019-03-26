/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class TableField {
    private @Getter @Setter String name;
    private @Getter @Setter String type;
    private @Getter @Setter boolean primaryKey;
    private @Getter @Setter int size;
    private @Getter @Setter int decimaldigits;
    private @Getter @Setter String nullable;

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
}
