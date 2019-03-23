package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import java.util.List;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Transactional
@Named("attributList")
@Component
public class AttributList {
    @Autowired CfAttributService cfattributService;
    
    private @Getter @Setter List<CfAttribut> attributlist;
    private @Getter @Setter CfAttribut selectedAttribut = null;

    public List<CfAttribut> init(CfClass cfclass) {
        attributlist = cfattributService.findByClassref(cfclass);
        
        return attributlist;
    }
}
