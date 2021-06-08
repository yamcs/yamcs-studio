package org.yamcs.studio.core;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.yamcs.client.ClientException;
import org.yamcs.client.ClientException.ExceptionData;
import org.yamcs.client.Page;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.mdb.MissionDatabaseClient;
import org.yamcs.client.mdb.MissionDatabaseClient.ListOptions;
import org.yamcs.protobuf.GetServerInfoResponse;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.UserInfo;

/**
 * A job that helps to switch context (instance/processor) while keeping the same YamcsClient.
 */
public class ContextSwitcher implements IRunnableWithProgress {

    private static final Logger log = Logger.getLogger(ContextSwitcher.class.getName());

    private YamcsClient yamcsClient;
    private GetServerInfoResponse serverInfo;
    private UserInfo userInfo;

    private String instance; // If null, connect without any context
    private String processor; // If null, use default processor for that instance

    /**
     * Switch context to a new instance, using a specific processor.
     */
    public ContextSwitcher(String instance, String processor) {
        this.yamcsClient = Objects.requireNonNull(YamcsPlugin.getYamcsClient());
        this.serverInfo = Objects.requireNonNull(YamcsPlugin.getServerInfo());
        this.userInfo = Objects.requireNonNull(YamcsPlugin.getUser());
        this.instance = instance;
        this.processor = processor;
    }

    /**
     * Switch context to a new instance, using the default processor for that instance.
     */
    public ContextSwitcher(String instance) {
        this(instance, null);
    }

    /**
     * Remove any current context (no instance, and no processor)
     */
    public ContextSwitcher() {
        this(null, null);
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            if (instance == null) {
                monitor.beginTask("Clearing context", IProgressMonitor.UNKNOWN);
                runBefore(monitor);
                if (!monitor.isCanceled()) {
                    clearContext(monitor);
                }
            } else {
                monitor.beginTask("Switching context", IProgressMonitor.UNKNOWN);
                runBefore(monitor);
                if (!monitor.isCanceled()) {
                    switchContext(monitor);
                }
            }
        } catch (CancellationException e) {
            log.fine("Job cancelled");
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof ClientException) {
                ClientException clientException = (ClientException) e.getCause();
                ExceptionData detail = clientException.getDetail();
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(null, "Could not switch context", detail.getMessage());
                });
            } else {
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(null, "Could not switch context", e.getMessage());
                });
            }

        }
        monitor.done();
    }

    /**
     * Extension hook to run actions before switching contexts
     */
    protected void runBefore(IProgressMonitor monitor)
            throws InterruptedException, java.util.concurrent.ExecutionException {
    }

    private void clearContext(IProgressMonitor monitor) {
        RemoteEntityHolder holder = new RemoteEntityHolder();
        holder.yamcsClient = yamcsClient;
        holder.serverInfo = serverInfo;
        holder.userInfo = userInfo;
        log.info("Clearing context");
        YamcsPlugin.updateEntities(holder);
    }

    private void switchContext(IProgressMonitor monitor)
            throws InterruptedException, java.util.concurrent.ExecutionException, CancellationException {
        ProcessorInfo processorInfo = findProcessorInfo(monitor);
        MissionDatabase missionDatabase = loadMissionDatabase(monitor);

        RemoteEntityHolder holder = new RemoteEntityHolder();
        holder.yamcsClient = yamcsClient;
        holder.serverInfo = serverInfo;
        holder.userInfo = userInfo;
        holder.missionDatabase = missionDatabase;
        holder.instance = processorInfo.getInstance();
        holder.processor = processorInfo;
        log.info(String.format("Switching to '%s' processor (instance: %s)", processorInfo.getName(),
                processorInfo.getInstance()));
        YamcsPlugin.updateEntities(holder);
    }

    private ProcessorInfo findProcessorInfo(IProgressMonitor monitor)
            throws InterruptedException, java.util.concurrent.ExecutionException, CancellationException {
        monitor.subTask("Determining processor");
        CompletableFuture<List<ProcessorInfo>> processorsFuture = yamcsClient.listProcessors(instance);
        RCPUtils.monitorCancellableFuture(monitor, processorsFuture);
        List<ProcessorInfo> processors = processorsFuture.get();

        if (processor != null) {
            return processors.stream()
                    .filter(processor -> processor.getName().equals(this.processor))
                    .findFirst()
                    .get();
        } else {
            return processors.stream()
                    .filter(processor -> processor.getPersistent() && !processor.getReplay())
                    .findFirst()
                    .get();
        }
    }

    private MissionDatabase loadMissionDatabase(IProgressMonitor monitor)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        monitor.subTask("Loading mission database");
        MissionDatabase missionDatabase = new MissionDatabase();

        MissionDatabaseClient mdbClient = yamcsClient.createMissionDatabaseClient(instance);
        log.fine("Fetching available parameters");
        Page<ParameterInfo> page = mdbClient.listParameters(ListOptions.limit(500)).get();
        page.iterator().forEachRemaining(missionDatabase::addParameter);
        while (page.hasNextPage()) {
            page = page.getNextPage().get();
            page.iterator().forEachRemaining(missionDatabase::addParameter);
        }

        log.fine("Fetching available commands");
        Page<CommandInfo> commandPage = mdbClient.listCommands(ListOptions.limit(200)).get();
        commandPage.iterator().forEachRemaining(missionDatabase::addCommand);
        while (commandPage.hasNextPage()) {
            commandPage = commandPage.getNextPage().get();
            commandPage.iterator().forEachRemaining(missionDatabase::addCommand);
        }
        log.info(String.format("Loaded %d parameters and %d commands", missionDatabase.getParameterCount(),
                missionDatabase.getCommandCount()));
        return missionDatabase;
    }
}
