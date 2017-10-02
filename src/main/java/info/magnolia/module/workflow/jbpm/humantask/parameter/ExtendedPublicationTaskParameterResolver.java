/**
 * This file Copyright (c) 2014-2017 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.workflow.jbpm.humantask.parameter;

import info.magnolia.context.Context;
import info.magnolia.module.workflow.api.WorkflowConstants;
import info.magnolia.module.workflow.jbpm.humantask.HumanTask;
import info.magnolia.module.workflow.jbpm.humantask.definition.PublicationTaskDefinition;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a publication related task with needed parameters.
 * Possible assignees are defined by configuration in {@link info.magnolia.module.workflow.jbpm.humantask.definition.PublicationTaskDefinition} and content is read from the
 * {@link WorkflowConstants#VARIABLE_DATA} passed by {@link WorkItem}
 *
 * This variant of the class ressolves un-publication paremeters from the publish action dialog.
 *
 * @author Lars Fischer
 */
public class ExtendedPublicationTaskParameterResolver extends AbstractHumanTaskParameterResolver<PublicationTaskDefinition> {

    private final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(ExtendedPublicationTaskParameterResolver.class);

    private static final String HAS_EXPIRATION = "hasExpirationDate";
    private static final String DEPUBLICATION_DATE = "expirationDate";


    @Inject
    public ExtendedPublicationTaskParameterResolver(PublicationTaskDefinition definition) {
        super(definition);
    }

    @Override
    public HumanTask createTask(WorkItem workItem, KieSession kieSession) {
        HumanTask task = new HumanTask();
        setTaskParameters(task, workItem);
        setProcessData(task, workItem);

        return task;
    }

    @Override
    public void setTaskParameters(HumanTask task, WorkItem workItem) {
        String taskName = (String) workItem.getParameter(TASK_NAME);
        task.setName(taskName);
        task.setActorIds(getDefinition().getActors());
        task.setGroupIds(getDefinition().getGroups());

        Map<String, Object> content = (Map<String, Object>) workItem.getParameter(WorkflowConstants.VARIABLE_DATA);
        if (content != null) {
            task.setComment((String) content.get(Context.ATTRIBUTE_COMMENT));
            task.setRequestor((String) content.get(Context.ATTRIBUTE_REQUESTOR));

            setTaskContent(task, content);
        }
    }

    /*
     * Clean the content of complex and unused types. The task content map can only hold simple types.
     */
    private void setTaskContent(HumanTask task, final Map<String, Object> content) {
        Map<String, Object> taskContent = new HashMap<String, Object>() {{
            put(Context.ATTRIBUTE_REPOSITORY, content.get(Context.ATTRIBUTE_REPOSITORY));
            put(Context.ATTRIBUTE_PATH, content.get(Context.ATTRIBUTE_PATH));
            put(Context.ATTRIBUTE_UUID, content.get(Context.ATTRIBUTE_UUID));
            put(Context.ATTRIBUTE_RECURSIVE, content.get(Context.ATTRIBUTE_RECURSIVE));
            put(Context.ATTRIBUTE_COMMENT, content.get(Context.ATTRIBUTE_COMMENT));
            put(Context.ATTRIBUTE_REQUESTOR, content.get(Context.ATTRIBUTE_REQUESTOR));
            put(Context.ATTRIBUTE_VERSION, content.get(Context.ATTRIBUTE_VERSION));
            put(WorkflowConstants.PUBLICATION_DATE, getDate(content.get(WorkflowConstants.PUBLICATION_DATE)));
            // comment out line below for Magnolia 5.4.x
            //put(WorkflowConstants.IS_DELETION, content.get(WorkflowConstants.IS_DELETION));

            // Add un-publication parameters if provided
            Object hasExpirationDate = content.get(HAS_EXPIRATION);
            if (hasExpirationDate != null && StringUtils.equals("true", (hasExpirationDate.toString()))) {
                put(HAS_EXPIRATION, "true");
                put(DEPUBLICATION_DATE, content.get(DEPUBLICATION_DATE));
            }
        }};

        task.setContent(taskContent);
    }

    private Date getDate(Object dateString) {
        Date date = null;
        if (dateString != null) {
            String string = String.valueOf(dateString);
            try {
                date = DATE_PARSER.parse(string);
            } catch (ParseException e) {
                log.error("Could not parse publication date string.", e);
            }
        }
        return date;
    }
}
