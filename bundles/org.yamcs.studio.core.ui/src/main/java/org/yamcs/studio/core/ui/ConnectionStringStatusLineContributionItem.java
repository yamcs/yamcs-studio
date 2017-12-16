package org.yamcs.studio.core.ui;

import static org.yamcs.studio.core.ui.utils.TextUtils.isBlank;

import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.core.ui.utils.StatusLineContributionItem;

public class ConnectionStringStatusLineContributionItem extends StatusLineContributionItem
        implements StudioConnectionListener {

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

        addClickListener(evt -> {
            if (connectionManager.getYamcsClient().isConnected()) {
                // TODO show server info (version string etc)
            } else {
                RCPUtils.runCommand(YamcsUIPlugin.CMD_CONNECT);
            }
        });

        // Listen to changes
        connectionManager = ConnectionManager.getInstance();
        connectionManager.addStudioConnectionListener(this);
    }

    @Override
    public void dispose() {
        connectionManager.removeStudioConnectionListener(this);
    }

    @Override
    public void onStudioConnect() {
        yprops = connectionManager.getYamcsClient().getYamcsConnectionProperties();
        if (yprops.getAuthenticationToken() != null) {
            subjectName = "" + yprops.getAuthenticationToken().getPrincipal();
        } else {
            subjectName = null;
        }
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
            setErrorText(null, null);
            String host = yprops.getHost();
            if (isBlank(subjectName))
                setText(String.format("anonymous@%s", host));
            else
                setText(String.format("%s@%s", subjectName, host));
            setImage(null);
        } else {
            setErrorText(DEFAULT_TEXT, null);
        }
    }
}
