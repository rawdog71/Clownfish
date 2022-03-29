/*
 * Copyright 2022 raine.
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
package io.clownfish.clownfish.endpoints;

import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author raine
 */
@RestController
public class EndpointSitemap {
    @Autowired private PropertyUtil propertyUtil;
    @Autowired CfSiteService cfsiteService;
    
    @GetMapping(path = "/sitemap.xml")
    public void sitemap(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            String domain = propertyUtil.getPropertyValue("domain");
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            
            StringBuilder sb = new StringBuilder(1024);
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
            // Site root
            sb.append("<url>");
            sb.append("<loc>").append(domain).append("/").append("</loc>");
            sb.append("</url>");
            List<CfSite> sitemapList = cfsiteService.findBySitemap(true);
            for (CfSite sitemapItem : sitemapList) {
                sb.append("<url>");
                sb.append("<loc>").append(domain).append("/").append(sitemapItem.getName()).append("</loc>");
                sb.append("</url>");
                if ((!sitemapItem.getAliaspath().isBlank()) && (0 != sitemapItem.getAliaspath().compareToIgnoreCase(sitemapItem.getName()))) {
                    sb.append("<url>");
                    sb.append("<loc>").append(domain).append("/").append(sitemapItem.getAliaspath()).append("</loc>");
                    sb.append("</url>");
                }
            }
            sb.append("</urlset>");
            PrintWriter outwriter = response.getWriter();
            outwriter.println(sb);
        } catch (IOException ex) {
           
        }
    }
}
