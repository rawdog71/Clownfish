/*
 * Copyright 2020 SulzbachR.
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
package io.clownfish.clownfish.rest;

import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.RestContentParameter;
import io.clownfish.clownfish.datamodels.RestContentParameterExt;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfContentversion;
import io.clownfish.clownfish.dbentities.CfContentversionPK;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.ClassUtil;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SulzbachR
 */
@RestController
public class RestContent {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired transient CfSitecontentService cfsitecontentService;
    @Autowired ContentUtil contentUtil;
    @Autowired ClassUtil classutil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired transient AuthTokenList authtokenlist;
    @Autowired private CfContentversionService cfcontentversionService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestContent.class);

    @PostMapping(value = "/insertcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestContentParameter restInsertContent(@RequestBody RestContentParameter icp) {
        return insertContent(icp);
    }
    
    private RestContentParameter insertContent(RestContentParameter icp) {
        try {
            String token = icp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = icp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClass clazz = cfclassService.findByName(icp.getClassname());
                    //System.out.println(clazz.isSearchrelevant());

                    CfClasscontent classcontentfind = cfclasscontentService.findByName(clazz.getName().toUpperCase() + "_" + icp.getContentname().trim().replaceAll("\\s+", "_"));
                    if (null != classcontentfind) {
                        LOGGER.warn("Duplicate Classcontent");
                        icp.setReturncode("Duplicate Classcontent");
                    } else {
                        CfClasscontent newclasscontent = new CfClasscontent();
                        newclasscontent.setName(clazz.getName().toUpperCase() + "_" + icp.getContentname().trim().replaceAll("\\s+", "_"));
                        newclasscontent.setCheckedoutby(BigInteger.valueOf(icp.getCheckedoutby()));
                        newclasscontent.setClassref(clazz);
                        CfClasscontent newclasscontent2 = cfclasscontentService.create(newclasscontent);
                        hibernateUtil.insertContent(newclasscontent);
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
                                CfAttributcontent newcontent2 = cfattributcontentService.create(newcontent);
                                icp.getAttributmap().put(attribut.getName(), newcontent2.getContentInteger().toString());
                                icp.setReturncode("OK");
                            } else {
                                CfAttributcontent newcontent = new CfAttributcontent();
                                newcontent.setAttributref(attribut);
                                newcontent.setClasscontentref(newclasscontent);
                                newcontent = contentUtil.setAttributValue(newcontent, icp.getAttributmap().get(attribut.getName()));

                                cfattributcontentService.create(newcontent);
                                if (icp.isIndexing()) {
                                    contentUtil.indexContent();
                                }
                                icp.setReturncode("OK");
                            }
                        });
                        hibernateUtil.updateContent(newclasscontent);
                    }
                } else {
                    icp.setReturncode("Wrong API KEY");
                }
            } else {
                icp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            icp.setReturncode("NoResultException");
        }
        return icp;
    }
    
    @PostMapping(value = "/deletecontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestContentParameter restDeleteContent(@RequestBody RestContentParameter ucp) {
        return deleteContent(ucp);
    }
    
    private RestContentParameter deleteContent(RestContentParameter ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClass clazz = cfclassService.findByName(ucp.getClassname());

                    CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
                    if (null != classcontent) {
                        classcontent.setScrapped(true);
                        
                        // Delete from Listcontent - consistency
                        List<CfListcontent> listcontent = cflistcontentService.findByClasscontentref(classcontent.getId());
                        for (CfListcontent lc : listcontent) {
                            cflistcontentService.delete(lc);
                            hibernateUtil.deleteRelation(cflistService.findById(lc.getCfListcontentPK().getListref()), cfclasscontentService.findById(lc.getCfListcontentPK().getClasscontentref()));
                        }

                        // Delete from Sitecontent - consistency
                        List<CfSitecontent> sitecontent = cfsitecontentService.findByClasscontentref(classcontent.getId());
                        for (CfSitecontent sc : sitecontent) {
                            cfsitecontentService.delete(sc);
                        }
                        
                        cfclasscontentService.edit(classcontent);
                        ucp.setReturncode("OK");
                        hibernateUtil.updateContent(classcontent);
                    } else {
                        ucp.setReturncode("Classcontent not found");
                    }
                } else {
                    ucp.setReturncode("Wrong API KEY");
                }
            } else {
                ucp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }
    
    @PostMapping(value = "/updatecontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestContentParameter restUpdateContent(@RequestBody RestContentParameter ucp) {
        return updateContent(ucp);
    }
    
    private RestContentParameter updateContent(RestContentParameter ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClass clazz = cfclassService.findByName(ucp.getClassname());

                    CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
                    if (null != classcontent) {
                        if ((null != ucp.getUpdatecontentname()) && (!ucp.getUpdatecontentname().isEmpty())) {
                            CfClasscontent updateclasscontent = cfclasscontentService.findByName(ucp.getUpdatecontentname().trim().replaceAll("\\s+", "_"));
                            if (null != updateclasscontent) {
                                classcontent.setName(ucp.getUpdatecontentname().trim().replaceAll("\\s+", "_"));
                                ucp.setContentname(ucp.getUpdatecontentname().trim().replaceAll("\\s+", "_"));
                                cfclasscontentService.edit(classcontent);
                            }
                        }
                        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
                        for (CfAttributcontent attributcontent : attributcontentlist) {
                            CfAttribut attribut = attributcontent.getAttributref();
                            // Check, if attribut exists in attributmap
                            if (ucp.getAttributmap().containsKey(attribut.getName())) {
                                contentUtil.setAttributValue(attributcontent, ucp.getAttributmap().get(attribut.getName()));
                                cfattributcontentService.edit(attributcontent);
                                if (ucp.isIndexing()) {
                                    contentUtil.indexContent();
                                }
                                ucp.setReturncode("OK");
                            }
                        }
                        hibernateUtil.updateContent(classcontent);
                    } else {
                        ucp.setReturncode("Classcontent not found");
                    }
                } else {
                    ucp.setReturncode("Wrong API KEY");
                }
            } else {
                ucp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }
    
    @PostMapping(value = "/destroycontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestContentParameter restDestroyContent(@RequestBody RestContentParameter ucp) {
        return destroyContent(ucp);
    }
    
    private RestContentParameter destroyContent(RestContentParameter ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClass clazz = cfclassService.findByName(ucp.getClassname());
                    if (null != clazz) {
                        CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
                        if (null != classcontent) {
                            // Delete corresponding attributcontent entries
                            List<CfAttributcontent> attributcontentlistdummy = cfattributcontentService.findByClasscontentref(classcontent);
                            for (CfAttributcontent attributcontent : attributcontentlistdummy) {
                                cfattributcontentService.delete(attributcontent);
                            }

                            // Delete corresponding listcontent entries
                            List<CfListcontent> selectedcontent = cflistcontentService.findByClasscontentref(classcontent.getId());
                            for (CfListcontent listcontent : selectedcontent) {
                                cflistcontentService.delete(listcontent);
                            }

                            // Delete corresponding keywordcontent entries
                            List<CfClasscontentkeyword> keywordcontentdummy = cfclasscontentkeywordService.findByClassContentRef(classcontent.getId());
                            for (CfClasscontentkeyword keywordcontent : keywordcontentdummy) {
                                cfclasscontentkeywordService.delete(keywordcontent);
                            }

                            // Delete corresponding sitecontent entries
                            List<CfSitecontent> sitecontentdummy = cfsitecontentService.findByClasscontentref(classcontent.getId());
                            for (CfSitecontent sitecontent : sitecontentdummy) {
                                cfsitecontentService.delete(sitecontent);
                            }

                            cfclasscontentService.delete(classcontent);
                            try {
                                hibernateUtil.deleteContent(classcontent);
                            } catch (javax.persistence.NoResultException ex) {
                                LOGGER.warn(ex.getMessage());
                            }

                            ucp.setReturncode("OK");
                        } else {
                            ucp.setReturncode("Classcontent not found");
                        }
                    } else {
                        ucp.setReturncode("Class not found");
                    }
                } else {
                    ucp.setReturncode("Wrong API KEY");
                }
            } else {
                ucp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }
    
    @PostMapping(value = "/copycontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestContentParameter restCopyContent(@RequestBody RestContentParameter ucp) {
        return copyContent(ucp);
    }
    
    private RestContentParameter copyContent(RestContentParameter ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClass selectedClass = cfclassService.findByName(ucp.getClassname());
                    CfClasscontent newclasscontent = new CfClasscontent();
                    try {
                        CfClasscontent selectedContent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
                        if (null != selectedContent) {
                            String newname = contentUtil.getUniqueName(selectedContent.getName());
                            ucp.setUpdatecontentname(newname);
                            if (newname.startsWith(selectedClass.getName().toUpperCase() + "_")) {
                                newname = newname.replaceAll("\\s+", "_");
                            } else {
                                newname = selectedClass.getName().toUpperCase() + "_" + newname.replaceAll("\\s+", "_");
                            }
                            newclasscontent.setName(newname);

                            newclasscontent.setClassref(selectedContent.getClassref());
                            cfclasscontentService.create(newclasscontent);

                            List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent.getClassref());
                            attributlist.stream().forEach((attribut) -> {
                                if (attribut.getAutoincrementor() == true) {
                                    List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent.getClassref());
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
                                    CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, selectedContent); 

                                    CfAttributcontent newcontent = new CfAttributcontent();
                                    newcontent.setAttributref(attribut);
                                    newcontent.setClasscontentref(newclasscontent);
                                    newcontent.setAssetcontentlistref(attributcontent.getAssetcontentlistref());
                                    newcontent.setClasscontentlistref(attributcontent.getClasscontentlistref());
                                    newcontent.setContentBoolean(attributcontent.getContentBoolean());
                                    newcontent.setContentDate(attributcontent.getContentDate());
                                    newcontent.setContentInteger(attributcontent.getContentInteger());
                                    newcontent.setContentReal(attributcontent.getContentReal());
                                    newcontent.setContentString(attributcontent.getContentString());
                                    newcontent.setContentText(attributcontent.getContentText());
                                    newcontent.setIndexed(attributcontent.isIndexed());
                                    newcontent.setSalt(attributcontent.getSalt());
                                    cfattributcontentService.create(newcontent);
                                }
                            });
                            hibernateUtil.insertContent(newclasscontent);
                            ucp.setReturncode("OK");
                        } else {
                            ucp.setReturncode("Clascontent not found");
                        }
                    } catch (ConstraintViolationException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                } else {
                    ucp.setReturncode("Wrong API KEY");
                }
            } else {
                ucp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }

    @PostMapping(value = "/checkoutcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestContentParameterExt restCheckoutContent(@RequestBody RestContentParameterExt ucp) {
        return checkoutContent(ucp);
    }
    
    private RestContentParameterExt checkoutContent(RestContentParameterExt ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
                    if (null != classcontent) {
                        if (0 == classcontent.getCheckedoutby().longValue()) {
                            classcontent.setCheckedoutby(BigInteger.valueOf(ucp.getUserid()));
                            ucp.setCheckedoutby(ucp.getUserid());
                            cfclasscontentService.edit(classcontent);
                            ucp.setReturncode("OK");
                        } else {
                            ucp.setReturncode("Classcontent already checkedout");
                        }
                    } else {
                        ucp.setReturncode("Classcontent not found");
                    }
                } else {
                    ucp.setReturncode("Wrong API KEY");
                }
            } else {
                ucp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }

    @PostMapping(value = "/checkincontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestContentParameterExt restCheckinContent(@RequestBody RestContentParameterExt ucp) {
        return checkinContent(ucp);
    }
    
    private RestContentParameterExt checkinContent(RestContentParameterExt ucp) {
        try {
            String token = ucp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ucp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
                    if (null != classcontent) {
                        if (ucp.getUserid() == classcontent.getCheckedoutby().longValue()) {
                            classcontent.setCheckedoutby(BigInteger.ZERO);
                            ucp.setCheckedoutby(0);
                            cfclasscontentService.edit(classcontent);
                            ucp.setReturncode("OK");
                        } else {
                            if (0 == classcontent.getCheckedoutby().longValue()) {
                                ucp.setReturncode("Classcontent not checked out");
                            } else {
                                ucp.setReturncode("Classcontent checkedout by other user");
                            }
                        }
                    } else {
                        ucp.setReturncode("Classcontent not found");
                    }
                } else {
                    ucp.setReturncode("Wrong API KEY");
                }
            } else {
                ucp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("NoResultException");
        }
        return ucp;
    }
    
    private String getContent(CfClasscontent content) {
        if (null != content) {
            List<CfContentversion> versionlist = cfcontentversionService.findByContentref(content.getId());
            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(content);
            long contentversionMax = versionlist.size();
            long selectedcontentversion = contentversionMax;
            
            if (selectedcontentversion != contentversionMax) {
                return contentUtil.getVersion(content.getId(), selectedcontentversion);
            } else {
                contentUtil.setContent(classutil.jsonExport(content, attributcontentlist));
                return contentUtil.getContent();
            }
        } else {
            return "";
        }
    }
    
    private void writeVersion(long ref, long version, byte[] content, long userid) {
        CfContentversionPK contentversionpk = new CfContentversionPK();
        contentversionpk.setContentref(ref);
        contentversionpk.setVersion(version);

        CfContentversion cfcontentversion = new CfContentversion();
        cfcontentversion.setCfContentversionPK(contentversionpk);
        cfcontentversion.setContent(content);
        cfcontentversion.setTstamp(new Date());
        cfcontentversion.setCommitedby(BigInteger.valueOf(userid));
        cfcontentversionService.create(cfcontentversion);
    }
    
    @PostMapping(value = "/commitcontent", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestContentParameterExt restCommitContent(@RequestBody RestContentParameterExt icp) {
        return commitContent(icp);
    }
    
    private RestContentParameterExt commitContent(RestContentParameterExt icp) {
        try {
            String token = icp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = icp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfClasscontent classcontent = cfclasscontentService.findByName(icp.getContentname().trim().replaceAll("\\s+", "_"));
                    if (null != classcontent) {
                        if (contentUtil.hasDifference(classcontent)) {
                            try {
                                String content = getContent(classcontent);
                                byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                                try {
                                    long maxversion = cfcontentversionService.findMaxVersion(classcontent.getId());
                                    contentUtil.setCurrentVersion(maxversion + 1);
                                    writeVersion(classcontent.getId(), contentUtil.getCurrentVersion(), output, icp.getUserid());
                                    icp.setReturncode("OK");
                                    // difference = contentUtil.hasDifference(classcontent);
                                    // contentversionMax = contentUtil.getCurrentVersion();
                                    // this.selectedcontentversion = this.contentversionMax;
                                } catch (NullPointerException npe) {
                                    writeVersion(classcontent.getId(), 1, output, icp.getUserid());
                                    contentUtil.setCurrentVersion(1);
                                    //difference = contentUtil.hasDifference(classcontent);
                                    icp.setReturncode("OK");
                                }
                            } catch (IOException ex) {
                                icp.setReturncode(ex.getMessage());
                                LOGGER.error(ex.getMessage());
                            }
                        } else {
                            icp.setReturncode("Cannot commit");
                        }
                    } else {
                        icp.setReturncode("Classcontent not found");
                    }
                } else {
                    icp.setReturncode("Wrong API KEY");
                }
            } else {
                icp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.error("NoResultException");
            icp.setReturncode("NoResultException");
        }
        return icp;
    }
    
    
}
