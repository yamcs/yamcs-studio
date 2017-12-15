package org.yamcs.studio.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.model.AlarmCatalogue;
import org.yamcs.studio.core.model.ArchiveCatalogue;
import org.yamcs.studio.core.model.Catalogue;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.ExtensionCatalogue;
import org.yamcs.studio.core.model.LinkCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.utils.TimeEncoding;

public class YamcsPlugin extends Plugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core";

    private static YamcsPlugin plugin;

    private ConnectionManager connectionManager;
    private Map<Class<? extends Catalogue>, Catalogue> catalogues = new HashMap<>();

    // Additionally, keep track of catalogues by extension type
    private Map<Integer, ExtensionCatalogue> extensionCatalogues = new HashMap<>(5);

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        TimeEncoding.setUp();

        connectionManager = new ConnectionManager();

        ManagementCatalogue managementCatalogue = new ManagementCatalogue();
        catalogues.put(ManagementCatalogue.class, managementCatalogue);

        connectionManager.addStudioConnectionListener(managementCatalogue);

        registerCatalogue(new TimeCatalogue());
        registerCatalogue(new ParameterCatalogue());
        registerCatalogue(new CommandingCatalogue());
        registerCatalogue(new AlarmCatalogue());
        registerCatalogue(new EventCatalogue());
        registerCatalogue(new LinkCatalogue());
        registerCatalogue(new ArchiveCatalogue());
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @SuppressWarnings("unchecked")
    public <T extends Catalogue> T getCatalogue(Class<T> clazz) {
        return (T) catalogues.get(clazz);
    }

    public ExtensionCatalogue getExtensionCatalogue(int extensionType) {
        return extensionCatalogues.get(extensionType);
    }

    public <T extends Catalogue> void registerCatalogue(T catalogue) {
        catalogues.put(catalogue.getClass(), catalogue);
        ManagementCatalogue managementCatalogue = getCatalogue(ManagementCatalogue.class);
        managementCatalogue.addInstanceListener(catalogue);
        connectionManager.addStudioConnectionListener(catalogue);
    }

    /**
     * Hook to register a catalogue that will be provided with incoming websocket data of the specified extension type.
     */
    public <T extends ExtensionCatalogue> void registerExtensionCatalogue(int extensionType, T catalogue) {
        catalogues.put(catalogue.getClass(), catalogue);
        extensionCatalogues.put(extensionType, catalogue);
        ManagementCatalogue managementCatalogue = getCatalogue(ManagementCatalogue.class);
        managementCatalogue.addInstanceListener(catalogue);
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
