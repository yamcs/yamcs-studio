package org.yamcs.studio.ui.application;

import static org.yamcs.studio.ui.TextUtils.isBlank;

import javax.security.auth.Subject;

import org.csstudio.security.SecurityListener;
import org.csstudio.security.SecuritySupport;
import org.csstudio.security.authorization.Authorizations;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.EditorAreaDropAdapter;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;

@SuppressWarnings("restriction")
public class YamcsStudioWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
        implements StudioConnectionListener, SecurityListener {

    private YamcsConnectionProperties yprops;
    private String subjectName;

    public YamcsStudioWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public void preWindowOpen() {
        super.preWindowOpen();
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

        configurer.setInitialSize(new Point(1920, 1200));
        configurer.setShowMenuBar(true);
        configurer.setShowCoolBar(true);
        configurer.setShowFastViewBars(true);
        configurer.setShowProgressIndicator(true);
        configurer.setShowPerspectiveBar(false);
        configurer.setShowStatusLine(false);
        configurer.setTitle("Yamcs Studio");

        // Workaround for text editor DND bug.
        // See http://www.eclipse.org/forums/index.php/m/333816/
        configurer.configureEditorAreaDropListener(new EditorAreaDropAdapter(configurer.getWindow()));
    }

    @Override
    public void postWindowCreate() {
        super.postWindowCreate();
    }

    @Override
    public void postWindowOpen() {
        super.postWindowOpen();

        // Set initial title
        changedSecurity(SecuritySupport.getSubject(), SecuritySupport.isCurrentUser(), SecuritySupport.getAuthorizations());

        // Listen for changes
        ConnectionManager.getInstance().addStudioConnectionListener(this);
        SecuritySupport.addListener(this);
    }

    @Override
    public void postWindowClose() {
        super.postWindowClose();

        if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0 && !PlatformUI.getWorkbench().isClosing()) {
            //This is required in order to at least partially clean up the mess that RCP leaves behind.
            //The code below will dispose of unused actions and a few other stuff that are not disposed from the
            //memory after the workbench window closes.
            IWorkbenchWindow win = getWindowConfigurer().getWindow();
            IWorkbenchPage[] pages = win.getPages();
            for (IWorkbenchPage p : pages) {
                try {
                    p.close();
                } catch (Exception e) {
                    //ignore
                }
            }
            win.setActivePage(null);
        }
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new YamcsStudioActionBarAdvisor(configurer);
    }

    @Override
    public void changedSecurity(Subject subject, boolean is_current_user, Authorizations authorizations) {
        Display.getDefault().asyncExec(() -> {
            String subjectName = "anonymous";
            if (subject != null)
                subjectName = SecuritySupport.getSubjectName(subject);

            this.subjectName = subjectName;
            updateTitle();
        });
    }

    @Override
    public void onStudioConnect() {
        yprops = ConnectionManager.getInstance().getWebProperties();
        subjectName = ConnectionManager.getInstance().getHornetqProperties().username;
        Display.getDefault().asyncExec(() -> updateTitle());
    }

    @Override
    public void onStudioDisconnect() {
        yprops = null;
        Display display = Display.getDefault();
        if (display.isDisposed())
            return;

        display.asyncExec(() -> {
            updateTitle();
        });
    }

    private void updateTitle() {
        IWorkbenchPage page = getWindowConfigurer().getWindow().getActivePage();
        String label = "Yamcs Studio";
        if (page != null && page.getPerspective() != null) {
            IPerspectiveDescriptor perspective = getWindowConfigurer().getWindow().getActivePage().getPerspective();
            if (perspective.getId().equals(IDs.OPI_EDITOR_PERSPECTIVE))
                label = "Yamcs Studio Editor";
            else if (perspective.getId().equals(IDs.OPI_RUNTIME_PERSPECTIVE))
                label = "Yamcs Studio Runtime";
            else
                label = perspective.getLabel();
        }

        if (yprops != null) {
            String host = yprops.getHost();
            String instance = yprops.getInstance();
            if (isBlank(subjectName))
                label = String.format("%s (anonymous@%s/%s)", label, host, instance);
            else
                label = String.format("%s (%s@%s/%s)", label, subjectName, host, instance);
        }
        getWindowConfigurer().setTitle(label);
    }
}
