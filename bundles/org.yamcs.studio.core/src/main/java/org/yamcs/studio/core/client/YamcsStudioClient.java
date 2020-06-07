package org.yamcs.studio.core.client;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.yamcs.client.ConnectionListener;
import org.yamcs.client.YamcsClient;
import org.yamcs.studio.core.client.YamcsConfiguration.AuthType;

public class YamcsStudioClient implements ConnectionListener {

    private static final Logger log = Logger.getLogger(YamcsStudioClient.class.getName());

    private YamcsConfiguration yprops;
    private String caCertFile;
    private String application;

    private YamcsClient yamcsClient;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();

    public YamcsStudioClient(String application) {
        this.application = application;
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        this.connectionListeners.add(connectionListener);
    }

    private FutureTask<Void> doConnect() {
        if (yamcsClient != null) {
            yamcsClient.close();
        }

        YamcsClient.Builder yamcsClientBuilder = YamcsClient
                .newBuilder(yprops.getPrimaryHost(), yprops.getPrimaryPort())
                // .withConnectionAttempts(10) // This works, but is not very useful unless reconnecting
                .withTls(yprops.isSsl())
                .withVerifyTls(false)
                .withUserAgent(application);

        if (caCertFile != null) {
            yamcsClientBuilder.withCaCertFile(Paths.get(caCertFile));
        }

        yamcsClient = yamcsClientBuilder.build();
        yamcsClient.addConnectionListener(this);

        FutureTask<Void> future = new FutureTask<>(() -> {
            log.info("Connecting to " + yprops);
            if (yprops.getAuthType() == AuthType.KERBEROS) {
                yamcsClient.connectWithKerberos();
            } else if (yprops.getUser() == null) {
                yamcsClient.connectAnonymously();
            } else {
                yamcsClient.connect(yprops.getUser(), yprops.getPassword().toCharArray());
            }
            return null;
        });
        executor.submit(future);

        // Add Progress indicator in status bar
        String jobName = "Connecting to " + yprops;
        scheduleAsJob(jobName, future, Job.SHORT);

        return future;
    }

    @Override
    public void connecting(String url) {
        for (ConnectionListener cl : connectionListeners) {
            cl.connecting(null);
        }
    }

    @Override
    public void connected(String url) {
        log.info("Connected to " + yprops);
        for (ConnectionListener listener : connectionListeners) {
            listener.connected(null);
        }
    }

    @Override
    public void disconnected() {
        if (isConnected()) {
            log.warning("Connection to " + yprops + " lost");
        }
        for (ConnectionListener listener : connectionListeners) {
            listener.disconnected();
        }
    }

    public void disconnect() {
        log.info("Disconnecting from " + yprops);
        if (yamcsClient != null) {
            yamcsClient.close();
        }
    }

    public boolean isConnected() {
        return yamcsClient != null && yamcsClient.isConnected();
    }

    public boolean isConnecting() {
        return false;
        // return yamcsClient != null && yamcsClient.isConnecting();
    }

    public Future<Void> connect(YamcsConfiguration yprops) {
        this.yprops = yprops;
        return doConnect();
    }

    public YamcsConfiguration getYamcsConfiguration() {
        return yprops;
    }

    private void scheduleAsJob(String jobName, Future<?> cf, int priority) {
        Job job = Job.create(jobName, monitor -> {
            cancellableJobs.put(monitor, cf);

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
    }

    public YamcsClient getYamcsClient() {
        return yamcsClient;
    }

    /**
     * Performs an orderly shutdown of this service
     */
    public void shutdown() {
        if (yamcsClient != null) {
            yamcsClient.close();
        }
        executor.shutdown();
    }
}
