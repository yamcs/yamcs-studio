package org.yamcs.studio.product;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.csstudio.startup.module.defaults.DefaultProject;
import org.csstudio.startup.module.defaults.Messages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Bundle;

/**
 * Creates a project called 'Sample MCS', but unlike the CSS DefaultProject, it will only do so if
 * there are no other projects in the workspace. So if the user wants to remove it, it will not be
 * recreated.
 */
public class DefaultYamcsStudioProject extends DefaultProject {

    @Override
    public Object openProjects(Display display, IApplicationContext context, Map<String, Object> parameters) {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
        if (allProjects != null && allProjects.length > 0)
            return null;

        // Create a default project
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("Sample MCS");
        try {
            project.create(new NullProgressMonitor());
        } catch (CoreException ex) {
            MessageDialog.openError(null, Messages.Error,
                    NLS.bind(Messages.CreateProjectErrorFmt, project.getName(), ex.getMessage()));
            return null;
        }

        // We created a new project, so open it now
        try {
            project.open(new NullProgressMonitor());
            importSampleMCS(project);
            // put project into parameters map as requested by API to make it available to other extension points
            parameters.put(PROJECTS, new IProject[] { project });
            return null;
        } catch (CoreException ex) {
            MessageDialog.openError(null, Messages.Error,
                    NLS.bind(Messages.OpenProjectErrorFmt, project.getName(), ex.getMessage()));
        }
        return null;
    }

    private void importSampleMCS(IProject projectHandle) throws CoreException {
        Bundle bundle = Activator.getDefault().getBundle();
        try {
            URL location = FileLocator.toFileURL(bundle.getEntry("/"));
            File templateRoot = new File(location.getPath(), "sample-mcs-project");
            RelativeFileSystemStructureProvider structureProvider = new RelativeFileSystemStructureProvider(templateRoot);
            ImportOperation operation = new ImportOperation(projectHandle.getFullPath(), templateRoot, structureProvider,
                    new IOverwriteQuery() {
                        @Override
                        public String queryOverwrite(String pathString) {
                            return ALL;
                        }
                    }, structureProvider.getChildren(templateRoot));

            operation.setContext(Display.getDefault().getActiveShell());
            operation.run(null);
        } catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage()));
        }
    }
}
