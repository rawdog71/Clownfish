/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.jdbc;

import java.util.ArrayList;

/**
 *
 * @author sulzbachr
 */
public class DatatableProperties {
    private String tablename;
    private String orderby;
    private String orderdir;
    private int pagination;
    private int page;
    private ArrayList<DatatableCondition> conditionlist;
    private ArrayList<String> groupbylist;
    private String groupbycount;

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

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getOrderby() {
        return orderby;
    }

    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }

    public String getOrderdir() {
        return orderdir;
    }

    public void setOrderdir(String orderdir) {
        this.orderdir = orderdir;
    }

    public int getPagination() {
        return pagination;
    }

    public void setPagination(int pagination) {
        this.pagination = pagination;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ArrayList<DatatableCondition> getConditionlist() {
        return conditionlist;
    }

    public void setConditionlist(ArrayList<DatatableCondition> conditionlist) {
        this.conditionlist = conditionlist;
    }

    public ArrayList<String> getGroupbylist() {
        return groupbylist;
    }

    public void setGroupbylist(ArrayList<String> groupbylist) {
        this.groupbylist = groupbylist;
    }

    public String getGroupbycount() {
        return groupbycount;
    }

    public void setGroupbycount(String groupbycount) {
        this.groupbycount = groupbycount;
    }
}
