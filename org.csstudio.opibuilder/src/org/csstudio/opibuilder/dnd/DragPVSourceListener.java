/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.dnd;

import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;

public class DragPVSourceListener extends AbstractTransferDragSourceListener {

    public DragPVSourceListener(EditPartViewer viewer) {
        super(viewer, TextTransfer.getInstance());
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        super.dragStart(event);
        List<?> widgetLists = getViewer().getSelectedEditParts();
        if (!widgetLists.isEmpty() && widgetLists.get(0) instanceof AbstractPVWidgetEditPart
                && ((AbstractPVWidgetEditPart) (widgetLists.get(0))).getPV() != null
                && ((AbstractPVWidgetEditPart) (widgetLists.get(0))).getPV().getName().trim().length() > 0) {
            event.doit = true;
        } else {
            event.doit = false;
        }
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        List<?> widgetLists = getViewer().getSelectedEditParts();
        if (!widgetLists.isEmpty() && widgetLists.get(0) instanceof AbstractPVWidgetEditPart
                && ((AbstractPVWidgetEditPart) (widgetLists.get(0))).getPV().getName().trim().length() > 0) {
            var text = ((AbstractPVWidgetEditPart) (widgetLists.get(0))).getPV().getName();
            if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
                event.data = text;
            }
        }

    }
}
