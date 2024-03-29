/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.utils;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A contribution item to be used with status line managers. Forked and modified from jface's
 * StatusLineContributionItem, because it does not provide good customization options. (e.g. no tooltip support)
 */
public class StatusLineContributionItem extends ContributionItem {

    /**
     * A constant indicating that the contribution should compute its actual size depending on the text. It will grab
     * all space necessary to display the whole text.
     */
    public final static int CALC_TRUE_WIDTH = -1;

    private int charWidth;
    private CLabel label;
    private String tooltip;
    private Image image;
    private String errorText;
    private String errorDetail;

    /**
     * The composite into which this contribution item has been placed. This will be <code>null</code> if this instance
     * has not yet been initialized.
     */
    private Composite statusLine = null;

    private String text = Util.ZERO_LENGTH_STRING;

    private int widthHint = -1;
    private int heightHint = -1;

    private Listener clickListener;
    private boolean addTrailingSeparator;

    /**
     * Creates a status line contribution item with the given id.
     *
     * @param id
     *            the contribution item's id, or <code>null</code> if it is to have no id
     */
    public StatusLineContributionItem(String id) {
        this(id, CALC_TRUE_WIDTH, false);
    }

    public StatusLineContributionItem(String id, boolean addTrailingSeparator) {
        this(id, CALC_TRUE_WIDTH, addTrailingSeparator);
    }

    /**
     * Creates a status line contribution item with the given id that displays the given number of characters.
     *
     * @param id
     *            the contribution item's id, or <code>null</code> if it is to have no id
     * @param charWidth
     *            the number of characters to display. If the value is CALC_TRUE_WIDTH then the contribution will
     *            compute the preferred size exactly. Otherwise the size will be based on the average character size *
     *            'charWidth'
     */
    public StatusLineContributionItem(String id, int charWidth) {
        this(id, charWidth, false);
    }

    public StatusLineContributionItem(String id, int charWidth, boolean addTrailingSeparator) {
        super(id);
        this.charWidth = charWidth;
        this.addTrailingSeparator = addTrailingSeparator;
        setVisible(false); // no text to start with
    }

    @Override
    public void fill(Composite parent) {
        statusLine = parent;

        var sep = new Label(parent, SWT.SEPARATOR);
        label = new CLabel(statusLine, SWT.SHADOW_NONE);
        updateMessageLabel();

        if (clickListener != null) {
            label.addListener(SWT.MouseDown, clickListener);
        }

        if (charWidth == CALC_TRUE_WIDTH) {
            // compute the size of the label to get the width hint for the
            // contribution
            var preferredSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            widthHint = preferredSize.x;
            heightHint = preferredSize.y;
        } else if (widthHint < 0) {
            // Compute the size base on 'charWidth' average char widths
            var gc = new GC(statusLine);
            gc.setFont(statusLine.getFont());
            var fm = gc.getFontMetrics();
            widthHint = ((int) fm.getAverageCharacterWidth()) * charWidth;
            heightHint = fm.getHeight();
            gc.dispose();
        }

        var data = new StatusLineLayoutData();
        data.widthHint = widthHint;
        label.setLayoutData(data);

        data = new StatusLineLayoutData();
        data.heightHint = heightHint;
        sep.setLayoutData(data);

        if (addTrailingSeparator) {
            sep = new Label(parent, SWT.SEPARATOR);
            data = new StatusLineLayoutData();
            data.heightHint = heightHint;
            sep.setLayoutData(data);
        }
    }

    private void updateMessageLabel() {
        if (label != null && !label.isDisposed()) {
            var display = label.getDisplay();
            if (errorText != null && errorText.length() > 0) {
                label.setForeground(JFaceColors.getErrorText(display));
                label.setText(escape(errorText));
                label.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
                if (errorDetail != null) {
                    label.setToolTipText(escape(errorDetail));
                } else if (tooltip != null) {
                    label.setToolTipText(escape(tooltip));
                } else {
                    label.setToolTipText(null);
                }

            } else {
                label.setForeground(label.getParent().getForeground());
                label.setText(escape(text));
                label.setImage(image);
                if (tooltip != null) {
                    label.setToolTipText(escape(tooltip));
                } else {
                    label.setToolTipText(null);
                }
            }
        }
    }

    public boolean isDisposed() {
        return label == null || label.isDisposed();
    }

    /**
     * An accessor for the current location of this status line contribution item -- relative to the display.
     *
     * @return The current location of this status line; <code>null</code> if not yet initialized.
     */
    public Point getDisplayLocation() {
        if (label != null && statusLine != null) {
            return statusLine.toDisplay(label.getLocation());
        }

        return null;
    }

    public void setToolTipText(String tooltip) {
        this.tooltip = tooltip;
        updateMessageLabel();
        updateManager();
    }

    public void setImage(Image image) {
        this.image = image;
        updateMessageLabel();
        updateManager();
    }

    public void setErrorText(String errorText, String errorDetail) {
        this.errorText = errorText;
        this.errorDetail = errorDetail;
        updateMessageLabel();
        updateManager();
    }

    public void addClickListener(Listener listener) {
        clickListener = listener;
        if (label != null && !label.isDisposed()) {
            label.addListener(SWT.MouseDown, listener);
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
        this.text = text;
        updateMessageLabel();
        updateManager();
    }

    private void updateManager() {
        if (text.length() == 0) {
            if (isVisible()) {
                setVisible(false);
                var contributionManager = getParent();

                if (contributionManager != null) {
                    contributionManager.update(true);
                }
            }
        } else {
            // Always update if using 'CALC_TRUE_WIDTH'
            if (!isVisible() || charWidth == CALC_TRUE_WIDTH) {
                setVisible(true);
                var contributionManager = getParent();

                if (contributionManager != null) {
                    contributionManager.update(true);
                }
            }
        }
    }

    private String escape(String text) {
        if (text == null) {
            return text;
        }
        return LegacyActionTools.escapeMnemonics(text);
    }
}
