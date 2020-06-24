package org.akaza.openclinica.config;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.dao.extract.OcQrtzTriggersDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.service.SchedulerUtilService;
import core.org.akaza.openclinica.service.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@DependsOn("liquibase")
public class ScheduledJobConfig {
    protected static final Logger logger = LoggerFactory.getLogger(ScheduledJobConfig.class);

    @Autowired
    private StudyDao studyDao;
    @Autowired
    private DatasetDAO datasetDao;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SchedulerUtilService schedulerUtilService;
    @Autowired
    private OcQrtzTriggersDAO ocQrtzTriggersDAO;

    @PostConstruct
    public void createSchemaSpecificSchedulers() throws BeansException {
        logger.info("In postProcessBeanFactory");

        Set<String> studyOcOids = studyDao.findAll().stream()
                .map(study -> study.getSchemaName())
                .collect(Collectors.toSet());

        for (String schema: studyOcOids) {
            CoreResources.setRequestSchema(schema);
            if (ocQrtzTriggersDAO.findAll().size() != 0) {
                CoreResources.setRequestSchema("public");
                logger.info("Found triggers in " + schema);
                schedulerUtilService.createSchedulerFactoryBean(applicationContext, schema);
            }
        }
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
