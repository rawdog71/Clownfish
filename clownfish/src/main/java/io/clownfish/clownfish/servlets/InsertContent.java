/*
 * Copyright 2020 sulzbachr.
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
package io.clownfish.clownfish.servlets;

import com.google.gson.Gson;
import io.clownfish.clownfish.datamodels.RestContentParameter;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "InsertContent", urlPatterns = {"/InsertContent"}, asyncSupported = true)
@Component
public class InsertContent extends HttpServlet {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAttributetypeService cfattributetypeService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfListService cflistService;
    @Autowired ContentUtil contentUtil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired HibernateUtil hibernateUtil;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(InsertContent.class);

    public InsertContent() {
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("GET");
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        Gson gson = new Gson();
        RestContentParameter icp = gson.fromJson(jb.toString(), RestContentParameter.class);
        insertContent(icp, response);
    }
    
    private RestContentParameter insertContent(RestContentParameter icp, HttpServletResponse response) throws IOException {
        try {
            String apikey = icp.getApikey();
            if (apikeyutil.checkApiKey(apikey, "RestService")) {
                CfClass clazz = cfclassService.findByName(icp.getClassname());
                try {
                    CfClasscontent classcontent = cfclasscontentService.findByName(icp.getContentname());
                    response.getOutputStream().println("Duplicate Classcontent: " + icp.getContentname());
                    icp.setReturncode("NOK");
                } catch (javax.persistence.NoResultException ex) {
                    try {
                        CfClasscontent newclasscontent = new CfClasscontent();
                        newclasscontent.setName(icp.getContentname());
                        newclasscontent.setClassref(clazz);
                        CfClasscontent newclasscontent2 = cfclasscontentService.create(newclasscontent);
                        hibernateUtil.insertContent(newclasscontent);
                        response.getOutputStream().println(newclasscontent.getName());

                        List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent2.getClassref());
                        attributlist.stream().forEach((attribut) -> {
                            if (attribut.getAutoincrementor() == true) {
                                List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent2.getClassref());
                                long max = 0;
                                int last = classcontentlist2.size();
                                if (1 == last) {
                                    max = 0;
                                } else {
                                    CfClasscontent classcontent = classcontentlist2.get(last - 2);
                                    CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);        
                                    if (attributcontent.getContentInteger().longValue() > max) {
                                        max = attributcontent.getContentInteger().longValue();
                                    }
                                }
                                CfAttributcontent newcontent = new CfAttributcontent();
                                newcontent.setAttributref(attribut);    
                                newcontent.setClasscontentref(newclasscontent);
                                newcontent.setContentInteger(BigInteger.valueOf(max+1));
                                cfattributcontentService.create(newcontent);
                            } else {
                                CfAttributcontent newcontent = new CfAttributcontent();
                                newcontent.setAttributref(attribut);
                                newcontent.setClasscontentref(newclasscontent);
                                newcontent = contentUtil.setAttributValue(newcontent, icp.getAttributmap().get(attribut.getName()));

                                cfattributcontentService.create(newcontent);
                                contentUtil.indexContent();
                            }
                        });
                        hibernateUtil.updateContent(newclasscontent);
                    } catch (IOException ex1) {
                        LOGGER.error(ex1.getMessage());
                    }
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            } else {
                try {
                    response.getOutputStream().println("Wrong API KEY");
                } catch (IOException ex1) {
                    LOGGER.error(ex1.getMessage());
                }
            }
        } catch (javax.persistence.NoResultException ex) {
            try {
                response.getOutputStream().println("Class not found: " + icp.getClassname());
            } catch (IOException ex1) {
                LOGGER.error(ex1.getMessage());
            }
        }
        return icp;
    }
}
