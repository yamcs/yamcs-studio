package org.yamcs.studio.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.model.Catalogue;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.utils.TimeEncoding;

public class YamcsPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core";
    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());

    private static YamcsPlugin plugin;

    // Reset for every application restart
    private static AtomicInteger cmdClientId = new AtomicInteger(1);

    private ConnectionManager connectionManager;
    private Map<Class<? extends Catalogue>, Catalogue> catalogues = new HashMap<>();

    private Set<MDBContextListener> mdbListeners = new HashSet<>();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        log.info("Yamcs Studio v." + getBundle().getVersion().toString());
        TimeEncoding.setUp();

        catalogues.put(TimeCatalogue.class, new TimeCatalogue());
        catalogues.put(ParameterCatalogue.class, new ParameterCatalogue());
        catalogues.put(ManagementCatalogue.class, new ManagementCatalogue());
        catalogues.put(CommandingCatalogue.class, new CommandingCatalogue());

        connectionManager = new ConnectionManager();
        catalogues.values().forEach(c -> connectionManager.addStudioConnectionListener(c));
    }

    public static int getNextCommandClientId() {
        return cmdClientId.incrementAndGet();
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @SuppressWarnings("unchecked")
    public <T extends Catalogue> T getCatalogue(Class<T> clazz) {
        return (T) catalogues.get(clazz);
    }

    public String getOrigin() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    public void addMdbListener(MDBContextListener listener) {
        mdbListeners.add(listener);
    }

    public void removeMdbListener(MDBContextListener listener) {
        mdbListeners.remove(listener);
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
