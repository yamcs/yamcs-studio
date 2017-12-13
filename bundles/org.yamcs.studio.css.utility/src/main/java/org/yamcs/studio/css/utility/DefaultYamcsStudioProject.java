package org.yamcs.studio.css.utility;

import java.io.File;
import java.io.IOException;
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

/**
 * Creates default projects. We should instead do this only through a menu option though
 */
public class DefaultYamcsStudioProject extends DefaultProject {

    public static final String PROJECT_STYLES = "Styles";
    public static final String PROJECT_YSS_LANDING = "YSS Landing";

    private static final Logger log = Logger.getLogger(DefaultYamcsStudioProject.class.getTypeName());

    @Override
    public Object openProjects(Display display, IApplicationContext context, Map<String, Object> parameters) {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
        if (allProjects != null && allProjects.length > 0)
            return null;

        try {
            IProject[] initialProjects = createProjects();

            // put project into parameters map as requested by API to make it available to other extension points
            parameters.put(PROJECTS, initialProjects);
            return null;
        } catch (IOException | CoreException e) {
            log.log(Level.SEVERE, "Could not create default projects", e);
        }
        return null;
    }

    public IProject[] createProjects() throws IOException, CoreException {
        IProject[] initialProjects = new IProject[2];
        initialProjects[0] = createDefaultProject(PROJECT_STYLES);
        initialProjects[1] = createDefaultProject(PROJECT_YSS_LANDING);
        return initialProjects;
    }

    /**
     * Creates and imports the project files, for a sample project included in the core Yamcs Studio plugins.
     */
    public IProject createDefaultProject(String projectName) throws IOException, CoreException {
        Bundle bundle = Activator.getDefault().getBundle();
        URL location = FileLocator.toFileURL(bundle.getEntry("/sample-projects/"));
        return createProject(location, projectName);
    }

    public IProject createProject(URL location, String sourceFolder) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(sourceFolder);
        try {
            project.create(new NullProgressMonitor());
            project.open(new NullProgressMonitor());
        } catch (CoreException ex) {
            MessageDialog.openError(null, Messages.Error,
                    NLS.bind(Messages.CreateProjectErrorFmt, project.getName(), ex.getMessage()));
            return null;
        }

        try {
            File templateRoot = new File(location.getPath(), sourceFolder);
            RelativeFileSystemStructureProvider structureProvider = new RelativeFileSystemStructureProvider(
                    templateRoot);
            ImportOperation operation = new ImportOperation(project.getFullPath(), templateRoot, structureProvider,
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

        return project;
    }
}
