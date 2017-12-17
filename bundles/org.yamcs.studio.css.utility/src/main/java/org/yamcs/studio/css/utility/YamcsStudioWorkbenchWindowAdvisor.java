package org.yamcs.studio.css.utility;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.logging.ui.ConsoleViewHandler;
import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.csstudio.utility.product.IWorkbenchWindowAdvisorExtPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.internal.ide.EditorAreaDropAdapter;

@SuppressWarnings("restriction")
public class YamcsStudioWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor {

    public YamcsStudioWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public void postWindowCreate() {
        // The inherited implementation adds a JUL handler that sends
        // output to the Console View
        super.postWindowCreate();

        // Modify the newly added JUL handler with custom config.
        for (Handler handler : Logger.getLogger("").getHandlers()) {
            if (handler instanceof ConsoleViewHandler) {
                handler.setLevel(Level.INFO);
                handler.setFormatter(new UserLogFormatter());
            }
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

        for (IWorkbenchWindowAdvisorExtPoint hook : hooks) {
            try {
                hook.preWindowOpen();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new YamcsStudioActionBarAdvisor(configurer);
    }

    public String getDefaultTitle() {
        return Platform.getProduct().getName();
    }
}
