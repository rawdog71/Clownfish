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
public class RpyTableRead {
    private @Getter @Setter String tablname;
    private @Getter @Setter String fieldname;
    private @Getter @Setter String dtelname;
    private @Getter @Setter String checktable;
    private @Getter @Setter String keyflag;
    private @Getter @Setter int position;
    private @Getter @Setter String reftable;
    private @Getter @Setter String reffield;
    private @Getter @Setter String inclname;
    private @Getter @Setter String notnull;
    private @Getter @Setter String domname;
    private @Getter @Setter String paramid;
    private @Getter @Setter String logflag;
    private @Getter @Setter int headlen;
    private @Getter @Setter int scrlen_s;
    private @Getter @Setter int scrlen_m;
    private @Getter @Setter int scrlen_l;
    private @Getter @Setter String datatype;
    private @Getter @Setter int length;
    private @Getter @Setter int outputlen;
    private @Getter @Setter int decimals;
    private @Getter @Setter String lowercase;
    private @Getter @Setter String signflag;
    private @Getter @Setter String langflag;
    private @Getter @Setter String valuetab;
    private @Getter @Setter String convexit;
    private @Getter @Setter String ddtext;
    private @Getter @Setter String reptext;
    private @Getter @Setter String scrtext_s;
    private @Getter @Setter String scrtext_m;
    private @Getter @Setter String scrtext_l;
    private @Getter @Setter String valueexist;
    private @Getter @Setter String adminfield;
    private @Getter @Setter int intlength;
    private @Getter @Setter String inttype;

    public RpyTableRead() {
    }

    public RpyTableRead(String tablname, String fieldname, String dtelname, String checktable, String keyflag, int position, String reftable, String reffield, String inclname, String notnull, String domname, String paramid, String logflag, int headlen, int scrlen_s, int scrlen_m, int scrlen_l, String datatype, int length, int outputlen, int decimals, String lowercase, String signflag, String langflag, String valuetab, String convexit, String ddtext, String reptext, String scrtext_s, String scrtext_m, String scrtext_l, String valueexist, String adminfield, int intlength, String inttype) {
        this.tablname = tablname;
        this.fieldname = fieldname;
        this.dtelname = dtelname;
        this.checktable = checktable;
        this.keyflag = keyflag;
        this.position = position;
        this.reftable = reftable;
        this.reffield = reffield;
        this.inclname = inclname;
        this.notnull = notnull;
        this.domname = domname;
        this.paramid = paramid;
        this.logflag = logflag;
        this.headlen = headlen;
        this.scrlen_s = scrlen_s;
        this.scrlen_m = scrlen_m;
        this.scrlen_l = scrlen_l;
        this.datatype = datatype;
        this.length = length;
        this.outputlen = outputlen;
        this.decimals = decimals;
        this.lowercase = lowercase;
        this.signflag = signflag;
        this.langflag = langflag;
        this.valuetab = valuetab;
        this.convexit = convexit;
        this.ddtext = ddtext;
        this.reptext = reptext;
        this.scrtext_s = scrtext_s;
        this.scrtext_m = scrtext_m;
        this.scrtext_l = scrtext_l;
        this.valueexist = valueexist;
        this.adminfield = adminfield;
        this.intlength = intlength;
        this.inttype = inttype;
    }
}
