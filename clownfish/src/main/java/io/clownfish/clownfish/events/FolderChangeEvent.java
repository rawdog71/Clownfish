package io.clownfish.clownfish.events;

import io.clownfish.clownfish.dbentities.CfFoldertrigger;
import org.springframework.context.ApplicationEvent;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class FolderChangeEvent extends ApplicationEvent {

    private final CfFoldertrigger trigger;
    private final Path filePath;
    private final WatchEvent.Kind<?> eventType;

    public FolderChangeEvent(Object source, CfFoldertrigger trigger, Path filePath, WatchEvent.Kind<?> eventType) {
        super(source);
        this.trigger = trigger;
        this.filePath = filePath;
        this.eventType = eventType;
    }

    public CfFoldertrigger getTrigger() {
        return trigger;
    }

    public Path getFilePath() {
        return filePath;
    }

    public WatchEvent.Kind<?> getEventType() {
        return eventType;
    }
}