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

import io.clownfish.clownfish.datamodels.RestDatabaseInsert;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sulzbachr
 */
public class JDBCUtil {
    private final String className;
    private final String url;
    private final String user;
    private final String password;
    private Connection connection;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(JDBCUtil.class);
    
    public JDBCUtil(String className, String url, String user, String password) {
        this.className = className;
        this.url = url;
        this.user = user;
        this.password = password;
        this.connection = null;
    }
    
    public Connection getConnection() {
        //Load the driver class
        try {
            Class.forName(className);
        } catch (ClassNotFoundException ex) {
            LOGGER.error("JDBC Class not found: " + ex.getMessage());
            return null;
        }
        //get the connection
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            LOGGER.error("FEHLER1");
            LOGGER.error(ex.getMessage());
            return null;
        } catch (Exception ex) {
            LOGGER.error("FEHLER2");
            LOGGER.error(ex.getMessage());
            return null;
        }
        return connection;
    }

    public DatabaseMetaData getMetadata() {
        try {
            DatabaseMetaData dmd = connection.getMetaData();
            return dmd;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    public long manageTableRead(Connection con, DatabaseMetaData dmd, String tablename, DatatableProperties dtp, HashMap<String, String[]> attributmap, HashMap<String, String> ordermap, ArrayList<HashMap> tablevalues) {
        Statement stmt = null;
        ResultSet result = null;
        long count = -1;
        try {
            long low_limit = 1;
            long high_limit = 50;
            
            String default_order = "";
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, default_order, null);
            default_order = tfs.getDefault_order();
            if (0 == dtp.getPagination()) {
                dtp.setPagination(high_limit);
            }
            if (0 == dtp.getPage()) {
                dtp.setPage(1);
            }
            
            String default_direction = "ASC";
            if (dtp != null) {
                if (dtp.getOrderby() != null) {
                    default_order = dtp.getOrderby();
                }
                if (dtp.getOrderdir() != null) {
                    default_direction = dtp.getOrderdir();
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
            
            int sqlmode = 0;
            if (con.getMetaData().getDriverName().contains("MS SQL")) {
                sqlmode = 1;
                sql_outer.append("SELECT TOP ");
                sql_outer.append(high_limit);
                sql_outer.append(" ");
                sql_inner.append("SELECT ");
                sql_count.append("SELECT COUNT(*) AS count FROM ");
                
                if ((dtp != null) && (!dtp.getGroupbylist().isEmpty())) {
                    sql_outer.append("count(*) AS groupbycount, ");
                    tfs.getTableFieldsList().stream().filter((tf) -> (dtp.getGroupbylist().contains(tf.getName()))).map((tf) -> {
                        sql_outer.append("[").append(tf.getName()).append("]");
                        return tf;
                    }).map((tf) -> {
                        sql_outer.append(", ");
                        sql_inner.append("[").append(tf.getName()).append("]");
                        return tf;
                    }).forEach((_item) -> {
                        sql_inner.append(", ");
                    });
                } else {
                    tfs.getTableFieldsList().stream().map((tf) -> {
                        sql_outer.append("[").append(tf.getName()).append("]");
                        return tf;
                    }).map((tf) -> {
                        sql_outer.append(", ");
                        sql_inner.append("[").append(tf.getName()).append("]");
                        return tf;
                    }).forEach((_item) -> {
                        sql_inner.append(", ");
                    });
                }
                sql_count.append(tablename);
                sql_outer.delete(sql_outer.length()-2, sql_outer.length());
                sql_outer.append(" FROM (");
                sql_inner.append(makeOrderBy(ordermap, default_order, default_direction, sqlmode, true));
                sql_inner.append(tablename);
                StringBuilder sql_condition = null;

                if (null != attributmap) {
                    sql_condition = buildCondition(attributmap, tfs.getTableFieldsList(), sqlmode);
                }
                if (null != sql_condition) {
                    sql_inner.append(sql_condition);
                    sql_count.append(sql_condition);
                }
                StringBuilder sql_groupby = null;
                /*
                if (dtp != null) {
                    sql_groupby = buildGroupBy(dtp.getGroupbylist());
                }
                */
                if (null != sql_groupby) {
                    sql_inner.append(sql_groupby);
                }
                if (dtp != null) {
                    if (!dtp.getGroupbycount().isEmpty()) {
                        sql_inner.append(" ,");
                        sql_inner.append(dtp.getGroupbycount());
                    }
                }
                if (null != sql_groupby) {
                    sql_count.append(sql_groupby);
                }
                
                sql_outer.append(sql_inner);
                sql_outer.append(") dummy WHERE rownumber >= ");
                sql_outer.append(low_limit);
                if (null != sql_groupby) {
                    sql_outer.append(sql_groupby);
                }
                sql_outer.append(makeOrderBy(ordermap, default_order, default_direction, sqlmode, false));
            } 
            if (con.getMetaData().getDriverName().contains("MySQL")) {
                sqlmode = 0;
                sql_outer.append("SELECT ");
                sql_count.append("SELECT COUNT(*) AS count FROM ");
                
                if ((dtp != null) && (!dtp.getGroupbylist().isEmpty())) {
                    sql_outer.append("count(*) AS groupbycount, ");
                    tfs.getTableFieldsList().stream().filter((tf) -> (dtp.getGroupbylist().contains(tf.getName()))).map((tf) -> {
                        sql_outer.append("`").append(tf.getName()).append("`");
                        return tf;
                    }).forEach((_item) -> {
                        sql_outer.append(", ");
                    });
                } else {
                    tfs.getTableFieldsList().stream().map((tf) -> {
                        sql_outer.append("`").append(tf.getName()).append("`");
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
                if (null != attributmap) {
                    sql_condition = buildCondition(attributmap, tfs.getTableFieldsList(), sqlmode);
                }
                if (null != sql_condition) {
                    sql_outer.append(sql_condition);
                    sql_count.append(sql_condition);
                }
                
                sql_outer.append(makeOrderBy(ordermap, default_order, default_direction, sqlmode, false));
                StringBuilder sql_groupby = null;
                /*
                if (dtp != null) {
                    sql_groupby = buildGroupBy(dtp.getGroupbylist());
                }
                */
                if (null != sql_groupby) {
                    sql_outer.append(sql_groupby);
                }
                if (dtp != null) {
                    if (!dtp.getGroupbycount().isEmpty()) {
                        sql_outer.append(" ,");
                        sql_outer.append(dtp.getGroupbycount());
                    }
                }
                if (null != sql_groupby) {
                    sql_count.append(sql_groupby);
                }
                
                sql_outer.append(" LIMIT ");
                sql_outer.append(low_limit-1);
                sql_outer.append(", ");
                sql_outer.append(high_limit);
                if (null != sql_groupby) {
                    sql_outer.append(sql_groupby);
                }
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
            while (result.next()) {
                HashMap<String, String> dbexportvalues = new HashMap<>();
                for (TableField tf : tfs.getTableFieldsList()) {
                    try {
                        String value = result.getString(tf.getName());
                        dbexportvalues.put(tf.getName(), value);
                    } catch (java.sql.SQLException ex) {
                        LOGGER.error(ex.getMessage());
                        LOGGER.error("Statement: " + stmt.toString());
                    }
                }
                tablevalues.add(dbexportvalues);
            }
            try {
                result.close();
            } catch (SQLException ex) {
                LOGGER.error(ex.getMessage());
            }
            result = stmt.executeQuery(sql_count.toString());
            
            while (result.next()) {
                String value = result.getString("count");
                count = Long.parseLong(value);
            }
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
        return count;
    }
    
    public RestDatabaseInsert manageTableInsert(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, String> attributmap) {
        Statement stmt = null;
        RestDatabaseInsert rdbi = new RestDatabaseInsert();
        int count = 0;
        try {
            int sqlmode = 0;
            if (con.getMetaData().getDriverName().contains("MS SQL")) {
                sqlmode = 1;
            } else {
                sqlmode = 0;
            }
            
            TableFieldStructure tfs = getTableFieldsList2(dmd, tablename, "", attributmap);
            StringBuilder sql_insert = new StringBuilder();
            sql_insert.append("INSERT INTO ");
            sql_insert.append(tablename);
            sql_insert.append(" (");
            for (TableField tf : tfs.getTableFieldsList()) {
                if (0 == sqlmode) {
                    sql_insert.append("`").append(tf.getName()).append("`, ");
                } else {
                    sql_insert.append("[").append(tf.getName()).append("], ");
                }
            }
            sql_insert.delete(sql_insert.length()-2, sql_insert.length());
            sql_insert.append(" ) VALUES (");
            for (TableField tf : tfs.getTableFieldsList()) {
                if ((0 == tf.getType().compareToIgnoreCase("string")) || (0 == tf.getType().compareToIgnoreCase("date"))) {
                    if (0 == tf.getType().compareToIgnoreCase("date")) {
                        String pattern = "dd.MM.yyyy HH:mm:ss";
                        DateTime dt = null;
                        try {
                            dt = new DateTime(attributmap.get((String) tf.getName()));
                            //dt = DateTime.parse(attributmap.get((String) tf.getName()), DateTimeFormat.forPattern(pattern));
                        } catch (Exception ex) {
                            try {
                                pattern = "yyyy-MM-dd HH:mm:ss";
                                dt = DateTime.parse(attributmap.get((String) tf.getName()), DateTimeFormat.forPattern(pattern));
                            } catch (Exception ex1) {
                                try {
                                    pattern = "yyyy-MM-dd";
                                    dt = DateTime.parse(attributmap.get((String) tf.getName()), DateTimeFormat.forPattern(pattern));
                                } catch (Exception ex2) {
                                    try {
                                        pattern = "dd.MM.yyyy";
                                        dt = DateTime.parse(attributmap.get((String) tf.getName()), DateTimeFormat.forPattern(pattern));
                                    } catch (Exception ex3) {
                                        
                                    }
                                }
                            }
                        }
                        if (null != dt) {
                            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                            sql_insert.append("'").append(dt.toString(dtf)).append("', ");
                        } else {
                            sql_insert.append(dt).append(", ");
                        }
                    } else {
                        if (null != attributmap.get((String) tf.getName())) {
                            String value = attributmap.get((String) tf.getName());
                            value = value.replace("\\", "\\\\");
                            value = value.replace("'", "''");
                            sql_insert.append("'").append(value).append("', ");
                        } else {
                            sql_insert.append(attributmap.get((String) tf.getName())).append(", ");
                        }
                    }
                } else {
                    sql_insert.append(attributmap.get((String) tf.getName())).append(", ");
                }
            }
            sql_insert.delete(sql_insert.length()-2, sql_insert.length());
            sql_insert.append(" )");
            
            stmt = con.createStatement();
            count = stmt.executeUpdate(sql_insert.toString(), Statement.RETURN_GENERATED_KEYS);
            rdbi.setCount(count);
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rdbi.setGeneratedid(generatedKeys.getLong(1));
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
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
        }
        return rdbi;
    }
    
    public int manageTableDelete(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, String[]> attributmap) {
        Statement stmt = null;
        int count = 0;
        try {
            int sqlmode = 0;
            if (con.getMetaData().getDriverName().contains("MS SQL")) {
                sqlmode = 1;
            } else {
                sqlmode = 0;
            }
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, "", attributmap);
            StringBuilder sql_delete = new StringBuilder();
            sql_delete.append("DELETE FROM ");
            sql_delete.append(tablename);
            StringBuilder sql_condition = null;
            if (null != attributmap) {
                sql_condition = buildCondition(attributmap, tfs.getTableFieldsList(), sqlmode);
            }
            if (null != sql_condition) {
                sql_delete.append(sql_condition);
            }
            
            stmt = con.createStatement();
            count = stmt.executeUpdate(sql_delete.toString());
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
        }
        return count;
    }
    
    public int manageTableUpdate(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, String[]> attributmap, HashMap<String, String> updatemap) {
        Statement stmt = null;
        int count = 0;
        try {
            int sqlmode = 0;
            if (con.getMetaData().getDriverName().contains("MS SQL")) {
                sqlmode = 1;
            } else {
                sqlmode = 0;
            }
            TableFieldStructure tfs = getTableFieldsList2(dmd, tablename, "", updatemap);
            StringBuilder sql_update = new StringBuilder();
            sql_update.append("UPDATE ");
            sql_update.append(tablename);
            sql_update.append(" SET ");
            
            StringBuilder sql_set = null;
            if (null != attributmap) {
                sql_set = buildSet(updatemap, tfs.getTableFieldsList(), sqlmode);
            }
            if (null != sql_set) {
                sql_update.append(sql_set);
            }
            
            tfs = getTableFieldsList(dmd, tablename, "", attributmap);

            StringBuilder sql_condition = null;
            if (null != attributmap) {
                sql_condition = buildCondition(attributmap, tfs.getTableFieldsList(), sqlmode);
            }
            if (null != sql_condition) {
                sql_update.append(sql_condition);
            }
            
            stmt = con.createStatement();
            count = stmt.executeUpdate(sql_update.toString());
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
        }
        return count;
    }
    
    private String getFieldType(ArrayList<TableField> tableFieldsList, String fieldname) {
        for (TableField tf : tableFieldsList) {
            if (tf.getName().compareToIgnoreCase(fieldname) == 0) {
                return tf.getType();
            }
        }
        return null;
    }
    
    private boolean isPrimaryKey(ArrayList<TableField> tableFieldsList, String fieldname) {
        for (TableField tf : tableFieldsList) {
            if (0 == tf.getName().compareToIgnoreCase(fieldname)) {
                return tf.isPrimaryKey();
            }
        }
        return false;
    }
    
    public TableFieldStructure getTableFieldsList(DatabaseMetaData dmd, String tablename, String default_order, HashMap<String, String[]> attributmap) {
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
                if ((null == attributmap) || (attributmap.containsKey(columnName))) {
                    String datatype = columns.getString("DATA_TYPE");
                    String colomuntypename = columns.getString("TYPE_NAME");
                    String columnsize = columns.getString("COLUMN_SIZE");
                    String decimaldigits = columns.getString("DECIMAL_DIGITS");
                    if (null == decimaldigits) {
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
                        case "-1":      // varchar -> String
                        case "1":
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
            }
            tfs.setDefault_order(default_order);
            tfs.setTableFieldsList(tableFieldsList);
            return tfs;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    private TableFieldStructure getTableFieldsList2(DatabaseMetaData dmd, String tablename, String default_order, HashMap<String, String> attributmap) {
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
                if ((null == attributmap) || (attributmap.containsKey(columnName))) {
                    String datatype = columns.getString("DATA_TYPE");
                    String colomuntypename = columns.getString("TYPE_NAME");
                    String columnsize = columns.getString("COLUMN_SIZE");
                    String decimaldigits = columns.getString("DECIMAL_DIGITS");
                    if (null == decimaldigits) {
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
                        case "-1":      // varchar -> String
                        case "1":
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
                        case "-2":      // bit
                            tf = new TableField(columnName, "BOOLEAN", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                            tableFieldsList.add(tf);
                            break;    
                        case "93":      // Date
                            tf = new TableField(columnName, "DATE", colomuntypename, pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                            tableFieldsList.add(tf);
                            break;
                    }
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
    
    private StringBuilder buildCondition(HashMap<String, String[]> attributmap, ArrayList<TableField> tableFieldsList, int sqlmode) {
        StringBuilder sql_condition = new StringBuilder();
        if (!attributmap.isEmpty()) {
            sql_condition.append(" WHERE ");
            boolean added = false;
            for (Object key : attributmap.keySet().toArray()) {
                String[] values = attributmap.get(key);
                if ((null != values) && (values.length > 0) && (!values[0].isBlank())) {
                    added = true;
                    sql_condition.append("(");
                    if (0 == sqlmode) {
                        sql_condition.append("`").append((String) key).append("`");
                    } else {
                        sql_condition.append("[").append((String) key).append("]");
                    }
                    String fieldType = getFieldType(tableFieldsList, (String) key);
                    sql_condition.append(buildWhere(values, fieldType));
                    sql_condition.append(") AND ");
                }
            }
            if (added) {
                sql_condition.delete(sql_condition.length()-4, sql_condition.length());
            } else {
                sql_condition.delete(0, sql_condition.length());
            }
        }
        return sql_condition;
    }
    
    private StringBuilder buildSet(HashMap<String, String> attributmap, ArrayList<TableField> tableFieldsList, int sqlmode) {
        StringBuilder sql_set = new StringBuilder();
        if (!attributmap.isEmpty()) {
            boolean added = false;
            for (Object key : attributmap.keySet().toArray()) {
                //if (!attributmap.get(key).isBlank()) {
                if (!isPrimaryKey(tableFieldsList, (String) key)) {
                    added = true;
                    if (0 == sqlmode) {
                        sql_set.append("`").append((String) key).append("`");
                    } else {
                        sql_set.append("[").append((String) key).append("]");
                    }
                    String fieldType = getFieldType(tableFieldsList, (String) key);

                    sql_set.append(" = ");
                    switch (fieldType.toLowerCase()) {
                        case "string":
                        case "date":
                    //if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            if (0 == fieldType.compareToIgnoreCase("date")) {
                                String pattern = "dd.MM.yyyy HH:mm:ss";
                                DateTime dt = null;
                                try {
                                    dt = DateTime.parse(attributmap.get(key), DateTimeFormat.forPattern(pattern));
                                } catch (Exception ex) {
                                    try {
                                        pattern = "yyyy-MM-dd HH:mm:ss";
                                        dt = DateTime.parse(attributmap.get(key), DateTimeFormat.forPattern(pattern));
                                    } catch (Exception ex1) {
                                        try {
                                            pattern = "yyyy-MM-dd";
                                            dt = DateTime.parse(attributmap.get(key), DateTimeFormat.forPattern(pattern));
                                        } catch (Exception ex4) {
                                            try {
                                                pattern = "dd.MM.yyyy";
                                                dt = DateTime.parse(attributmap.get(key), DateTimeFormat.forPattern(pattern));
                                            } catch (Exception ex5) {

                                            }
                                        }
                                    }
                                }
                                if (null != dt) {
                                    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                                    sql_set.append("'").append(dt.toString(dtf)).append("'");
                                } else {
                                    sql_set.append(dt);
                                }
                            } else {
                                if (null != (String) attributmap.get(key)) {
                                    String value = (String) attributmap.get(key);
                                    value = value.replace("\\", "\\\\");
                                    value = value.replace("'", "''");
                                    sql_set.append("'").append(value).append("'");
                                } else {
                                    sql_set.append((String) attributmap.get(key));
                                }
                            }
                            sql_set.append(", ");
                            break;
                    //} else {
                        case "boolean":
                            if (0 == attributmap.get(key).compareToIgnoreCase("true")) {
                                sql_set.append(1);
                            } else {
                                sql_set.append(0);
                            }
                            sql_set.append(", ");
                            break;
                        default:
                            sql_set.append((String) attributmap.get(key));
                            sql_set.append(", ");
                    }
                    //}
                }
            }
            if (added) {
                sql_set.delete(sql_set.length()-2, sql_set.length());
            } else {
                sql_set.delete(0, sql_set.length());
            }
        }
        return sql_set;
    }
    
    private StringBuilder buildWhere(String[] values, String fieldType) {
        StringBuilder sql_where = new StringBuilder();
        String comparator = "";
        String val1 = "";
        String val2 = "";
        
        switch (values.length) {
            case 1:
                comparator = "=";
                val1 = values[0];
                break;
            case 2:
                comparator = values[0];
                val1 = values[1];
                break;
            case 3:
                comparator = values[0];
                val1 = values[1];
                val2 = values[2];
                break;
            default:
                comparator = "";
        }
        
        switch (comparator) {
            case "eq":
                comparator = "=";
                break;
            case "gt":
                comparator = ">";
                break;
            case "lt":
                comparator = "<";
                break;
            case "ge":
                comparator = ">=";
                break;
            case "le":
                comparator = "<=";
                break;
            case "ne":
                comparator = "<>";
                break;
            case "lk":
                comparator = "like";
                break;
            case "bt":
                comparator = "between";
                break;
        }
        
        switch (comparator) {
            case "=":
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "<>":
            case "like":
                sql_where.append(" ");
                sql_where.append(comparator);
                sql_where.append(" ");
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_where.append("'");
                }
                if (0 == comparator.compareToIgnoreCase("like")) {
                    sql_where.append("%").append(val1).append("%");
                } else {
                    sql_where.append(val1);
                }
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_where.append("'");
                }
                break;
            case "between":
                sql_where.append(" ");
                sql_where.append(comparator);
                sql_where.append(" ");
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_where.append("'");
                }
                if (0 == comparator.compareToIgnoreCase("like")) {
                    sql_where.append("%").append(val1).append("%");
                } else {
                    sql_where.append(val1);
                }
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_where.append("'");
                }
                sql_where.append(" AND ");
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_where.append("'");
                }
                if (0 == comparator.compareToIgnoreCase("like")) {
                    sql_where.append("%").append(val2).append("%");
                } else {
                    sql_where.append(val2);
                }
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_where.append("'");
                }
                break;
        }
        return sql_where;
    }

    private StringBuilder makeOrderBy(HashMap<String, String> ordermap, String default_order, String default_direction, int sqlmode, boolean inner) {
        StringBuilder order_builder = new StringBuilder();
        if (1 == sqlmode) {
            if (inner) {
                order_builder.append("ROW_NUMBER() OVER (ORDER BY ");
            } else {
                order_builder.append(" ORDER BY ");
            }
            if ((null == ordermap) || (ordermap.isEmpty())) {
                order_builder.append("[").append(default_order).append("]");
                order_builder.append(" ");
                order_builder.append(default_direction);
            } else {
                boolean added = false;
                for (String key : ordermap.keySet()) {
                    if (!ordermap.get(key).isBlank()) {
                        added = true;
                        order_builder.append("[").append((String) key).append("]");
                        order_builder.append(" ");
                        order_builder.append(ordermap.get(key));
                        order_builder.append(", ");
                    }
                }
                if (added) {
                    order_builder.delete(order_builder.length()-2, order_builder.length());
                } else {
                    order_builder.append("[").append(default_order).append("]");
                    order_builder.append(" ");
                    order_builder.append(default_direction);
                }
            }
            if (inner) {
                order_builder.append(" ) AS rownumber FROM ");
            }
        } else {
            order_builder.append(" ORDER BY ");
            if ((null == ordermap) || (ordermap.isEmpty())) {
                order_builder.append("`").append(default_order).append("`");
                order_builder.append(" ");
                order_builder.append(default_direction);
            } else {
                boolean added = false;
                for (String key : ordermap.keySet()) {
                    if (!ordermap.get(key).isBlank()) {
                        added = true;
                        order_builder.append("`").append((String) key).append("`");
                        order_builder.append(" ");
                        order_builder.append(ordermap.get(key));
                        order_builder.append(", ");
                    }
                }
                if (added) {
                    order_builder.delete(order_builder.length()-2, order_builder.length());
                } else {
                    order_builder.append("`").append(default_order).append("`");
                    order_builder.append(" ");
                    order_builder.append(default_direction);
                }
            }
        }
        return order_builder;
    }
}
