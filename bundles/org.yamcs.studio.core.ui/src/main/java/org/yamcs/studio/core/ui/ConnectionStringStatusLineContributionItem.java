package org.yamcs.studio.core.ui;

import static org.yamcs.studio.core.ui.utils.TextUtils.isBlank;

import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.core.ui.utils.StatusLineContributionItem;

public class ConnectionStringStatusLineContributionItem extends StatusLineContributionItem
        implements YamcsConnectionListener {

    public ConnectionStringStatusLineContributionItem(String id) {
        this(id, CALC_TRUE_WIDTH);
    }

    public ConnectionStringStatusLineContributionItem(String id, int charWidth) {
        super(id, charWidth);

        setText("Offline");
        setToolTipText("Yamcs Connection Status");

        addClickListener(evt -> {
            if (YamcsPlugin.getYamcsClient().isConnected()) {
                // TODO show server info (version string etc)
            } else {
                RCPUtils.runCommand(YamcsUIPlugin.CMD_CONNECT);
            }
        });

        // Listen to changes
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
    }

    @Override
    public void dispose() {
        YamcsPlugin.getDefault().removeYamcsConnectionListener(this);
    }

    @Override
    public void onYamcsConnecting() {
        Display.getDefault().asyncExec(() -> {
            setErrorText(null, null);
            setImage(null);
            setText("Connecting...");
        });
    }

    @Override
    public void onYamcsConnected() {
        YamcsConnectionProperties yprops = YamcsPlugin.getYamcsClient().getYamcsConnectionProperties();
        Display.getDefault().asyncExec(() -> {
            setErrorText(null, null);
            setImage(null);
            setText(getConnectionString(yprops));
        });
    }

    @Override
    public void onYamcsDisconnected() {
        Display display = Display.getDefault();
        if (display.isDisposed()) {
            return;
        }
        display.asyncExec(() -> {
            setErrorText(null, null);
            setImage(null);
            setText("Offline");
        });
    }

    @Override
    public void onYamcsConnectionFailed(Throwable t) {
        Display display = Display.getDefault();
        if (display.isDisposed()) {
            return;
        }
        display.asyncExec(() -> {
            setErrorText(null, null);
            setImage(null);
            setText("Offline");
        });
    }

    private String getConnectionString(YamcsConnectionProperties yprops) {
        String subjectName = null;
        if (yprops.getAuthenticationToken() != null) {
            subjectName = "" + yprops.getAuthenticationToken().getPrincipal();
        }
        if (subjectName == null || isBlank(subjectName)) {
            subjectName = "anonymous";
        }

        return String.format("%s@%s", subjectName, yprops.getHost());
    }
}
