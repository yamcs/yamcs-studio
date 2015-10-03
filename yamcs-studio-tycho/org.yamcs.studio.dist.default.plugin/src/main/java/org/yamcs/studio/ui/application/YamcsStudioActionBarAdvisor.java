package org.yamcs.studio.ui.application;

import org.eclipse.core.runtime.IExtension;
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
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.OpenPreferencesAction;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.part.CoolItemGroupMarker;

/**
 * Forked from org.csstudio.utility.product.ApplicationActionBarAdvisor
 * <p>
 * {@link ActionBarAdvisor} that can be called by CSS application startup code to create the menu
 * and tool bar.
 * <p>
 * The menu bar is mostly empty, only providing the "additions" section that is used by the
 * contributions in plugin.xml.
 * <p>
 * The toolbar also mostly defines sections used by contributions from plugin.xml.
 * <p>
 * Some actions are created for Eclipse command names in the help menu that have no default
 * implementation.
 */
@SuppressWarnings("restriction")
public class YamcsStudioActionBarAdvisor extends ActionBarAdvisor {

    public static final String COOL_GROUP_PROCESSOR_INFO = "processorinfo";
    public static final String COOL_GROUP_BOOKMARK_SHORTCUTS = "bookmarkshortcuts";
    public static final String COOL_GROUP_PROCESSOR_CONTROLS = "processorcontrols";
    private static final String TOOLBAR_USER = "user";

    final private IWorkbenchWindow window;

    private IWorkbenchAction save;
    private IWorkbenchAction saveAll;
    private IWorkbenchAction resetPerspectiveAction;
    private IWorkbenchAction preferencesAction;
    private IWorkbenchAction helpContentsAction;
    private IWorkbenchAction onlineHelpAction;
    private IWorkbenchAction raiseIssueAction;
    private IWorkbenchAction aboutAction;

    public YamcsStudioActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
        window = configurer.getWindowConfigurer().getWindow();

        // Remove menu items that are not suitable in CS-Studio
        removeActionById("org.eclipse.ui.edit.text.actionSet.navigation");
        removeActionById("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo");
        removeActionById("org.eclipse.ui.edit.text.actionSet.annotationNavigation");

        // Redefined in our own plugin.xml for improved customization
        removeActionById("org.csstudio.opibuilder.actionSet");
    }

    /**
     * Create actions.
     * <p>
     * Some of these actions are created to programmatically add them to the toolbar. Other actions
     * like save_as are not used at all in here, but they need to be created because the plugin.xml
     * registers their command ID in the menu bar, and the action actually implements the handler.
     * The actions also provide the dynamic enablement.
     */
    @Override
    protected void makeActions(final IWorkbenchWindow window) {
        save = ActionFactory.SAVE.create(window);
        register(save);

        saveAll = ActionFactory.SAVE_ALL.create(window);
        register(saveAll);

        register(ActionFactory.SAVE_AS.create(window));

        if (window.getWorkbench().getIntroManager().hasIntro())
            register(ActionFactory.INTRO.create(window));

        onlineHelpAction = new OnlineHelpAction();
        register(onlineHelpAction);

        raiseIssueAction = new RaiseIssueAction();
        register(raiseIssueAction);

        // Not using this, because we hide that default action id. CSS's advisor puts
        // it under Edit menu, which is quite confusing.
        // preferencesAction = ActionFactory.PREFERENCES.create(window);
        preferencesAction = (new ActionFactory("preferences-2", IWorkbenchCommandConstants.WINDOW_PREFERENCES) {
            @Override
            public IWorkbenchAction create(IWorkbenchWindow window) {
                if (window == null)
                    throw new IllegalArgumentException();

                IWorkbenchAction action = new OpenPreferencesAction(window);
                action.setId(getId());
                return action;
            }
        }).create(window);
        register(preferencesAction);

        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
        register(resetPerspectiveAction);

        helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
        register(helpContentsAction);
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
    }

    @Override
    protected void fillMenuBar(final IMenuManager menubar) {
        menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        IMenuManager windowMenu = new MenuManager("Window", IWorkbenchActionConstants.M_WINDOW);
        menubar.add(windowMenu);
        windowMenu.add(resetPerspectiveAction);
        windowMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        windowMenu.add(new Separator());
        windowMenu.add(preferencesAction);

        // plugin.xml in css menu.app defines a non-brandable icon.
        // through plugin.xml in this bundle, that help menu is hidden, and
        // we replace it here with another one (shorter) version
        IMenuManager helpMenu = new MenuManager("Help", "help-2");
        menubar.add(helpMenu);
        helpMenu.add(onlineHelpAction);
        helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        helpMenu.add(new Separator());
        helpMenu.add(raiseIssueAction);
        helpMenu.add(new Separator());
        helpMenu.add(aboutAction);
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolbar) {
        coolbar.setLockLayout(true);

        // Specific to Yamcs Studio
        IToolBarManager studioBar = new ToolBarManager();
        studioBar.add(new CoolItemGroupMarker(COOL_GROUP_PROCESSOR_INFO));
        studioBar.add(new CoolItemGroupMarker(COOL_GROUP_BOOKMARK_SHORTCUTS));
        studioBar.add(new CoolItemGroupMarker(COOL_GROUP_PROCESSOR_CONTROLS));
        coolbar.add(new ToolBarContributionItem(studioBar, "studiocoolbar"));

        // 'File' section of the cool bar
        IToolBarManager fileBar = new ToolBarManager();
        // File 'new' and 'save' actions
        fileBar.add(ActionFactory.NEW.create(window));
        fileBar.add(save);
        fileBar.add(saveAll);
        fileBar.add(new CoolItemGroupMarker(IWorkbenchActionConstants.FILE_END));
        coolbar.add(new ToolBarContributionItem(fileBar, IWorkbenchActionConstants.M_FILE));

        // 'User' section of the cool bar
        final IToolBarManager user_bar = new ToolBarManager();
        coolbar.add(new ToolBarContributionItem(user_bar, TOOLBAR_USER));

        // Explicitly add "additions" and "editor" to work around https://bugs.eclipse.org/bugs/show_bug.cgi?id=422651
        // After a restart, merging of persisted model, PerspectiveSpacer, contributions resulted in re-arranged toolbar with E4.
        coolbar.add(new CoolItemGroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        coolbar.add(new CoolItemGroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
    }

    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
        super.fillStatusLine(statusLine);
    }

    private void removeActionById(String actionSetId) {
        // Use of an internal API is required to remove actions that are provided
        // by including Eclipse bundles.
        ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
        IActionSetDescriptor[] actionSets = reg.getActionSets();
        for (int i = 0; i < actionSets.length; i++) {
            if (actionSets[i].getId().equals(actionSetId)) {
                IExtension ext = actionSets[i].getConfigurationElement().getDeclaringExtension();
                reg.removeExtension(ext, new Object[] { actionSets[i] });
                return;
            }
        }
    }
}
