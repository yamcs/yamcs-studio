package org.yamcs.studio.editor.base;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Version;

public class Application implements IApplication {

    @Override
    public Object start(IApplicationContext context) throws Exception {

        // Workspace resolution:
        // 1. Start with ~/yamcs-studio
        // 2. Override with -Dworkspace.default vm arg (if specified)
        // 3. Override with last used workspace (via ~/.config/yamcs-studio/workspace_history (if present)
        // 4. Override with -workspace command option (if specified)

        Path userHome = Paths.get(System.getProperty("user.home"));

        Path userDataDir = UserPreferences.getDataDir();
        Files.createDirectories(userDataDir);

        String workspace = System.getProperty("workspace.default");
        if (workspace == null) {
            workspace = userHome.resolve("yamcs-studio").toString();
        }
        workspace = workspace.replace("@user.home", userHome.toString());

        List<String> workspaceHistory = UserPreferences.readWorkspaceHistory();
        if (!workspaceHistory.isEmpty()) {
            workspace = workspaceHistory.get(0);
        }

        boolean workspacePrompt = false;

        configureLogging(userDataDir);

        String args[] = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "-version":
                Version version = context.getBrandingBundle().getVersion();
                System.out.println(context.getBrandingName() + " " + version);
                return EXIT_OK;
            case "-help":
                showHelp();
                return EXIT_OK;
            case "-workspace":
                workspace = args[++i];
                break;
            case "-force-workspace-prompt":
                workspacePrompt = true;
                break;
            }
        }

        Display display = PlatformUI.createDisplay();
        try {
            boolean success = YamcsStudioWorkspace.prompt(new URL("file:" + workspace), workspacePrompt);
            if (!success) {
                System.exit(0); // TODO remove?
                return EXIT_OK;
            }

            openProjects();

            return runWorkbench(display, context);
        } finally {
            if (display != null) {
                display.dispose();
            }
        }
    }

    private static void showHelp() {
        System.out.println("Command-line options:");
        System.out.format("  %-40s : Version info\n", "-version");
        System.out.format("  %-40s : Use the provided workspace\n", "-workspace /some/workspace");
        System.out.format("  %-40s : Prompt for the workspace\n", "-force-workspace-prompt");
    }

    private void configureLogging(Path userDataDir) throws IOException {
        Logger root = Logger.getLogger("");

        // We use the convention where INFO goes to end-user (via 'Console View' inside Yamcs Studio)
        // And FINE goes to stdout (--> debuggable by end-user if needed, and visible in PDE/UI)

        // By default only allow WARNING messages
        root.setLevel(Level.WARNING);

        // Exceptions only for plugins that do not flood the Console View with INFO messages:
        Logger.getLogger("com.spaceapplications").setLevel(Level.FINE);
        Logger.getLogger("org.csstudio").setLevel(Level.FINE);
        Logger.getLogger("org.yamcs.studio").setLevel(Level.FINE);

        // Java installs only a console handler. Add a file handler as well.
        Path logDir = userDataDir.resolve("logs");
        Files.createDirectories(logDir);
        String pattern = logDir.resolve("main.log").toString();
        int limit = 10_000_000; // ~10 MB
        int count = 10;
        FileHandler fileHandler = new FileHandler(pattern, limit, count);
        fileHandler.setFormatter(new LogFormatter());
        root.addHandler(fileHandler);

        // At this point in the startup there should be only one handler (for stdout)
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(Level.FINE);
            handler.setFormatter(new LogFormatter());
        }

        // A third user-level handler will be created by the workbench window advisor when the ConsoleView
        // is available.
    }

    protected void openProjects() {
        // Default implementation does nothing
    }

    protected WorkbenchAdvisor createWorkbenchAdvisor() {
        return new YamcsStudioWorkbenchAdvisor();
    }

    private int runWorkbench(Display display, IApplicationContext context) {
        Logger log = Logger.getLogger(getClass().getName());

        // Run the workbench
        int returnCode = PlatformUI.createAndRunWorkbench(display, createWorkbenchAdvisor());

        // Plain exit from IWorkbench.close()
        if (returnCode != PlatformUI.RETURN_RESTART) {
            return EXIT_OK;
        }

        // IWorkbench.restart() was called.
        Integer exitCode = Integer.getInteger("eclipse.exitcode");
        if (EXIT_RELAUNCH.equals(exitCode)) { // RELAUCH with new command line
            log.fine(String.format("RELAUNCH, command line: %s", System.getProperty("eclipse.exitdata")));
            return EXIT_RELAUNCH;
        }
        // RESTART without changes
        return EXIT_RESTART;
    }

    protected IProject createProject(URL location, String sourceFolder) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(sourceFolder);
        try {
            project.create(new NullProgressMonitor());
            project.open(new NullProgressMonitor());
        } catch (CoreException e) {
            MessageDialog.openError(null, "Error",
                    String.format("Error creating project %s: %s", project.getName(), e.getMessage()));
            return null;
        }

        try {
            File templateRoot = new File(location.getPath(), sourceFolder);
            RelativeFileSystemStructureProvider structureProvider = new RelativeFileSystemStructureProvider(
                    templateRoot);
            ImportOperation operation = new ImportOperation(project.getFullPath(), templateRoot, structureProvider,
                    pathString -> IOverwriteQuery.ALL, structureProvider.getChildren(templateRoot));

            operation.setContext(Display.getDefault().getActiveShell());
            operation.run(null);
        } catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage()));
        }

        return project;
    }

    @Override
    public void stop() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return;
        }
        Display display = workbench.getDisplay();
        display.syncExec(() -> {
            if (!display.isDisposed()) {
                workbench.close();
            }
        });
    }
}
