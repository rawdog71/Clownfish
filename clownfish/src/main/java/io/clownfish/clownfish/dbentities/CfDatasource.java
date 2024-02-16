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
package io.clownfish.clownfish.dbentities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_datasource", catalog = "clownfish", schema = "")
@Cacheable(false)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfDatasource.findAll", query = "SELECT c FROM CfDatasource c"),
    @NamedQuery(name = "CfDatasource.findById", query = "SELECT c FROM CfDatasource c WHERE c.id = :id"),
    @NamedQuery(name = "CfDatasource.findByName", query = "SELECT c FROM CfDatasource c WHERE c.name = :name"),
    @NamedQuery(name = "CfDatasource.findByServer", query = "SELECT c FROM CfDatasource c WHERE c.server = :server"),
    @NamedQuery(name = "CfDatasource.findByUrl", query = "SELECT c FROM CfDatasource c WHERE c.url = :url"),
    @NamedQuery(name = "CfDatasource.findByPort", query = "SELECT c FROM CfDatasource c WHERE c.port = :port"),
    @NamedQuery(name = "CfDatasource.findByDatabasename", query = "SELECT c FROM CfDatasource c WHERE c.databasename = :databasename"),
    @NamedQuery(name = "CfDatasource.findByUser", query = "SELECT c FROM CfDatasource c WHERE c.user = :user"),
    @NamedQuery(name = "CfDatasource.findByPassword", query = "SELECT c FROM CfDatasource c WHERE c.password = :password"),
    @NamedQuery(name = "CfDatasource.findByDriverclass", query = "SELECT c FROM CfDatasource c WHERE c.driverclass = :driverclass"),
    @NamedQuery(name = "CfDatasource.findByRestservice", query = "SELECT c FROM CfDatasource c WHERE c.restservice = :restservice")})
public class CfDatasource implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "server")
    private String server;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "url")
    private String url;
    @Basic(optional = false)
    @NotNull
    @Column(name = "port")
    private int port;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "databasename")
    private String databasename;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "user")
    private String user;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "password")
    private String password;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "driverclass")
    private String driverclass;
    @Column(name = "restservice")
    private boolean restservice;

    public CfDatasource() {
    }

    public CfDatasource(Long id) {
        this.id = id;
    }

    public CfDatasource(Long id, String name, String server, String url, int port, String databasename, String user, String password, String driverclass) {
        this.id = id;
        this.name = name;
        this.server = server;
        this.url = url;
        this.port = port;
        this.databasename = databasename;
        this.user = user;
        this.password = password;
        this.driverclass = driverclass;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabasename() {
        return databasename;
    }

    public void setDatabasename(String databasename) {
        this.databasename = databasename;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverclass() {
        return driverclass;
    }

    public void setDriverclass(String driverclass) {
        this.driverclass = driverclass;
    }

    public boolean isRestservice() {
        return restservice;
    }

    public void setRestservice(boolean restservice) {
        this.restservice = restservice;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfDatasource)) {
            return false;
        }
        CfDatasource other = (CfDatasource) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfDatasource[ id=" + id + " ]";
    }
    
}
