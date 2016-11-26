package org.yamcs.studio.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.model.AlarmCatalogue;
import org.yamcs.studio.core.model.ArchiveCatalogue;
import org.yamcs.studio.core.model.Catalogue;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.LinkCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.StreamCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.utils.TimeEncoding;

public class YamcsPlugin extends Plugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core";
    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());

    private static YamcsPlugin plugin;
    private static String productIdentifier;

    private ConnectionManager connectionManager;
    private Map<Class<? extends Catalogue>, Catalogue> catalogues = new HashMap<>();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        log.info(getProductIdentifier());
        TimeEncoding.setUp();

        ManagementCatalogue managementCatalogue = new ManagementCatalogue();

        catalogues.put(TimeCatalogue.class, new TimeCatalogue());
        catalogues.put(ParameterCatalogue.class, new ParameterCatalogue());
        catalogues.put(ManagementCatalogue.class, managementCatalogue);
        catalogues.put(CommandingCatalogue.class, new CommandingCatalogue());
        catalogues.put(AlarmCatalogue.class, new AlarmCatalogue());
        catalogues.put(EventCatalogue.class, new EventCatalogue());
        catalogues.put(LinkCatalogue.class, new LinkCatalogue());
        catalogues.put(ArchiveCatalogue.class, new ArchiveCatalogue());
        catalogues.put(StreamCatalogue.class, new StreamCatalogue());

        connectionManager = new ConnectionManager();
        catalogues.values().forEach(c -> {
            managementCatalogue.addInstanceListener(c);
            connectionManager.addStudioConnectionListener(c);
        });
    }

    public static void setProductIdentifier(String productIdentifier) {
        YamcsPlugin.productIdentifier = productIdentifier;
    }

    public String getProductIdentifier() {
        if (productIdentifier == null) {
            productIdentifier = "Yamcs Studio v" + getBundle().getVersion().toString();
        }
        return productIdentifier;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @SuppressWarnings("unchecked")
    public <T extends Catalogue> T getCatalogue(Class<T> clazz) {
        return (T) catalogues.get(clazz);
    }

    public <T extends Catalogue> void registerCatalogue(T catalogue) {
        catalogues.put(catalogue.getClass(), catalogue);
        connectionManager.addStudioConnectionListener(catalogue);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            plugin = null;
            connectionManager.shutdown();
            catalogues.values().forEach(c -> c.shutdown());
        } finally {
            super.stop(context);
        }
    }

    public static YamcsPlugin getDefault() {
        return plugin;
    }
}
