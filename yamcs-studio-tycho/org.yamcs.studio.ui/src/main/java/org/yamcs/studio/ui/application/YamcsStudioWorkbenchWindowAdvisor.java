package org.yamcs.studio.ui.application;

import javax.security.auth.Subject;

import org.csstudio.security.SecurityListener;
import org.csstudio.security.SecuritySupport;
import org.csstudio.security.authorization.Authorizations;
import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.yamcs.studio.core.YamcsPlugin;

public class YamcsStudioWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor implements SecurityListener {

    public YamcsStudioWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public void preWindowOpen() {
        super.preWindowOpen();
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1920, 1200));
        configurer.setShowPerspectiveBar(false);
        configurer.setShowStatusLine(false);
        setTitle("anonymous");
    }

    @Override
    public void postWindowOpen() {

        // Set initial title
        changedSecurity(SecuritySupport.getSubject(), SecuritySupport.isCurrentUser(), SecuritySupport.getAuthorizations());

        // Listen for changes
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

            setTitle(subjectName);
        });
    }

    private void setTitle(String subject) {
        String host = YamcsPlugin.getDefault().getHost();
        String instance = YamcsPlugin.getDefault().getInstance();

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
        getWindowConfigurer().setTitle(String.format("%s (%s@%s/%s)", label, subject, host, instance));
    }
}
