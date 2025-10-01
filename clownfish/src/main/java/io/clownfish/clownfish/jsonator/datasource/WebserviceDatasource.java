/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.datasource;

/**
 *
 * @author SulzbachR
 */
public class WebserviceDatasource implements IDatasource {
    private String name;
    private String type;
    private String connection;
    private String user;
    private String password;
    private String format;
    private String method;
    private Auth auth;

    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    public void setType(String type) { 
        this.type = type; 
    }

    @Override
    public String getConnection() {
        return connection;
    }
    
    public void setConnection(String connection) { 
        this.connection = connection; 
    }

    @Override
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) { 
        this.user = user; 
    }

    @Override
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }

    @Override
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) { 
        this.format = format; 
    }

    @Override
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) { 
        this.method = method; 
    }

    @Override
    public Auth getAuth() {
        return auth;
    }
    
    public void setAuth(Auth auth) { 
        this.auth = auth; 
    }
}
