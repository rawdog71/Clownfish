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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.beans.JsonFormParameter;
import io.clownfish.clownfish.beans.JsonSAPFormParameter;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.jdbc.DatatableCondition;
import io.clownfish.clownfish.jdbc.DatatableDeleteProperties;
import io.clownfish.clownfish.jdbc.DatatableDeleteValue;
import io.clownfish.clownfish.jdbc.DatatableNewProperties;
import io.clownfish.clownfish.jdbc.DatatableNewValue;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.DatatableUpdateProperties;
import io.clownfish.clownfish.mail.EmailProperties;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.models.RfcFunctionParam;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sulzbachr
 */
@Component
@Accessors(chain = true)
public class ClownfishUtil {
    private @Getter @Setter String version;
    private @Getter @Setter String versionMojarra;
    private @Getter @Setter String versionTomcat;
    /*
        getParametermap
        Übergibt die POST Parameter in eine Hashmap um
    */
    public Map getParametermap(List<JsonFormParameter> postmap) {
        Map parametermap = new HashMap<>();
        if (postmap != null) {
            postmap.stream().forEach((jfp) -> {
                parametermap.put(jfp.getName(), (String)jfp.getValue());
            });
        }
        return parametermap;
    }
    
    public static List<JsonFormParameter> getJsonFormParameterList(Map parameterlist) {
        List<JsonFormParameter> jsonlist = new ArrayList<>();
        if (null != parameterlist) {
            for (Object param : parameterlist.keySet()) {
                JsonFormParameter jsfp = new JsonFormParameter();
                jsfp.setName((String) param);
                jsfp.setValue((String)parameterlist.get(param));
                jsonlist.add(jsfp);
            }
        }
        return jsonlist;
    }
    
    public static List<JsonSAPFormParameter> getJsonSAPFormParameterList(Map parameterlist) {
        List<JsonSAPFormParameter> jsonlist = new ArrayList<>();
        if (null != parameterlist) {
            for (Object param : parameterlist.keySet()) {
                JsonSAPFormParameter jsfp = new JsonSAPFormParameter();
                jsfp.setName((String) param);
                jsfp.setValue(parameterlist.get(param));
                jsonlist.add(jsfp);
            }
        }
        return jsonlist;
    }
    
    /*
        getSaprfcfunctionparamMap
    */
    public HashMap<String, List> getSaprfcfunctionparamMap(List<CfSitesaprfc> sitesaprfclist, RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface) {
        HashMap<String, List> saprfcfunctionparamMap = new HashMap<>();
        sitesaprfclist.stream().forEach((knsitesaprfc) -> {
            List<RfcFunctionParam> rfcfunctionparamlist = new ArrayList<>();
            rfcfunctionparamlist.addAll(rfc_get_function_interface.getRfcFunctionsParamList(knsitesaprfc.getCfSitesaprfcPK().getRfcfunction(), null));
            saprfcfunctionparamMap.put(knsitesaprfc.getCfSitesaprfcPK().getRfcfunction(), rfcfunctionparamlist);
        });
        return saprfcfunctionparamMap;
    }
    
    /*
        getDatatableproperties
        Setzt die Properties für ein DB READ Aufruf
    */
    public HashMap<String, DatatableProperties> getDatatableproperties(List<JsonFormParameter> postmap) {
        HashMap<String, DatatableProperties> datatableproperties = new HashMap<>();
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                // Datenbank READ Parameter
                if (jfp.getName().compareToIgnoreCase("db$table") == 0) {
                    if (null == datatableproperties.get((String)jfp.getValue())) {
                        DatatableProperties dtp = new DatatableProperties();
                        dtp.setTablename((String)jfp.getValue());
                        datatableproperties.put((String)jfp.getValue(), dtp);
                    }
                }
                if (jfp.getName().startsWith("db$table$")) {
                    String rest = jfp.getName().substring(9);
                    String[] values = rest.split("\\$");
                    if (values[1].compareToIgnoreCase("orderby") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setOrderby((String)jfp.getValue());
                    }
                    if (values[1].compareToIgnoreCase("orderdir") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setOrderdir((String)jfp.getValue());
                    }
                    if (values[1].compareToIgnoreCase("pagination") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setPagination(Integer.parseInt((String)jfp.getValue()));
                    }
                    if (values[1].compareToIgnoreCase("page") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setPage(Integer.parseInt((String)jfp.getValue()));
                    }
                    if (values[1].compareToIgnoreCase("groupbycount") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setGroupbycount((String)jfp.getValue());
                    }
                    if (values[1].compareToIgnoreCase("groupby") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).getGroupbylist().add((String)jfp.getValue());
                    }
                    if (values[1].compareToIgnoreCase("condition") == 0) {
                        DatatableCondition dtc = new DatatableCondition();
                        dtc.setField(values[2]);
                        dtc.setOperand(values[3]);
                        dtc.setValue((String)jfp.getValue());
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).getConditionlist().add(dtc);
                    }
                }
            }
        }
        return datatableproperties;
    }
    
    /*
        getDatatablenewproperties
        Setzt die Properties für ein DB INSERT Aufruf
    */
    public HashMap<String, DatatableNewProperties> getDatatablenewproperties(List<JsonFormParameter> postmap) {  
        HashMap<String, DatatableNewProperties> datatablenewproperties = new HashMap<>();
        if (postmap != null) {
            postmap.stream().map((jfp) -> {
                // Datenbank INSERT Parameter
                if (jfp.getName().compareToIgnoreCase("db$tablenew") == 0) {
                    DatatableNewProperties dtnp = new DatatableNewProperties();
                    dtnp.setTablename((String)jfp.getValue());
                    datatablenewproperties.put((String)jfp.getValue(), dtnp);
                }
                return jfp;
            }).filter((jfp) -> (jfp.getName().startsWith("db$tablenew$"))).forEach((jfp) -> {
                String rest = jfp.getName().substring(12);
                String[] values = rest.split("\\$");
                DatatableNewValue dtnv = new DatatableNewValue();
                dtnv.setField(values[1]);
                dtnv.setValue((String)jfp.getValue());
                datatablenewproperties.get(values[0]).getValuelist().add(dtnv);
            });
        }
        return datatablenewproperties;
    }
    
    /*
        getDatatabledeleteproperties
        Setzt die Properties für ein DB DELETE Aufruf
    */
    public HashMap<String, DatatableDeleteProperties> getDatatabledeleteproperties(List<JsonFormParameter> postmap) {  
        HashMap<String, DatatableDeleteProperties> datatabledeleteproperties = new HashMap<>();
        if (postmap != null) {
            postmap.stream().map((jfp) -> {
                // Datenbank DELETE Parameter
                if (jfp.getName().compareToIgnoreCase("db$tabledelete") == 0) {
                    DatatableDeleteProperties dtdp = new DatatableDeleteProperties();
                    dtdp.setTablename((String)jfp.getValue());
                    datatabledeleteproperties.put((String)jfp.getValue(), dtdp);
                }
                return jfp;
            }).filter((jfp) -> (jfp.getName().startsWith("db$tabledelete$"))).forEach((jfp) -> {
                String rest = jfp.getName().substring(15);
                String[] values = rest.split("\\$");
                DatatableDeleteValue dtdv = new DatatableDeleteValue();
                dtdv.setField(values[1]);
                dtdv.setValue((String)jfp.getValue());
                datatabledeleteproperties.get(values[0]).getValuelist().add(dtdv);
            });
        }
        return datatabledeleteproperties;
    }
    
    /*
        getDatatableupdateproperties
        Setzt die Properties für ein DB UPDATE Aufruf
    */
    public HashMap<String, DatatableUpdateProperties> getDatatableupdateproperties(List<JsonFormParameter> postmap) {  
        HashMap<String, DatatableUpdateProperties> datatableupdateproperties = new HashMap<>();
        if (postmap != null) {
            postmap.stream().map((jfp) -> {
                // Datenbank UPDATE Parameter
                if (jfp.getName().compareToIgnoreCase("db$tableupdate") == 0) {
                    DatatableUpdateProperties dtup = new DatatableUpdateProperties();
                    dtup.setTablename((String)jfp.getValue());
                    datatableupdateproperties.put((String)jfp.getValue(), dtup);
                }
                return jfp;
            }).filter((jfp) -> (jfp.getName().startsWith("db$tableupdate$"))).forEach((jfp) -> {
                String rest = jfp.getName().substring(15);
                String[] values = rest.split("\\$");
                if (values[1].compareToIgnoreCase("condition") == 0) {
                    DatatableCondition dtc = new DatatableCondition();
                    dtc.setField(values[2]);
                    dtc.setOperand(values[3]);
                    dtc.setValue((String)jfp.getValue());
                    datatableupdateproperties.get(values[0]).getConditionlist().add(dtc);
                } else {
                    DatatableNewValue dtnv = new DatatableNewValue();
                    dtnv.setField(values[1]);
                    dtnv.setValue((String)jfp.getValue());
                    datatableupdateproperties.get(values[0]).getValuelist().add(dtnv);
                }
            });
        }
        return datatableupdateproperties;
    }
                
    /*
        getEmailproperties
        Setzt die Properties für ein EMAIL send Aufruf
    */
    public EmailProperties getEmailproperties(List<JsonFormParameter> postmap) {  
        EmailProperties emailproperties = null;
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                // EMAIL Parameter
                if (jfp.getName().compareToIgnoreCase("email$to") == 0) {
                    if (null == emailproperties) {
                        emailproperties = new EmailProperties();
                    }
                    emailproperties.setSendto((String)jfp.getValue());
                }
                if (jfp.getName().compareToIgnoreCase("email$subject") == 0) {
                    if (null == emailproperties) {
                        emailproperties = new EmailProperties();
                    }
                    emailproperties.setSubject((String)jfp.getValue());
                }
                if (jfp.getName().compareToIgnoreCase("email$body") == 0) {
                    if (null == emailproperties) {
                        emailproperties = new EmailProperties();
                    }
                    emailproperties.setBody((String)jfp.getValue());
                }
            }
        }
        return emailproperties;
    }

    public void addUrlParams(Map parametermap, List urlParams) {
        if ((null != urlParams) && (!urlParams.isEmpty())) {
            int counter = 0;
            String key = "";
            String value = "";
            for (Object urlparam : urlParams) {
                counter++;
                if (counter % 2 == 0) {
                    value = (String) (urlparam);
                    parametermap.put(key, value);
                } else {
                    key = (String) (urlparam);
                }
            }
        }
    }
    
    public static List<String> toList(String[] array) {
        if (null == array) {
           return new ArrayList(0);
        } else {
           int size = array.length;
           List<String> list = new ArrayList(size);
           for(int i = 0; i < size; i++) {
              list.add(array[i]);
           }
           return list;
        }
    }
    
    public static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static String classpathDelim()
    {
        return isWindows() ? ";" : ":";
    }
    
    public static boolean getBoolean(String booleanfield, boolean defaultvalue) {
        boolean value;
        if (null != booleanfield) {
            try {
                value = (0 == booleanfield.compareToIgnoreCase("on")) || (0 == booleanfield.compareToIgnoreCase("true"));
            } catch (NumberFormatException nex) {
                value = defaultvalue;
            }
        } else {
            value = defaultvalue;
        }
        return value;
    }
    
    public static HashMap<String, String> getHashmap(Map map) {
        HashMap<String, String> hm = new HashMap<>();
        map.keySet().stream().forEach((key) -> {
            hm.put((String)key, (String)map.get(key));
        });
        return hm;
    }
}
