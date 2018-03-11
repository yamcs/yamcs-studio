package org.yamcs.studio.editor.base;

import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_DLGBAN_SAVEAS_DLG;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_DISABLED;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_HOVER;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_ERROR;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_INFO;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_WARNING;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEM_CATEGORY;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_DISABLED;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_HOVER;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_LCL_FLAT_LAYOUT;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_LCL_HIERARCHICAL_LAYOUT;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_COMPLETE_TSK;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_ERROR;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_INFO;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_WARNING;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_INCOMPLETE_TSK;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_BANNER;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_ITEM;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTDIR_WIZ;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTZIP_WIZ;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTDIR_WIZ;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTZIP_WIZ;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFILE_WIZ;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_WIZBAN_NEWPRJ_WIZ;
import static org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ;

import java.net.URL;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.framework.Bundle;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.studio.core.ui.utils.RCPUtils;

/**
 * Forked from org.csstudio.utility.product.ApplicationWorkbenchAdvisor to clear dependency on utility.product
 */
@SuppressWarnings("restriction")
public class YamcsStudioWorkbenchAdvisor extends WorkbenchAdvisor {

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

    /**
     * Declares all IDE-specific workbench images. CSS uses the same IDE-specific images as Eclipse product. This
     * includes both "shared" images (named in {@link org.eclipse.ui.ide.IDE.SharedImages}) and internal images (named
     * in {@link org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages}).
     */
    private void declareWorkbenchImages() {
        String ICONS = "$nl$/icons/full/";
        String ELOCALTOOL = ICONS + "elcl16/";
        String DLOCALTOOL = ICONS + "dlcl16/";
        String ETOOL = ICONS + "etool16/";
        String DTOOL = ICONS + "dtool16/";
        String OBJECT = ICONS + "obj16/";
        String WIZBAN = ICONS + "wizban/";
        String EVIEW = ICONS + "eview16/";
        Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);

        declareWorkbenchImage(ideBundle, IMG_ETOOL_BUILD_EXEC, ETOOL + "build_exec.png", false);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_BUILD_EXEC_HOVER, ETOOL + "build_exec.png", false);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_BUILD_EXEC_DISABLED, DTOOL + "build_exec.png", false);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_SEARCH_SRC, ETOOL + "search_src.png", false);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_SEARCH_SRC_HOVER, ETOOL + "search_src.png", false);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_SEARCH_SRC_DISABLED, DTOOL + "search_src.png", false);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_NEXT_NAV, ETOOL + "next_nav.png", false);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_PREVIOUS_NAV, ETOOL + "prev_nav.png", false);
        declareWorkbenchImage(ideBundle, IMG_WIZBAN_NEWPRJ_WIZ, WIZBAN + "newprj_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_WIZBAN_NEWFOLDER_WIZ, WIZBAN + "newfolder_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_WIZBAN_NEWFILE_WIZ, WIZBAN + "newfile_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_WIZBAN_IMPORTDIR_WIZ, WIZBAN + "importdir_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_WIZBAN_IMPORTZIP_WIZ, WIZBAN + "importzip_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_WIZBAN_EXPORTDIR_WIZ, WIZBAN + "exportdir_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_WIZBAN_EXPORTZIP_WIZ, WIZBAN + "exportzip_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_WIZBAN_RESOURCEWORKINGSET_WIZ, WIZBAN + "workset_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_DLGBAN_SAVEAS_DLG, WIZBAN + "saveas_wiz.png", false);
        declareWorkbenchImage(ideBundle, IMG_DLGBAN_QUICKFIX_DLG, WIZBAN + "quick_fix.png", false);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT, OBJECT + "prj_obj.png", true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, OBJECT + "cprj_obj.png", true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER, ELOCALTOOL + "gotoobj_tsk.png", true);

        // Quick fix icons
        declareWorkbenchImage(ideBundle, IMG_ELCL_QUICK_FIX_ENABLED, ELOCALTOOL + "smartmode_co.png", true);
        declareWorkbenchImage(ideBundle, IMG_DLCL_QUICK_FIX_DISABLED, DLOCALTOOL + "smartmode_co.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_FIXABLE_WARNING, OBJECT + "quickfix_warning_obj.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_FIXABLE_ERROR, OBJECT + "quickfix_error_obj.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_FIXABLE_INFO, OBJECT + "quickfix_info_obj.png", true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_TASK_TSK, OBJECT + "taskmrk_tsk.png", true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_BKMRK_TSK, OBJECT + "bkmrk_tsk.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_COMPLETE_TSK, OBJECT + "complete_tsk.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_INCOMPLETE_TSK, OBJECT + "incomplete_tsk.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_WELCOME_ITEM, OBJECT + "welcome_item.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_WELCOME_BANNER, OBJECT + "welcome_banner.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_ERROR_PATH, OBJECT + "error_tsk.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_WARNING_PATH, OBJECT + "warn_tsk.png", true);
        declareWorkbenchImage(ideBundle, IMG_OBJS_INFO_PATH, OBJECT + "info_tsk.png", true);

        declareWorkbenchImage(ideBundle, IMG_LCL_FLAT_LAYOUT, ELOCALTOOL + "flatLayout.png", true);
        declareWorkbenchImage(ideBundle, IMG_LCL_HIERARCHICAL_LAYOUT, ELOCALTOOL + "hierarchicalLayout.png", true);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_PROBLEM_CATEGORY, ETOOL + "problem_category.png", true);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_PROBLEMS_VIEW, EVIEW + "problems_view.png", true);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_PROBLEMS_VIEW_ERROR, EVIEW + "problems_view_error.png", true);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_PROBLEMS_VIEW_WARNING, EVIEW + "problems_view_warning.png", true);
        declareWorkbenchImage(ideBundle, IMG_ETOOL_PROBLEMS_VIEW_INFO, EVIEW + "problems_view_info.png", true);
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

        YamcsUIPlugin.getDefault().postWorkbenchStartup(workbench);
    }

    // Important. Without this the 'Display Explorer' view only shows content when right-clicking in it...
    @Override
    public IAdaptable getDefaultPageInput() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
}
