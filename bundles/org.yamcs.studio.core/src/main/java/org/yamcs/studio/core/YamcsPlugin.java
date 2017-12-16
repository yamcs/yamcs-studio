package org.yamcs.studio.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.yamcs.YamcsException;
import org.yamcs.api.ws.ConnectionListener;
import org.yamcs.studio.core.client.YamcsClient;
import org.yamcs.studio.core.model.AlarmCatalogue;
import org.yamcs.studio.core.model.ArchiveCatalogue;
import org.yamcs.studio.core.model.Catalogue;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.LinkCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.security.YamcsAuthorizations;
import org.yamcs.utils.TimeEncoding;

public class YamcsPlugin extends Plugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core";

    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());

    private static YamcsPlugin plugin;

    private YamcsClient yamcsClient;
    private Set<YamcsConnectionListener> connectionListeners = new CopyOnWriteArraySet<>();
    private Map<Class<? extends Catalogue>, Catalogue> catalogues = new HashMap<>();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        TimeEncoding.setUp();

        yamcsClient = new YamcsClient(getProductString());
        yamcsClient.addConnectionListener(new CentralConnectionListener());

        ManagementCatalogue managementCatalogue = new ManagementCatalogue();
        catalogues.put(ManagementCatalogue.class, managementCatalogue);

        addYamcsConnectionListener(managementCatalogue);

        registerCatalogue(new TimeCatalogue());
        registerCatalogue(new ParameterCatalogue());
        registerCatalogue(new CommandingCatalogue());
        registerCatalogue(new AlarmCatalogue());
        registerCatalogue(new EventCatalogue());
        registerCatalogue(new LinkCatalogue());
        registerCatalogue(new ArchiveCatalogue());
    }

    public void addYamcsConnectionListener(YamcsConnectionListener listener) {
        connectionListeners.add(listener);
        if (yamcsClient.isConnected()) {
            listener.onYamcsConnected();
        }
    }

    public void removeYamcsConnectionListener(YamcsConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends Catalogue> T getCatalogue(Class<T> clazz) {
        return (T) catalogues.get(clazz);
    }

    private <T extends Catalogue> void registerCatalogue(T catalogue) {
        catalogues.put(catalogue.getClass(), catalogue);
        ManagementCatalogue managementCatalogue = getCatalogue(ManagementCatalogue.class);
        managementCatalogue.addInstanceListener(catalogue);
        addYamcsConnectionListener(catalogue);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            plugin = null;
            yamcsClient.shutdown();
            catalogues.values().forEach(c -> c.shutdown());
        } finally {
            super.stop(context);
        }
    }

    public static YamcsPlugin getDefault() {
        return plugin;
    }

    public static YamcsClient getYamcsClient() {
        return plugin.yamcsClient;
    }

    public static String getProductString() {
        String productName = Platform.getProduct().getName();
        Version productVersion = Platform.getProduct().getDefiningBundle().getVersion();
        return productName + " v" + productVersion;
    }

    private class CentralConnectionListener implements ConnectionListener {

        @Override
        public void connecting(String url) {
            // TODO Auto-generated method stub

        }

        @Override
        public void connected(String url) {
            YamcsAuthorizations.getInstance().loadAuthorizations().thenRun(() -> {
                connectionListeners.forEach(l -> l.onYamcsConnected());
            });
        }

        @Override
        public void connectionFailed(String url, YamcsException exception) {
            log.severe("Could not connect: " + exception.getMessage());
            connectionListeners.forEach(l -> l.onYamcsConnectionFailed(exception));
        }

        @Override
        public void disconnected() {
            log.fine("Notify downstream components of Studio disconnect");
            for (YamcsConnectionListener l : connectionListeners) {
                log.fine(String.format(" -> Inform %s", l.getClass().getSimpleName()));
                l.onYamcsDisconnected();
            }
        }

        @Override
        public void log(String message) {
            System.out.println("log message: " + message);
        }
    }
}
