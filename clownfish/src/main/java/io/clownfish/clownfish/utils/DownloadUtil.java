/*
 * Copyright 2025 SulzbachR.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author SulzbachR
 */
public class DownloadUtil {
    
    public void downloadJson(String url, String path, String filename) {
        try {
            String jsoncontent = getJsonFromWebservice(url);
            String jsoncontentformatted = formatJsonString(jsoncontent);
            saveJsonToFile(jsoncontentformatted, path + File.separator + filename);
        } catch (IOException ex) {
            Logger.getLogger(DownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Ruft einen Webservice auf und gibt die JSON-Antwort als String zurück.
     * @param url Die URL des aufzurufenden Webservices.
     * @return Die JSON-Antwort als String.
     * @throws IOException wenn ein Fehler bei der HTTP-Anfrage auftritt.
     */
    private static String getJsonFromWebservice(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            return httpClient.execute(request, response -> {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity);
                }
                return null;
            });
        }
    }
    
    private static String formatJsonString(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        // Pretty-Print die JSON-Struktur mit 2 Leerzeichen Einzug
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    /**
     * Speichert einen String in einer Datei.
     * @param content Der zu schreibende String.
     * @param filePath Der Pfad zur Zieldatei.
     * @throws IOException wenn ein Fehler beim Schreiben der Datei auftritt.
     */
    private static void saveJsonToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
    }
}
