package org.yamcs.studio.editor.base;

import org.csstudio.opibuilder.actions.EditOPIAction;
import org.csstudio.opibuilder.actions.RunOPIAction;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.NewWizardDropDownAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.yamcs.studio.core.ui.ConnectionStringStatusLineContributionItem;
import org.yamcs.studio.core.ui.MissionTimeStatusLineContributionItem;
import org.yamcs.studio.core.ui.ProcessorStatusLineContributionItem;
import org.yamcs.studio.core.ui.actions.BringAllToFrontAction;
import org.yamcs.studio.core.ui.actions.MaximizeWindowAction;
import org.yamcs.studio.core.ui.actions.MinimizeWindowAction;
import org.yamcs.studio.core.ui.actions.OnlineHelpAction;
import org.yamcs.studio.core.ui.actions.RaiseIssueAction;

@SuppressWarnings("restriction")
public class YamcsStudioActionBarAdvisor extends ActionBarAdvisor {

    public static final String STATUS_CONN_ID = "ystudio.status.conn";
    public static final String STATUS_PROCESSOR_ID = "ystudio.status.processor";
    public static final String STATUS_MISSION_TIME_ID = "ystudio.status.missionTime";

    public static final String COOL_GROUP_PROCESSOR_INFO = "processorinfo";
    public static final String COOL_GROUP_BOOKMARK_SHORTCUTS = "bookmarkshortcuts";
    public static final String COOL_GROUP_PROCESSOR_CONTROLS = "processorcontrols";

    /**
     * Whether to consider the platform OS for advanced menu customization. Not exposed to users. Only for development
     * purposes.
     */
    private static final boolean APPLY_OS_TWEAKS = true;

    private IWorkbenchWindow window;

    // File
    private NewWizardMenu newWizardMenu;
    private IWorkbenchAction newOther;
    private IWorkbenchAction close;
    private IWorkbenchAction closeAll;
    private IWorkbenchAction save;
    private IWorkbenchAction saveAs;
    private IWorkbenchAction saveAll;
    private IWorkbenchAction move;
    private IWorkbenchAction rename;
    private IWorkbenchAction refresh;
    private IWorkbenchAction print;
    private IWorkbenchAction importWizard;
    private IWorkbenchAction exportWizard;
    private IWorkbenchAction exit;

    // Edit
    private IWorkbenchAction undo;
    private IWorkbenchAction redo;
    private IWorkbenchAction cut;
    private IWorkbenchAction copy;
    private IWorkbenchAction paste;
    private IWorkbenchAction delete;
    private IWorkbenchAction selectAll;
    private IWorkbenchAction find;

    // Window
    private IWorkbenchAction toggleToolBar;
    private IWorkbenchAction showViewMenu;
    private IWorkbenchAction preferences;
    private IWorkbenchAction resetPerspective;

    // Help
    private IWorkbenchAction onlineHelp;
    private IWorkbenchAction raiseIssue;
    private IWorkbenchAction about;

    public YamcsStudioActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
        window = configurer.getWindowConfigurer().getWindow();

        // removeAction("org.eclipse.ui.actions.openFiles");
        // removeAction("org.eclipse.search.searchActionSet");
        // removeAction("org.eclipse.ui.NavigateActionSet");

        removeAction("org.eclipse.ui.cheatsheets.actionSet");
        removeAction("org.eclipse.ui.edit.text.actionSet.navigation");
        removeAction("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo");
        removeAction("org.eclipse.ui.edit.text.actionSet.annotationNavigation");
        // removeAction("org.eclipse.ui.edit.text.actionSet.presentation");
    }

    /**
     * Create actions.
     * <p>
     * Some of these actions are created to programmatically add them to the toolbar. Other actions like save_as are not
     * used at all in here, but they need to be created because the plugin.xml registers their command ID in the menu
     * bar, and the action actually implements the handler. The actions also provide the dynamic enablement.
     */
    @Override
    protected void makeActions(IWorkbenchWindow window) {
        /*
         * FILE
         */
        newWizardMenu = new NewWizardMenu(window);
        newOther = ActionFactory.NEW.create(window);
        register(newOther);
        close = ActionFactory.CLOSE.create(window);
        register(close);
        closeAll = ActionFactory.CLOSE_ALL.create(window);
        register(closeAll);
        save = ActionFactory.SAVE.create(window);
        register(save);
        saveAs = ActionFactory.SAVE_AS.create(window);
        register(saveAs);
        saveAll = ActionFactory.SAVE_ALL.create(window);
        register(saveAll);
        move = ActionFactory.MOVE.create(window);
        register(move);
        rename = ActionFactory.RENAME.create(window);
        register(rename);
        refresh = ActionFactory.REFRESH.create(window);
        register(refresh);
        print = ActionFactory.PRINT.create(window);
        register(print);
        importWizard = ActionFactory.IMPORT.create(window);
        register(importWizard);
        exportWizard = ActionFactory.EXPORT.create(window);
        register(exportWizard);
        exit = ActionFactory.QUIT.create(window);
        register(exit);

        /*
         * EDIT
         */
        undo = ActionFactory.UNDO.create(window);
        register(undo);
        redo = ActionFactory.REDO.create(window);
        register(redo);
        cut = ActionFactory.CUT.create(window);
        register(cut);
        copy = ActionFactory.COPY.create(window);
        register(copy);
        paste = ActionFactory.PASTE.create(window);
        register(paste);
        delete = ActionFactory.DELETE.create(window);
        register(delete);
        selectAll = ActionFactory.SELECT_ALL.create(window);
        register(selectAll);
        find = ActionFactory.FIND.create(window);
        register(find);

        /*
         * WINDOW
         */
        toggleToolBar = ActionFactory.TOGGLE_COOLBAR.create(window);
        register(toggleToolBar);
        showViewMenu = ActionFactory.SHOW_VIEW_MENU.create(window);
        register(showViewMenu);
        preferences = ActionFactory.PREFERENCES.create(window);
        register(preferences);

        /*
         * HELP
         */
        onlineHelp = new OnlineHelpAction();
        register(onlineHelp);
        raiseIssue = new RaiseIssueAction();
        register(raiseIssue);
        resetPerspective = ActionFactory.RESET_PERSPECTIVE.create(window);
        resetPerspective.setText("Reset Window Layout...");
        register(resetPerspective);
        about = ActionFactory.ABOUT.create(window);
        register(about);
    }

    @Override
    protected void fillMenuBar(IMenuManager menubar) {
        IMenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        IMenuManager editMenu = new MenuManager("Edit", IWorkbenchActionConstants.M_EDIT);
        IMenuManager navigateMenu = new MenuManager("Navigate", IWorkbenchActionConstants.M_NAVIGATE);
        IMenuManager windowMenu = new MenuManager("Window", IWorkbenchActionConstants.M_WINDOW);
        IMenuManager helpMenu = new MenuManager("Help", IWorkbenchActionConstants.M_HELP);

        menubar.add(fileMenu);
        menubar.add(editMenu);
        menubar.add(navigateMenu);
        menubar.add(new GroupMarker("yamcs"));
        menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menubar.add(windowMenu);
        menubar.add(helpMenu);

        /*
         * FILE
         */
        IMenuManager newMenu = new MenuManager("New", "new");
        newMenu.add(newWizardMenu);
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
        fileMenu.add(newMenu);
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        fileMenu.add(new Separator("close"));
        fileMenu.add(close);
        fileMenu.add(closeAll);
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
        fileMenu.add(new Separator(IWorkbenchActionConstants.SAVE_GROUP));
        fileMenu.add(save);
        fileMenu.add(saveAs);
        fileMenu.add(saveAll);
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
        fileMenu.add(new Separator());
        fileMenu.add(move);
        fileMenu.add(rename);
        fileMenu.add(refresh);
        fileMenu.add(new Separator());
        fileMenu.add(print);
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        fileMenu.add(new Separator("export"));
        fileMenu.add(importWizard);
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
        fileMenu.add(exportWizard);
        fileMenu.add(new Separator());
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        fileMenu.add(new SwitchWorkspaceAction(window));
        if (!isMac()) { // Already linked in app menu
            fileMenu.add(exit);
        }
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));

        /*
         * EDIT
         */
        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.add(cut);
        editMenu.add(copy);
        editMenu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        editMenu.add(paste);
        editMenu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));
        editMenu.add(new Separator("select"));
        editMenu.add(delete);
        editMenu.add(selectAll);
        editMenu.add(new Separator());
        editMenu.add(find);
        editMenu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
        editMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        /*
         * NAVIGATE
         */
        navigateMenu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        navigateMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        /*
         * WINDOW
         */
        if (isMac()) {
            windowMenu.add(new MinimizeWindowAction(window));
            MaximizeWindowAction zoom = new MaximizeWindowAction(window);
            zoom.setText("Zoom");
            windowMenu.add(zoom);
        } else {
            windowMenu.add(new MinimizeWindowAction(window));
            windowMenu.add(new MaximizeWindowAction(window));
        }
        windowMenu.add(toggleToolBar);
        windowMenu.add(new Separator());
        windowMenu.add(new EditOPIAction());
        windowMenu.add(new RunOPIAction());
        windowMenu.add(new Separator());
        IMenuManager showViewMenu = new MenuManager("Show View");
        showViewMenu.add(new ShowViewMenu());
        windowMenu.add(showViewMenu);
        windowMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        windowMenu.add(new Separator());
        windowMenu.add(resetPerspective);
        windowMenu.add(new Separator());
        if (!isMac()) { // Already linked in app menu
            windowMenu.add(preferences);
        }
        windowMenu.add(new Separator());
        windowMenu.add(new BringAllToFrontAction(window.getWorkbench()));
        windowMenu.add(new Separator());
        // windowMenu.add(new OpenWindowMenu());

        /*
         * HELP
         */
        helpMenu.add(onlineHelp);
        helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        helpMenu.add(new Separator());
        helpMenu.add(raiseIssue);
        helpMenu.add(new Separator());
        if (!isMac()) { // Already linked in app menu
            helpMenu.add(about);
        }
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolbar) {
        IToolBarManager appToolBar = new ToolBarManager(coolbar.getStyle());

        coolbar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
        coolbar.add(new ToolBarContributionItem(appToolBar, IWorkbenchActionConstants.TOOLBAR_FILE));
        coolbar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        coolbar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));

        fillAppToolBar(appToolBar);

        // Doesn't seem to work...
        coolbar.setLockLayout(true);
    }

    protected void fillAppToolBar(IToolBarManager appToolBar) {
        appToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
        appToolBar.add(new NewWizardDropDownAction(window));
        appToolBar.add(save);
        appToolBar.add(saveAll);
        appToolBar.add(new GroupMarker(COOL_GROUP_PROCESSOR_INFO));
        appToolBar.add(new GroupMarker(COOL_GROUP_BOOKMARK_SHORTCUTS));
        appToolBar.add(new GroupMarker(COOL_GROUP_PROCESSOR_CONTROLS));
        appToolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
        statusLine.add(new ConnectionStringStatusLineContributionItem(STATUS_CONN_ID));
        statusLine.add(new ProcessorStatusLineContributionItem(STATUS_PROCESSOR_ID));
        statusLine.add(new MissionTimeStatusLineContributionItem(STATUS_MISSION_TIME_ID,
                false /* progress indicator already adds one */));
    }

    @Override
    public void dispose() {
        super.dispose();
        newWizardMenu.dispose();
    }

    /**
     * Remove an unnecessary action. (does not appear to work with every matching id)
     */
    private void removeAction(String id) {
        ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
        IActionSetDescriptor[] actionSets = reg.getActionSets();
        for (IActionSetDescriptor actionSet : actionSets) {
            if (actionSet.getId().equals(id)) {
                IExtension ext = actionSet.getConfigurationElement().getDeclaringExtension();
                reg.removeExtension(ext, new Object[] { actionSet });
            }
        }
    }

    private boolean isMac() {
        return APPLY_OS_TWEAKS && Platform.OS_MACOSX.equals(Platform.getOS());
    }
}
