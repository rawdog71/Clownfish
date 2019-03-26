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
public class TableFieldStructure {
    private @Getter @Setter ArrayList<TableField> tableFieldsList;
    private @Getter @Setter String default_order;

    public TableFieldStructure() {
    }

    public TableFieldStructure(ArrayList<TableField> tableFieldsList, String default_order) {
        this.tableFieldsList = tableFieldsList;
        this.default_order = default_order;
    }
}
