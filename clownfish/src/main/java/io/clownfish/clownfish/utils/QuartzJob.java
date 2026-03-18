package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.beans.QuartzList;
import io.clownfish.clownfish.dbentities.CfQuartz;
import io.clownfish.clownfish.service.TemplateExecutionService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuartzJob implements Job {

    @Autowired QuartzList quartzlist;
    @Autowired TemplateExecutionService templateExecutionService;
    
    private final transient Logger LOGGER = LoggerFactory.getLogger(QuartzJob.class);

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        List<CfQuartz> joblist = quartzlist.getQuartzlist();
        joblist.stream()
                .filter(quartz -> quartz.getName().compareToIgnoreCase(jec.getJobDetail().getKey().getName()) == 0)
                .forEach(quartz -> {
                    LOGGER.info("JOB CLOWNFISH CMS: {} - {}", quartz.getName(), quartz.getSiteRef());
                    
                    Map<String, String> paramMap = makeParamMap(quartz.getParameter());
                    // Den neuen Service aufrufen
                    templateExecutionService.executeTemplate(quartz.getSiteRef().longValue(), paramMap, quartz.getName());
                });
    }

    private Map<String, String> makeParamMap(String params) {
        Map<String, String> paramMap = new HashMap<>();
        if (params != null && !params.isBlank()) {
            for (String param : params.split("&")) {
                String[] val = param.split("=");
                if(val.length == 2) {
                    paramMap.put(val[0], val[1]);
                }
            }
        }
        return paramMap;
    }
}