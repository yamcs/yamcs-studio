package org.csstudio.platform.libs.yamcs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.web.MessageHandler;
import org.csstudio.platform.libs.yamcs.web.SimpleYamcsRequests;
import org.csstudio.platform.libs.yamcs.web.XtceDbHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.protostuff.NamedObjectList;
import org.yamcs.xtce.MetaCommand;
import org.yamcs.xtce.XtceDb;

public class YamcsPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.csstudio.utility.platform.libs.yamcs";
    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());

    // The shared instance
    private static YamcsPlugin plugin;
    
    private Set<MDBContextListener> mdbListeners = new HashSet<>();
    
    private NamedObjectList parameters;
    private CountDownLatch parametersLoaded = new CountDownLatch(1);
    
    private Collection<MetaCommand> commands;
    private CountDownLatch commandsLoaded = new CountDownLatch(1);
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        System.out.println("yamcs plugin started....");
        
        String yamcsHost = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_host");
        int yamcsPort = YamcsPlugin.getDefault().getPreferenceStore().getInt("yamcs_port");
        String yamcsInstance = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
        YamcsConnectionProperties yprops = new YamcsConnectionProperties(yamcsHost, yamcsPort, yamcsInstance);
        fetchInitialMdbAsync(yprops);
    }
    
    private void fetchInitialMdbAsync(YamcsConnectionProperties yprops) {
        System.out.println("list params?");
        // Load list of parameters
        new Thread(() -> {
            SimpleYamcsRequests.listAllAvailableParameters(yprops, new MessageHandler<NamedObjectList>() {
                @Override
                public void onMessage(NamedObjectList msg) {
                    Display.getDefault().asyncExec(() -> {
                        parameters = msg;
                        for (MDBContextListener l : mdbListeners) {
                            l.onParametersChanged(parameters);
                        }
                        parametersLoaded.countDown();
                    });
                }

                @Override
                public void onException(Throwable t) {
                    log.log(Level.SEVERE, "Could not fetch available yamcs parameters", t);
                }
            });            
        }).start();
        
        System.out.println("list commands?");
        // Load commands
        new Thread(() -> {
            SimpleYamcsRequests.listAllAvailableCommands(yprops, new XtceDbHandler() {
                @Override
                public void onMessage(XtceDb msg) {
                    Display.getDefault().asyncExec(() -> {
                        commands = msg.getMetaCommands();
                        for (MDBContextListener l : mdbListeners) {
                            l.onCommandsChanged(commands);
                        }
                        commandsLoaded.countDown();
                    });
                }
                
                @Override
                public void onException(Throwable t) {
                    log.log(Level.SEVERE, "Could not fetch available yamcs commands", t);
                }
            });
        }).start();
    }
    
    public void addMdbListener(MDBContextListener listener) {
        mdbListeners.add(listener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static YamcsPlugin getDefault() {
        return plugin;
    }

    /**
     * Return the available parameters. Waits on the thread
     * while request is still ongoing.
     */
    public NamedObjectList getParameters() {
        try {
            parametersLoaded.await();
            return parameters;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
