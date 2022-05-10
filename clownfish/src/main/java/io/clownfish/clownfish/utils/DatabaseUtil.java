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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.datamodels.ColumnData;
import io.clownfish.clownfish.datamodels.TableData;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.jdbc.DatatableCondition;
import io.clownfish.clownfish.jdbc.DatatableDeleteProperties;
import io.clownfish.clownfish.jdbc.DatatableNewProperties;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.DatatableUpdateProperties;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.TableField;
import io.clownfish.clownfish.jdbc.TableFieldStructure;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.head;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.script;
import static j2html.TagCreator.title;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope("singleton")
@Component
public class DatabaseUtil {
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired CfTemplateService cfTemplateService;
    @Autowired CfSiteService cfSiteService;
    private @Getter @Setter SiteTreeBean sitetree;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(DatabaseUtil.class);

    public DatabaseUtil() {
    }
    
    public HashMap<String, HashMap> getDbexport(List<CfSitedatasource> sitedatasourcelist, HashMap<String, DatatableProperties> datatableproperties, HashMap<String, DatatableNewProperties> datatablenewproperties, HashMap<String, DatatableDeleteProperties> datatabledeleteproperties, HashMap<String, DatatableUpdateProperties> datatableupdateproperties) {
        HashMap<String, HashMap> dbexport = new HashMap<>();
        for (CfSitedatasource sitedatasource : sitedatasourcelist) {
            CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());

            JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
            Connection con = jdbcutil.getConnection();
            if (null != con) {
                try {
                    DatabaseMetaData dmd = con.getMetaData();

                    ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});

                    HashMap<String, ArrayList> dbtables = new HashMap<>();
                    HashMap<String, Object> dbvalues = new HashMap<>();
                    while(resultSetTables.next())
                    {
                        String tablename = resultSetTables.getString("TABLE_NAME");
                        //System.out.println(tablename);
                        if (datatableproperties.get(tablename) != null) {
                            manageTableRead(con, dmd, tablename, datatableproperties, dbtables, dbvalues);
                        }
                        if (datatablenewproperties.get(tablename) != null) {
                            boolean ok = manageTableInsert(con, dmd, tablename, datatablenewproperties, dbtables, dbvalues);
                            if (ok) {
                                dbvalues.put("INSERT", "true");
                            } else {
                                dbvalues.put("INSERT", "false");
                            }
                        }
                        if (datatabledeleteproperties.get(tablename) != null) {
                            boolean ok = manageTableDelete(con, dmd, tablename, datatabledeleteproperties, dbtables, dbvalues);
                            if (ok) {
                                dbvalues.put("DELETE", "true");
                            } else {
                                dbvalues.put("DELETE", "false");
                            }
                        }
                        if (datatableupdateproperties.get(tablename) != null) {
                            boolean ok = manageTableUpdate(con, dmd, tablename, datatableupdateproperties, dbtables, dbvalues);
                            if (ok) {
                                dbvalues.put("UPDATE", "true");
                            } else {
                                dbvalues.put("UPDATE", "false");
                            }
                        }
                    }

                    resultSetTables = dmd.getTables(null, null, null, new String[]{"VIEW"});
                    while(resultSetTables.next())
                    {
                        String tablename = resultSetTables.getString("TABLE_NAME");
                        //System.out.println(tablename);
                        if (datatableproperties.get(tablename) != null) {
                            manageTableRead(con, dmd, tablename, datatableproperties, dbtables, dbvalues);
                        }
                    }

                    dbvalues.put("table", dbtables);
                    dbexport.put(cfdatasource.getDatabasename(), dbvalues);
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
            } else {
                return null;
            }
        }
        return dbexport;
    }
    
    private void manageTableRead(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, DatatableProperties> datatableproperties, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        Statement stmt = null;
        ResultSet result = null;
        try {
            long low_limit = 1;
            long high_limit = 50;
            
            //System.out.println(con.getMetaData().getDriverName());

            String default_order = "";
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, default_order);
            default_order = tfs.getDefault_order();
            
            String default_direction = "ASC";
            DatatableProperties dtp = datatableproperties.get(tablename);
            if (dtp != null) {
                if (dtp.getOrderby() != null) {
                    default_order = datatableproperties.get(tablename).getOrderby();
                }
                if (dtp.getOrderdir() != null) {
                    default_direction = datatableproperties.get(tablename).getOrderdir();
                }
                low_limit = 1L + (long)((dtp.getPage()-1) * dtp.getPagination());
                if (con.getMetaData().getDriverName().contains("MS SQL")) {
                    high_limit = (long) dtp.getPage() * (long) dtp.getPagination();
                }
                if (con.getMetaData().getDriverName().contains("MySQL")) {
                    high_limit = dtp.getPagination();
                }
            }
            
            StringBuilder sql_count = new StringBuilder();
            StringBuilder sql_outer = new StringBuilder();
            StringBuilder sql_inner = new StringBuilder();
            
            if (con.getMetaData().getDriverName().contains("MS SQL")) {
                sql_outer.append("SELECT ");
                sql_inner.append("SELECT ");
                sql_count.append("SELECT COUNT(*) AS count FROM ");
                
                if ((dtp != null) && (!dtp.getGroupbylist().isEmpty())) {
                    sql_outer.append("count(*) AS groupbycount, ");
                    tfs.getTableFieldsList().stream().filter((tf) -> (dtp.getGroupbylist().contains(tf.getName()))).map((tf) -> {
                        sql_outer.append(tf.getName());
                        return tf;
                    }).map((tf) -> {
                        sql_outer.append(", ");
                        sql_inner.append(tf.getName());
                        return tf;
                    }).forEach((_item) -> {
                        sql_inner.append(", ");
                    });
                } else {
                    tfs.getTableFieldsList().stream().map((tf) -> {
                        sql_outer.append(tf.getName());
                        return tf;
                    }).map((tf) -> {
                        sql_outer.append(", ");
                        sql_inner.append(tf.getName());
                        return tf;
                    }).forEach((_item) -> {
                        sql_inner.append(", ");
                    });
                }
                sql_count.append(tablename);
                sql_outer.delete(sql_outer.length()-2, sql_outer.length());
                sql_outer.append(" FROM (");
                sql_inner.append("ROW_NUMBER() OVER (ORDER BY ");
                sql_inner.append(default_order);
                sql_inner.append(" ");
                sql_inner.append(default_direction);
                sql_inner.append(" ) AS rownumber FROM ");
                sql_inner.append(tablename);
                StringBuilder sql_condition = null;
                if (dtp != null) {
                    sql_condition = buildCondition(dtp.getConditionlist(), tfs.getTableFieldsList());
                }
                sql_inner.append(sql_condition);
                sql_count.append(sql_condition);
                StringBuilder sql_groupby = null;
                if (dtp != null) {
                    sql_groupby = buildGroupBy(dtp.getGroupbylist());
                }
                sql_inner.append(sql_groupby);
                if (dtp != null) {
                    if (!dtp.getGroupbycount().isEmpty()) {
                        sql_inner.append(" ,");
                        sql_inner.append(dtp.getGroupbycount());
                    }
                }
                sql_count.append(sql_groupby);
                
                sql_outer.append(sql_inner);
                sql_outer.append(") orderedselection WHERE rownumber between ");
                sql_outer.append(low_limit);
                sql_outer.append(" AND ");
                sql_outer.append(high_limit);
                sql_outer.append(sql_groupby);
            } 
            if (con.getMetaData().getDriverName().contains("MySQL")) {
                sql_outer.append("SELECT ");
                sql_count.append("SELECT COUNT(*) AS count FROM ");
                
                if ((dtp != null) && (!dtp.getGroupbylist().isEmpty())) {
                    sql_outer.append("count(*) AS groupbycount, ");
                    tfs.getTableFieldsList().stream().filter((tf) -> (dtp.getGroupbylist().contains(tf.getName()))).map((tf) -> {
                        sql_outer.append(tf.getName());
                        return tf;
                    }).forEach((_item) -> {
                        sql_outer.append(", ");
                    });
                } else {
                    tfs.getTableFieldsList().stream().map((tf) -> {
                        sql_outer.append(tf.getName());
                        return tf;
                    }).forEach((_item) -> {
                        sql_outer.append(", ");
                    });
                }
                
                sql_count.append(tablename);
                sql_outer.delete(sql_outer.length()-2, sql_outer.length());
                sql_outer.append(" FROM ");
                sql_outer.append(tablename);
                StringBuilder sql_condition = null;
                if (dtp != null) {
                    sql_condition = buildCondition(dtp.getConditionlist(), tfs.getTableFieldsList());
                }
                sql_outer.append(sql_condition);
                sql_count.append(sql_condition);
                
                StringBuilder sql_groupby = null;
                if (dtp != null) {
                    sql_groupby = buildGroupBy(dtp.getGroupbylist());
                }
                sql_outer.append(sql_groupby);
                if (dtp != null) {
                    if (!dtp.getGroupbycount().isEmpty()) {
                        sql_outer.append(" ,");
                        sql_outer.append(dtp.getGroupbycount());
                    }
                }
                sql_count.append(sql_groupby);
                
                sql_outer.append(" LIMIT ");
                sql_outer.append(low_limit-1);
                sql_outer.append(", ");
                sql_outer.append(high_limit);
                sql_outer.append(sql_groupby);
            }

            if (dtp != null) {
                if (!dtp.getGroupbycount().isEmpty()) {
                    TableField groupbycountfield = new TableField();
                    groupbycountfield.setName("groupbycount");
                    groupbycountfield.setType("INT");
                    tfs.getTableFieldsList().add(groupbycountfield);
                }
            }
            
            stmt = con.createStatement();
            result = stmt.executeQuery(sql_outer.toString());
            ArrayList<HashMap> tablevalues = new ArrayList<>();
            while (result.next()) {
                HashMap<String, String> dbexportvalues = new HashMap<>();
                for (TableField tf : tfs.getTableFieldsList()) {
                    try {
                        String value = result.getString(tf.getName());
                        dbexportvalues.put(tf.getName(), value);
                    } catch (java.sql.SQLException ex) {
                        
                    }
                }
                tablevalues.add(dbexportvalues);
            }
            dbtables.put(tablename, tablevalues);
            try {
                result.close();
            } catch (SQLException ex) {
                LOGGER.error(ex.getMessage());
            }
            result = stmt.executeQuery(sql_count.toString());
            HashMap<String, String> dbexportvalues = new HashMap<>();
            while (result.next()) {
                String value = result.getString("count");
                dbexportvalues.put("count", value);
            }
            dbvalues.put(tablename, dbexportvalues);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
            if (null != result) {
                try {
                    result.close();
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
    }
    
    private boolean manageTableInsert(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, DatatableNewProperties> datatablenewproperties, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        Statement stmt = null;
        try {
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, "");
            DatatableNewProperties dtnp = datatablenewproperties.get(tablename);
            
            StringBuilder sql_insert_fields = new StringBuilder();
            StringBuilder sql_insert_values = new StringBuilder();
            dtnp.getValuelist().stream().map((dtnv) -> {
                sql_insert_fields.append(dtnv.getField());
                return dtnv;
            }).forEach((dtnv) -> {
                sql_insert_fields.append(", ");
                String fieldType = getFieldType(tfs.getTableFieldsList(), dtnv.getField());
                if (null != fieldType) {
                    if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                        sql_insert_values.append("'");
                    }
                    sql_insert_values.append(dtnv.getValue());
                    if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                        sql_insert_values.append("'");
                    }
                    sql_insert_values.append(", ");
                }
            });
            sql_insert_fields.delete(sql_insert_fields.length()-2, sql_insert_fields.length());
            sql_insert_values.delete(sql_insert_values.length()-2, sql_insert_values.length());
            
            StringBuilder sql_insert = new StringBuilder();
            sql_insert.append("INSERT INTO ");
            sql_insert.append(tablename);
            sql_insert.append(" (");
            sql_insert.append(sql_insert_fields);
            sql_insert.append(") VALUES (");
            sql_insert.append(sql_insert_values);
            sql_insert.append(")");
            
            stmt = con.createStatement();
            int count = stmt.executeUpdate(sql_insert.toString());
            boolean ok = false;
            if (count > 0 ) {
                ok = true;
            }
            return ok;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
    }
    
    private boolean manageTableDelete(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, DatatableDeleteProperties> datatabledeleteproperties, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        Statement stmt = null;
        try {
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, "");
            DatatableDeleteProperties dtdp = datatabledeleteproperties.get(tablename);
            
            StringBuilder sql_condition = new StringBuilder();
            if (dtdp != null) {
                sql_condition.append(" WHERE ");
                dtdp.getValuelist().stream().map((dtdv) -> {
                    sql_condition.append("(");
                    sql_condition.append(dtdv.getField());
                    return dtdv;
                }).forEach((dtdv) -> {
                    String fieldType = getFieldType(tfs.getTableFieldsList(), dtdv.getField());
                    sql_condition.append(" = ");
                    if (null != fieldType) {
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        sql_condition.append(dtdv.getValue());
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        sql_condition.append(") AND ");
                    }
                });
                sql_condition.delete(sql_condition.length()-4, sql_condition.length());
            }
            
            StringBuilder sql_delete = new StringBuilder();
            sql_delete.append("DELETE FROM ");
            sql_delete.append(tablename);
            sql_delete.append(sql_condition);
            
            stmt = con.createStatement();
            int count = stmt.executeUpdate(sql_delete.toString());
            boolean ok = false;
            if (count > 0 ) {
                ok = true;
            }
            
            return ok;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
    }
    
    private boolean manageTableUpdate(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, DatatableUpdateProperties> datatableproperties, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        Statement stmt = null;
        try {
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, "");
            DatatableUpdateProperties dtup = datatableproperties.get(tablename);
            
            StringBuilder sql_update_values = new StringBuilder();
            dtup.getValuelist().stream().map((dtuv) -> {
                sql_update_values.append(dtuv.getField());
                return dtuv;
            }).forEach((dtuv) -> {
                sql_update_values.append(" = ");
                String fieldType = getFieldType(tfs.getTableFieldsList(), dtuv.getField());
                if (null != fieldType) {
                    if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                        sql_update_values.append("'");
                    }
                    sql_update_values.append(dtuv.getValue());
                    if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                        sql_update_values.append("'");
                    }
                    sql_update_values.append(", ");
                }
            });
            sql_update_values.delete(sql_update_values.length()-2, sql_update_values.length());
            
            StringBuilder sql_update = new StringBuilder();
            sql_update.append("UPDATE ");
            sql_update.append(tablename);
            sql_update.append(" SET ");
            sql_update.append(sql_update_values);
            StringBuilder sql_condition = new StringBuilder();
            if (null != dtup) {
                sql_condition = buildCondition(dtup.getConditionlist(), tfs.getTableFieldsList());
            }
            sql_update.append(sql_condition);
            
            stmt = con.createStatement();
            int count = stmt.executeUpdate(sql_update.toString());
            boolean ok = false;
            if (count > 0 ) {
                ok = true;
            }
            return ok;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
    }
    
    private TableFieldStructure getTableFieldsList(DatabaseMetaData dmd, String tablename, String default_order) {
        try {
            TableFieldStructure tfs = new TableFieldStructure();
            List<String> pkList = new ArrayList<>();
            ResultSet resultSetPK = dmd.getPrimaryKeys(null, null, tablename);
            int counter = 0;
            while(resultSetPK.next())
            {
                pkList.add(resultSetPK.getString("COLUMN_NAME"));
                if (counter == 0) {
                    default_order = resultSetPK.getString("COLUMN_NAME");
                }
                counter++;
            }
            
            ArrayList<TableField> tableFieldsList = new ArrayList<>();
            ResultSet columns = dmd.getColumns(null, null, tablename, null);
            while(columns.next())
            {
                String columnName = columns.getString("COLUMN_NAME");
                String datatype = columns.getString("DATA_TYPE");
                String colomuntypename = columns.getString("TYPE_NAME");
                String columnsize = columns.getString("COLUMN_SIZE");
                String decimaldigits = columns.getString("DECIMAL_DIGITS");
                if (decimaldigits == null) {
                    decimaldigits = "0";
                }
                String isNullable = columns.getString("IS_NULLABLE");
                //String is_autoIncrment = columns.getString("IS_AUTOINCREMENT");
                String is_autoIncrment = "";
                
                if ((default_order.isEmpty()) && (counter == 0)) {
                    default_order = columnName;
                }
                TableField tf;
                switch (datatype) {
                    case "1":      // varchar -> String
                    case "12":
                    case "2005":    
                        tf = new TableField(columnName, "STRING", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "2":       // int
                    case "4":
                    case "5":    
                        tf = new TableField(columnName, "INT", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "7":       // real
                        tf = new TableField(columnName, "REAL", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;            
                    case "8":       // float
                        tf = new TableField(columnName, "FLOAT", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;    
                    case "-5":      // long
                        tf = new TableField(columnName, "LONG", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "-6":      // bit
                    case "-7":      // bit
                        tf = new TableField(columnName, "BOOLEAN", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;    
                    case "93":      // Date
                        tf = new TableField(columnName, "DATE", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                }
            }
            tfs.setDefault_order(default_order);
            tfs.setTableFieldsList(tableFieldsList);
            return tfs;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    private StringBuilder buildGroupBy(ArrayList<String> groupbylist) {
        StringBuilder sql_groupby = new StringBuilder();
        if (!groupbylist.isEmpty()) {
            sql_groupby.append(" GROUP BY ");
            groupbylist.stream().map((groupby) -> {
                sql_groupby.append(groupby);
                return groupby;
            }).forEach((_item) -> {
                sql_groupby.append(", ");
            });
            sql_groupby.delete(sql_groupby.length()-2, sql_groupby.length());
        }
        return sql_groupby;
    }
    
    private StringBuilder buildCondition(ArrayList<DatatableCondition> conditionlist, ArrayList<TableField> tableFieldsList) {
        StringBuilder sql_condition = new StringBuilder();
        if (!conditionlist.isEmpty()) {
            sql_condition.append(" WHERE ");
            for (DatatableCondition dtc : conditionlist) {
                sql_condition.append("(");
                sql_condition.append(dtc.getField());
                String fieldType = getFieldType(tableFieldsList, dtc.getField());
                if (null != fieldType) {
                    switch (dtc.getOperand()) {
                        case "eq" : 
                            sql_condition.append(" = ");
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            sql_condition.append(dtc.getValue());
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            break;
                        case "lk" : 
                            sql_condition.append(" LIKE ");
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0) || (fieldType.compareToIgnoreCase("int") == 0) || (fieldType.compareToIgnoreCase("float") == 0)) {
                                sql_condition.append("'%");
                            }
                            sql_condition.append(dtc.getValue());
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0) || (fieldType.compareToIgnoreCase("int") == 0) || (fieldType.compareToIgnoreCase("float") == 0)) {
                                sql_condition.append("%'");
                            }
                            break;
                        case "gt" : 
                            sql_condition.append(" > ");
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            sql_condition.append(dtc.getValue());
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            break;
                        case "ge" : 
                            sql_condition.append(" >= ");
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            sql_condition.append(dtc.getValue());
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            break;    
                        case "lt" : 
                            sql_condition.append(" < ");
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            sql_condition.append(dtc.getValue());
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            break;
                        case "le" : 
                            sql_condition.append(" <= ");
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            sql_condition.append(dtc.getValue());
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            break;    
                        case "neq" : 
                            sql_condition.append(" <> ");
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            sql_condition.append(dtc.getValue());
                            if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                                sql_condition.append("'");
                            }
                            break;
                    }
                    sql_condition.append(") AND ");
                }
            }
            sql_condition.delete(sql_condition.length()-4, sql_condition.length());
        }
        return sql_condition;
    }
    
    private String getFieldType(ArrayList<TableField> tableFieldsList, String fieldname) {
        for (TableField tf : tableFieldsList) {
            if (tf.getName().compareToIgnoreCase(fieldname) == 0) {
                return tf.getType();
            }
        }
        return null;
    }
    
    public void generateHTMLForm(CfDatasource datasource, TableData tabledata) {
        StringBuilder html = new StringBuilder();
        CfTemplate template = new CfTemplate();
        CfSite site = new CfSite();
        CfJavascript js = new CfJavascript();
        
        html.append("<!DOCTYPE html>").append("\n");
        html.append("<html lang=\"en\" ng-app=\"webformApp\">").append("\n\n");
        html.append(head(
                meta().attr("charset", "UTF-8"),
                meta().attr("http-equiv", "X-UA-Compatible").attr("content", "IE=edge"),
                meta().attr("name", "viewport").attr("content", "width=device-width, initial-scale=1.0"),
                script().withSrc("resources/js/angularjs_1_8_2.js"),
                link().withHref("resources/css/bootstrap5.css").withRel("stylesheet"),
                script().withSrc("resources/js/bootstrap5.js"),
                script().withSrc("resources/js/User_WebformDB.js"),
                script().withSrc("resources/js/axios.js"),
                title("Webform")).renderFormatted()).append("\n");

        html.append("<body ng-controller=\"WebformCtrl\" ng-init=\"init('").append(datasource.getName()).append("', '").append(tabledata.getName()).append("', 1, 50, ").append(makeFieldlist(tabledata.getColumns())).append(", ").append(makePKlist(tabledata.getColumns())).append(")\">").append("\n");
        html.append("\t").append(h1(tabledata.getName()).withId("classname").withClass("text-center mt-3")).append("\n");
        
        html.append("\t").append(("<div class=\"mx-5\">")).append("\n");
        html.append("\t\t").append(("<div class=\"d-flex flex-row-reverse\">")).append("\n");
        html.append("\t\t\t").append(("<button class=\"btn btn-primary\" data-bs-toggle=\"modal\" data-bs-target=\"#exampleModal\"><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-plus-lg\" viewBox=\"0 0 16 16\">\n" +
"                <path fill-rule=\"evenodd\" d=\"M8 2a.5.5 0 0 1 .5.5v5h5a.5.5 0 0 1 0 1h-5v5a.5.5 0 0 1-1 0v-5h-5a.5.5 0 0 1 0-1h5v-5A.5.5 0 0 1 8 2Z\"/>\n" +
"              </svg> Hinzufügen</button>")).append("\n");
        html.append("\t\t").append(("</div>")).append("\n");
        
        html.append("\t").append(("<table class=\"table\">")).append("\n");
        html.append("\t\t").append(("<thead>")).append("\n");
        html.append("\t\t\t").append(("<tr>")).append("\n");
        //html.append("\t\t\t\t").append("<th scope=\"col\">#</th>\n");
        
        for (ColumnData attr : tabledata.getColumns()) {
            //if (0 == attr.getAutoinc().compareToIgnoreCase("yes")) {
            //    continue;
            //}
            html.append("\t").append("<th scope=\"col\">").append(attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1)).append("</th>\n");
        }
        html.append("\t\t\t\t").append("<th class=\"text-end\" scope=\"col\">Aktionen</th>\n");
        html.append("\t\t\t").append(("</tr>")).append("\n");
        html.append("\t\t").append(("</thead>")).append("\n");
        
        html.append("\t\t").append(("<tbody>")).append("\n");
        html.append("\t\t\t").append(("<tr ng-repeat=\"info in contentList track by $index\">")).append("\n");
        //html.append("\t\t\t\t").append("<th scope=\"row\">{{$index}}</th>\n");
        
        for (ColumnData attr : tabledata.getColumns()) {
            //if (0 == attr.getAutoinc().compareToIgnoreCase("yes")) {
            //    continue;
            //}
            html.append("\t\t\t\t").append("<td> {{info[\"").append(attr.getName()).append("\"]}}").append("</td>\n");
        }
        
        html.append("\t\t\t\t").append(("<td class=\"text-end\">")).append("\n");
        html.append("\t\t\t\t\t").
                append(("<button class=\"btn btn-primary\" ng-click=\"edit($index)\" data-bs-toggle=\"modal\" data-bs-target=\"#editModal\">\n" +
"                            <div class=\"d-flex align-items-center\">\n" +
"                                <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-pencil-square\" viewBox=\"0 0 16 16\">\n" +
"                                    <path d=\"M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z\"/>\n" +
"                                    <path fill-rule=\"evenodd\" d=\"M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z\"/>\n" +
"                                </svg>\n" +
"                                <p class=\"m-0 ms-1\">Editieren</p>\n" +
"                            </div>\n" +
"                        </button>")).append("\n");
        
        html.append("\t\t\t\t\t").
                append(("<button class=\"btn btn-danger\" ng-click=\"deleteI($index)\">\n" +
"                            <div class=\"d-flex align-items-center\">\n" +
"                                <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-trash\" viewBox=\"0 0 16 16\">\n" +
"                                    <path d=\"M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z\"/>\n" +
"                                    <path fill-rule=\"evenodd\" d=\"M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z\"/>\n" +
"                                </svg>\n" +
"                                <p class=\"m-0 ms-1\">Löschen</p>\n" +
"                            </div>\n" +
"                        </button>")).append("\n");
        html.append("\t\t\t\t").append(("</td>")).append("\n");
        html.append("\t\t\t").append(("</tr>")).append("\n");
        html.append("\t\t").append(("</tbody>")).append("\n");
        html.append("\t").append(("</table>")).append("\n");
        html.append("\t").append(("</div>")).append("\n");
        
        html.append("\t").append(("<div class=\"modal fade\" id=\"exampleModal\" tabindex=\"-1\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">")).append("\n");
        html.append("\t\t").append(("<div class=\"modal-dialog\">")).append("\n");
        html.append("\t\t\t").append(("<div class=\"modal-content\">")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-header\">")).append("\n");
        html.append("\t\t\t\t\t").append(h5(tabledata.getName()).withId("exampleModalLabel").withClass("modal-title")).append("\n");
        html.append("\t\t\t\t\t").append(("<button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        
        html.append("\t\t\t\t").append(("<div class=\"modal-body\">")).append("\n");
        html.append("\t\t\t\t\t").append(("<form id=\"forms\" class=\"row g-3\">")).append("\n");
        
        for (ColumnData attr : tabledata.getColumns()) {
            if (0 == attr.getAutoinc().compareToIgnoreCase("yes")) {
                continue;
            }
            switch (attr.getType()) {
                case -6:
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(attr.getName()).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("checkbox").withId(attr.getName())).append("\n");
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case -1:
                case 1:
                case 12:
                case 2005:
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(input().withType("text").withId(attr.getName()).withClass("form-control")).append("\n");
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case 2:
                case 4:
                case 5:
                case 7:
                case 8:
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(input().withType("number").withId(attr.getName()).withClass("form-control"));
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case 93:
                    html.append("\t\t\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t\t\t\t").append(input().withType("date").withId(attr.getName()).withValue("{{getTodaysDate()}}").withClass("form-control"));
                    html.append("\t\t\t\t\t\t").append(("</div>")).append("\n");
                    break;
            }
        }
        
        html.append("\t\t\t\t\t").append(("</form>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        
        html.append("\t\t\t\t").append(("<div class=\"modal-footer\">")).append("\n");
        html.append("\t\t\t\t").append(("<button class=\"btn btn-primary w-100\" data-bs-dismiss=\"modal\" ng-click=\"add()\">Hinzufügen</button>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        
        
        html.append("\t\t\t\t").append(("<div class=\"modal fade\" id=\"editModal\" tabindex=\"-1\" aria-labelledby=\"editModalLabel\" aria-hidden=\"true\">")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-dialog\">")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-content\" ng-repeat=\"info in recordEdit\">")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-header\">")).append("\n");
        html.append("\t\t\t\t\t").append(h5(tabledata.getName()).withId("exampleModalLabel").withClass("modal-title")).append("\n");
        html.append("\t\t\t\t").append(("<button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-body\">")).append("\n");
        html.append("\t\t\t\t").append(("<form id=\"forms2\" class=\"row g-3\">")).append("\n");
        
        for (ColumnData attr : tabledata.getColumns()) {
            if (0 == attr.getAutoinc().compareToIgnoreCase("yes")) {
                continue;
            }
            
            switch (attr.getType()) {
                case -6:
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(attr.getName()).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t\t\t").append(("<input type=\"checkbox\" id=\"" + attr.getName() + "\" ng-checked=\"{{info['" + attr.getName() + "']}}\">")).append("\n");
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case -1:
                case 1:
                case 12:
                case 2005:
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("text").withId(attr.getName()).withClass("form-control").withValue("{{info['" + attr.getName() + "']}}")).append("\n");
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case 2:
                case 4:
                case 5:
                case 7:
                case 8:
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("number").withId(attr.getName()).withClass("form-control").withValue("{{info['" + attr.getName() + "']}}"));
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
                case 93:
                    html.append("\t\t\t\t").append(("<div class=\"col-md-6\">")).append("\n");
                    html.append("\t\t").append(label(StringUtils.capitalise(attr.getName())).withFor(attr.getName()).withClass("form-label")).append("\n");
                    html.append("\t\t").append(input().withType("date").withId(attr.getName()).withClass("form-control").withValue("{{formatDate(info['" + attr.getName() + "'])}}"));
                    html.append("\t\t\t\t").append(("</div>")).append("\n");
                    break;
            }
        }
        html.append("\t\t\t\t").append(("</form>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("<div class=\"modal-footer\">")).append("\n");
        html.append("\t\t\t\t").append(("<button class=\"btn btn-primary w-100\" data-bs-dismiss=\"modal\" ng-click=\"update(info['id'])\">Editieren</button>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</div>")).append("\n");
        html.append("\t\t\t\t").append(("</body>")).append("\n");
        html.append("\t\t\t\t").append(("</html>")).append("\n");
        
        template.setName(tabledata.getName() + "_Webform");
        try {
            CfTemplate dummytemplate = cfTemplateService.findByName(template.getName());

            if (null == dummytemplate) {
                template.setScriptlanguage(2);
                template.setCheckedoutby(BigInteger.ZERO);
                template.setContent(html.toString());
                cfTemplateService.create(template);
            } else {
                dummytemplate.setContent(html.toString());
                cfTemplateService.edit(dummytemplate);
            }
        } catch (Exception ex) {
            template.setScriptlanguage(2);
            template.setCheckedoutby(BigInteger.ZERO);
            template.setContent(html.toString());
            cfTemplateService.create(template);
        }

        site.setName(tabledata.getName() + "_Webform");
        try {
            CfSite dummysite = cfSiteService.findByName(site.getName());
        } catch (Exception ex) {
            site.setCharacterencoding("UTF-8");
            site.setHitcounter(BigInteger.ZERO);
            site.setTitle("");
            site.setContenttype("text/html");
            site.setSearchrelevant(false);
            site.setHtmlcompression(0);
            site.setGzip(0);
            site.setLocale("");
            site.setDescription("");
            site.setAliaspath(site.getName());
            site.setParentref(BigInteger.ZERO);
            site.setTemplateref(BigInteger.valueOf(template.getId()));
            cfSiteService.create(site);
        }
        sitetree.loadTree();
    }

    private String makeFieldlist(ArrayList<ColumnData> columns) {
        StringBuilder fieldlist = new StringBuilder();
        fieldlist.append("{");
        for (ColumnData attr : columns) {
            fieldlist.append("'");
            fieldlist.append(attr.getName());
            fieldlist.append("' : '', ");
        }
        fieldlist.delete(fieldlist.length()-2, fieldlist.length());
        fieldlist.append("}");
        return fieldlist.toString();
    }

    private String makePKlist(ArrayList<ColumnData> columns) {
        StringBuilder fieldlist = new StringBuilder();
        fieldlist.append("{");
        for (ColumnData attr : columns) {
            if (attr.isPrimarykey()) {
                fieldlist.append("'");
                fieldlist.append(attr.getName());
                fieldlist.append("' : '', ");
            }
        }
        fieldlist.delete(fieldlist.length()-2, fieldlist.length());
        fieldlist.append("}");
        return fieldlist.toString();
    }
}
