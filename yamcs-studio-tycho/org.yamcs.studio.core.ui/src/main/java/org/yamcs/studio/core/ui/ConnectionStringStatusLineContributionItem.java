package org.yamcs.studio.core.ui;

import static org.yamcs.studio.core.ui.utils.TextUtils.isBlank;

import javax.security.auth.Subject;

import org.csstudio.security.SecurityListener;
import org.csstudio.security.SecuritySupport;
import org.csstudio.security.authorization.Authorizations;
import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.ui.utils.StatusLineContributionItem;

public class ConnectionStringStatusLineContributionItem extends StatusLineContributionItem
        implements StudioConnectionListener, SecurityListener {

    private static final String DEFAULT_TEXT = "Not Connected";
    private ConnectionManager connectionManager;

    private YamcsConnectionProperties yprops;
    private String subjectName;

    public ConnectionStringStatusLineContributionItem(String id) {
        this(id, CALC_TRUE_WIDTH);
    }

    public ConnectionStringStatusLineContributionItem(String id, int charWidth) {
        super(id, charWidth);

        setToolTipText("Yamcs Server Connection String");

        // Initial text
        changedSecurity(SecuritySupport.getSubject(), true, null);

        // Listen to changes
        connectionManager = ConnectionManager.getInstance();
        connectionManager.addStudioConnectionListener(this);
        SecuritySupport.addListener(this);
    }

    @Override
    public void dispose() {
        connectionManager.removeStudioConnectionListener(this);
        SecuritySupport.removeListener(this);
    }

    @Override
    public void changedSecurity(Subject subject, boolean isCurrentUser, Authorizations authz) {
        Display.getDefault().asyncExec(() -> {
            subjectName = (subject != null) ? SecuritySupport.getSubjectName(subject) : null;
            updateLabel();
        });
    }

    @Override
    public void onStudioConnect() {
        yprops = connectionManager.getConnectionProperties();
        subjectName = connectionManager.getUsername();
        Display.getDefault().asyncExec(() -> updateLabel());
    }

    @Override
    public void onStudioDisconnect() {
        yprops = null;
        Display display = Display.getDefault();
        if (display.isDisposed())
            return;

        display.asyncExec(() -> updateLabel());
    }

    private void updateLabel() {
        if (yprops != null) {
            String host = yprops.getHost();
            if (isBlank(subjectName))
                setText(String.format("anonymous@%s", host));
            else
                setText(String.format("%s@%s", subjectName, host));
        } else {
            setText(DEFAULT_TEXT);
        }
    }
}
