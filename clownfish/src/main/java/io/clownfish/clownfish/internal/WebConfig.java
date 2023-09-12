package io.clownfish.clownfish.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;

@Configuration
@ConditionalOnProperty(value = "server.internalServer", havingValue = "1")
public class WebConfig implements WebMvcConfigurer {

    @Value("${server.internalPort}")
    private int internalPort;

    @Value("${server.internalPaths}")
    private ArrayList<String> internalPaths;

    @Bean
    public FilterRegistrationBean<InternalEndpointsFilter> trustedEndpointsFilter() {
        return new FilterRegistrationBean<>(new InternalEndpointsFilter(internalPort, internalPaths));
    }
}

