package org.yamcs.studio.core;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.yamcs.client.Page;
import org.yamcs.client.mdb.MissionDatabaseClient;
import org.yamcs.client.mdb.MissionDatabaseClient.ListOptions;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.ParameterInfo;

public class MissionDatabaseLoader implements IJobFunction {

    private static final Logger log = Logger.getLogger(MissionDatabaseLoader.class.getName());

    @Override
    public IStatus run(IProgressMonitor monitor) {
        MissionDatabase mdb = new MissionDatabase();
        MissionDatabaseClient mdbClient = YamcsPlugin.getMissionDatabaseClient();
        if (mdbClient != null) {
            try {
                log.fine("Fetching available parameters");
                Page<ParameterInfo> page = mdbClient.listParameters(ListOptions.limit(500)).get();
                page.iterator().forEachRemaining(mdb::addParameter);
                while (page.hasNextPage()) {
                    page = page.getNextPage().get();
                    page.iterator().forEachRemaining(mdb::addParameter);
                }

                log.fine("Fetching available commands");
                Page<CommandInfo> commandPage = mdbClient.listCommands(ListOptions.limit(200)).get();
                commandPage.iterator().forEachRemaining(mdb::addCommand);
                while (commandPage.hasNextPage()) {
                    commandPage = commandPage.getNextPage().get();
                    commandPage.iterator().forEachRemaining(mdb::addCommand);
                }
            } catch (InterruptedException e) {
                return Status.CANCEL_STATUS;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                log.log(Level.SEVERE, "Exception while loading mission database: " + cause.getMessage(), cause);
                return Status.OK_STATUS;
            }
        }

        log.info(String.format("Loaded %d parameters and %d commands", mdb.getParameterCount(), mdb.getCommandCount()));

        // YamcsPlugin.notifyMissionDatabase(mdb);

        return Status.OK_STATUS;
    }
}
