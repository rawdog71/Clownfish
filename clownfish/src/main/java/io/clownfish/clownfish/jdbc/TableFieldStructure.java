/*
 * Copyright 2019 sulzbachr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    
    public TableField getTableFieldByName(String name) {
        for (TableField tf : tableFieldsList) {
            if (0 == tf.getName().compareToIgnoreCase(name)) {
                return tf;
            }
        }
        return null;
    }
}
