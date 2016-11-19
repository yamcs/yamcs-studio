package org.yamcs.studio.p2;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * PreloadingRepositoryHandler provides background loading of repositories before executing the
 * provisioning handler.
 */
abstract class PreloadingRepositoryHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        doExecuteAndLoad();
        return null;
    }

    void doExecuteAndLoad() {
        if (preloadRepositories()) {
            //cancel any load that is already running
            Job.getJobManager().cancel(LoadMetadataRepositoryJob.LOAD_FAMILY);
            final LoadMetadataRepositoryJob loadJob = new LoadMetadataRepositoryJob(getProvisioningUI());
            setLoadJobProperties(loadJob);
            if (waitForPreload()) {
                loadJob.addJobChangeListener(new JobChangeAdapter() {
                    public void done(IJobChangeEvent event) {
                        if (PlatformUI.isWorkbenchRunning())
                            if (event.getResult().isOK()) {
                                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        doExecute(loadJob);
                                    }
                                });
                            }
                    }
                });
                loadJob.setUser(true);
                loadJob.schedule();

            } else {
                loadJob.setSystem(true);
                loadJob.setUser(false);
                loadJob.schedule();
                doExecute(null);
            }
        } else {
            doExecute(null);
        }
    }

    protected abstract void doExecute(LoadMetadataRepositoryJob job);

    protected boolean preloadRepositories() {
        return true;
    }

    protected boolean waitForPreload() {
        return true;
    }

    protected void setLoadJobProperties(Job loadJob) {
        loadJob.setProperty(LoadMetadataRepositoryJob.ACCUMULATE_LOAD_ERRORS, Boolean.toString(true));
    }

    protected ProvisioningUI getProvisioningUI() {
        return ProvisioningUI.getDefaultUI();
    }

    /**
     * Return a shell appropriate for parenting dialogs of this handler.
     *
     * @return a Shell
     */
    protected Shell getShell() {
        return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
    }
}
