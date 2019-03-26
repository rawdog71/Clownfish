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
public class DatatableNewProperties {
    private @Getter @Setter String tablename;
    private @Getter @Setter ArrayList<DatatableNewValue> valuelist;

    public DatatableNewProperties() {
        valuelist = new ArrayList<>();
    }

    public DatatableNewProperties(String tablename, ArrayList<DatatableNewValue> valuelist) {
        this.tablename = tablename;
        valuelist = new ArrayList<>();
    }
}
