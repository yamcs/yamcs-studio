package org.yamcs.studio.core.ui.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A contribution item to be used with status line managers. Forked and modified
 * from jface's StatusLineContributionItem, because it does not provide good
 * customization options. (e.g. no tooltip support)
 */
public class StatusLineContributionItem extends ContributionItem {

    private final static int DEFAULT_CHAR_WIDTH = 40;

    /**
     * A constant indicating that the contribution should compute its actual
     * size depending on the text. It will grab all space necessary to display
     * the whole text.
     *
     * @since 3.6
     */
    public final static int CALC_TRUE_WIDTH = -1;

    private int charWidth;

    private CLabel label;

    private String tooltip;

    /**
     * The composite into which this contribution item has been placed. This
     * will be <code>null</code> if this instance has not yet been initialized.
     */
    private Composite statusLine = null;

    private String text = Util.ZERO_LENGTH_STRING;

    private int widthHint = -1;

    private int heightHint = -1;

    /**
     * Creates a status line contribution item with the given id.
     *
     * @param id
     *            the contribution item's id, or <code>null</code> if it is to
     *            have no id
     */
    public StatusLineContributionItem(String id) {
        this(id, DEFAULT_CHAR_WIDTH);
    }

    /**
     * Creates a status line contribution item with the given id that displays
     * the given number of characters.
     *
     * @param id
     *            the contribution item's id, or <code>null</code> if it is to
     *            have no id
     * @param charWidth
     *            the number of characters to display. If the value is
     *            CALC_TRUE_WIDTH then the contribution will compute the
     *            preferred size exactly. Otherwise the size will be based on
     *            the average character size * 'charWidth'
     */
    public StatusLineContributionItem(String id, int charWidth) {
        super(id);
        this.charWidth = charWidth;
        setVisible(false); // no text to start with
    }

    @Override
    public void fill(Composite parent) {
        statusLine = parent;

        Label sep = new Label(parent, SWT.SEPARATOR);
        label = new CLabel(statusLine, SWT.SHADOW_NONE);
        label.setText(text);
        if (tooltip != null) {
            label.setToolTipText(tooltip);
        }

        if (charWidth == CALC_TRUE_WIDTH) {
            // compute the size of the label to get the width hint for the
            // contribution
            Point preferredSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            widthHint = preferredSize.x;
            heightHint = preferredSize.y;
        } else if (widthHint < 0) {
            // Compute the size base on 'charWidth' average char widths
            GC gc = new GC(statusLine);
            gc.setFont(statusLine.getFont());
            FontMetrics fm = gc.getFontMetrics();
            widthHint = fm.getAverageCharWidth() * charWidth;
            heightHint = fm.getHeight();
            gc.dispose();
        }

        StatusLineLayoutData data = new StatusLineLayoutData();
        data.widthHint = widthHint;
        label.setLayoutData(data);

        data = new StatusLineLayoutData();
        data.heightHint = heightHint;
        sep.setLayoutData(data);
    }

    /**
     * An accessor for the current location of this status line contribution
     * item -- relative to the display.
     *
     * @return The current location of this status line; <code>null</code> if
     *         not yet initialized.
     */
    public Point getDisplayLocation() {
        if ((label != null) && (statusLine != null)) {
            return statusLine.toDisplay(label.getLocation());
        }

        return null;
    }

    public void setToolTipText(String tooltip) {
        this.tooltip = tooltip;
        if (label != null && !label.isDisposed()) {
            label.setToolTipText(tooltip);
        }

        updateManager();
    }

    /**
     * Retrieves the text that is being displayed in the status line.
     *
     * @return the text that is currently being displayed
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text to be displayed in the status line.
     *
     * @param text
     *            the text to be displayed, must not be <code>null</code>
     */
    public void setText(String text) {
        Assert.isNotNull(text);

        this.text = LegacyActionTools.escapeMnemonics(text);

        if (label != null && !label.isDisposed()) {
            label.setText(this.text);
        }

        updateManager();
    }

    private void updateManager() {
        if (this.text.length() == 0) {
            if (isVisible()) {
                setVisible(false);
                IContributionManager contributionManager = getParent();

                if (contributionManager != null) {
                    contributionManager.update(true);
                }
            }
        } else {
            // Always update if using 'CALC_TRUE_WIDTH'
            if (!isVisible() || charWidth == CALC_TRUE_WIDTH) {
                setVisible(true);
                IContributionManager contributionManager = getParent();

                if (contributionManager != null) {
                    contributionManager.update(true);
                }
            }
        }
    }
}
