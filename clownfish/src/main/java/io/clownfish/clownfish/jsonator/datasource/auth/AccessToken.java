/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.datasource.auth;

/**
 *
 * @author SulzbachR
 */
public class AccessToken {
    private String accesstoken;
    private long expires;
    
    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }
    
    public void setExpires(long expires) {
        this.expires = expires;
    }
    
    public String getAccesstoken() {
        return accesstoken;
    }
    
    public long getExpires() {
        return expires;
    }
}
