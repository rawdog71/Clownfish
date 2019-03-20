package io.clownfish.clownfish;

import io.clownfish.clownfish.beans.LoginBean;
import io.clownfish.clownfish.beans.StylesheetList;
import io.clownfish.clownfish.beans.TemplateList;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author rawdog71
 */
@Configuration
@ComponentScan("io.clownfish.*")
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
public class Main extends SpringBootServletInitializer implements ServletContextAware {

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
    public ServletRegistrationBean facesServletRegistraiton() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new FacesServlet(), new String[]{"*.xhtml"});
        servletRegistrationBean.setName("XHTML Faces Servlet");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }
    
    @Bean
    @Scope(WebApplicationContext.SCOPE_SESSION)
    public LoginBean loginbean() {
        return new LoginBean();
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());
    }
}
