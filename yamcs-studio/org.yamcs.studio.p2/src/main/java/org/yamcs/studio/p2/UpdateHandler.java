package org.yamcs.studio.p2;

import org.eclipse.equinox.internal.p2.ui.dialogs.UpdateSingleIUWizard;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * UpdateHandler invokes the check for updates UI
 */
public class UpdateHandler extends PreloadingRepositoryHandler {

    boolean hasNoRepos = false;

    @Override
    protected void doExecute(LoadMetadataRepositoryJob job) {
        if (hasNoRepos) {
            MessageDialog.openInformation(null, "Update Yamcs Studio", "Could not check for updates since no repository is configured");
            return;
        }

        UpdateOperation operation = getProvisioningUI().getUpdateOperation(null, null);
        // check for updates
        operation.resolveModal(null);
        if (getProvisioningUI().getPolicy().continueWorkingWithOperation(operation, getShell())) {
            if (UpdateSingleIUWizard.validFor(operation)) {
                // Special case for only updating a single root
                UpdateSingleIUWizard wizard = new UpdateSingleIUWizard(getProvisioningUI(), operation);
                WizardDialog dialog = new WizardDialog(getShell(), wizard);
                dialog.create();
                dialog.open();
            } else {
                // Open the normal version of the update wizard
                getProvisioningUI().openUpdateWizard(false, operation, job);
            }
        }
    }

    @Override
    protected boolean preloadRepositories() {
        hasNoRepos = false;
        RepositoryTracker repoMan = getProvisioningUI().getRepositoryTracker();
        if (repoMan.getKnownRepositories(getProvisioningUI().getSession()).length == 0) {
            hasNoRepos = true;
            return false;
        }
        return super.preloadRepositories();
    }
}
