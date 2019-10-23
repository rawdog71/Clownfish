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

import io.clownfish.clownfish.Clownfish;
import io.clownfish.clownfish.dbentities.CfQuartz;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.serviceinterface.CfQuartzService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
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
    
    private Clownfish clownfish;
    
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
    private @Getter @Setter int hoursType;
    private @Getter @Setter int everyhour;
    private @Getter @Setter int startinghour;
    private @Getter @Setter int startingathour;
    private @Getter @Setter int endingathour;
    private @Getter @Setter List<Integer> hourslist1 = null;
    private @Getter @Setter List<Integer> hourslist2 = null;
    private String[] selectedHours;
    
    private @Getter @Setter String dayOfMonthPart;
    private @Getter @Setter String dayOfWeekPart;
    private @Getter @Setter int daysType;
    private @Getter @Setter int everyday;
    private @Getter @Setter int startingweekday;
    private @Getter @Setter int startingday;
    private @Getter @Setter int lastweekday;
    private @Getter @Setter int nearestweekday;
    private @Getter @Setter int daysbeforeend;
    private @Getter @Setter int factorday;
    private @Getter @Setter int factorweekday;
    private @Getter @Setter List<Integer> dayslist1 = null;
    private @Getter @Setter List<Integer> factorlist = null;
    private @Getter @Setter LinkedHashMap<String, Integer> weekdaylist;
    private String[] selectedWeekdays;
    private String[] selectedDays;
    
    private @Getter @Setter String monthsPart;
    private @Getter @Setter int monthType;
    private @Getter @Setter int everymonth;
    private @Getter @Setter int startingmonth;
    private @Getter @Setter int startingatmonth;
    private @Getter @Setter int endingatmonth;
    private @Getter @Setter List<Integer> monthslist1 = null;
    private @Getter @Setter List<Integer> monthslist2 = null;
    private String[] selectedMonths;
    
    private @Getter @Setter LinkedHashMap<String, Integer> monthlist;
    
    private @Getter @Setter String yearsPart;
    private @Getter @Setter int yearType;
    private @Getter @Setter int everyyear;
    private @Getter @Setter int startingyear;
    private @Getter @Setter int startingatyear;
    private @Getter @Setter int endingatyear;
    private @Getter @Setter List<Integer> yearlist1 = null;
    private @Getter @Setter List<Integer> yearlist2 = null;
    private int currentYear;
    private int diffYear;
    private String[] selectedYears;
    
    private @Getter @Setter String jobPreview;
    
    final transient Logger logger = LoggerFactory.getLogger(QuartzList.class);

    public QuartzList() {
    }
    
    @PostConstruct
    public void init() {
        monthlist = new LinkedHashMap<>();
        monthlist.put("JAN", 1);
        monthlist.put("FEB", 2);
        monthlist.put("MAR", 3);
        monthlist.put("APR", 4);
        monthlist.put("MAY", 5);
        monthlist.put("JUN", 6);
        monthlist.put("JUL", 7);
        monthlist.put("AUG", 8);
        monthlist.put("SEP", 9);
        monthlist.put("OCT", 10);
        monthlist.put("NOV", 11);
        monthlist.put("DEC", 12);
        
        weekdaylist = new LinkedHashMap<>();
        weekdaylist.put("SUN", 1);
        weekdaylist.put("MON", 2);
        weekdaylist.put("TUE", 3);
        weekdaylist.put("WED", 4);
        weekdaylist.put("THU", 5);
        weekdaylist.put("FRI", 6);
        weekdaylist.put("SAT", 7);
        
        newJobButtonDisabled = false;
        quartzlist = cfquartzService.findAll();
        sitelist = cfsiteService.findAll();
        
        secondsPart = "*";
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

        minutesPart = "*";
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

        hoursPart = "*";
        hourslist1 = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            hourslist1.add(i);
        }
        hourslist2 = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            hourslist2.add(i);
        }
        everyhour = 1;
        startinghour = 0;
        startingathour = 0;
        endingathour = 0;
        
        dayOfMonthPart = "?";
        dayOfWeekPart = "*";
        dayslist1 = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            dayslist1.add(i);
        }
        factorlist = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            factorlist.add(i);
        }
        everyday = 1;
        startingday = 1;
        startingweekday = 1;
        lastweekday = 1;
        daysbeforeend = 1;
        nearestweekday = 1;
        factorday = 1;
        factorweekday = 1;
        
        monthsPart = "*";
        monthslist1 = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            monthslist1.add(i);
        }
        monthslist2 = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            monthslist2.add(i);
        }
        everymonth = 1;
        startingmonth = 1;
        startingatmonth = 1;
        endingatmonth = 1;
        
        yearsPart = "*";
        currentYear = new DateTime().getYear(); 
        diffYear = 50;
        yearlist1 = new ArrayList<>();
        for (int i = 1; i <= diffYear; i++) {
            yearlist1.add(i);
        }
        yearlist2 = new ArrayList<>();
        for (int i = currentYear; i <= currentYear+diffYear; i++) {
            yearlist2.add(i);
        }
        everyyear = 1;
        startingyear = currentYear;
        startingatyear = currentYear;
        endingatyear = currentYear;
        
        jobPreview = combine();
    }
    
    public void setClownfish(Clownfish clownfish) {
        this.clownfish = clownfish;
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
            clownfish.init();
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
                clownfish.init();
            }
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDeleteJob(ActionEvent actionEvent) {
        if (null != selectedQuartz) {
            cfquartzService.delete(selectedQuartz);
            quartzlist = cfquartzService.findAll();
            clownfish.init();
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
    
    
    public void onTransferJob(ActionEvent actionEvent) {
        jobvalue = jobPreview;
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
                if ((null == selectedSeconds) || (0 == selectedSeconds.length)) {
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
                if ((null == selectedMinutes) || (0 == selectedMinutes.length)) {
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
    
    public void hoursValueChange() {
        switch (hoursType) {
            case 0:
                hoursPart = "*";
                break;
            case 1:
                hoursPart = startinghour + "/" + everyhour;
                break;
            case 2:
                if ((null == selectedHours) || (0 == selectedHours.length)) {
                    hoursPart = "0";
                } else {
                    hoursPart = "";
                    for (String hour : selectedHours) {
                        hoursPart += hour + ",";
                    }
                    hoursPart = hoursPart.substring(0, hoursPart.length()-1);
                }
                break;
            case 3:
                hoursPart = startingathour + "-" + endingathour;
                break;
        }
        jobPreview = combine();
    }
    
    public void everyhoursValueChange() {
        if (1 == hoursType) {
            hoursPart = startinghour + "/" + everyhour;
            jobPreview = combine();
        }
    }
    
    public void startingathourValueChange() {
        if (1 == hoursType) {
            hoursPart = startinghour + "/" + everyhour;
            jobPreview = combine();
        }
    }

    public void multihoursValueChange() {
        if (2 == hoursType) {
            if (0 == selectedHours.length) {
                hoursPart = "0";
                jobPreview = combine();
            } else {
                hoursPart = "";
                for (String hour : selectedHours) {
                    hoursPart += hour + ",";
                }
                hoursPart = hoursPart.substring(0, hoursPart.length()-1);
                jobPreview = combine();
            }
        }
    }
    
    public void startingHoursValueChange() {
        if (3 == hoursType) {
            hoursPart = startingathour + "-" + endingathour;
            jobPreview = combine();
        }
    }
    
    public void endingHoursValueChange() {
        if (3 == hoursType) {
            hoursPart = startingathour + "-" + endingathour;
            jobPreview = combine();
        }
    }
    
    public void daysValueChange() {
        switch (daysType) {
            case 0:
                dayOfMonthPart = "?";
                dayOfWeekPart = "*";
                break;
            case 1:
                dayOfMonthPart = "?";
                dayOfWeekPart = startingweekday + "/" + everyday;
                break;
            case 2:
                dayOfMonthPart = "?";
                dayOfWeekPart = startingday + "/" + everyday;
                break;    
            case 3:
                if ((null == selectedWeekdays) || (0 == selectedWeekdays.length)) {
                    dayOfMonthPart = "?";
                    dayOfWeekPart = "SUN";
                } else {
                    dayOfMonthPart = "?";
                    dayOfWeekPart = "";
                    for (String weekday : selectedWeekdays) {
                        dayOfWeekPart += getKeyFromValue(weekdaylist, Integer.parseInt(weekday)) + ",";
                    }
                    dayOfWeekPart = dayOfWeekPart.substring(0, dayOfWeekPart.length()-1);
                }
                break;
            case 4:
                if ((null == selectedDays) || (0 == selectedDays.length)) {
                    dayOfMonthPart = "1";
                    dayOfWeekPart = "?";
                } else {
                    dayOfMonthPart = "";
                    dayOfWeekPart = "?";
                    for (String day : selectedDays) {
                        dayOfMonthPart += day + ",";
                    }
                    dayOfMonthPart = dayOfMonthPart.substring(0, dayOfMonthPart.length()-1);
                }
                break;
            case 5:
                dayOfMonthPart = "L";
                dayOfWeekPart = "?";
                break;    
            case 6:
                dayOfMonthPart = "LW";
                dayOfWeekPart = "?";
                break;
            case 7:
                dayOfMonthPart = "?";
                dayOfWeekPart = "1L";
                break;
            case 8:
                dayOfWeekPart = "?";
                dayOfMonthPart = "L-" + daysbeforeend;
                break;
            case 9:
                dayOfWeekPart = "?";
                dayOfMonthPart = nearestweekday + "W";
                break;
            case 10:
                dayOfMonthPart = "?";
                dayOfWeekPart = factorweekday + "#" + factorday;
                break;
        }
        jobPreview = combine();
    }
    
    public void everydayValueChange() {
        if (1 == daysType) {
            dayOfMonthPart = "?";
            dayOfWeekPart = startingweekday + "/" + everyday;
            jobPreview = combine();
        }
        if (2 == daysType) {
            dayOfMonthPart = "?";
            dayOfWeekPart = startingday + "/" + everyday;
            jobPreview = combine();
        }
    }
    
    public void startingatweekdayValueChange() {
        if (1 == daysType) {
            dayOfMonthPart = "?";
            dayOfWeekPart = startingweekday + "/" + everyday;
            jobPreview = combine();
        }
    }
    
    public void startingatdayValueChange() {
        if (2 == daysType) {
            dayOfMonthPart = "?";
            dayOfWeekPart = startingday + "/" + everyday;
            jobPreview = combine();
        }
    }

    public void multiweekdaysValueChange() {
        if (3 == daysType) {
            if (0 == selectedWeekdays.length) {
                dayOfMonthPart = "?";
                dayOfWeekPart = "SUN";
                jobPreview = combine();
            } else {
                dayOfMonthPart = "?";
                dayOfWeekPart = "";
                for (String weekday : selectedWeekdays) {
                    dayOfWeekPart += getKeyFromValue(weekdaylist, Integer.parseInt(weekday)) + ",";
                }
                dayOfWeekPart = dayOfWeekPart.substring(0, dayOfWeekPart.length()-1);
                jobPreview = combine();
            }
        }
    }
    
    public void multidaysValueChange() {
        if (4 == daysType) {
            if (0 == selectedDays.length) {
                dayOfMonthPart = "1";
                dayOfWeekPart = "?";
                jobPreview = combine();
            } else {
                dayOfMonthPart = "";
                dayOfWeekPart = "?";
                for (String day : selectedDays) {
                    dayOfMonthPart += day + ",";
                }
                dayOfMonthPart = dayOfMonthPart.substring(0, dayOfMonthPart.length()-1);
                jobPreview = combine();
            }
        }
    }
    
    public void lastweekdayValueChange() {
        dayOfMonthPart = "?";
        dayOfWeekPart = lastweekday + "L";
        jobPreview = combine();
    }
    
    public void daysbeforemonthValueChange() {
        dayOfWeekPart = "?";
        dayOfMonthPart = "L-" + daysbeforeend;
        jobPreview = combine();
    }

    public void nearestweekdayValueChange() {
        dayOfWeekPart = "?";
        dayOfMonthPart = nearestweekday + "W";
        jobPreview = combine();
    }
    
    public void factordayValueChange() {
        dayOfMonthPart = "?";
        dayOfWeekPart = factorweekday + "#" + factorday;
        jobPreview = combine();
    }
    
    public void factorweekdayValueChange() {
        dayOfMonthPart = "?";
        dayOfWeekPart = factorweekday + "#" + factorday;
        jobPreview = combine();
    }
    
    public void monthsValueChange() {
        switch (monthType) {
            case 0:
                monthsPart = "*";
                break;
            case 1:
                monthsPart = startingmonth + "/" + everymonth;
                break;
            case 2:
                if ((null == selectedMonths) || (0 == selectedMonths.length)) {
                    monthsPart = "1";
                } else {
                    monthsPart = "";
                    for (String month : selectedMonths) {
                        monthsPart += getKeyFromValue(monthlist, Integer.parseInt(month)) + ",";
                    }
                    monthsPart = monthsPart.substring(0, monthsPart.length()-1);
                }
                break;
            case 3:
                monthsPart = startingatmonth + "-" + endingatmonth;
                break;
        }
        jobPreview = combine();
    }
    
    public void everymonthsValueChange() {
        if (1 == monthType) {
            monthsPart = startingmonth + "/" + everymonth;
            jobPreview = combine();
        }
    }
    
    public void startingatmonthValueChange() {
        if (1 == monthType) {
            monthsPart = startingmonth + "/" + everymonth;
            jobPreview = combine();
        }
    }

    public void multimonthsValueChange() {
        if (2 == monthType) {
            if (0 == selectedMonths.length) {
                monthsPart = "1";
                jobPreview = combine();
            } else {
                monthsPart = "";
                for (String month : selectedMonths) {
                    monthsPart += getKeyFromValue(monthlist, Integer.parseInt(month)) + ",";
                }
                monthsPart = monthsPart.substring(0, monthsPart.length()-1);
                jobPreview = combine();
            }
        }
    }
    
    public void startingMonthsValueChange() {
        if (3 == monthType) {
            monthsPart = startingatmonth + "-" + endingatmonth;
            jobPreview = combine();
        }
    }
    
    public void endingMonthsValueChange() {
        if (3 == monthType) {
            monthsPart = startingatmonth + "-" + endingatmonth;
            jobPreview = combine();
        }
    }
    
    public void yearsValueChange() {
        switch (yearType) {
            case 0:
                yearsPart = "*";
                break;
            case 1:
                yearsPart = startingyear + "/" + everyyear;
                break;
            case 2:
                if ((null == selectedYears) || (0 == selectedYears.length)) {
                    yearsPart = String.valueOf(currentYear);
                } else {
                    yearsPart = "";
                    for (String year : selectedYears) {
                        yearsPart += year + ",";
                    }
                    yearsPart = yearsPart.substring(0, yearsPart.length()-1);
                }
                break;
            case 3:
                yearsPart = startingatyear + "-" + endingatyear;
                break;
        }
        jobPreview = combine();
    }
    
    public void everyyearsValueChange() {
        if (1 == yearType) {
            yearsPart = startingyear + "/" + everyyear;
            jobPreview = combine();
        }
    }
    
    public void startingatyearValueChange() {
        if (1 == yearType) {
            yearsPart = startingyear + "/" + everyyear;
            jobPreview = combine();
        }
    }

    public void multiyearsValueChange() {
        if (2 == yearType) {
            if (0 == selectedYears.length) {
                yearsPart = String.valueOf(currentYear);
                jobPreview = combine();
            } else {
                yearsPart = "";
                for (String year : selectedYears) {
                    yearsPart += year + ",";
                }
                yearsPart = yearsPart.substring(0, yearsPart.length()-1);
                jobPreview = combine();
            }
        }
    }
    
    public void startingYearsValueChange() {
        if (3 == yearType) {
            yearsPart = startingatyear + "-" + endingatyear;
            jobPreview = combine();
        }
    }
    
    public void endingYearsValueChange() {
        if (3 == yearType) {
            yearsPart = startingatyear + "-" + endingatyear;
            jobPreview = combine();
        }
    }
    
    public String[] getSelectedSeconds() {
        return selectedSeconds;
    }

    public void setSelectedSeconds(String[] selectedSeconds) {
        this.selectedSeconds = selectedSeconds;
    }

    public String[] getSelectedMinutes() {
        return selectedMinutes;
    }

    public void setSelectedMinutes(String[] selectedMinutes) {
        this.selectedMinutes = selectedMinutes;
    }
    
    public String[] getSelectedHours() {
        return selectedHours;
    }

    public void setSelectedHours(String[] selectedHours) {
        this.selectedHours = selectedHours;
    }

    public String[] getSelectedMonths() {
        return selectedMonths;
    }

    public String[] getSelectedWeekdays() {
        return selectedWeekdays;
    }

    public void setSelectedWeekdays(String[] selectedWeekdays) {
        this.selectedWeekdays = selectedWeekdays;
    }

    public String[] getSelectedDays() {
        return selectedDays;
    }

    public void setSelectedDays(String[] selectedDays) {
        this.selectedDays = selectedDays;
    }

    public void setSelectedMonths(String[] selectedMonths) {
        this.selectedMonths = selectedMonths;
    }

    public String[] getSelectedYears() {
        return selectedYears;
    }

    public void setSelectedYears(String[] selectedYears) {
        this.selectedYears = selectedYears;
    }
    
    private String combine() {
        return secondsPart + " " + minutesPart + " " + hoursPart + " " + dayOfMonthPart + " " + monthsPart + " " + dayOfWeekPart + " " + yearsPart;
    }
    
    private String getKeyFromValue(LinkedHashMap lhm, Integer value) {
        if (lhm.containsValue(value)) {
            for (Object entry : lhm.keySet()) {
                if (lhm.get(entry) == value) {
                    return entry.toString();
                }
            }
        } else {
            return null;
        }
        return null;
    }
}
