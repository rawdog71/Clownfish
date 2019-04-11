/*
 * Copyright 2019 sulzbachr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.clownfish.clownfish;

import java.util.concurrent.TimeUnit;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author rawdog71
 */
@Configuration
@ComponentScan("io.clownfish.*")
@ServletComponentScan
@EnableWebMvc
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
public class Main extends SpringBootServletInitializer implements ServletContextAware, WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     *
     * @param application
     * @return
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(new Class[]{Main.class, Initializer.class, ContainerInitializer.class});
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new FacesServlet(), "*.jsf");
        servletRegistrationBean.setName("JSF Faces Servlet");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean facesServletRegistratiton() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new FacesServlet(), new String[]{"*.xhtml"});
        servletRegistrationBean.setName("XHTML Faces Servlet");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

       // Register resource handler for CSS and JS
       registry.addResourceHandler("/resources/**").addResourceLocations("/WEB-INF/resources/")
             .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());

       // Register resource handler for images
       registry.addResourceHandler("/images/**").addResourceLocations("/WEB-INF/images/")
             .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
    }
}
