package org.yamcs.studio.editor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.utility.file.IFileUtil;
import org.diirt.datasource.CompositeDataSource;
import org.diirt.datasource.CompositeDataSourceConfiguration;
import org.diirt.datasource.PVManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.EditorAreaDropAdapter;
import org.yamcs.studio.css.core.pvmanager.ParameterDataSourceProvider;
import org.yamcs.studio.css.core.pvmanager.SoftwareParameterDataSourceProvider;

@SuppressWarnings("restriction")
public class YamcsStudioWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public YamcsStudioWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public void postWindowCreate() {

        // Add console view to the logger
        ConsoleViewHandler handler = ConsoleViewHandler.install();
        if (handler != null) {
            handler.setLevel(Level.INFO);
            handler.setFormatter(new UserLogFormatter());
        }

        // Now that we now that the user will see it:
        Logger log = Logger.getLogger(getClass().getName());
        log.info(Platform.getProduct().getName() + " v" + Platform.getProduct().getDefiningBundle().getVersion());
        log.info("Workspace: " + Platform.getInstanceLocation().getURL().getPath());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1920, 1200));
        configurer.setShowMenuBar(true);
        configurer.setShowCoolBar(true);
        configurer.setShowFastViewBars(true);
        configurer.setShowProgressIndicator(true);
        configurer.setShowPerspectiveBar(true);
        configurer.setShowStatusLine(true);
        configurer.setTitle(getDefaultTitle());

        // Workaround for text editor DND bug.
        // See http://www.eclipse.org/forums/index.php/m/333816/
        configurer.configureEditorAreaDropListener(
                new EditorAreaDropAdapter(configurer.getWindow()));

        // Bootstrap DIIRT
        CompositeDataSource defaultDs = (CompositeDataSource) PVManager.getDefaultDataSource();
        defaultDs.putDataSource(new ParameterDataSourceProvider());
        defaultDs.putDataSource(new SoftwareParameterDataSourceProvider());
        defaultDs.setConfiguration(new CompositeDataSourceConfiguration().defaultDataSource("para").delimiter("://"));
        PVManager.setDefaultDataSource(defaultDs);
    }

    @Override
    public void postWindowRestore() throws WorkbenchException {
    }

    @Override
    public void postWindowOpen() {
    }

    @Override
    public boolean preWindowShellClose() {
        return super.preWindowShellClose();
    }

    @Override
    public void postWindowClose() {
        if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0 && !PlatformUI.getWorkbench().isClosing()) {
            // This is required in order to at least partially clean up the mess that RCP leaves behind.
            // The code below will dispose of unused actions and a few other stuff that are not disposed from the
            // memory after the workbench window closes.
            IWorkbenchWindow win = getWindowConfigurer().getWindow();
            IWorkbenchPage[] pages = win.getPages();
            for (IWorkbenchPage p : pages) {
                try {
                    p.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            win.setActivePage(null);
        }
    }

    @Override
    public IStatus saveState(IMemento memento) {
        IFileUtil.getInstance().saveState(memento);
        return Status.OK_STATUS;
    }

    @Override
    public IStatus restoreState(IMemento memento) {
        IFileUtil.getInstance().restoreState(memento);
        return Status.OK_STATUS;
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new YamcsStudioActionBarAdvisor(configurer);
    }

    public String getDefaultTitle() {
        return Platform.getProduct().getName();
    }
}
