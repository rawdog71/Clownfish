package io.clownfish.clownfish.listeners;

import io.clownfish.clownfish.events.FolderChangeEvent;
import io.clownfish.clownfish.service.TemplateExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class FolderTriggerActionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderTriggerActionHandler.class);

    @Autowired
    private TemplateExecutionService templateExecutionService;

    @EventListener
    public void handleFolderChange(FolderChangeEvent event) {
        String action = "";
        
        if (event.getEventType() == ENTRY_CREATE) {
            action = "ANGELEGT";
        } else if (event.getEventType() == ENTRY_MODIFY) {
            action = "GEÄNDERT";
        } else if (event.getEventType() == ENTRY_DELETE) {
            action = "GELÖSCHT";
        }

        LOGGER.info("CMS-TRIGGER AUSGELÖST: Datei '{}' wurde {}.", event.getFilePath().getFileName(), action);
        LOGGER.info("Zugehöriger Trigger: {} (Parameter: {})", 
                event.getTrigger().getName(), 
                event.getTrigger().getParameter());

        // Parameter für das Template vorbereiten
        Map<String, String> paramMap = makeParamMap(event.getTrigger().getParameter());
        
        // Da es sich um ein Datei-Event handelt, packen wir die Datei-Infos als zusätzliche Parameter mit rein, 
        // damit das Template im CMS weiß, welche Datei betroffen ist!
        paramMap.put("trigger_filepath", event.getFilePath().toString());
        paramMap.put("trigger_filename", event.getFilePath().getFileName().toString());
        paramMap.put("trigger_action", action);

        // Den neuen Service aufrufen
        templateExecutionService.executeTemplate(
                event.getTrigger().getSiteref().longValue(), 
                paramMap, 
                event.getTrigger().getName()
        );
    }

    private Map<String, String> makeParamMap(String params) {
        Map<String, String> paramMap = new HashMap<>();
        if (params != null && !params.isBlank()) {
            for (String param : params.split("&")) {
                String[] val = param.split("=");
                if(val.length == 2) {
                    paramMap.put(val[0], val[1]);
                }
            }
        }
        return paramMap;
    }
}