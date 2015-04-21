package org.csstudio.utility.pvmanager.yamcs;

import java.util.logging.Logger;

import org.csstudio.autocomplete.AutoCompleteHelper;
import org.eclipse.ui.IStartup;

public class Bootstrap implements IStartup {
    private static final Logger log = Logger.getLogger(Bootstrap.class.getName());

    /**
     * TODO This is a bit of a hack to get yamcs datasources registered early on. Surely there's a
     * better way? This method will be triggered thanks to the org.eclipse.ui.startup extension
     * point.
     */
    @Override
    public void earlyStartup() {
        StringBuilder msg = new StringBuilder("Registering datasources early on: ");
        for (String prefix : AutoCompleteHelper.retrievePVManagerSupported()) {
            msg.append(prefix + "://   ");
        }
        log.info(msg.toString());
    }
}
