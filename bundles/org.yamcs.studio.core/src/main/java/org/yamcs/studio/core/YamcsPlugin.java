package org.yamcs.studio.core;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.yamcs.client.ClientException;
import org.yamcs.client.ConnectionListener;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.client.mdb.MissionDatabaseClient;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.client.storage.StorageClient;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.studio.connect.YamcsConfiguration;
import org.yamcs.studio.connect.YamcsConfiguration.AuthType;
import org.yamcs.studio.core.model.YamcsAware;
import org.yamcs.studio.core.security.YamcsAuthorizations;

public class YamcsPlugin extends Plugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core";
    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());
    private static final AtomicInteger COMMAND_SEQUENCE = new AtomicInteger(1);

    private static YamcsPlugin plugin;

    private YamcsClient yamcsClient;

    private String instance;
    private String processor;
    private SignificanceLevelType clearance;
    private MissionDatabase missionDatabase;
    private Instant currentTime;

    private Set<YamcsConnectionListener> connectionListeners = new CopyOnWriteArraySet<>();
    private Set<YamcsAware> listeners = new CopyOnWriteArraySet<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    public static FutureTask<Void> connect(YamcsConfiguration configuration) {
        if (plugin.yamcsClient != null) {
            plugin.yamcsClient.close();
        }

        YamcsClient.Builder clientBuilder = YamcsClient
                .newBuilder(configuration.getPrimaryHost(), configuration.getPrimaryPort())
                .withTls(configuration.isSsl())
                .withVerifyTls(false)
                .withUserAgent(getProductString());

        if (configuration.getCaCertFile() != null) {
            clientBuilder.withCaCertFile(Paths.get(configuration.getCaCertFile()));
        }
        plugin.yamcsClient = clientBuilder.build();

        FutureTask<Void> future = new FutureTask<>(() -> {
            log.info("Connecting to " + configuration);
            if (configuration.getAuthType() == AuthType.KERBEROS) {
                plugin.yamcsClient.connectWithKerberos();
            } else if (configuration.getUser() == null) {
                plugin.yamcsClient.connectAnonymously();
            } else {
                plugin.yamcsClient.connect(configuration.getUser(), configuration.getPassword().toCharArray());
            }
            return null;
        });
        plugin.executor.submit(future);

        // Add Progress indicator in status bar
        String jobName = "Connecting to " + configuration;
        scheduleAsJob(jobName, future, Job.SHORT);

        return future;
    }

    private static void scheduleAsJob(String jobName, Future<?> cf, int priority) {
        Job job = Job.create(jobName, monitor -> {
            try {
                cf.get();
                return Status.OK_STATUS;
            } catch (CancellationException | InterruptedException e) {
                return Status.CANCEL_STATUS;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                log.log(Level.SEVERE, "Exception while executing job '" + jobName + "': " + cause.getMessage(),
                        cause);
                return Status.OK_STATUS;
            }
        });
        job.setPriority(Job.LONG);
        job.schedule();

        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                MissionDatabaseLoader loader = new MissionDatabaseLoader();
                Job job = Job.create("Loading mission database", loader);
                job.setPriority(Job.LONG);
                job.schedule(1000L);
            }
        });
    }

    public void addYamcsConnectionListener(YamcsConnectionListener listener) {
        connectionListeners.add(listener);
        /*if (yamcsClient.isConnected()) {
            listener.onYamcsConnected();
        }*/
    }

    public void removeYamcsConnectionListener(YamcsConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    public static void addListener(YamcsAware listener) {
        plugin.listeners.add(listener);
    }

    public static void removeListener(YamcsAware listener) {
        plugin.listeners.remove(listener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            plugin = null;
            if (yamcsClient != null) {
                yamcsClient.close();
            }
            if (executor != null) {
                executor.shutdownNow();
            }
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

    public static ArchiveClient getArchiveClient() {
        YamcsClient client = getYamcsClient();
        if (client != null) {
            return client.createArchiveClient(plugin.instance);
        }
        return null;
    }

    public static StorageClient getStorageClient() {
        YamcsClient client = getYamcsClient();
        if (client != null) {
            return client.createStorageClient();
        }
        return null;
    }

    public static ProcessorClient getProcessorClient() {
        YamcsClient client = getYamcsClient();
        if (client != null) {
            return client.createProcessorClient(plugin.instance, plugin.processor);
        }
        return null;
    }

    public static MissionDatabaseClient getMissionDatabaseClient() {
        YamcsClient client = getYamcsClient();
        if (client != null) {
            return client.createMissionDatabaseClient(plugin.instance);
        }
        return null;
    }

    public static String getInstance() {
        return plugin.instance;
    }

    public static String getProcessor() {
        return plugin.processor;
    }

    public static SignificanceLevelType getClearance() {
        return plugin.clearance;
    }

    public static MissionDatabase getMissionDatabase() {
        return plugin.missionDatabase;
    }

    public static Instant getMissionTime() {
        return getMissionTime(false);
    }

    public static Instant getMissionTime(boolean wallClockIfUnset) {
        Instant t = plugin.currentTime;
        if (wallClockIfUnset && t == null) {
            t = Instant.now();
        }
        return t;
    }

    public static TimeZone getTimeZone() {
        // Currently always using local timezone, because need to hack into XYChart because
        // it doesn't support Timezones. Only date formats seem to be accounted for.
        // At least for now, it should stay consistent with the workbench
        // TODO Research modifications to SWT xychart and then make this controllable from user prefs
        return TimeZone.getDefault();
    }

    public static ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    public static String getProductString() {
        String productName = Platform.getProduct().getName();
        Version productVersion = Platform.getProduct().getDefiningBundle().getVersion();
        return productName + " v" + productVersion;
    }

    public static int nextCommandSequenceNumber() {
        return COMMAND_SEQUENCE.incrementAndGet();
    }

    /**
     * Connection listener that maps the connection events from Yamcs API to the slightly different Studio API.
     */
    private class UIConnectionListener implements ConnectionListener {

        @Override
        public void connecting() {
            connectionListeners.forEach(l -> l.onYamcsConnecting());
        }

        @Override
        public void connected() {
            YamcsAuthorizations.getInstance().loadAuthorizations().thenRun(() -> {
                connectionListeners.forEach(l -> l.onYamcsConnected());
            });
        }

        @Override
        public void connectionFailed(ClientException exception) {
            connectionListeners.forEach(l -> l.onYamcsConnectionFailed(exception));
        }

        @Override
        public void disconnected() {
            if (plugin == null) {
                // Plugin is shutting down
                // Prevent downstream exceptions
                return;
            }

            log.fine("Notify downstream components of Studio disconnect");
            for (YamcsConnectionListener l : connectionListeners) {
                log.fine(String.format(" -> Inform %s", l.getClass().getSimpleName()));
                l.onYamcsDisconnected();
            }
        }
    }
}
