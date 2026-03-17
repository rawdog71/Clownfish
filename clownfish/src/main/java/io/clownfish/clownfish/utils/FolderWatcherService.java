package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.dbentities.CfFoldertrigger;
import io.clownfish.clownfish.events.FolderChangeEvent;
import io.clownfish.clownfish.serviceinterface.CfFoldertriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class FolderWatcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderWatcherService.class);

    @Autowired
    private CfFoldertriggerService cffoldertriggerService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private WatchService watchService;
    private ExecutorService executorService;

    // Speichert die Zuordnung von WatchKey zum echten Pfad
    private final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    
    // Speichert die Zuordnung von WatchKey zum Trigger aus der Datenbank
    private final Map<WatchKey, CfFoldertrigger> triggerMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            
            // Lade alle Trigger aus der DB
            List<CfFoldertrigger> triggers = cffoldertriggerService.findAll();
            for (CfFoldertrigger trigger : triggers) {
                if (trigger.getActive()) {
                    registerTrigger(trigger);
                }
            }

            // Starte die Endlosschleife in einem separaten Thread
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this::processEvents);
            
            LOGGER.info("FolderWatcherService erfolgreich gestartet.");
            
        } catch (IOException e) {
            LOGGER.error("Fehler beim Initialisieren des WatchService", e);
        }
    }
    
    /**
     * Stoppt die aktuelle Überwachung und baut sie anhand der Datenbank neu auf.
     */
    public synchronized void rebuild() {
        LOGGER.info("Ordner-Überwachung wird neu aufgebaut...");
        cleanup(); // Beendet den aktuellen WatchService und Thread
        keys.clear();
        triggerMap.clear();
        init();    // Startet alles frisch (liest DB neu aus)
    }

    /**
     * Registriert einen Trigger. Wenn rekursiv, wird der Baum durchlaufen.
     */
    public void registerTrigger(CfFoldertrigger trigger) throws IOException {
        Path startPath = Paths.get(trigger.getFolder());
        
        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            LOGGER.warn("Pfad existiert nicht oder ist kein Verzeichnis: {}", startPath);
            return;
        }

        if (trigger.getRecursive()) {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    registerDirectory(dir, trigger);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            registerDirectory(startPath, trigger);
        }
    }

    /**
     * Hilfsmethode für die eigentliche Registrierung beim WatchService.
     */
    private void registerDirectory(Path dir, CfFoldertrigger trigger) throws IOException {
        WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
        triggerMap.put(key, trigger);
        LOGGER.info("Verzeichnis {} für Trigger '{}' registriert.", dir, trigger.getName());
    }

    /**
     * Die Endlosschleife, die auf Datei-Events wartet.
     */
    @SuppressWarnings("unchecked")
    private void processEvents() {
        while (true) {
            WatchKey key;
            try {
                key = watchService.take(); // Blockiert, bis ein Event eintritt
            } catch (InterruptedException x) {
                LOGGER.info("FolderWatcherService wurde unterbrochen.");
                Thread.currentThread().interrupt();
                return;
            } catch (ClosedWatchServiceException x) {
                // Diese Exception fliegt absichtlich, wenn wir watchService.close() beim Rebuild aufrufen
                LOGGER.info("WatchService wurde für einen Neustart beendet.");
                return;
            }

            Path dir = keys.get(key);
            CfFoldertrigger trigger = triggerMap.get(key);
            
            if (dir == null || trigger == null) {
                LOGGER.warn("WatchKey nicht erkannt!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path name = ev.context();
                Path child = dir.resolve(name); // Der absolute Pfad der betroffenen Datei

                LOGGER.info("Event: {} auf Datei: {} (Trigger: {})", kind.name(), child, trigger.getName());

                // WICHTIG: Wenn ein neuer Ordner erstellt wird und der Trigger rekursiv ist, 
                // muss der neue Ordner ZUSÄTZLICH registriert werden!
                if (kind == ENTRY_CREATE && trigger.getRecursive()) {
                    try {
                        if (Files.isDirectory(child)) {
                            registerTriggerForNewFolder(child, trigger);
                        }
                    } catch (IOException x) {
                        LOGGER.error("Fehler bei der dynamischen Registrierung von {}", child, x);
                    }
                }

                // HIER FEUERN WIR DAS EVENT AB:
                // Wir stellen sicher, dass es sich um Erstellen, Löschen oder Ändern handelt
                if (kind == ENTRY_CREATE || kind == ENTRY_DELETE || kind == ENTRY_MODIFY) {
                    
                    // Optional: Prüfen, ob es wirklich eine Datei ist (und kein Verzeichnis), 
                    // falls du nur auf echte Dateien reagieren willst:
                    if (!Files.isDirectory(child) || kind == ENTRY_DELETE) {
                        FolderChangeEvent folderEvent = new FolderChangeEvent(this, trigger, child, kind);
                        eventPublisher.publishEvent(folderEvent);
                    }
                }
            }

            // Key zurücksetzen. Wenn er ungültig ist (z.B. Ordner gelöscht), aus den Maps entfernen
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                triggerMap.remove(key);
                if (keys.isEmpty()) {
                    LOGGER.info("Alle überwachten Verzeichnisse wurden gelöscht.");
                }
            }
        }
    }

    /**
     * Registriert dynamisch zur Laufzeit erstellte Unterordner.
     */
    private void registerTriggerForNewFolder(Path newPath, CfFoldertrigger trigger) throws IOException {
        Files.walkFileTree(newPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir, trigger);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (watchService != null) watchService.close();
            if (executorService != null) executorService.shutdownNow();
        } catch (IOException e) {
            LOGGER.error("Fehler beim Beenden des WatchService", e);
        }
    }
}