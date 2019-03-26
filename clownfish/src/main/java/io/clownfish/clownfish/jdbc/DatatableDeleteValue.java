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
public class DatatableDeleteValue {
    private @Getter @Setter String field;
    private @Getter @Setter String value;

    public DatatableDeleteValue() {
    }

    public DatatableDeleteValue(String field, String value) {
        this.field = field;
        this.value = value;
    }
}
