/*
 * Copyright Rainer Sulzbach
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
