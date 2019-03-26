/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class DatatableUpdateProperties {
    private @Getter @Setter String tablename;
    private @Getter @Setter ArrayList<DatatableNewValue> valuelist;
    private @Getter @Setter ArrayList<DatatableCondition> conditionlist;

    public DatatableUpdateProperties() {
        valuelist = new ArrayList<>();
        conditionlist = new ArrayList<>();
    }

    public DatatableUpdateProperties(String tablename) {
        this.tablename = tablename;
        valuelist = new ArrayList<>();
        conditionlist = new ArrayList<>();
    }
}
