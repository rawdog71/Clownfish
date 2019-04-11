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
package io.clownfish.clownfish.sap.models;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class RfcFunctionParam {
    private @Getter @Setter String paramclass;
    private @Getter @Setter String parameter;
    private @Getter @Setter String tabname;
    private @Getter @Setter String fieldname;
    private @Getter @Setter String exid;
    private @Getter @Setter int position;
    private @Getter @Setter int offset;
    private @Getter @Setter int intlength;
    private @Getter @Setter int decimals;
    private @Getter @Setter String defaultvalue;
    private @Getter @Setter String paramtext;
    private @Getter @Setter String optional;

    public RfcFunctionParam() {
    }

    public RfcFunctionParam(String paramclass, String parameter, String tabname, String fieldname, String exid, int position, int offset, int intlength, int decimals, String defaultvalue, String paramtext, String optional) {
        this.paramclass = paramclass;
        this.parameter = parameter;
        this.tabname = tabname;
        this.fieldname = fieldname;
        this.exid = exid;
        this.position = position;
        this.offset = offset;
        this.intlength = intlength;
        this.decimals = decimals;
        this.defaultvalue = defaultvalue;
        this.paramtext = paramtext;
        this.optional = optional;
    }
}
