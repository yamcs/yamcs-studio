package org.yamcs.studio.product.utility;

import static org.yamcs.studio.core.ui.utils.TextUtils.isBlank;

import javax.security.auth.Subject;

import org.csstudio.security.SecurityListener;
import org.csstudio.security.SecuritySupport;
import org.csstudio.security.authorization.Authorizations;
import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;


public class YamcsStudioWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor
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
        configurer.setShowPerspectiveBar(true);
        configurer.setShowStatusLine(false);
        configurer.setTitle(getDefaultTitle());
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
        yprops = ConnectionManager.getInstance().getConnectionProperties();
        subjectName = ConnectionManager.getInstance().getUsername();
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
        String label = getDefaultTitle();
        if (yprops != null) {
            String host = yprops.getHost();
            String instance = yprops.getInstance();
            if (isBlank(subjectName))
                label = String.format("%s (anonymous@%s/%s)", getDefaultTitle(), host, instance);
            else
                label = String.format("%s (%s@%s/%s)", getDefaultTitle(), subjectName, host, instance);
        }
        getWindowConfigurer().setTitle(label);
    }

    public String getDefaultTitle() {
        return "Yamcs Studio";
    }
}
