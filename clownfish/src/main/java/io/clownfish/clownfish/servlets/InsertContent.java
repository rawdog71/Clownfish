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
import io.clownfish.clownfish.datamodels.InsertContentParameter;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.lucene.ContentIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
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
    @Autowired IndexService indexService;
    @Autowired ContentIndexer contentIndexer;
    @Autowired FolderUtil folderUtil;
    
    final transient Logger logger = LoggerFactory.getLogger(GetAssetPreview.class);

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
        } catch (Exception e) {
            /*report an error*/ }

        Gson gson = new Gson();
        InsertContentParameter icp = gson.fromJson(jb.toString(), InsertContentParameter.class);
        response.getOutputStream().println(icp.getClassname());
        insertContent(icp, response);
    }
    
    private void insertContent(InsertContentParameter icp, HttpServletResponse response) {
        try {
            CfClass clazz = cfclassService.findByName(icp.getClassname());
            System.out.println(clazz.isSearchrelevant());
            
            try {
                CfClasscontent classcontent = cfclasscontentService.findByName(icp.getContentname());
                response.getOutputStream().println("Duplicate Classcontent: " + icp.getContentname());
            } catch (javax.persistence.NoResultException ex) {
                try {
                    CfClasscontent newclasscontent = new CfClasscontent();
                    newclasscontent.setName(icp.getContentname());
                    newclasscontent.setClassref(clazz);
                    cfclasscontentService.create(newclasscontent);
                    response.getOutputStream().println(newclasscontent.getName());
                    
                    List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent.getClassref());
                    attributlist.stream().forEach((attribut) -> {
                        if (attribut.getAutoincrementor() == true) {
                            List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent.getClassref());
                            long max = 0;
                            for (CfClasscontent classcontent : classcontentlist2) {
                                try {
                                    CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);
                                    if (attributcontent.getContentInteger().longValue() > max) {
                                        max = attributcontent.getContentInteger().longValue();
                                    }
                                } catch (javax.persistence.NoResultException ex2) {
                                    logger.error(ex2.getMessage());
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
                            newcontent = setAttributValue(newcontent, icp.getAttributmap().get(attribut.getName()));
                            
                            cfattributcontentService.create(newcontent);
                        }
                    });
                } catch (IOException ex1) {
                    java.util.logging.Logger.getLogger(InsertContent.class.getName()).log(Level.SEVERE, null, ex1);
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(InsertContent.class.getName()).log(Level.SEVERE, null, ex);
            }    
        } catch (javax.persistence.NoResultException ex) {
            try {
                response.getOutputStream().println("Class not found: " + icp.getClassname());
            } catch (IOException ex1) {
                java.util.logging.Logger.getLogger(InsertContent.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    private CfAttributcontent setAttributValue(CfAttributcontent selectedAttribut, String editContent) {
        switch (selectedAttribut.getAttributref().getAttributetype().getName()) {
            case "boolean":
                selectedAttribut.setContentBoolean(Boolean.valueOf(editContent));
                break;
            case "string":
                if (selectedAttribut.getAttributref().getIdentity() == true) {
                    List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(selectedAttribut.getClasscontentref().getClassref());
                    boolean found = false;
                    for (CfClasscontent classcontent : classcontentlist2) {
                        try {
                            CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(selectedAttribut.getAttributref(), classcontent);
                            if (attributcontent.getContentString().compareToIgnoreCase(editContent) == 0) {
                                found = true;
                            }
                        } catch (javax.persistence.NoResultException | NullPointerException ex) {
                            logger.error(ex.getMessage());
                        }
                    }
                    if (!found) {
                        selectedAttribut.setContentString(editContent);
                    }
                } else {
                    selectedAttribut.setContentString(editContent);
                }
                break;
            case "hashstring":
                String salt = PasswordUtil.getSalt(30);
                selectedAttribut.setContentString(PasswordUtil.generateSecurePassword(editContent, salt));
                selectedAttribut.setSalt(salt);
                break;    
            case "integer":
                selectedAttribut.setContentInteger(BigInteger.valueOf(Long.parseLong(editContent)));
                break;
            case "real":
                selectedAttribut.setContentReal(Double.parseDouble(editContent));
                break;
            case "htmltext":
                selectedAttribut.setContentText(editContent);
                break;    
            case "text":
                selectedAttribut.setContentText(editContent);
                break;
            case "markdown":
                selectedAttribut.setContentText(editContent);
                break;
            /*    
            case "datetime":
                selectedAttribut.setContentDate(editCalendar);
                break;
            case "media":
                if (null != selectedMedia) {
                    selectedAttribut.setContentInteger(BigInteger.valueOf(selectedMedia.getId()));
                } else {
                    selectedAttribut.setContentInteger(null);
                }
                break;
            case "classref":
                selectedAttribut.setClasscontentlistref(editDatalist);
                break;    
            */
        }
        selectedAttribut.setIndexed(false);
        // Index the changed content and merge the Index files
        if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getMedia_folder().isEmpty())) {
            try {
                contentIndexer.run();
                indexService.getWriter().commit();
                indexService.getWriter().forceMerge(10);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(InsertContent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return selectedAttribut;
    }
    
}
