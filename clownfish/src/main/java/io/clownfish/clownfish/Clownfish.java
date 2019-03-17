package io.clownfish.clownfish;

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

    @RequestMapping("/")
    String home() {
        return "Hello Clownfish";
    }
}
