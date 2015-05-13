package org.yamcs.studio.ui.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.protobuf.YamcsManagement.TmStatistics;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.utils.TimeEncoding;

public class TimeInfoControlContribution extends WorkbenchWindowControlContribution implements ProcessorListener {

    private long processorTime = TimeEncoding.INVALID_INSTANT;
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-DD HH:mm:ss", Locale.US);
    private SimpleDateFormat format = format1; // TODO load from some pref store

    private Canvas canvas;

    @Override
    protected Control createControl(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        top.setLayout(gl);

        // Not using a Label, because had problems with vertical centering
        canvas = new Canvas(top, SWT.NONE);
        canvas.addPaintListener(evt -> {
            GC gc = evt.gc;
            int text_y = (canvas.getBounds().height - gc.getFontMetrics().getHeight()) / 2;
            gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
            gc.drawText(processorTimeToString(), 0, text_y, true);
        });

        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 150;
        canvas.setLayoutData(gd);

        canvas.addListener(SWT.MouseDown, l -> {
            format = (format == format1) ? format2 : format1;
            if (!canvas.isDisposed())
                canvas.redraw();
        });

        YamcsPlugin.getDefault().addProcessorListener(this);
        return top;
    }

    private String processorTimeToString() {
        if (processorTime == TimeEncoding.INVALID_INSTANT) {
            return "";
        } else {
            // TODO Improve this. Don't use Date
            Calendar cal = TimeEncoding.toCalendar(processorTime);
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.format(cal.getTime());
        }
    }

    @Override
    public void processorUpdated(ProcessorInfo updatedInfo) {
    }

    @Override
    public void yProcessorClosed(ProcessorInfo updatedInfo) {
    }

    @Override
    public void updateStatistics(Statistics stats) {
        // Check for disposal on all involved threads
        if (canvas.isDisposed())
            return;
        canvas.getDisplay().asyncExec(() -> {
            if (canvas.isDisposed())
                return;
            ClientInfo clientInfo = YamcsPlugin.getDefault().getClientInfo();
            if (clientInfo != null
                    && stats.getYProcessorName().equals(clientInfo.getProcessorName())
                    && stats.getInstance().equals(clientInfo.getInstance())) {

                // find the timestamp of the most recent packet received
                long pos = 0;
                for (TmStatistics ts : stats.getTmstatsList())
                    pos = Math.max(pos, ts.getLastPacketTime());

                processorTime = pos;
                canvas.redraw();
            }
        });
    }

    @Override
    public void clientUpdated(ClientInfo updatedInfo) {
    }

    @Override
    public void clientDisconnected(ClientInfo updatedInfo) {
    }
}
