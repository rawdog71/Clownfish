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

import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.ScriptRunner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.CacheControl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 *
 * @author sulzbachr
 */
@Import({ SchedulerConfig.class })

@Configuration
@ComponentScan("io.clownfish.*")
@EnableScheduling
@ServletComponentScan
@EnableWebMvc
@EnableAutoConfiguration(exclude = {HibernateJpaAutoConfiguration.class})
@PropertySources({
    @PropertySource("file:application.properties")
})
public class Main extends SpringBootServletInitializer implements ServletContextAware, WebMvcConfigurer {
    @Value("${bootstrap}") static int bootstrap;
    @Value("${app.datasource.root}") static String dbuser;
    @Value("${app.datasource.rootpw}") static String dbpassword;
    @Value("${app.datasource.url}") static String dburl;
    @Value("${app.datasource.driverClassName}") static String dbclass;
        
    public static void main(String[] args) {
        bootstrap();
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
       registry.addResourceHandler("resources/**").addResourceLocations("/WEB-INF/resources/")
            .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic())
            .resourceChain(true)
            .addResolver(new PathResourceResolver());

       // Register resource handler for images
       registry.addResourceHandler("images/**").addResourceLocations("/WEB-INF/images/")
            .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic())
            .resourceChain(true)
            .addResolver(new PathResourceResolver());
       registry.setOrder(-1);
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }
    
    public static void bootstrap() {
        if (1 == bootstrap) {
            try {
                JDBCUtil jdbcutil = new JDBCUtil(dbclass, dburl, dbuser, dbpassword);
                ScriptRunner runner = new ScriptRunner(jdbcutil.getConnection(), false, false);
                String file = "sql-bootstrap.sql";
                runner.runScript(new BufferedReader(new FileReader(file)));
            } catch (IOException | SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
