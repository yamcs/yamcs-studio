package org.yamcs.studio.core.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

public class TimeInfo extends WorkbenchWindowControlContribution {

    private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-DD HH:mm:ss", Locale.US);
    private SimpleDateFormat format = format1; // TODO load from some pref store

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
            gc.drawText(format.format(new Date()), 0, text_y, true);
        });

        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 150;
        time.setLayoutData(gd);

        exec.scheduleAtFixedRate(() -> {
            parent.getDisplay().asyncExec(() -> {
                if (!time.isDisposed())
                    time.redraw();
            });
        }, 500, 500, TimeUnit.MILLISECONDS);

        time.addListener(SWT.MouseDown, l -> {
            format = (format == format1) ? format2 : format1;
            if (!time.isDisposed())
                time.redraw();
        });

        return top;
    }

    @Override
    public void dispose() {
        exec.shutdown();
        super.dispose();
    }
}
