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
public class DatatableDeleteProperties {
    private @Getter @Setter String tablename;
    private @Getter @Setter ArrayList<DatatableDeleteValue> valuelist;

    public DatatableDeleteProperties() {
        valuelist = new ArrayList<>();
    }

    public DatatableDeleteProperties(String tablename, ArrayList<DatatableDeleteValue> valuelist) {
        this.tablename = tablename;
        valuelist = new ArrayList<>();
    }
}
