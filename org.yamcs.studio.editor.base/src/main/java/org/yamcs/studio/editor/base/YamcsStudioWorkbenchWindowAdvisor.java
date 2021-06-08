package org.yamcs.studio.editor.base;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.EditorAreaDropAdapter;
import org.yamcs.studio.core.ui.logging.ConsoleViewHandler;
import org.yamcs.studio.core.ui.logging.UserLogFormatter;

@SuppressWarnings("restriction")
public class YamcsStudioWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    private static boolean logProductInfo = true;

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

        // Now that we know that the user will see it:
        if (logProductInfo) {
            Logger log = Logger.getLogger(getClass().getName());
            logProductInfo(log);
            // Prevent this message from appearing when runtime window is opened from builder
            logProductInfo = false;
        }
    }

    protected void logProductInfo(Logger log) {
        log.info(Platform.getProduct().getName() + " v" + Platform.getProduct().getDefiningBundle().getVersion());
        log.info("Workspace: " + Platform.getInstanceLocation().getURL().getPath());
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new YamcsStudioActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1920, 1200));
        configurer.setShowMenuBar(true);
        configurer.setShowCoolBar(true);
        configurer.setShowProgressIndicator(true);
        configurer.setShowPerspectiveBar(false);
        configurer.setShowStatusLine(true);
        updateTitle();

        configurer.getWindow().addPerspectiveListener(new IPerspectiveListener() {

            @Override
            public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
                updateTitle();
            }

            @Override
            public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
                updateTitle();
            }
        });

        // Workaround for text editor DND bug.
        // See http://www.eclipse.org/forums/index.php/m/333816/
        configurer.configureEditorAreaDropListener(new EditorAreaDropAdapter(configurer.getWindow()));
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
    public boolean isDurableFolder(String perspectiveId, String folderId) {
        // This method no longer does anything...
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=355750

        // Otherwise could have been used to prevent the part stack closing when the last
        // view is closed in runtime mode.
        return super.isDurableFolder(perspectiveId, folderId);
    }

    private void updateTitle() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        String title = Platform.getProduct().getName();
        IWorkbenchPage currentPage = configurer.getWindow().getActivePage();
        if (currentPage != null) {
            IPerspectiveDescriptor perspective = currentPage.getPerspective();
            if (perspective != null) {
                title = perspective.getLabel();
            }
        }
        configurer.setTitle(title);
    }
}
