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
import io.clownfish.clownfish.datamodels.RestDatabaseInsert;
import io.clownfish.clownfish.datamodels.RestDatabaseParameter;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "/readdb", produces = MediaType.APPLICATION_JSON_VALUE)
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
                    JDBCUtil jdbcutil = new JDBCUtil(datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword());
                    Connection con = jdbcutil.getConnection();
                    
                    if (null != con) {
                        try {
                            DatabaseMetaData dmd = con.getMetaData();
                            DatatableProperties datatableproperties = new DatatableProperties();
                            datatableproperties.setTablename(icp.getTablename());
                            datatableproperties.setPagination(icp.getPagination());
                            datatableproperties.setPage(icp.getPage());

                            ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});

                            ArrayList<HashMap> resultlist = new ArrayList<>();
                            while(resultSetTables.next())
                            {
                                String tablename = resultSetTables.getString("TABLE_NAME");
                                if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                                    icp.setCount(jdbcutil.manageTableRead(con, dmd, tablename, datatableproperties, icp.getConditionmap(), icp.getValuemap(), resultlist));
                                }
                                
                            }
                            resultSetTables = dmd.getTables(null, null, null, new String[]{"VIEW"});
                            while(resultSetTables.next())
                            {
                                String tablename = resultSetTables.getString("TABLE_NAME");
                                if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                                    icp.setCount(jdbcutil.manageTableRead(con, dmd, tablename, datatableproperties, icp.getConditionmap(), icp.getValuemap(), resultlist));
                                }
                            }
                            icp.setResult(resultlist);
                            icp.setReturncode("OK");
                            con.close();
                        } catch (SQLException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                        finally {
                            try {
                                con.close();
                            }
                            catch (SQLException e) {
                                LOGGER.error(e.getMessage());
                            }
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

    @PostMapping(value = "/insertdb", produces = MediaType.APPLICATION_JSON_VALUE)
    public String restInsertContent(@RequestBody RestDatabaseParameter icp) {
        Gson gson = new Gson();
        return gson.toJson(insertContent(icp));
    }
    
    private RestDatabaseParameter insertContent(RestDatabaseParameter icp) {
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
                            while(resultSetTables.next())
                            {
                                String tablename = resultSetTables.getString("TABLE_NAME");
                                if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                                    RestDatabaseInsert rdbi = jdbcutil.manageTableInsert(con, dmd, tablename, icp.getValuemap());
                                    icp.setCount(rdbi.getCount());
                                    icp.setGeneratedid(rdbi.getGeneratedid());
                                    icp.setReturncode("OK");
                                }
                            }
                            con.close();
                        } catch (SQLException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                        finally {
                            try {
                                con.close();
                            }
                            catch (SQLException e) {
                                LOGGER.error(e.getMessage());
                            }
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

    @PostMapping(value = "/deletedb", produces = MediaType.APPLICATION_JSON_VALUE)
    public String restDeleteContent(@RequestBody RestDatabaseParameter ucp) {
        Gson gson = new Gson();
        return gson.toJson(deleteContent(ucp));
    }
    
    private RestDatabaseParameter deleteContent(RestDatabaseParameter ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfDatasource datasource = cfdatasourceService.findByName(ucp.getDatasource());

                    JDBCUtil jdbcutil = new JDBCUtil(datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword());
                    Connection con = jdbcutil.getConnection();
                    if (null != con) {
                        try {
                            DatabaseMetaData dmd = con.getMetaData();
                            DatatableProperties datatableproperties = new DatatableProperties();
                            datatableproperties.setTablename(ucp.getTablename());
                            ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});
                            while(resultSetTables.next())
                            {
                                String tablename = resultSetTables.getString("TABLE_NAME");
                                if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                                    int count = jdbcutil.manageTableDelete(con, dmd, tablename, ucp.getConditionmap());
                                    ucp.setCount(count);
                                    ucp.setReturncode("OK");
                                }
                            }
                            con.close();
                        } catch (SQLException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                        finally {
                            try {
                                con.close();
                            }
                            catch (SQLException e) {
                                LOGGER.error(e.getMessage());
                            }
                        }
                    } else {
                        return null;
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

    @PostMapping(value = "/updatedb", produces = MediaType.APPLICATION_JSON_VALUE)
    public String restUpdateContent(@RequestBody RestDatabaseParameter ucp) {
        Gson gson = new Gson();
        return gson.toJson(updateContent(ucp));
    }
    
    private RestDatabaseParameter updateContent(RestDatabaseParameter ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfDatasource datasource = cfdatasourceService.findByName(ucp.getDatasource());

                    JDBCUtil jdbcutil = new JDBCUtil(datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword());
                    Connection con = jdbcutil.getConnection();
                    if (null != con) {
                        try {
                            DatabaseMetaData dmd = con.getMetaData();
                            DatatableProperties datatableproperties = new DatatableProperties();
                            datatableproperties.setTablename(ucp.getTablename());
                            ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});
                            while(resultSetTables.next())
                            {
                                String tablename = resultSetTables.getString("TABLE_NAME");
                                if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                                    int count = jdbcutil.manageTableUpdate(con, dmd, tablename, ucp.getConditionmap(), ucp.getValuemap());
                                    ucp.setCount(count);
                                    ucp.setReturncode("OK");
                                }
                            }
                            con.close();
                        } catch (SQLException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                        finally {
                            try {
                                con.close();
                            }
                            catch (SQLException e) {
                                LOGGER.error(e.getMessage());
                            }
                        }
                    } else {
                        return null;
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
}