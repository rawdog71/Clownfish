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

import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.ScriptRunner;
import io.clownfish.clownfish.servlets.ClownfishWebdavServlet;
import io.clownfish.clownfish.servlets.EmptyWebdavServlet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Arrays;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.util.DefaultPropertiesPersister;
import io.clownfish.clownfish.utils.PropertyUtil;

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
@PropertySources({
    @PropertySource("file:application.properties")
})
public class Main extends SpringBootServletInitializer implements ServletContextAware, WebMvcConfigurer {
    final static transient Logger LOGGER = LoggerFactory.getLogger(Main.class);
    @Value("${server.port:9000}")
    int serverPortHttp;
    @Value("${webdav.use:0}")
    int webdavuse;
    
    @Autowired
    AutowireCapableBeanFactory beanFactory;
    @Autowired PropertyList propertylist;
    private static PropertyUtil propertyUtil;
    
    public static void main(String[] args) {
        try {
            bootstrap();
            bootstrap_update();
            SpringApplication.run(Main.class, args);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            LOGGER.info("Check database or other Clownfish instance");
        }
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
    
    @Bean
    public ServletRegistrationBean webdavRegistratiton() {
        if (1 == webdavuse) {
            System.out.println(ansi().fg(GREEN));
            System.out.println("WEBDAV activated");
            System.out.println(ansi().reset());
            ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
            final ClownfishWebdavServlet servlet = new ClownfishWebdavServlet();
            //final WebdavServlet servlet = new WebdavServlet();
            beanFactory.autowireBean(servlet);
            servletRegistrationBean.setServlet(servlet);
            servletRegistrationBean.setUrlMappings(Arrays.asList("/webdav/*"));
            servletRegistrationBean.setName("WebDAV Servlet");
            servletRegistrationBean.setLoadOnStartup(1);
            return servletRegistrationBean;
        } else {
            ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
            final EmptyWebdavServlet servlet = new EmptyWebdavServlet();
            servletRegistrationBean.setServlet(servlet);
            servletRegistrationBean.setUrlMappings(Arrays.asList("/webdav/*"));
            servletRegistrationBean.setName("Empty Servlet");
            servletRegistrationBean.setLoadOnStartup(1);
            return servletRegistrationBean;
        }
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/GetContent").allowedOrigins("http://localhost");
            }
        };
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
        
        propertyUtil = new PropertyUtil(propertylist);
        // Register resource handler for cached_images
        registry.addResourceHandler("cache/**").addResourceLocations("file:///" + propertyUtil.getPropertyValue("folder_cache")+"/")
            .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic())
            .resourceChain(true)
            .addResolver(new PathResourceResolver());
        
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
            LOGGER.error(ex.getMessage());
        } catch (IOException | SQLException ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    /**
     * Checks the applications.properties file and runs the bootstrap routine when the bootstrap parameter is set to 1
     * Fetches the database (MySQL) parameters from applications.properties and runs the sql-bootstrap.sql script
     * The script creates the database user for reading/writing (user=clownfish), creates all tables and initializes some tables with data
     */
    public static void bootstrap_update() {
        InputStream fis = null;
        try {
            Properties props = new Properties();
            String propsfile = "application.properties";
            fis = new FileInputStream(propsfile);
            if (null != fis) {
                props.load(fis);
            }
            String dbclass = props.getProperty("app.datasource.driverClassName");
            String dburl = props.getProperty("app.datasource.url");
            String dbuser = props.getProperty("app.datasource.username");
            String dbpassword = props.getProperty("app.datasource.password");
        
            String path = new File(".").getCanonicalPath();
            File bootstrapDirectory = new File(path);
            
            File[] files = bootstrapDirectory.listFiles();
            Arrays.sort(files);
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().startsWith("bootstrap_")) {
                    InputStream is = null;
                    try {
                        Properties boot_props = new Properties();
                        is = new FileInputStream(files[i]);
                        if (null != is) {
                            boot_props.load(is);
                        }
                        int bootstrap = Integer.parseInt(boot_props.getProperty("bootstrap"));
                        String version = boot_props.getProperty("version");
                        String bootstrapfile = boot_props.getProperty("bootstrapfile");
                        if (1 == bootstrap) {
                            bootstrap = 0;
                            AnsiConsole.systemInstall();
                            System.out.println(ansi().fg(GREEN));
                            System.out.println("BOOTSTRAPPING UPDATE VERSION " + version);
                            System.out.println(ansi().reset());
                            JDBCUtil jdbcutil = new JDBCUtil(dbclass, dburl, dbuser, dbpassword);
                            
                            ScriptRunner runner = new ScriptRunner(jdbcutil.getConnection(), false, false);
                            runner.runScript(new BufferedReader(new FileReader(bootstrapfile)));

                            File f = new File(files[i].getName());
                            OutputStream out = new FileOutputStream(f);
                            boot_props.setProperty("bootstrap", String.valueOf(bootstrap));
                            DefaultPropertiesPersister p = new DefaultPropertiesPersister();
                            p.store(boot_props, out, "Bootstrap properties");
                        }
                    } catch (FileNotFoundException ex) {
                        LOGGER.error(ex.getMessage());
                    } catch (IOException | SQLException  ex) {
                        LOGGER.error(ex.getMessage());
                    } finally {
                        try {
                            if (null != is) {
                                is.close();
                            }
                        } catch (IOException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }    
    }
}
