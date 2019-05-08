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

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class TableField {
    private @Getter @Setter String name;
    private @Getter @Setter String type;
    private @Getter @Setter String typename;
    private @Getter @Setter boolean primaryKey;
    private @Getter @Setter int size;
    private @Getter @Setter int decimaldigits;
    private @Getter @Setter String nullable;

    public TableField() {
    }

    public TableField(String name, String type, String typename, boolean primaryKey, int size, int decimaldigits, String nullable) {
        this.name = name;
        this.type = type;
        this.typename = typename;
        this.primaryKey = primaryKey;
        this.size = size;
        this.decimaldigits = decimaldigits;
        this.nullable = nullable;
    }
}
