/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui.content;

import java.awt.Dimension;
import java.util.List;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.yamcs.studio.autocomplete.tooltips.TooltipContent;
import org.yamcs.studio.autocomplete.tooltips.TooltipData;
import org.yamcs.studio.autocomplete.tooltips.TooltipDataHandler;
import org.yamcs.studio.autocomplete.ui.util.SSStyledText;

/**
 * The popup used to display tooltips.
 */
public class ContentHelperPopup extends PopupDialog {

    private final class PopupCloserListener extends SelectionAdapter implements Listener {

        @Override
        public void handleEvent(Event e) {
            // If focus is leaving an important widget or the field's
            // shell is deactivating
            if (e.type == SWT.FocusOut) {
                /*
                 * Ignore this event if it's only happening because focus is
                 * moving between the helper shells or their controls. Do this
                 * in an asynchronous way since the focus is not actually
                 * switched when this event is received.
                 */
                e.display.asyncExec(() -> {
                    if (isValid()) {
                        if (hasFocus()) {
                            return;
                        }
                        // Workaround a problem on X and Mac, whereby at
                        // this point, the focus control is not known.
                        // This can happen, for example, when resizing
                        // the helper shell on the Mac.
                        // Check the active shell.
                        var activeShell = e.display.getActiveShell();
                        if (activeShell == getShell()) {
                            return;
                        }
                        close();
                    }
                });
                return;
            }
            if (e.type == SWT.Resize) {
                // Do not close helper on resize for web version.
                // RAP raise too many resize event
                return;
            }
            // For all other events, merely getting them dictates closure.
            close();
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            close();
        }

        // Install the listeners for events that need to be monitored for
        // helper closure.
        void installListeners() {
            // Listeners on this popup's shell
            getShell().addListener(SWT.Deactivate, this);
            getShell().addListener(SWT.Close, this);

            // Listeners on the target control
            control.addListener(SWT.MouseDoubleClick, this);
            control.addListener(SWT.MouseDown, this);
            control.addListener(SWT.Dispose, this);
            control.addListener(SWT.FocusOut, this);
            // Listeners on the target control's shell
            var controlShell = control.getShell();
            controlShell.addListener(SWT.Move, this);
            controlShell.addListener(SWT.Resize, this);

            control.addListener(SWT.DefaultSelection, this);
            if (control instanceof Text) {
                ((Text) control).addSelectionListener(this);
            }
            if (control instanceof Combo) {
                ((Combo) control).addSelectionListener(this);
            }
        }

        // Remove installed listeners
        void removeListeners() {
            if (isValid()) {
                getShell().removeListener(SWT.Deactivate, this);
                getShell().removeListener(SWT.Close, this);
            }
            if (control != null && !control.isDisposed()) {
                control.removeListener(SWT.MouseDoubleClick, this);
                control.removeListener(SWT.MouseDown, this);
                control.removeListener(SWT.Dispose, this);
                control.removeListener(SWT.FocusOut, this);

                var controlShell = control.getShell();
                controlShell.removeListener(SWT.Move, this);
                controlShell.removeListener(SWT.Resize, this);

                control.removeListener(SWT.DefaultSelection, this);
                if (control instanceof Text) {
                    ((Text) control).removeSelectionListener(this);
                }
                if (control instanceof Combo) {
                    ((Combo) control).removeSelectionListener(this);
                }
            }
        }
    }

    private Dimension margins = new Dimension(8, 8);
    private boolean isOpened = false;
    private boolean canOpen = true;

    /*
     * The listener installed in order to close the helper.
     */
    private PopupCloserListener popupCloser;

    /*
     * The control for which content helper is provided.
     */
    private Control control;

    /*
     * The text controls that displays the text.
     */
    private SSStyledText text;

    /*
     * The data handler used to generate helper content.
     */
    private TooltipDataHandler dataHandler;

    /*
     * The content to show in the helper.
     */
    private TooltipContent content;

    private ContentProposalAdapter adapter;

    /*
     * Construct an helper with the specified parent.
     */
    public ContentHelperPopup(ContentProposalAdapter adapter) {
        super(adapter.getControl().getShell(), SWT.NO_TRIM | SWT.ON_TOP, false, false, false, false, false, null, null);
        this.adapter = adapter;
        control = adapter.getControl();
        dataHandler = new TooltipDataHandler();
    }

    /*
     * Create a text control for showing the helper.
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        // Use the compact margins employed by PopupDialog.
        var gd = new GridData(GridData.BEGINNING);
        gd.horizontalIndent = margins.width;
        gd.verticalIndent = margins.height;
        text = new SSStyledText();
        var c = text.init(parent, SWT.MULTI | SWT.READ_ONLY | SWT.NO_FOCUS, gd);
        updateDisplay();
        return c;
    }

    private void updateDisplay() {
        if (content == null || !isValid()) {
            return;
        }
        text.setText(content.value);
        for (var ps : content.styles) {
            var color = control.getDisplay().getSystemColor(ps.fontColor);
            text.setStyle(color, ps.fontStyle, ps.from, ps.to - ps.from);
        }
    }

    /*
     * Adjust the bounds so that we appear on the top of the control.
     */
    @Override
    protected void adjustBounds() {
        // Get our control's location in display coordinates.
        var location = control.getDisplay().map(control.getParent(), null, control.getLocation());
        var controlX = location.x;
        var controlY = location.y;
        var controlWidht = control.getBounds().width;
        var controlHeight = control.getBounds().height;

        if (content != null) {
            var size = text.getSize();
            controlWidht = size.x + margins.width * 2;
            controlHeight = size.y + margins.height * 2;
        }

        var parentBounds = new Rectangle(controlX, controlY, controlWidht, controlHeight);

        // Try placing the helper on the top
        var proposedBounds = new Rectangle(parentBounds.x + PopupDialog.POPUP_HORIZONTALSPACING,
                parentBounds.y - parentBounds.height - PopupDialog.POPUP_VERTICALSPACING, parentBounds.width,
                parentBounds.height);

        // Constrain to the display
        var constrainedBounds = getConstrainedShellBounds(proposedBounds);

        // If it won't fit on the top, try the bottom
        if (constrainedBounds.intersects(parentBounds)) {
            proposedBounds.y = parentBounds.y + parentBounds.height + PopupDialog.POPUP_VERTICALSPACING;
        }
        proposedBounds.x = constrainedBounds.x;

        getShell().setBounds(proposedBounds);
    }

    @Override
    protected Color getForeground() {
        return control.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
    }

    @Override
    protected Color getBackground() {
        return control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    }

    /**
     * Opens this ContentHelperPopup
     *
     * @return the return code
     */
    @Override
    public int open() {
        if (!canOpen) {
            return 0;
        }
        if (control.isDisposed()) {
            isOpened = false;
            return Window.CANCEL;
        }
        var fieldContent = adapter.getControlContentAdapter().getControlContents(control);
        content = dataHandler.generateTooltipContent(fieldContent);
        if (content == null) {
            isOpened = false;
            return Window.CANCEL;
        }
        var value = super.open();
        if (popupCloser == null) {
            popupCloser = new PopupCloserListener();
        }
        popupCloser.installListeners();
        isOpened = true;
        return value;
    }

    /**
     * Closes this ContentHelperPopup.
     *
     * @return <code>true</code> if the window is (or was already) closed, and <code>false</code> if it is still open
     */
    @Override
    public boolean close() {
        if (!isOpened) {
            return false;
        }
        text.dispose();
        popupCloser.removeListeners();
        isOpened = false;
        return super.close();
    }

    public boolean close(boolean canOpen) {
        this.canOpen = canOpen;
        return close();
    }

    /**
     * Refresh this ContentHelperPopup if already opened.
     *
     * @return <code>true</code> if the window was refreshed, and <code>false</code> if not
     */
    public boolean refresh() {
        if (!isOpened) {
            return false;
        }
        var fieldContent = adapter.getControlContentAdapter().getControlContents(control);
        content = dataHandler.generateTooltipContent(fieldContent);
        if (content == null) {
            close();
            return true;
        }
        updateDisplay();
        adjustBounds();
        return true;
    }

    /*
     * Return whether the helper is opened.
     */
    public boolean isOpened() {
        return isOpened;
    }

    private boolean hasFocus() {
        return text.hasFocus();
    }

    private boolean isValid() {
        return text.isValid();
    }

    public void updateData(List<TooltipData> dataList) {
        if (dataList == null) {
            return;
        }
        for (var data : dataList) {
            dataHandler.addData(data);
        }
        if (control != null && !control.isDisposed()) {
            control.getDisplay().asyncExec(() -> {
                if (!refresh()) {
                    open();
                }
            });
        }
    }

    public void clearData() {
        dataHandler.clearData();
    }
}
