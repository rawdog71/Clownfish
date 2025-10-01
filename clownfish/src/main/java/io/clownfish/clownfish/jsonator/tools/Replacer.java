/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.tools;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author SulzbachR
 */
public class Replacer {
    public static String replaceVariables(String text, ObjectNode mainNode) {
        // Regex-Muster, das nach #{WORT} sucht
        Pattern pattern = Pattern.compile("#REPL\\{([^{}]+)\\}");
        Matcher matcher = pattern.matcher(text);
        
        // StringBuilder für eine effiziente String-Manipulation
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;
        
        while (matcher.find()) {
            // Finde den Variablennamen innerhalb der Klammern
            String variableName = matcher.group(1);
            
            // Finde den Wert der Variable in der Map
            String replacement = mainNode.at(variableName).asText();
            
            // Teile des ursprünglichen Strings anfügen
            builder.append(text, lastIndex, matcher.start());
            builder.append(replacement);
            
            lastIndex = matcher.end();
        }
        
        // Den Rest des Strings anfügen
        builder.append(text, lastIndex, text.length());
        
        return builder.toString();
    }
    
    public static String processSubstringPattern(String input) {
        // Regulärer Ausdruck, um #SUB(TEXT, START, LENGTH) zu finden
        Pattern pattern = Pattern.compile("#SUB\\{([^,]+),\\s*(\\d+),\\s*(\\d+)\\}");
        Matcher matcher = pattern.matcher(input);

        // StringBuilder für eine effiziente String-Manipulation
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;
        
        // Überprüfen, ob das Muster im String gefunden wurde
        if (matcher.find()) {
            String textToProcess = matcher.group(1); // Der zu bearbeitende Text, z.B. JZMTTKRS
            int startIndex = Integer.parseInt(matcher.group(2)); // Start-Index, z.B. 0
            int length = Integer.parseInt(matcher.group(3)); // Länge, z.B. 2

            // Führe die substring-Operation durch
            if (startIndex >= 0 && startIndex < textToProcess.length() && length > 0) {
                int endIndex = Math.min(startIndex + length, textToProcess.length());
                
                // Teile des ursprünglichen Strings anfügen
                builder.append(input, lastIndex, matcher.start());
                builder.append(textToProcess.substring(startIndex, endIndex));
            
                lastIndex = matcher.end();
            }
        }
        
        // Den Rest des Strings anfügen
        builder.append(input, lastIndex, input.length());
        
        return builder.toString();
    }
}
