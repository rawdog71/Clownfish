/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.clownfish.clownfish.jsonator.datasource.auth;

/**
 *
 * @author SulzbachR
 */
public interface IAuth {
    public AccessToken getAccessToken(String url);
}
