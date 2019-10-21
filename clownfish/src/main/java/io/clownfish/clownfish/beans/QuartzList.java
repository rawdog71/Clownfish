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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
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
    
    private @Getter @Setter String secondsPart;
    private @Getter @Setter int secondsType;
    private @Getter @Setter int everysecond;
    private @Getter @Setter int startingsecond;
    private @Getter @Setter int startingatsecond;
    private @Getter @Setter int endingatsecond;
    private @Getter @Setter List<Integer> secondslist1 = null;
    private @Getter @Setter List<Integer> secondslist2 = null;
    private String[] selectedSeconds;
    
    private @Getter @Setter String minutesPart;
    private @Getter @Setter int minutesType;
    private @Getter @Setter int everyminute;
    private @Getter @Setter int startingminute;
    private @Getter @Setter int startingatminute;
    private @Getter @Setter int endingatminute;
    private @Getter @Setter List<Integer> minuteslist1 = null;
    private @Getter @Setter List<Integer> minuteslist2 = null;
    private String[] selectedMinutes;
    
    private @Getter @Setter String hoursPart;
    private @Getter @Setter String daysPart;
    private @Getter @Setter String monthsPart;
    private @Getter @Setter String yearsPart;
    private @Getter @Setter String jobPreview;
    
    final transient Logger logger = LoggerFactory.getLogger(QuartzList.class);

    public QuartzList() {
    }
    
    @PostConstruct
    public void init() {
        newJobButtonDisabled = false;
        quartzlist = cfquartzService.findAll();
        sitelist = cfsiteService.findAll();
        secondslist1 = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            secondslist1.add(i);
        }
        secondslist2 = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            secondslist2.add(i);
        }
        everysecond = 1;
        startingsecond = 0;
        startingatsecond = 0;
        endingatsecond = 0;
        
        minuteslist1 = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            minuteslist1.add(i);
        }
        minuteslist2 = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            minuteslist2.add(i);
        }
        everyminute = 1;
        startingminute = 0;
        startingatminute = 0;
        endingatminute = 0;
        
        secondsPart = "*";
        minutesPart = "*";
        hoursPart = "*";
        daysPart = "*";
        monthsPart = "*";
        yearsPart = "*";
        
        jobPreview = combine();
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
    
    public void secondsValueChange() {
        switch (secondsType) {
            case 0:
                secondsPart = "*";
                break;
            case 1:
                secondsPart = startingsecond + "/" + everysecond;
                break;
            case 2:
                if (null == selectedSeconds) {
                    secondsPart = "0";
                } else {
                    secondsPart = "";
                    for (String second : selectedSeconds) {
                        secondsPart += second + ",";
                    }
                    secondsPart = secondsPart.substring(0, secondsPart.length()-1);
                }
                break;
            case 3:
                secondsPart = startingatsecond + "-" + endingatsecond;
                break;
        }
        jobPreview = combine();
    }
    
    public void everysecondsValueChange() {
        if (1 == secondsType) {
            secondsPart = startingsecond + "/" + everysecond;
            jobPreview = combine();
        }
    }
    
    public void startingatsecondValueChange() {
        if (1 == secondsType) {
            secondsPart = startingsecond + "/" + everysecond;
            jobPreview = combine();
        }
    }

    public void multisecondsValueChange() {
        if (2 == secondsType) {
            if (0 == selectedSeconds.length) {
                secondsPart = "0";
                jobPreview = combine();
            } else {
                secondsPart = "";
                for (String second : selectedSeconds) {
                    secondsPart += second + ",";
                }
                secondsPart = secondsPart.substring(0, secondsPart.length()-1);
                jobPreview = combine();
            }
        }
    }
    
    public void startingSecondsValueChange() {
        if (3 == secondsType) {
            secondsPart = startingatsecond + "-" + endingatsecond;
            jobPreview = combine();
        }
    }
    
    public void endingSecondsValueChange() {
        if (3 == secondsType) {
            secondsPart = startingatsecond + "-" + endingatsecond;
            jobPreview = combine();
        }
    }
    
    public void minutesValueChange() {
        switch (minutesType) {
            case 0:
                minutesPart = "*";
                break;
            case 1:
                minutesPart = startingminute + "/" + everyminute;
                break;
            case 2:
                if (null == selectedMinutes) {
                    minutesPart = "0";
                } else {
                    minutesPart = "";
                    for (String minute : selectedMinutes) {
                        minutesPart += minute + ",";
                    }
                    minutesPart = minutesPart.substring(0, minutesPart.length()-1);
                }
                break;
            case 3:
                minutesPart = startingatminute + "-" + endingatminute;
                break;
        }
        jobPreview = combine();
    }
    
    public void everyminutesValueChange() {
        if (1 == minutesType) {
            minutesPart = startingminute + "/" + everyminute;
            jobPreview = combine();
        }
    }
    
    public void startingatminuteValueChange() {
        if (1 == minutesType) {
            minutesPart = startingminute + "/" + everyminute;
            jobPreview = combine();
        }
    }

    public void multiminutesValueChange() {
        if (2 == minutesType) {
            if (0 == selectedMinutes.length) {
                minutesPart = "0";
                jobPreview = combine();
            } else {
                minutesPart = "";
                for (String minute : selectedMinutes) {
                    minutesPart += minute + ",";
                }
                minutesPart = minutesPart.substring(0, minutesPart.length()-1);
                jobPreview = combine();
            }
        }
    }
    
    public void startingMinutesValueChange() {
        if (3 == minutesType) {
            minutesPart = startingatminute + "-" + endingatminute;
            jobPreview = combine();
        }
    }
    
    public void endingMinutesValueChange() {
        if (3 == minutesType) {
            minutesPart = startingatminute + "-" + endingatminute;
            jobPreview = combine();
        }
    }
    
    public String[] getSelectedSeconds() {
        return selectedSeconds;
    }

    public void setSelectedSeconds(String[] selectedSeconds) {
        this.selectedSeconds = selectedSeconds;
    }

    private String combine() {
        return secondsPart + " " + minutesPart + " " + hoursPart + " " + daysPart + " " + monthsPart + " " + yearsPart;
    }

    public String[] getSelectedMinutes() {
        return selectedMinutes;
    }

    public void setSelectedMinutes(String[] selectedMinutes) {
        this.selectedMinutes = selectedMinutes;
    }
}
