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
public class DatatableProperties {
    private @Getter @Setter String tablename;
    private @Getter @Setter String orderby;
    private @Getter @Setter String orderdir;
    private @Getter @Setter int pagination;
    private @Getter @Setter int page;
    private @Getter @Setter ArrayList<DatatableCondition> conditionlist;
    private @Getter @Setter ArrayList<String> groupbylist;
    private @Getter @Setter String groupbycount;

    public DatatableProperties() {
        conditionlist = new ArrayList<>();
        groupbylist = new ArrayList<>();
        groupbycount = "";
    }

    public DatatableProperties(String tablename, String orderby, String orderdir, int pagination, int page, String groupbycount) {
        this.tablename = tablename;
        this.orderby = orderby;
        this.orderdir = orderdir;
        this.pagination = pagination;
        this.page = page;
        conditionlist = new ArrayList<>();
        groupbylist = new ArrayList<>();
        this.groupbycount = groupbycount;
    }
}
