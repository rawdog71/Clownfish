/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.datasource.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 *
 * @author SulzbachR
 */
public class BearerToken implements IAuth {

    @Override
    public AccessToken getAccessToken(String url) {
        HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String jsonBody = response.body();

                    // Jackson ObjectMapper erstellen
                    ObjectMapper mapper = new ObjectMapper();

                    // Den JSON-String als JsonNode parsen
                    JsonNode rootNode = mapper.readTree(jsonBody);

                    // Nun kannst du auf die Daten zugreifen, ohne die Struktur zu kennen
                    //System.out.println("JSON-Struktur erfolgreich als Baummodell geparst.");

                    //String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
                    //System.out.println(jsonOutput);
                    
                    AccessToken at = new AccessToken();
                    at.setAccesstoken(rootNode.at("/access_token").asText());
                    at.setExpires(rootNode.at("/expires_in").asLong());
                    return at;
                } else {
                    System.out.println("Fehler beim Aufruf des Webservices. Statuscode: " + response.statusCode());
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
    }
    
}
