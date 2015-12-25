package org.yamcs.studio.ui.application;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.yamcs.studio.product.ProductPlugin;

/**
 * Creates default projects. We should instead do this only through a menu option though
 */
public class DefaultYamcsStudioProject extends DefaultProject {

    private static final Logger log = Logger.getLogger(DefaultYamcsStudioProject.class.getTypeName());

    @Override
    public Object openProjects(Display display, IApplicationContext context, Map<String, Object> parameters) {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
        if (allProjects != null && allProjects.length > 0)
            return null;

        try {
            IProject landingProject = createProject("YSS Landing");
            landingProject.open(new NullProgressMonitor());
            importIntoProject(landingProject, "YSS Landing");

            //IProject leoSpacecraftProject = createProject("YSS LEO Spacecraft");
            //leoSpacecraftProject.open(new NullProgressMonitor());
            //importIntoProject(leoSpacecraftProject, "YSS LEO Spacecraft");
            // put project into parameters map as requested by API to make it available to other extension points
            parameters.put(PROJECTS, new IProject[] { landingProject /* , leoSpacecraftProject */ });
            return null;
        } catch (CoreException e) {
            log.log(Level.SEVERE, "Could not create default projects", e);
        }
        return null;
    }

    private IProject createProject(String name) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        try {
            project.create(new NullProgressMonitor());
            return project;
        } catch (CoreException ex) {
            MessageDialog.openError(null, Messages.Error,
                    NLS.bind(Messages.CreateProjectErrorFmt, project.getName(), ex.getMessage()));
            return null;
        }
    }

    private void importIntoProject(IProject projectHandle, String sourceFolder) throws CoreException {
        Bundle bundle = ProductPlugin.getDefault().getBundle();
        try {
            URL location = FileLocator.toFileURL(bundle.getEntry("/"));
            File templateRoot = new File(location.getPath(), sourceFolder);
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
            throw new CoreException(new Status(IStatus.ERROR, ProductPlugin.PLUGIN_ID, e.getLocalizedMessage()));
        }
    }
}
