package org.yamcs.studio.css.utility;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.csstudio.startup.module.StartupParametersExtPoint;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;

public class StartupParameters implements StartupParametersExtPoint {

    public static final String PARAM_PROMPT_FOR_WORKSPACE = "workspace.prompt";
    public static final String PARAM_SUGGESTED_WORKSPACE = "workspace.path";

    @Override
    public Map<String, Object> readStartupParameters(Display display, IApplicationContext context) throws Exception {
        String args[] = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);

        Map<String, Object> cssParameters = new HashMap<>();

        boolean forceWorkspacePrompt = false;
        URL suggestedWorkspace = null;

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
            case "-help":
            case "--help":
                showHelp();
                cssParameters.put(EXIT_CODE, IApplication.EXIT_OK);
                System.exit(0);
                return cssParameters;
            case "-workspace_prompt":
                forceWorkspacePrompt = true;
                if ((i + 1) < args.length) {
                    String next = args[i + 1];
                    if (!next.startsWith("-")) {
                        suggestedWorkspace = new URL("file:" + next);
                        i++;
                    }
                }
                break;
            }
        }

        cssParameters.put(PARAM_PROMPT_FOR_WORKSPACE, forceWorkspacePrompt);
        cssParameters.put(PARAM_SUGGESTED_WORKSPACE, suggestedWorkspace);
        return cssParameters;
    }

    private void showHelp() {
        System.out.println("Command-line options:");
        System.out.format("  %-40s : Version info\n", "-version");
        System.out.format("  %-40s : Show workspace dialog, with preconfigured default\n", "-workspace_prompt");
        System.out.format("  %-40s : Show workspace dialog with given default\n", "-workspace_prompt /some/workspace");
        System.out.format("  %-40s : Select workspace on command-line, no prompt\n", "-data /some/workspace");
    }
}
