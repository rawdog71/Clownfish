/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.clownfish.clownfish.jsonator.listconditions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author SulzbachR
 */
public class ListConditionParser {
    // Regex, um einzelne Bedingungen in Klammern zu erfassen
    private static final Pattern CONDITION_PATTERN = Pattern.compile(
        "\\((.+?)\\s*([=><!]=?|\\b(?:AND|OR)\\b)\\s*(.+?)\\)"
    );
    // Ein einfacherer Regex für Ihre Struktur (nur einzelne Bedingungen)
    private static final Pattern SIMPLE_CONDITION_PATTERN = Pattern.compile(
        "\\((.+?)\\)"
    );
    
    List<ListCondition> listconditions;
    
    public static List<ListCondition> parseConditions(String input) {
        List<String> conditions = new ArrayList<>();
        List<ListCondition> listconditions = new ArrayList<>();
        
        // Zuerst entfernen wir das " AND " und Leerzeichen drumherum
        String sanitizedInput = input.trim();
        
        // Verwenden des einfachen Musters, um jeden geklammerten Ausdruck zu finden
        Matcher matcher = SIMPLE_CONDITION_PATTERN.matcher(sanitizedInput);
        
        while (matcher.find()) {
            // matcher.group(0) ist die gesamte gefundene Klammer (z.B. "(left == 10)")
            String fullCondition = matcher.group(0);
            conditions.add(fullCondition);
        }
        
        for (String condition : conditions) {
            String c1 = condition.substring(1, condition.length()-1);
            String[] c2 = c1.split(" ");
            ListCondition lc = new ListCondition();
            lc.setLeft(c2[0]);
            lc.setRight(c2[2]);
            lc.setOperator(c2[1]);
            listconditions.add(lc);
        }
        
        return listconditions;
    }
    
    public static JsonNode filterNodes(JsonNode arrayNode, List<ListCondition> listconditions) {
        ObjectMapper mapper = new ObjectMapper(); // Benötigt für die Erstellung des neuen ArrayNode

        ArrayNode filteredArray = mapper.createArrayNode();
        arrayNode.forEach(element -> {
            // Beispiel: Füge nur Objekte hinzu, bei denen "status" != "DISABLED"
            if (isValid(element, listconditions)) {
                filteredArray.add(element); // Das Element hinzufügen, wenn es den Filter passiert
            }
        });
        return filteredArray;
    }
    
    private static boolean isValid(JsonNode node, List<ListCondition> listconditions) {
        boolean valid = true;
        for (ListCondition lc : listconditions) {
            JsonNode checknode = node.findValue(lc.left);
            //System.out.println(checknode);
            String comparevalue = lc.right;
            if (0 == comparevalue.compareToIgnoreCase("EMPTY")) {
                comparevalue = "";
            }
            switch (lc.operator) {
                case "==" -> {
                    if (!checknode.asText().equalsIgnoreCase(comparevalue)) {
                        valid = false;
                    }
                }
                case "!=" -> {
                    if (checknode.asText().equalsIgnoreCase(comparevalue)) {
                        valid = false;
                    }
                }
                case "SW" -> {
                    if (!checknode.asText().startsWith(comparevalue)) {
                        valid = false;
                    }
                }
                case "!SW" -> {
                    if (checknode.asText().startsWith(comparevalue)) {
                        valid = false;
                    }
                }
                case "CO" -> {
                    if (!checknode.asText().contains(comparevalue)) {
                        valid = false;
                    }
                }
                case "!CO" -> {
                    if (checknode.asText().contains(comparevalue)) {
                        valid = false;
                    }
                }
                case ">" -> {
                    if (!(checknode.asInt() > Integer.parseInt(comparevalue))) {
                        valid = false;
                    }
                }
                case ">=" -> {
                    if (!(checknode.asInt() >= Integer.parseInt(comparevalue))) {
                        valid = false;
                    }
                }
                case "<" -> {
                    if (!(checknode.asInt() < Integer.parseInt(comparevalue))) {
                        valid = false;
                    }
                }
                case "<=" -> {
                    if (!(checknode.asInt() <= Integer.parseInt(comparevalue))) {
                        valid = false;
                    }
                }
            }
        }
        return valid;
    }
}
