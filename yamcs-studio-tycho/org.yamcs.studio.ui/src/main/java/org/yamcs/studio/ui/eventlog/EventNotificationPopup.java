package org.yamcs.studio.ui.eventlog;

import org.eclipse.mylyn.commons.ui.dialogs.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Yamcs.Event;

public class EventNotificationPopup extends AbstractNotificationPopup {
    private Event event;
    private Image errorIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
    private Image warnIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
    private Image infoIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);

    public EventNotificationPopup(Display display, Event event) {
        super(display);
        this.event = event;
    }

    @Override
    protected void createContentArea(Composite parent) {
        parent.setLayout(new GridLayout(2, false));

        Label img = new Label(parent, SWT.NONE);
        switch (event.getSeverity()) {
        case INFO:
            img.setImage(infoIcon);
            break;
        case WARNING:
            img.setImage(warnIcon);
            break;
        case ERROR:
            img.setImage(errorIcon);
            break;
        }
        GridData gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        img.setLayoutData(gd);

        Label l = new Label(parent, SWT.NONE);
        l.setText(event.getMessage());
        l.setBackground(parent.getBackground());
        l.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    @Override
    protected String getPopupShellTitle() {
        if (event.hasType())
            return event.getSource() + " :: " + event.getType();
        else
            return event.getSource();
    }
}
