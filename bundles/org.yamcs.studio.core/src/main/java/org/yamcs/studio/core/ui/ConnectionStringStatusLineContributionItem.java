package org.yamcs.studio.core.ui;

import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.UserInfo;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.utils.RCPUtils;
import org.yamcs.studio.core.utils.StatusLineContributionItem;

public class ConnectionStringStatusLineContributionItem extends StatusLineContributionItem implements YamcsAware {

    public ConnectionStringStatusLineContributionItem(String id) {
        this(id, CALC_TRUE_WIDTH);
    }

    public ConnectionStringStatusLineContributionItem(String id, int charWidth) {
        super(id, charWidth);

        setText("Offline");
        setToolTipText("Yamcs Connection Status");

        addClickListener(evt -> {
            if (YamcsPlugin.getYamcsClient().getWebSocketClient().isConnected()) {
                // TODO show server info (version string etc)
            } else {
                RCPUtils.runCommand(YamcsPlugin.CMD_CONNECT);
            }
        });

        YamcsPlugin.addListener(this);
    }

    @Override
    public void dispose() {
        YamcsPlugin.removeListener(this);
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
        Display.getDefault().asyncExec(() -> {
            setErrorText(null, null);
            setImage(null);
            UserInfo user = YamcsPlugin.getUser();
            String host = YamcsPlugin.getYamcsClient().getHost();
            setText(user.getName() + "@" + host);
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
        if (isDisposed()) {
            return;
        }
        Display.getDefault().asyncExec(() -> {
            setErrorText(null, null);
            setImage(null);
            setText("Offline");
        });
    }
}
