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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
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
            LOGGER.error(ex.getMessage());
            return null;
        } catch (Exception ex) {
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
}
