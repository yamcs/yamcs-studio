package org.yamcs.studio.core.application;

import java.util.logging.Logger;

import org.csstudio.autocomplete.AutoCompleteHelper;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

@SuppressWarnings("restriction")
public class LifeCycleManager {

    private static final Logger log = Logger.getLogger(LifeCycleManager.class.getName());

    @PostContextCreate
    public void postContextCreate(IEventBroker broker) {
        registerAutocompleteExtensions();
        /*
         * broker.subscribe(UILifeCycle.APP_STARTUP_COMPLETE, evt -> { System.out.println("-0-0-0");
         * for (String s : evt.getPropertyNames()) { System.out.println("11 got all this: " + s +
         * " ---> " + evt.getProperty(s)); }
         * 
         * MApplication app = (MApplication) evt.getProperty("org.eclipse.e4.data");
         * System.out.println("11 have trims " + app.getTrimContributions()); });
         */
    }

    /**
     * This is a bit of a hack to get yamcs datasources registered early on. Maybe there's a better
     * way, but couldn't find it right away.
     */
    private void registerAutocompleteExtensions() {
        StringBuilder msg = new StringBuilder("Registering datasources early on: ");
        for (String prefix : AutoCompleteHelper.retrievePVManagerSupported()) {
            msg.append(prefix + "://   ");
        }
        log.info(msg.toString());
    }
}
