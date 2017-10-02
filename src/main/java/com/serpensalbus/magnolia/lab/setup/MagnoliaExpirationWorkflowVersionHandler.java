package com.serpensalbus.magnolia.lab.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.CheckOrCreatePropertyTask;
import info.magnolia.module.delta.ReplaceIfExistsTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is optional and lets you manage the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 *
 * @see info.magnolia.module.DefaultModuleVersionHandler
 * @see info.magnolia.module.ModuleVersionHandler
 * @see info.magnolia.module.delta.Task
 * 
 * @author Lars Fischer
 */
public class MagnoliaExpirationWorkflowVersionHandler extends DefaultModuleVersionHandler {

    private final Task setupUnpublishDialog = new ReplaceIfExistsTask("Replace unpublish dialog", "", "The node for the unpublish dialog could not be found.", RepositoryConstants.CONFIG, "/modules/workflow/dialogs/unpublish", "/mgnl-bootstrap/magnolia-expiration-workflow/config/dialogs/config.modules.workflow.dialogs.unpublish.xml");
    private final Task addUnpublicationField = new BootstrapConditionally("Add unpublication field to publish dialog", "", "/mgnl-bootstrap/magnolia-expiration-workflow/config/dialogs/config.modules.workflow.dialogs.publish.form.tabs.comment.fields.expirationDate.xml");
    private final Task addUnpublicationFlag = new BootstrapConditionally("Add unpublication flag to publish dialog", "", "/mgnl-bootstrap/magnolia-expiration-workflow/config/dialogs/config.modules.workflow.dialogs.publish.form.tabs.comment.fields.hasExpirationDate.xml");
    private final Task addUnpublicationType = new BootstrapConditionally("Add unpublication form type to schedulePublication action", "", "/mgnl-bootstrap/magnolia-expiration-workflow/config/generic/config.modules.workflow.generic.actions.schedulePublication.formTypes.expirationDate.xml");
    private final Task addUnpublicationFlagType = new BootstrapConditionally("Add unpublication flag type to schedulePublication action", "", "/mgnl-bootstrap/magnolia-expiration-workflow/config/generic/config.modules.workflow.generic.actions.schedulePublication.formTypes.hasExpirationDate.xml");
    private final Task changePublishTaskParameterResolverClass = new CheckAndModifyPropertyValueTask("Change property resolver class for publish task", "", RepositoryConstants.CONFIG, "/modules/workflow-jbpm/tasks/publish", "parameterResolver", "info.magnolia.module.workflow.jbpm.humantask.parameter.PublicationTaskParameterResolver", "info.magnolia.module.workflow.jbpm.humantask.parameter.ExtendedPublicationTaskParameterResolver");
    private final Task addUnpublishTaskParameterResolverClass = new CheckOrCreatePropertyTask( "Add property resolver class for unpublish task", "", RepositoryConstants.CONFIG, "/modules/workflow-jbpm/tasks/unpublish", "parameterResolver",  "info.magnolia.module.workflow.jbpm.humantask.parameter.ExtendedPublicationTaskParameterResolver");
    private final Task changeWorkItemHandlerHumanTaskClass = new CheckAndModifyPropertyValueTask("Change the class of the human task in the work item handler", "", RepositoryConstants.CONFIG, "/modules/workflow-jbpm/workItemHandlers/humanTask", "class", "info.magnolia.module.workflow.jbpm.humantask.handler.definition.HumanTaskWorkItemHandlerDefinition", "info.magnolia.module.workflow.jbpm.humantask.handler.definition.ExtendedHumanTaskWorkItemHandlerDefinition");

    public MagnoliaExpirationWorkflowVersionHandler() {
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<>();
        tasks.add(setupUnpublishDialog);
        tasks.add(addUnpublicationField);
        tasks.add(addUnpublicationFlag);
        tasks.add(addUnpublicationType);
        tasks.add(addUnpublicationFlagType);
        tasks.add(changePublishTaskParameterResolverClass);
        tasks.add(addUnpublishTaskParameterResolverClass);
        tasks.add(changeWorkItemHandlerHumanTaskClass);
        return tasks;
    }

}
