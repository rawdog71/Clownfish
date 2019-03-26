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
public class DatatableNewValue {
    private @Getter @Setter String field;
    private @Getter @Setter String value;

    public DatatableNewValue() {
    }

    public DatatableNewValue(String field, String value) {
        this.field = field;
        this.value = value;
    }
}
