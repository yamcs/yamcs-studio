package org.yamcs.studio.ui.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.TimeCatalogue;
import org.yamcs.studio.core.TimeListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.utils.TimeEncoding;

public class TimeInfoControlContribution extends WorkbenchWindowControlContribution
        implements StudioConnectionListener, TimeListener {

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

        ConnectionManager.getInstance().addStudioConnectionListener(this);
        return top;
    }

    private String processorTimeToString() {
        if (processorTime == TimeEncoding.INVALID_INSTANT) {
            return "---";
        } else {
            // TODO Improve this. Don't use Date
            Calendar cal = TimeEncoding.toCalendar(processorTime);
            cal.setTimeZone(TimeCatalogue.getInstance().getTimeZone());
            format.setTimeZone(cal.getTimeZone());
            return format.format(cal.getTime());
        }
    }

    @Override
    public void processTime(TimeInfo timeInfo) {
        // Check for disposal on all involved threads
        if (canvas.isDisposed())
            return;
        canvas.getDisplay().asyncExec(() -> {
            if (canvas.isDisposed())
                return;
            processorTime = timeInfo.getCurrentTime();
            canvas.redraw();
        });
    }

    @Override
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        if (webSocketClient != null) {
            webSocketClient.addTimeListener(this);
        }
    }

    @Override
    public void onStudioDisconnect() {
        if (canvas.isDisposed())
            return;
        canvas.getDisplay().asyncExec(() -> {
            if (canvas.isDisposed())
                return;
            processorTime = TimeEncoding.INVALID_INSTANT;
            canvas.redraw();
        });
    }
}
