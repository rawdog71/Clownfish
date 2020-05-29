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
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.lucene.ContentIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
    @Autowired IndexService indexService;
    @Autowired ContentIndexer contentIndexer;
    @Autowired FolderUtil folderUtil;
    @Autowired ApiKeyUtil apikeyutil;
    
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
            /*report an error*/ 
        }

        Gson gson = new Gson();
        InsertContentParameter icp = gson.fromJson(jb.toString(), InsertContentParameter.class);
        insertContent(icp, response);
        
        String json = gson.toJson(icp);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().println(json);
    }
    
    private InsertContentParameter insertContent(InsertContentParameter icp, HttpServletResponse response) {
        try {
            String apikey = icp.getApikey();
            if (apikeyutil.checkApiKey(apikey, "InsertContent")) {
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
                        CfClasscontent newclasscontent2 = cfclasscontentService.create(newclasscontent);
                        response.getOutputStream().println(newclasscontent.getName());

                        List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent2.getClassref());
                        attributlist.stream().forEach((attribut) -> {
                            if (attribut.getAutoincrementor() == true) {
                                List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent2.getClassref());
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
                                indexContent();
                            }
                        });
                    } catch (IOException ex1) {
                        java.util.logging.Logger.getLogger(InsertContent.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(InsertContent.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    response.getOutputStream().println("Wrong API KEY");
                } catch (IOException ex1) {
                    java.util.logging.Logger.getLogger(InsertContent.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } catch (javax.persistence.NoResultException ex) {
            try {
                response.getOutputStream().println("Class not found: " + icp.getClassname());
            } catch (IOException ex1) {
                java.util.logging.Logger.getLogger(InsertContent.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return icp;
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
            case "datetime":
                Date datum;
                DateTime dt = new DateTime();
                DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yyyy").withZone(DateTimeZone.forID("Europe/Berlin"));
                try {
                    datum = dt.parse(editContent, fmt).toDate();
                    selectedAttribut.setContentDate(datum);
                } catch (IllegalArgumentException ex) {
                    try {
                        fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(DateTimeZone.forID("Europe/Berlin"));
                        datum = dt.parse(editContent, fmt).toDate();
                        selectedAttribut.setContentDate(datum);
                    } catch (IllegalArgumentException ex2) {
                        fmt = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss").withZone(DateTimeZone.forID("Europe/Berlin"));
                        datum = dt.parse(editContent, fmt).toDate();
                        selectedAttribut.setContentDate(datum);
                    }
                }
                break;
            case "media":
                if (null != editContent) {
                    try {
                        CfAsset asset = cfassetService.findByName(editContent);
                        selectedAttribut.setContentInteger(BigInteger.valueOf(asset.getId()));
                    } catch (Exception ex) {
                        selectedAttribut.setContentInteger(null);
                        logger.error("INSERTCONTENT: Media " + editContent + " not found!");
                    }
                } else {
                    selectedAttribut.setContentInteger(null);
                }
                break;
            case "classref":
                CfList list_ref = cflistService.findById(Long.parseLong(editContent));
                selectedAttribut.setClasscontentlistref(list_ref);
                break;   
        }
        selectedAttribut.setIndexed(false);
        return selectedAttribut;
    }
    
    private void indexContent() {
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
    }
    
}
