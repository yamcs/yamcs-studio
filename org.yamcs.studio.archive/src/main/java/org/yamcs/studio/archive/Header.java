package org.yamcs.studio.archive;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

public class Header extends Line {

    private String label;

    public Header(Timeline timeline, String label) {
        super(timeline);
        this.label = label;
    }

    @Override
    void drawContent(GC gc) {
        super.drawContent(gc);

        var background = new Color(timeline.getDisplay(), new RGB(230, 230, 230));
        gc.setBackground(background);
        gc.fillRectangle(coords);

        var fontData = JFaceResources.getTextFont().getFontData();
        var fontHeight = (int) Math.round(coords.height / 2.5);
        var font = new Font(timeline.getDisplay(), fontData[0].getName(), fontHeight, SWT.NORMAL);

        gc.setFont(font);
        gc.setForeground(timeline.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        gc.drawText(label, coords.x, coords.y + (int) Math.round((coords.height / 2.) - (fontHeight / 2.)));
        font.dispose();

        background.dispose();
    }

    @Override
    public int getHeight() {
        return 30;
    }
}
