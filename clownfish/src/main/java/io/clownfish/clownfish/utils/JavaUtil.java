package io.clownfish.clownfish.utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.dbentities.CfJavaversion;
import io.clownfish.clownfish.dbentities.CfJavaversionPK;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.serviceinterface.CfJavaversionService;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;
import org.springframework.context.annotation.Scope;

@Scope("singleton")
@Component
@Accessors(chain = true)
public class JavaUtil implements IVersioningInterface<CfJava>, Serializable {
    @Autowired
    transient CfJavaService cfjavaService;
    @Autowired transient CfJavaversionService cfjavaversionService;

    private @Getter
    @Setter
    long currentVersion;
    private @Getter @Setter String javaContent = "";
    private transient @Getter @Setter
    Patch<String> patch = null;
    private transient @Getter @Setter
    List<String> source = null;
    private transient @Getter @Setter List<String> target = null;

    final transient Logger LOGGER = LoggerFactory.getLogger(JavaUtil.class);

    public JavaUtil() {}

    @Override
    public String getVersion(long javaref, long version) {
        try {
            CfJavaversion java = cfjavaversionService.findByPK(javaref, version);
            byte[] decompress = CompressionUtils.decompress(java.getContent());
            return new String(decompress, StandardCharsets.UTF_8);
        } catch (IOException | DataFormatException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public void writeVersion(long javaref, long version, byte[] content, long currentuserid) {
        CfJavaversionPK javaversionpk = new CfJavaversionPK();
        javaversionpk.setJavaref(javaref);
        javaversionpk.setVersion(version);

        CfJavaversion cfjavaversion = new CfJavaversion();
        cfjavaversion.setCfJavaversionPK(javaversionpk);
        cfjavaversion.setContent(content);
        cfjavaversion.setTstamp(new Date());
        cfjavaversion.setCommitedby(BigInteger.valueOf(currentuserid));
        cfjavaversionService.create(cfjavaversion);
    }

    @Override
    public boolean hasDifference(CfJava cfJava) {
        boolean diff = false;
        try {
            currentVersion = cfjavaversionService.findMaxVersion(cfJava.getId());
        } catch (NullPointerException ex) {
            currentVersion = 0;
        }
        if (currentVersion > 0) {
            javaContent = cfJava.getContent();
            String contentVersion = getVersion(cfJava.getId(), currentVersion);
            source = Arrays.asList(javaContent.split("\\r?\\n"));
            target = Arrays.asList(contentVersion.split("\\r?\\n"));
            patch = DiffUtils.diff(source, target);
            if (!patch.getDeltas().isEmpty()) {
                diff = true;
            }
        } else {
            diff = true;
        }
        return diff;
    }

    @Override
    public long getCurrentVersionNumber(String name) {
        CfJava cfjava = cfjavaService.findByName(name);
        return cfjavaversionService.findMaxVersion((cfjava).getId());
    }

    @Override
    public String getUniqueName(String name) {
        int i = 1;
        boolean found = false;
        do {
            try {
                cfjavaService.findByName(name+"("+i+")");
                i++;
            } catch(Exception ex) {
                found = true;
            }
        } while (!found);
        return name+"("+i+")";
    }
}
