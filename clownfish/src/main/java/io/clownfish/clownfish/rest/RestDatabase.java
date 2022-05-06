/*
 * Copyright 2020 SulzbachR.
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
package io.clownfish.clownfish.rest;

import com.google.gson.Gson;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.RestDatabaseParameter;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.TableField;
import io.clownfish.clownfish.jdbc.TableFieldStructure;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SulzbachR
 */
@RestController
public class RestDatabase {
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    @Autowired private CfDatasourceService cfdatasourceService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestDatabase.class);

    @PostMapping("/readdb")
    public String restReadContent(@RequestBody RestDatabaseParameter icp) {
        Gson gson = new Gson();
        return gson.toJson(readContent(icp));
    }
    
    private RestDatabaseParameter readContent(RestDatabaseParameter icp) {
        HashMap<String, HashMap> dbexport = new HashMap<>();
        try {
            String token = icp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = icp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfDatasource datasource = cfdatasourceService.findByName(icp.getDatasource());
                    //System.out.println(datasource.getDatabasename());

                    JDBCUtil jdbcutil = new JDBCUtil(datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword());
                    Connection con = jdbcutil.getConnection();
                    
                    if (null != con) {
                        try {
                            DatabaseMetaData dmd = con.getMetaData();
                            DatatableProperties datatableproperties = new DatatableProperties();
                            datatableproperties.setTablename(icp.getTablename());
                            datatableproperties.setOrderby(icp.getOrderby());
                            datatableproperties.setOrderdir(icp.getOrderdir());
                            datatableproperties.setPagination(icp.getPagination());
                            datatableproperties.setPage(icp.getPage());

                            ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});

                            HashMap<String, ArrayList> dbtables = new HashMap<>();
                            HashMap<String, Object> dbvalues = new HashMap<>();
                            while(resultSetTables.next())
                            {
                                String tablename = resultSetTables.getString("TABLE_NAME");
                                //System.out.println(tablename);
                                if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                                    manageTableRead(con, dmd, tablename, datatableproperties, icp.getAttributmap(), dbtables, dbvalues);
                                }
                                
                            }

                            resultSetTables = dmd.getTables(null, null, null, new String[]{"VIEW"});
                            while(resultSetTables.next())
                            {
                                String tablename = resultSetTables.getString("TABLE_NAME");
                                //System.out.println(tablename);
                                if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                                    manageTableRead(con, dmd, tablename, datatableproperties, icp.getAttributmap(), dbtables, dbvalues);
                                }
                            }

                            dbvalues.put("table", dbtables);
                            dbexport.put(datasource.getDatabasename(), dbvalues);
                            icp.setResult(dbexport);
                        } catch (SQLException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                    } else {
                        return null;
                    }
                } else {
                    icp.setReturncode("Wrong API KEY");
                }
            } else {
                icp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            icp.setReturncode("NoResultException");
        }
        return icp;
    }

    @PostMapping("/insertdb")
    public String restInsertContent(@RequestBody RestDatabaseParameter icp) {
        Gson gson = new Gson();
        return gson.toJson(insertContent(icp));
    }
    
    private RestDatabaseParameter insertContent(RestDatabaseParameter icp) {
        HashMap<String, HashMap> dbexport = new HashMap<>();
        try {
            String token = icp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = icp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfDatasource datasource = cfdatasourceService.findByName(icp.getDatasource());

                    JDBCUtil jdbcutil = new JDBCUtil(datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword());
                    Connection con = jdbcutil.getConnection();
                    if (null != con) {
                        try {
                            DatabaseMetaData dmd = con.getMetaData();
                            DatatableProperties datatableproperties = new DatatableProperties();
                            datatableproperties.setTablename(icp.getTablename());
                            ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});
                            HashMap<String, ArrayList> dbtables = new HashMap<>();
                            HashMap<String, Object> dbvalues = new HashMap<>();
                            while(resultSetTables.next())
                            {
                                String tablename = resultSetTables.getString("TABLE_NAME");
                                //System.out.println(tablename);
                                if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                                    int count = manageTableInsert(con, dmd, tablename, icp.getAttributmap());
                                    icp.setCount(count);
                                }
                            }

                            dbvalues.put("table", dbtables);
                            dbexport.put(datasource.getDatabasename(), dbvalues);
                        } catch (SQLException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                    } else {
                        return null;
                    }
                } else {
                    icp.setReturncode("Wrong API KEY");
                }
            } else {
                icp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            icp.setReturncode("NoResultException");
        }
        return icp;
    }

    /*
    @PostMapping("/deletecontent")
    public RestDatabaseParameter restDeleteContent(@RequestBody RestDatabaseParameter ucp) {
        return deleteContent(ucp);
    }
    
    private RestDatabaseParameter deleteContent(RestDatabaseParameter ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClass clazz = cfclassService.findByName(ucp.getClassname());

                    try {
                        CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
                        classcontent.setScrapped(true);
                        
                        // Delete from Listcontent - consistency
                        List<CfListcontent> listcontent = cflistcontentService.findByClasscontentref(classcontent.getId());
                        for (CfListcontent lc : listcontent) {
                            cflistcontentService.delete(lc);
                            hibernateUtil.deleteRelation(cflistService.findById(lc.getCfListcontentPK().getListref()), cfclasscontentService.findById(lc.getCfListcontentPK().getClasscontentref()));
                        }

                        // Delete from Sitecontent - consistency
                        List<CfSitecontent> sitecontent = cfsitecontentService.findByClasscontentref(classcontent.getId());
                        for (CfSitecontent sc : sitecontent) {
                            cfsitecontentService.delete(sc);
                        }
                        
                        cfclasscontentService.edit(classcontent);
                        ucp.setReturncode("OK");
                        hibernateUtil.updateContent(classcontent);
                    } catch (javax.persistence.NoResultException ex) {
                        ucp.setReturncode("Classcontent not found");
                    }
                } else {
                    ucp.setReturncode("Wrong API KEY");
                }
            } else {
                ucp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }

    @PostMapping("/updatecontent")
    public RestDatabaseParameter restUpdateContent(@RequestBody RestDatabaseParameter ucp) {
        return updateContent(ucp);
    }
    
    private RestDatabaseParameter updateContent(RestDatabaseParameter ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClass clazz = cfclassService.findByName(ucp.getClassname());

                    try {
                        CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
                        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
                        for (CfAttributcontent attributcontent : attributcontentlist) {
                            CfAttribut attribut = attributcontent.getAttributref();
                            // Check, if attribut exists in attributmap
                            if (ucp.getAttributmap().containsKey(attribut.getName())) {
                                contentUtil.setAttributValue(attributcontent, ucp.getAttributmap().get(attribut.getName()));
                                cfattributcontentService.edit(attributcontent);
                                if (ucp.isIndexing()) {
                                    contentUtil.indexContent();
                                }
                                ucp.setReturncode("OK");
                            }
                        }
                        hibernateUtil.updateContent(classcontent);
                    } catch (javax.persistence.NoResultException ex) {
                        ucp.setReturncode("Classcontent not found");
                    }
                } else {
                    ucp.setReturncode("Wrong API KEY");
                }
            } else {
                ucp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }
    */
    
    private void manageTableRead(Connection con, DatabaseMetaData dmd, String tablename, DatatableProperties dtp, HashMap<String, String> attributmap, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        Statement stmt = null;
        ResultSet result = null;
        try {
            long low_limit = 1;
            long high_limit = 50;
            
            //System.out.println(con.getMetaData().getDriverName());

            String default_order = "";
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, default_order, attributmap);
            default_order = tfs.getDefault_order();
            
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

                if (null != attributmap) {
                    sql_condition = buildCondition(attributmap, tfs.getTableFieldsList());
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
                sql_outer.append(") orderedselection WHERE rownumber between ");
                sql_outer.append(low_limit);
                sql_outer.append(" AND ");
                sql_outer.append(high_limit);
                if (null != sql_groupby) {
                    sql_outer.append(sql_groupby);
                }
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
                if (null != attributmap) {
                    sql_condition = buildCondition(attributmap, tfs.getTableFieldsList());
                }
                if (null != sql_condition) {
                    sql_outer.append(sql_condition);
                    sql_count.append(sql_condition);
                }
                
                sql_outer.append(" ORDER BY ");
                sql_outer.append(default_order);
                sql_outer.append(" ");
                sql_outer.append(default_direction);
                
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
    
    private String getFieldType(ArrayList<TableField> tableFieldsList, String fieldname) {
        for (TableField tf : tableFieldsList) {
            if (tf.getName().compareToIgnoreCase(fieldname) == 0) {
                return tf.getType();
            }
        }
        return null;
    }
    
    private TableFieldStructure getTableFieldsList(DatabaseMetaData dmd, String tablename, String default_order, HashMap<String, String> attributmap) {
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
                if (attributmap.containsKey(columnName)) {
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
            }
            tfs.setDefault_order(default_order);
            tfs.setTableFieldsList(tableFieldsList);
            return tfs;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    private StringBuilder buildCondition(HashMap<String, String> attributmap, ArrayList<TableField> tableFieldsList) {
        StringBuilder sql_condition = new StringBuilder();
        if (!attributmap.isEmpty()) {
            sql_condition.append(" WHERE ");
            boolean added = false;
            for (Object key : attributmap.keySet().toArray()) {
                if (!attributmap.get(key).isBlank()) {
                    added = true;
                    sql_condition.append("(");
                    sql_condition.append((String) key);
                    String fieldType = getFieldType(tableFieldsList, (String) key);
                    
                    sql_condition.append(" = ");
                    if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                        sql_condition.append("'");
                    }
                    sql_condition.append((String) attributmap.get(key));
                    if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                        sql_condition.append("'");
                    }
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
    
    private int manageTableInsert(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, String> attributmap) {
        Statement stmt = null;
        int count = 0;
        try {
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, "", attributmap);
            StringBuilder sql_insert = new StringBuilder();
            sql_insert.append("INSERT INTO ");
            sql_insert.append(tablename);
            sql_insert.append(" (");
            for (TableField tf : tfs.getTableFieldsList()) {
                sql_insert.append("`").append(tf.getName()).append("`, ");
            }
            sql_insert.delete(sql_insert.length()-2, sql_insert.length());
            sql_insert.append(" ) VALUES (");
            for (TableField tf : tfs.getTableFieldsList()) {
                if ((0 == tf.getType().compareToIgnoreCase("string")) || (0 == tf.getType().compareToIgnoreCase("date"))) {
                    sql_insert.append("'").append(attributmap.get((String) tf.getName())).append("', ");
                } else {
                    sql_insert.append(attributmap.get((String) tf.getName())).append(", ");
                }
            }
            sql_insert.delete(sql_insert.length()-2, sql_insert.length());
            sql_insert.append(" )");
            
            stmt = con.createStatement();
            count = stmt.executeUpdate(sql_insert.toString());
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
}