package org.yamcs.studio.core.ui;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.yamcs.studio.core.commanding.TelecommandView;

public class TopItem {

    private LocalResourceManager resourceManager;

    @PostConstruct
    public void createControls(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        Bundle bundle = FrameworkUtil.getBundle(TelecommandView.class);

        ImageDescriptor desc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/reverse.png"), null));
        Image reverseImage = resourceManager.createImage(desc);

        desc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/pause.png"), null));
        Image pauseImage = resourceManager.createImage(desc);

        desc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/forward.png"), null));
        Image forwardImage = resourceManager.createImage(desc);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Composite buttons = new Composite(composite, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        buttons.setLayoutData(gd);

        RowLayout rl = new RowLayout();
        rl.center = true;
        buttons.setLayout(rl);

        Label reverse = new Label(buttons, SWT.NONE);
        reverse.setImage(reverseImage);

        Label pause = new Label(buttons, SWT.NONE);
        pause.setImage(pauseImage);

        Label forward = new Label(buttons, SWT.NONE);
        forward.setImage(forwardImage);

        // We should eventually draw something similar to the IndexLine of the archive browser here.
        Composite timeline = new Composite(composite, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        timeline.setLayout(gl);
        gd = new GridData();
        gd.widthHint = 600;
        gd.heightHint = 3;
        timeline.setLayoutData(gd);
        timeline.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
    }

    @PreDestroy
    public void dispose() {
        resourceManager.dispose();
    }
}
