/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.cmdhist;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.time.Instant;
import java.util.Locale;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Lists acknowledgments
 */
public class AckTableViewer extends TableViewer {

    private static final long ONE_SECOND = 1000; // millis
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;

    public static final String COL_NAME = "Name";
    public static final String COL_STATUS = "Status";
    public static final String COL_DELTA = "Delta";
    public static final String COL_DATE = "Date";

    private CommandHistoryView commandHistoryView;

    public AckTableViewer(Composite parent, CommandHistoryView commandHistoryView) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        this.commandHistoryView = commandHistoryView;
        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);

        var tl = new TableLayout();
        getTable().setLayout(tl);

        addFixedColumns(tl);

        setContentProvider(new AckTableContentProvider());
    }

    private void addFixedColumns(TableLayout tl) {
        var nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_NAME);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var rec = (AckTableRecord) element;
                return rec.acknowledgment.getName();
            }
        });
        tl.addColumnData(new ColumnWeightData(200));

        var statusColumn = new TableViewerColumn(this, SWT.NONE);
        statusColumn.getColumn().setText(COL_STATUS);
        statusColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(Object element) {
                var rec = (AckTableRecord) element;
                if (rec.acknowledgment.getStatus().equals("OK")) {
                    return commandHistoryView.greenBubble;
                } else if (!rec.acknowledgment.getStatus().equals("PENDING")) {
                    return commandHistoryView.redBubble;
                } else {
                    return null;
                }
            }

            @Override
            public String getText(Object element) {
                var rec = (AckTableRecord) element;
                return rec.acknowledgment.getStatus();
            }
        });
        tl.addColumnData(new ColumnWeightData(200));

        var deltaColumn = new TableViewerColumn(this, SWT.NONE);
        deltaColumn.getColumn().setText(COL_DELTA);
        deltaColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var rec = (AckTableRecord) element;
                if (!rec.acknowledgment.getStatus().equals("PENDING")) {
                    var time = rec.acknowledgment.getTime();
                    var command = rec.rec.getCommand();
                    return time != null ? toHumanTimeDiff(command.getGenerationTime(), time) : null;
                } else {
                    return null;
                }
            }
        });
        tl.addColumnData(new ColumnWeightData(200));

        var dateColumn = new TableViewerColumn(this, SWT.NONE);
        dateColumn.getColumn().setText(COL_DATE);
        dateColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var rec = (AckTableRecord) element;
                var time = rec.acknowledgment.getTime();
                return time != null ? YamcsPlugin.getDefault().formatInstant(time) : null;
            }
        });
        tl.addColumnData(new ColumnWeightData(200));
    }

    private String toHumanTimeDiff(Instant refTime, Instant time) {
        var millis = time.toEpochMilli() - refTime.toEpochMilli();
        var sign = (millis >= 0) ? "+" : "-";
        if (millis >= ONE_DAY) {
            return YamcsPlugin.getDefault().formatInstant(time);
        } else if (millis >= ONE_HOUR) {
            return sign + String.format("%d h, %d m", MILLISECONDS.toHours(millis),
                    MILLISECONDS.toMinutes(millis) - HOURS.toMinutes(MILLISECONDS.toHours(millis)));
        } else if (millis >= ONE_MINUTE) {
            return sign + String.format("%d m, %d s", MILLISECONDS.toMinutes(millis),
                    MILLISECONDS.toSeconds(millis) - MINUTES.toSeconds(MILLISECONDS.toMinutes(millis)));
        } else {
            return String.format(Locale.US, "%+,d ms", millis);
        }
    }
}
