package io.clownfish.clownfish;

import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author rawdog71
 */
@RestController
@EnableAutoConfiguration
public class Clownfish {
    
    @Autowired CfUserService cfuserService;

    @RequestMapping("/")
    String home() {
        
        CfUser cfUser = cfuserService.findById(1L);
        
        return "Hello Clownfish";
    }
}
