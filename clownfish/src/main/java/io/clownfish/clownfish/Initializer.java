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

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.MultipartFilter;

/**
 *
 * @author sulzbachr
 */
@Configuration
public class Initializer implements ServletContextInitializer {

    /**
     * Ititialization of custom settings
     * Sets Primefaces configuartion
     * @param servletContext
     * @throws javax.servlet.ServletException
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", "true");
        servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
        servletContext.setInitParameter("primefaces.UPLOADER", "commons");
        servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");
        //servletContext.setInitParameter("primefaces.THEME", "cupertino");
    }
    
    /**
     * Initializes the file upload filter for the servlet
     * @return 
     */
    @Bean
    public FilterRegistrationBean ServletFileUploadFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new MultipartFilter());
        registration.setName("Servlet Upload Filter");
        registration.addUrlPatterns("/InsertAsset");
        registration.setOrder(0);
        registration.setDispatcherTypes(DispatcherType.FORWARD, DispatcherType.REQUEST);
        registration.setAsyncSupported(true);
        return registration;
    }
    
    /**
     * Initializes the file upload filter for the backend
     * @return 
     */
    @Bean
    public FilterRegistrationBean PrimefacesFileUploadFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new org.primefaces.webapp.filter.FileUploadFilter());
        registration.setName("PrimeFaces FileUpload Filter");
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setDispatcherTypes(DispatcherType.FORWARD, DispatcherType.REQUEST);
        registration.setAsyncSupported(true);
        return registration;
    }
    
    /**
     * Initializes the WEBDAV filter
     * @return 
     */
    /*
    @Bean
    public FilterRegistrationBean ServletWebDAVFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new MiltonFilter());
        registration.setName("WebDAV Filter");
        registration.addUrlPatterns("/webdav/*");
        registration.addInitParameter("resource.factory.class", "io.milton.http.annotated.AnnotationResourceFactory");
        registration.addInitParameter("controllerPackagesToScan", "io.clownfish.clownfish.webdav");
        registration.addInitParameter("milton.configurator", "io.clownfish.clownfish.webdav.WebDAVConfigurator");
        registration.addInitParameter("contextPath", "/webdav");
        registration.setOrder(2);
        registration.setDispatcherTypes(DispatcherType.FORWARD, DispatcherType.REQUEST);
        return registration;
    }
    */
} 
