package org.yamcs.studio.core.ui;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class TrimBarInfo extends WorkbenchWindowControlContribution {

    private LocalResourceManager resourceManager;

    @Override
    protected Control createControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        //CoolItem coolItem = new CoolItem(parent, 0);

        Composite top = new Composite(parent, SWT.NONE) {
            @Override
            public Point computeSize(int wHint, int hHint) {
                return super.computeSize(wHint, 150);
            }
        };
        GridLayout gl = new GridLayout(1, false);
        top.setLayout(gl);

        Bundle bundle = FrameworkUtil.getBundle(TrimBarInfo.class);
        ImageDescriptor desc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/pause.png"), null));
        Image pauseImage = resourceManager.createImage(desc);

        Label pause = new Label(top, SWT.NONE);
        //pause.setText("Login info...");
        pause.setToolTipText("Pause TM feed");
        pause.setImage(pauseImage);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        pause.setLayoutData(gd);

        return top;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
    }
}
