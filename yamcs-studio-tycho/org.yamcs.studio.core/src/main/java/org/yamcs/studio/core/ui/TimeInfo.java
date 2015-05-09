package org.yamcs.studio.core.ui;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * TODO on-click should change time format to ordinal or some such and should be stored for next
 * workbench run.
 */
public class TimeInfo extends WorkbenchWindowControlContribution {

    private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected Control createControl(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        top.setLayout(gl);

        // Not using a Label, because had problems with vertical centering
        Canvas time = new Canvas(top, SWT.NONE);
        time.addPaintListener(evt -> {
            GC gc = evt.gc;
            int text_y = (time.getBounds().height - gc.getFontMetrics().getHeight()) / 2;
            gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
            gc.drawText("" + new Date(), 0, text_y);
        });

        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 150;
        time.setLayoutData(gd);

        exec.scheduleAtFixedRate(() -> {
            parent.getDisplay().asyncExec(() -> {
                time.redraw();
            });
        }, 500, 500, TimeUnit.MILLISECONDS);

        return top;
    }

    @Override
    public void dispose() {
        exec.shutdown();
        super.dispose();
    }
}
