/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfStylesheetversion;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sulzbachr
 */
public class StylesheetUtil {
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfStylesheetversionService cfstylesheetversionService;
    
    private @Getter @Setter  long currentVersion;
    private @Getter @Setter  String styelsheetContent = "";
    private @Getter @Setter  Patch<String> patch = null;
    private @Getter @Setter  List<String> source = null;
    private @Getter @Setter  List<String> target = null;

    public StylesheetUtil() {
    }

    public String getVersion(long stylesheetref, long version) {
        try {
            //CfStylesheetversion stylesheet = (Knstylesheetversion) em.createNamedQuery("Knstylesheetversion.findByPK").setParameter("stylesheetref", stylesheetref).setParameter("version", version).getSingleResult();
            CfStylesheetversion stylesheet = cfstylesheetversionService.findByPK(stylesheetref, version);
            byte[] decompress = CompressionUtils.decompress(stylesheet.getContent());
            return new String(decompress, StandardCharsets.UTF_8);
        } catch (IOException | DataFormatException ex) {
            Logger.getLogger(StylesheetUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public boolean hasDifference(CfStylesheet selectedStylesheet) {
        boolean diff = false;
        try {
            try {
                //currentVersion = (long) em.createNamedQuery("Knstylesheetversion.findMaxVersion").setParameter("stylesheetref", selectedStylesheet.getId()).getSingleResult();
                currentVersion = cfstylesheetversionService.findMaxVersion(selectedStylesheet.getId());
            } catch (NullPointerException ex) {
                currentVersion = 0;
            }
            if (currentVersion > 0) {
                styelsheetContent = selectedStylesheet.getContent();
                String contentVersion = getVersion(selectedStylesheet.getId(), currentVersion);
                source = Arrays.asList(styelsheetContent.split("\\r?\\n"));
                target = Arrays.asList(contentVersion.split("\\r?\\n"));
                patch = DiffUtils.diff(source, target);
                if (!patch.getDeltas().isEmpty()) {
                    diff = true;
                }
            } else {
                diff = true;
            }
        } catch (DiffException ex) {
            Logger.getLogger(StylesheetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return diff;
    }
}
