package info.magnolia.module.workflow.jbpm.humantask.handler.definition;

import info.magnolia.module.workflow.jbpm.humantask.handler.ExtendedHumanTaskWorkItemHandler;
import info.magnolia.module.workflow.jbpm.workitem.handler.definition.ConfiguredWorkItemHandlerDefinition;

/**
 * Definition for {@link info.magnolia.module.workflow.jbpm.humantask.handler.ExtendedHumanTaskWorkItemHandler}.
 *
 * @author Lars Fischer
 */
public class ExtendedHumanTaskWorkItemHandlerDefinition extends ConfiguredWorkItemHandlerDefinition {

    public ExtendedHumanTaskWorkItemHandlerDefinition() {
        setImplementationClass(ExtendedHumanTaskWorkItemHandler.class);
    }
}
