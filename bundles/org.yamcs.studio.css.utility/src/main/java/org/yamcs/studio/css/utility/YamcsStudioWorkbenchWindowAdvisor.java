package org.yamcs.studio.css.utility;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.diirt.util.DiirtStartup;
import org.csstudio.logging.LogFormatDetail;
import org.csstudio.logging.LogFormatter;
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

    private List<IWorkbenchWindowAdvisorExtPoint> hooks;

    public YamcsStudioWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);

        // parent 'hooks' field is 'private'
        this.hooks = forceAccessToHooks();

        // Substitute broken DIIRT config setup with our own version
        for (int i = 0; i < hooks.size(); i++) {
            if (hooks.get(i) instanceof DiirtStartup) {
                hooks.set(i, new PatchedDiirtStartup());
            }
        }
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
                handler.setFormatter(new LogFormatter(LogFormatDetail.LOW));
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

    /**
     * Forces access to the hooks field of the parent class.
     */
    @SuppressWarnings("unchecked")
    private List<IWorkbenchWindowAdvisorExtPoint> forceAccessToHooks() {
        Field privateHooks;
        try {
            privateHooks = ApplicationWorkbenchWindowAdvisor.class.getDeclaredField("hooks");
            privateHooks.setAccessible(true);
            return (List<IWorkbenchWindowAdvisorExtPoint>) privateHooks.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access workbench window hooks", e);
        }
    }
}
