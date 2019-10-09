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
package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfQuartz;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.serviceinterface.CfQuartzService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import java.math.BigInteger;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("quartzlist")
@Scope("singleton")
@Component
public class QuartzList {
    @Autowired CfQuartzService cfquartzService;
    @Autowired CfSiteService cfsiteService;
    
    private @Getter @Setter List<CfQuartz> quartzlist;
    private @Getter @Setter CfQuartz selectedQuartz;
    private @Getter @Setter List<CfQuartz> filteredQuartz;
    private transient @Getter @Setter List<CfSite> sitelist = null;
    private @Getter @Setter boolean newJobButtonDisabled;
    private @Getter @Setter String jobname;
    private @Getter @Setter String jobvalue;
    private @Getter @Setter boolean active;
    private @Getter @Setter CfSite siteref;
    private @Getter @Setter boolean deleteJobButtonDisabled;
    
    final transient Logger logger = LoggerFactory.getLogger(QuartzList.class);

    public QuartzList() {
    }
    
    @PostConstruct
    public void init() {
        quartzlist = cfquartzService.findAll();
        sitelist = cfsiteService.findAll();
        newJobButtonDisabled = false;
    }
    
    public void onSelect(SelectEvent event) {
        selectedQuartz = (CfQuartz) event.getObject();
        jobname = selectedQuartz.getName();
        jobvalue = selectedQuartz.getSchedule();
        active = selectedQuartz.isActive();
        siteref = cfsiteService.findById(selectedQuartz.getSiteRef().longValue());
        newJobButtonDisabled = true;
    }
    
    public void onCreateJob(ActionEvent actionEvent) {
        try {
            CfQuartz newquartz = new CfQuartz();
            newquartz.setName(jobname);
            newquartz.setSchedule(jobvalue);
            newquartz.setActive(active);
            newquartz.setSiteRef(BigInteger.valueOf(siteref.getId()));
            cfquartzService.create(newquartz);

            quartzlist = cfquartzService.findAll();
            //fillPropertyMap();
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onEditJob(ActionEvent actionEvent) {
        try {
            if (null != selectedQuartz) {
                selectedQuartz.setName(jobname);
                selectedQuartz.setSchedule(jobvalue);
                selectedQuartz.setActive(active);
                selectedQuartz.setSiteRef(BigInteger.valueOf(siteref.getId()));
                cfquartzService.edit(selectedQuartz);
                quartzlist = cfquartzService.findAll();
            }
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDeleteJob(ActionEvent actionEvent) {
        if (null != selectedQuartz) {
            cfquartzService.delete(selectedQuartz);
            quartzlist = cfquartzService.findAll();
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cfquartzService.findByName(jobname);
            newJobButtonDisabled = true;
        } catch (NoResultException ex) {
            newJobButtonDisabled = selectedQuartz.getName().isEmpty();
        }
    }
}
