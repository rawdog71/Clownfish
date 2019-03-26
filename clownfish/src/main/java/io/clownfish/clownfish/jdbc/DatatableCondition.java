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
public class DatatableCondition {
    private @Getter @Setter String field;
    private @Getter @Setter String operand;
    private @Getter @Setter String value;

    public DatatableCondition() {
    }

    public DatatableCondition(String field, String operand, String value) {
        this.field = field;
        this.operand = operand;
        this.value = value;
    }
}
