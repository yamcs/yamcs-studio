package org.yamcs.studio.runtime;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.yamcs.studio.core.ui.ConnectionStringStatusLineContributionItem;
import org.yamcs.studio.core.ui.MissionTimeStatusLineContributionItem;
import org.yamcs.studio.core.ui.ProcessorStatusLineContributionItem;
import org.yamcs.studio.core.ui.actions.OnlineHelpAction;
import org.yamcs.studio.core.ui.actions.RaiseIssueAction;

@SuppressWarnings("restriction")
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    public static final String STATUS_CONN_ID = "ystudio.status.conn";
    public static final String STATUS_PROCESSOR_ID = "ystudio.status.processor";
    public static final String STATUS_MISSION_TIME_ID = "ystudio.status.missionTime";

    private IWorkbenchAction exitAction;

    private IWorkbenchAction onlineHelpAction;
    private IWorkbenchAction raiseIssueAction;
    private IWorkbenchAction aboutAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    @Override
    protected void makeActions(IWorkbenchWindow window) {
        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);

        onlineHelpAction = new OnlineHelpAction();
        register(onlineHelpAction);

        raiseIssueAction = new RaiseIssueAction();
        register(raiseIssueAction);

        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
    }

    @Override
    protected void fillMenuBar(IMenuManager menuBar) {
        beforeFillMenuBar();
        MenuManager fileMenu = new MenuManager("File", IWorkbenchActionConstants.M_FILE);
        MenuManager windowMenu = new MenuManager("Window", IWorkbenchActionConstants.M_WINDOW);
        MenuManager helpMenu = new MenuManager("Help", IWorkbenchActionConstants.M_HELP);

        menuBar.add(fileMenu);
        menuBar.add(windowMenu);
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(helpMenu);

        // File
        fileMenu.add(new Separator());
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        fileMenu.add(new Separator());
        // fileMenu.add(exitAction);

        // Help
        helpMenu.add(onlineHelpAction);
        helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        helpMenu.add(new Separator());
        helpMenu.add(raiseIssueAction);
        helpMenu.add(new Separator());
        helpMenu.add(aboutAction);
    }

    protected void beforeFillMenuBar() {
        // removeAction("org.eclipse.ui.edit.text.actionSet.navigation");
        // removeAction("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo");
        // removeAction("org.eclipse.ui.edit.text.actionSet.annotationNavigation");
    }

    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
        statusLine.add(new ConnectionStringStatusLineContributionItem(STATUS_CONN_ID));
        statusLine.add(new ProcessorStatusLineContributionItem(STATUS_PROCESSOR_ID));
        statusLine.add(new MissionTimeStatusLineContributionItem(STATUS_MISSION_TIME_ID, true));
    }

    /**
     * Remove an unnecessary action
     */
    protected void removeAction(String id) {
        ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
        IActionSetDescriptor[] actionSets = reg.getActionSets();
        for (IActionSetDescriptor actionSet : actionSets) {
            if (actionSet.getId().equals(id)) {
                IExtension ext = actionSet.getConfigurationElement().getDeclaringExtension();
                reg.removeExtension(ext, new Object[] { actionSet });
            }
        }
    }
}
