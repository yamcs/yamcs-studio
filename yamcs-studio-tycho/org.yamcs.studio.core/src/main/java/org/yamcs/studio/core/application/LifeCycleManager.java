package org.yamcs.studio.core.application;

import org.eclipse.e4.core.services.events.IEventBroker;

public class LifeCycleManager {

    public void postContextCreate(IEventBroker broker) {
        /*
         * broker.subscribe(UILifeCycle.APP_STARTUP_COMPLETE, evt -> { System.out.println("-0-0-0");
         * for (String s : evt.getPropertyNames()) { System.out.println("11 got all this: " + s +
         * " ---> " + evt.getProperty(s)); }
         * 
         * MApplication app = (MApplication) evt.getProperty("org.eclipse.e4.data");
         * System.out.println("11 have trims " + app.getTrimContributions()); });
         */
    }
}
