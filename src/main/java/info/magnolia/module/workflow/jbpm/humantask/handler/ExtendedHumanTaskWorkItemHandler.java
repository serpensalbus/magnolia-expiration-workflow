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
package info.magnolia.module.workflow.jbpm.humantask.handler;

import info.magnolia.context.Context;
import info.magnolia.context.SimpleContext;
import info.magnolia.module.workflow.WorkflowManagerProvider;
import info.magnolia.module.workflow.api.WorkflowConstants;
import info.magnolia.module.workflow.commands.PublicationWorkflowCommand;
import info.magnolia.module.workflow.jbpm.humantask.HumanTask;
import info.magnolia.module.workflow.jbpm.humantask.definition.HumanTaskDefinition;
import info.magnolia.module.workflow.jbpm.humantask.parameter.HumanTaskParameterResolver;
import info.magnolia.module.workflow.registry.WorkflowDefinitionRegistry;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WorkItemHandler} taking care of creating a {@link HumanTask}'s.
 *
 * This variant schedules an additonal task for un-publication if parameters are provided.
 *
 * @author Lars Fischer
 */
public class ExtendedHumanTaskWorkItemHandler implements WorkItemHandler {

    private static final Logger log = LoggerFactory.getLogger(ExtendedHumanTaskWorkItemHandler.class);

    private static final String TASK_NAME = "TaskName";
    private static final String SWIMLANE_ACTOR_ID = "SwimlaneActorId";

    private static final String HAS_EXPIRATION = "hasExpirationDate";
    private static final String DEPUBLICATION_DATE = "expirationDate";

    private TaskDefinitionRegistry taskDefinitionRegistry;
    private ComponentProvider componentProvider;
    private TasksManager tasksManager;
    private KieSession kieSession;
    private WorkflowDefinitionRegistry workflowRegistry;
    private WorkflowManagerProvider workflowManagerProvider;

    @Inject
    public ExtendedHumanTaskWorkItemHandler(TaskDefinitionRegistry taskDefinitionRegistry, ComponentProvider componentProvider, TasksManager tasksManager, KieSession kieSession, WorkflowDefinitionRegistry workflowRegistry, WorkflowManagerProvider workflowManagerProvider) {
        this.taskDefinitionRegistry = taskDefinitionRegistry;
        this.componentProvider = componentProvider;
        this.tasksManager = tasksManager;
        this.kieSession = kieSession;
        this.workflowRegistry = workflowRegistry;
        this.workflowManagerProvider = workflowManagerProvider;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        try {
            HumanTaskParameterResolver parameterResolver = getParameterResolver((String) workItem.getParameter(TASK_NAME));
            HumanTask task = parameterResolver.createTask(workItem, kieSession);

            for (Object o : task.getContent().entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                log.info(pair.getKey() + " = " + pair.getValue());
            }

            tasksManager.addTask(task);

            if (isAutoClaim(workItem)) {
                tasksManager.claim(task.getId(), (String) workItem.getParameter(SWIMLANE_ACTOR_ID));
            }

            // if criteria are met, create an additional task for un-publication
            createUnpublicationTask(task);

        } catch (Exception e) {
            log.error("Could not retrieve task definition.", e);
        }
    }

    /**
     * Creates a task for scheduled un-publication.
     * @param task Task
     */
    private void createUnpublicationTask(Task task) {
        if (task != null && task.getContent() != null) {
            // the name of the task has to reflect the wish to unpublish
            if (!StringUtils.contains(task.getName(), "unpub")) {

                Object hasExpirationDate = task.getContent().get(HAS_EXPIRATION);
                Object depublicationDate = task.getContent().get(DEPUBLICATION_DATE);

                if (hasExpirationDate != null && org.apache.commons.lang3.StringUtils.equals("true", (hasExpirationDate.toString())) && depublicationDate != null) {
                    log.debug("The task meets all criteria for unpublishing.");

                    task.getContent().put(WorkflowConstants.PUBLICATION_DATE, depublicationDate);

                    PublicationWorkflowCommand command = new PublicationWorkflowCommand(workflowManagerProvider);
                    command.setCommandName("deactivate");
                    command.setParameterMapName("mgnlData");
                    String repository = StringUtils.defaultString(task.getContent().get(Context.ATTRIBUTE_REPOSITORY).toString(), "website");
                    command.setRepository(repository);
                    command.setWorkflow("reviewForUnpublication");
                    command.setPath(task.getPath());

                    try {
                        command.execute(new SimpleContext(task.getContent()));
                    } catch (Exception e) {
                        log.error("Problem while automatically creating an unpublication workflow/task: ", e);
                    }
                }
            }
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        log.warn("Human task aborted.");
    }

    private boolean isAutoClaim(WorkItem workItem) {
        String swimlaneUser = (String) workItem.getParameter(SWIMLANE_ACTOR_ID);

        return StringUtils.isNotEmpty(swimlaneUser);

    }

    private HumanTaskParameterResolver getParameterResolver(String taskName, Object... objects) throws RegistrationException {
        HumanTaskDefinition definition = (HumanTaskDefinition) taskDefinitionRegistry.get(taskName);

        return componentProvider.newInstance(definition.getParameterResolver(), definition, objects);
    }


}
