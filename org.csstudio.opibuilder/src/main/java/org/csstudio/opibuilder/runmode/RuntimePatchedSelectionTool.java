/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

/**
 * Custom selection tool for OPI Runtime. Copies PV name to pastebuffer on middle click. Also patches behaviour on
 * right-click.
 */
public class RuntimePatchedSelectionTool extends SelectionTool {

    private Cursor oldCursor = null;
    private boolean cursorChanged = false;

    /**
     * Work around a bug in GEF: right click is recognized as mouse exit event in LightWeightSystem, so handleButtonUp()
     * will not be invoked for right click button up. This will cause unexpected select behavior.
     */
    @Override
    protected boolean handleViewerExited() {
        getCurrentInput().setMouseButton(3, false);
        handleButtonUp(3);
        return super.handleViewerExited();
    }

    /**
     * Intercept middle clicks and copy PV name to pastebuffer if available. Change cursor to copy symbol.
     */
    @Override
    protected boolean handleButtonDown(int button) {
        if (button == 2) {
            var editPart = getTargetEditPart();
            if (editPart instanceof AbstractPVWidgetEditPart) {
                var apvwep = (AbstractPVWidgetEditPart) editPart;
                var pvName = ((AbstractPVWidgetModel) editPart.getModel()).getPVName();
                if (pvName != "" && pvName != null) {
                    var display = Display.getCurrent();
                    var clipboard = new Clipboard(display);

                    // Copies to middle button paste buffer,
                    // to be pasted via another middle-button click
                    clipboard.setContents(new Object[] { pvName }, new Transfer[] { TextTransfer.getInstance() },
                            DND.SELECTION_CLIPBOARD);

                    // Copies to normal clipboard,
                    // to be pasted via Ctrl-V or Edit/Paste
                    clipboard.setContents(new String[] { pvName }, new Transfer[] { TextTransfer.getInstance() });

                    clipboard.dispose();
                    var figure = apvwep.getFigure();
                    oldCursor = figure.getCursor();
                    figure.setCursor(ResourceUtil.getCopyPvCursor());
                    cursorChanged = true;
                }
            }
            return true;
        } else {
            return super.handleButtonDown(button);
        }
    }

    /**
     * Intercept middle clicks and restore original cursor if it has changed.
     */
    @Override
    protected boolean handleButtonUp(int button) {
        if (button == 2) {
            var editPart = getTargetEditPart();
            if (editPart instanceof AbstractPVWidgetEditPart) {
                var apvwep = (AbstractPVWidgetEditPart) editPart;
                var figure = apvwep.getFigure();
                if (cursorChanged) {
                    figure.setCursor(oldCursor);
                    oldCursor = null;
                    cursorChanged = false;
                }
            }
            return true;
        } else {
            return super.handleButtonUp(button);
        }
    }
}
