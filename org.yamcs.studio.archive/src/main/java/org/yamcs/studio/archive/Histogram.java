/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.archive;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.yamcs.protobuf.IndexEntry;

public class Histogram extends Line {

    public enum HistogramKind {
        COMPLETENESS, TM, PP, CMDHIST, EVENT,
    }

    public static final int LINE_HEIGHT = 20;

    private String label;
    private HistogramKind kind;
    private List<IndexEntry> data = new ArrayList<>();

    public Histogram(Timeline timeline, HistogramKind kind, String label) {
        super(timeline);
        this.kind = kind;
        this.label = label;
    }

    @Override
    void drawContent(GC gc) {
        super.drawContent(gc);

        var background = new Color(135, 206, 250);
        for (var rec : data) {
            var x = (int) Math.round(timeline.positionTime(fromTimestamp(rec.getStart())));
            var y = coords.y;
            var height = coords.height;
            var width = (int) Math.round(timeline.positionTime(fromTimestamp(rec.getStop())) - x);
            width = Math.max(width, 1); // Make sure "something" is visible

            gc.setBackground(background);
            gc.fillRectangle(x, y, width, height);
        }
        background.dispose();

        var fontData = JFaceResources.getTextFont().getFontData();
        var fontHeight = coords.height / 2;
        var font = new Font(timeline.getDisplay(), fontData[0].getName(), fontHeight, SWT.NORMAL);
        gc.setFont(font);
        gc.setBackground(timeline.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.setForeground(timeline.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        gc.drawText(label, coords.x + 5, coords.y + (int) Math.round((getHeight() - fontHeight) / 2.0));
        font.dispose();
    }

    @Override
    public int getHeight() {
        return LINE_HEIGHT;
    }

    public String getLabel() {
        return label;
    }

    public HistogramKind getKind() {
        return kind;
    }

    public List<IndexEntry> getData() {
        return data;
    }

    public void setData(List<IndexEntry> data) {
        this.data = data;
        reportMutation();
    }

    private OffsetDateTime fromTimestamp(String timestamp) {
        return Instant.parse(timestamp).atOffset(ZoneOffset.UTC);
    }
}
