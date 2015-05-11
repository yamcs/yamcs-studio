package org.yamcs.studio.core.application;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.CoolItemGroupMarker;

/**
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
 *
 * @author Kay Kasemir
 * @author Xihui Chen
 * @author mfurseman Remove Eclipse menu items that don't apply
 * @author fqqb Forked from ApplicationActionBarAdvisor for yamcs-studio in order to better position
 *         custom coolbar items with CSS-ones.
 */
@SuppressWarnings("restriction")
public class YamcsStudioActionBarAdvisor extends ActionBarAdvisor {

    public static final String COOL_GROUP_PROCESSOR_INFO = "processorinfo";
    public static final String COOL_GROUP_BOOKMARK_SHORTCUTS = "bookmarkshortcuts";
    public static final String COOL_GROUP_PROCESSOR_CONTROLS = "processorcontrols";
    public static final String COOL_GROUP_PROCESSOR_POSITION = "processorposition";
    private static final String TOOLBAR_USER = "user";

    final private IWorkbenchWindow window;

    private IWorkbenchAction save;

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

        register(ActionFactory.SAVE_AS.create(window));

        if (window.getWorkbench().getIntroManager().hasIntro())
            register(ActionFactory.INTRO.create(window));

        register(ActionFactory.HELP_CONTENTS.create(window));
    }

    @Override
    protected void fillMenuBar(final IMenuManager menubar) {
        menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolbar) {
        // Set up the context Menu
        final MenuManager coolbarPopupMenuManager = new MenuManager();
        coolbar.setContextMenuManager(coolbarPopupMenuManager);
        final IMenuService menuService = (IMenuService) window.getService(IMenuService.class);
        menuService.populateContributionManager(coolbarPopupMenuManager, "popup:windowCoolbarContextMenu");

        // Specific to Yamcs Studio
        IToolBarManager studioBar = new ToolBarManager();
        studioBar.add(new CoolItemGroupMarker(COOL_GROUP_PROCESSOR_INFO));
        studioBar.add(new CoolItemGroupMarker(COOL_GROUP_BOOKMARK_SHORTCUTS));
        studioBar.add(new CoolItemGroupMarker(COOL_GROUP_PROCESSOR_CONTROLS));
        studioBar.add(new CoolItemGroupMarker(COOL_GROUP_PROCESSOR_POSITION));
        coolbar.add(new ToolBarContributionItem(studioBar, "studiocoolbar"));

        // 'File' section of the cool bar
        final IToolBarManager file_bar = new ToolBarManager();
        // File 'new' and 'save' actions
        file_bar.add(ActionFactory.NEW.create(window));
        file_bar.add(save);
        file_bar.add(new CoolItemGroupMarker(IWorkbenchActionConstants.FILE_END));
        coolbar.add(new ToolBarContributionItem(file_bar, IWorkbenchActionConstants.M_FILE));

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
