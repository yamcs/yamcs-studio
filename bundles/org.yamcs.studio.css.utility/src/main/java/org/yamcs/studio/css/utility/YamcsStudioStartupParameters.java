package org.yamcs.studio.css.utility;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.csstudio.startup.module.StartupParametersExtPoint;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;

public class YamcsStudioStartupParameters implements StartupParametersExtPoint {

    public static final String PARAM_WORKSPACE = "workspace.path";

    // When set, a workspace prompt is always shown.
    // If -workspace is also set, it is used as the initial value
    public static final String PARAM_WORKSPACE_PROMPT = "workspace.prompt";

    @Override
    public Map<String, Object> readStartupParameters(Display display, IApplicationContext context) throws Exception {
        String args[] = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);

        Map<String, Object> workspaceParameters = new HashMap<>();

        String workspace = null;
        boolean forcePrompt = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "-help":
                showHelp();
                workspaceParameters.put(EXIT_CODE, IApplication.EXIT_OK);
                System.exit(0);
                return workspaceParameters;
            case "-workspace":
                workspace = args[++i];
                break;
            case "-force-workspace-prompt":
                forcePrompt = true;
                break;
            }
        }

        if (workspace == null) {
            workspace = System.getProperty("user.home") + File.separator + "yamcs-studio";
        }

        workspaceParameters.put(PARAM_WORKSPACE, new URL("file:" + workspace));
        workspaceParameters.put(PARAM_WORKSPACE_PROMPT, forcePrompt);
        return workspaceParameters;
    }

    private void showHelp() {
        System.out.println("Command-line options:");
        System.out.format("  %-40s : Version info\n", "-version");
        System.out.format("  %-40s : Use the provided workspace\n", "-workspace /some/workspace");
        System.out.format("  %-40s : Prompt for the workspace\n", "-force-workspace-prompt");
    }
}
