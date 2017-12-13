package org.yamcs.studio.css.utility;

import java.net.URL;

import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.framework.Bundle;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.studio.core.ui.utils.RCPUtils;

/**
 * Forked from org.csstudio.utility.product.ApplicationWorkbenchAdvisor to clear dependency on utility.product
 */
@SuppressWarnings("restriction")
public class YamcsStudioWorkbenchAdvisor extends WorkbenchAdvisor {

    private OpenDocumentEventProcessor openDocProcessor;

    public YamcsStudioWorkbenchAdvisor(OpenDocumentEventProcessor openDocProcessor) {
        this.openDocProcessor = openDocProcessor;
    }

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new YamcsStudioWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
        // Per default, state is not preserved (RCP book 5.1.1)
        configurer.setSaveAndRestore(true);

        // Register adapters needed by Navigator view to display workspace files
        IDE.registerAdapters();

        // Declares all IDE-specific workbench images. This includes both "shared"
        // images (named in {@link IDE.SharedImages}) and internal images.
        configurer.declareImage(IDE.SharedImages.IMG_OBJ_PROJECT,
                RCPUtils.getImageDescriptor(YamcsStudioWorkbenchAdvisor.class, "icons/project_open.png"), true);
        configurer.declareImage(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED,
                RCPUtils.getImageDescriptor(YamcsStudioWorkbenchAdvisor.class, "icons/project_close.png"), true);

        declareWorkbenchImages();
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return IDs.OPI_EDITOR_PERSPECTIVE;
    }

    @Override
    public void eventLoopIdle(Display display) {
        if (openDocProcessor != null)
            openDocProcessor.catchUp(display);
        super.eventLoopIdle(display);
    }

    @Override
    public boolean preShutdown() {
        try {
            ResourcesPlugin.getWorkspace().save(true, new NullProgressMonitor());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Workaround for RCP bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=234252
     *
     * Declares all IDE-specific workbench images. This includes both "shared" images (named in
     * {@link IDE.SharedImages}) and internal images (named in
     * {@link org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages}).
     *
     * @see org.eclipse.ui.internal.ide.IDEWorkbenchAdvisor#declareImage
     */
    private void declareWorkbenchImages() {
        final String ICONS_PATH = "$nl$/icons/full/";
        final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; // Enabled
        final String PATH_DLOCALTOOL = ICONS_PATH + "dlcl16/"; // Disabled
        final String PATH_ETOOL = ICONS_PATH + "etool16/"; // Enabled toolbar
        final String PATH_DTOOL = ICONS_PATH + "dtool16/"; // Disabled toolbar
        final String PATH_OBJECT = ICONS_PATH + "obj16/"; // Model object
        final String PATH_WIZBAN = ICONS_PATH + "wizban/"; // Wizard
        final String PATH_EVIEW = ICONS_PATH + "eview16/";
        Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC,
                PATH_ETOOL + "build_exec.gif", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_HOVER,
                PATH_ETOOL + "build_exec.gif", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_DISABLED,
                PATH_DTOOL + "build_exec.gif", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC,
                PATH_ETOOL + "search_src.gif", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_HOVER,
                PATH_ETOOL + "search_src.gif", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_DISABLED,
                PATH_DTOOL + "search_src.gif", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV,
                PATH_ETOOL + "next_nav.gif", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV,
                PATH_ETOOL + "prev_nav.gif", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_NEWPRJ_WIZ,
                PATH_WIZBAN + "newprj_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ,
                PATH_WIZBAN + "newfolder_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFILE_WIZ,
                PATH_WIZBAN + "newfile_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTDIR_WIZ,
                PATH_WIZBAN + "importdir_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTZIP_WIZ,
                PATH_WIZBAN + "importzip_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTDIR_WIZ,
                PATH_WIZBAN + "exportdir_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTZIP_WIZ,
                PATH_WIZBAN + "exportzip_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ,
                PATH_WIZBAN + "workset_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_DLGBAN_SAVEAS_DLG,
                PATH_WIZBAN + "saveas_wiz.png", false);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG,
                PATH_WIZBAN + "quick_fix.png", false);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
                PATH_OBJECT + "prj_obj.gif", true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED,
                PATH_OBJECT + "cprj_obj.gif", true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER,
                PATH_ELOCALTOOL + "gotoobj_tsk.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED,
                PATH_ELOCALTOOL + "smartmode_co.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED,
                PATH_DLOCALTOOL + "smartmode_co.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_WARNING,
                PATH_OBJECT + "quickfix_warning_obj.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_ERROR,
                PATH_OBJECT + "quickfix_error_obj.gif", true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_TASK_TSK,
                PATH_OBJECT + "taskmrk_tsk.gif", true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_BKMRK_TSK,
                PATH_OBJECT + "bkmrk_tsk.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_COMPLETE_TSK,
                PATH_OBJECT + "complete_tsk.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_INCOMPLETE_TSK,
                PATH_OBJECT + "incomplete_tsk.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_ITEM,
                PATH_OBJECT + "welcome_item.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_BANNER,
                PATH_OBJECT + "welcome_banner.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH,
                PATH_OBJECT + "error_tsk.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH,
                PATH_OBJECT + "warn_tsk.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH,
                PATH_OBJECT + "info_tsk.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_LCL_FLAT_LAYOUT,
                PATH_ELOCALTOOL + "flatLayout.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_LCL_HIERARCHICAL_LAYOUT,
                PATH_ELOCALTOOL + "hierarchicalLayout.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEM_CATEGORY,
                PATH_ETOOL + "problem_category.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW,
                PATH_EVIEW + "problems_view.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_ERROR,
                PATH_EVIEW + "problems_view_error.gif", true);
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_WARNING,
                PATH_EVIEW + "problems_view_warning.gif", true);
    }

    private void declareWorkbenchImage(Bundle ideBundle, String symbolicName, String path, boolean shared) {
        URL url = FileLocator.find(ideBundle, new Path(path), null);
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        getWorkbenchConfigurer().declareImage(symbolicName, desc, shared);
    }

    @Override
    public void postStartup() {
        IWorkbench workbench = PlatformUI.getWorkbench();

        PreferenceManager pm = workbench.getPreferenceManager();
        pm.remove("org.eclipse.help.ui.browsersPreferencePage");
        pm.remove("org.eclipse.team.ui.TeamPreferences");
        pm.remove("org.csstudio.platform.ui.css.applications");
        pm.remove("org.csstudio.platform.ui.css.platform");

        YamcsUIPlugin.getDefault().postWorkbenchStartup(workbench);
    }
}