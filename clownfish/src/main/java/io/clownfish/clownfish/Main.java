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
import io.milton.config.HttpManagerBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.ansi;
import org.fusesource.jansi.AnsiConsole;
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
import org.springframework.http.CacheControl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;

/**
 *
 * @author sulzbachr
 */
@Import({ SchedulerConfig.class })

@Configuration
@ComponentScan("io.clownfish.*")
@EnableScheduling
@EnableCaching
@ServletComponentScan
@EnableWebMvc
@EnableAutoConfiguration(exclude = {HibernateJpaAutoConfiguration.class})
public class Main extends SpringBootServletInitializer implements ServletContextAware, WebMvcConfigurer {
    final static transient Logger logger = LoggerFactory.getLogger(Main.class);
    
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
    
    /*
    @Bean
    public ServletRegistrationBean webdavRegistratiton() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new ClownfishWebdavServlet(), "/webdav/*");
        servletRegistrationBean.setName("WebDAV Servlet");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }
    */
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/GetContent").allowedOrigins("http://localhost");
            }
        };
    }
    
    @Bean
    HttpManagerBuilder httpManagerBuilder() {
        HttpManagerBuilder builder = new HttpManagerBuilder();
        builder.setRootDir(new File("/webdav/"));
        return builder;
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
        
        // Register resource handler for webdav
        //registry.addResourceHandler("webdav/**").addResourceLocations("/WEB-INF/webdavtest/");
        
        registry.setOrder(-1);
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * Checks the applications.properties file and runs the bootstrap routine when the bootstrap parameter is set to 1
     * Fetches the database (MySQL) parameters from applications.properties and runs the sql-bootstrap.sql script
     * The script creates the database user for reading/writing (user=clownfish), creates all tables and initializes some tables with data
     */
    public static void bootstrap() {
        InputStream is = null;
        try {
            Properties props = new Properties();
            String propsfile = "application.properties";
            is = new FileInputStream(propsfile);
            if (null != is) {
                props.load(is);
            }
            int bootstrap = Integer.parseInt(props.getProperty("bootstrap"));
            String dbclass = props.getProperty("app.datasource.driverClassName");
            String dburl = props.getProperty("app.datasource.urlroot");
            String dbuser = props.getProperty("app.datasource.root");
            String dbpassword = props.getProperty("app.datasource.rootpw");
            if (1 == bootstrap) {
                AnsiConsole.systemInstall();
                System.out.println(ansi().fg(GREEN));
                System.out.println("BOOTSTRAPPING I");
                System.out.println(ansi().reset());
                JDBCUtil jdbcutil = new JDBCUtil(dbclass, dburl, dbuser, dbpassword);
                ScriptRunner runner = new ScriptRunner(jdbcutil.getConnection(), true, false);
                String file = "sql-bootstrap.sql";
                runner.runScript(new BufferedReader(new FileReader(file)));
            }
        } catch (FileNotFoundException ex) {
            logger.error(ex.getMessage());
        } catch (IOException | SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
}
