/********************************************************************************
 * Copyright (c) 2011, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.dnd;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;

/**
 * General purpose utility to allowing Drag-and-Drop "Drop" of any adaptable or serializable object.
 * <p>
 * Filters the received items to match the desired type, based on the order or preference specified. Can also accept
 * plain text.
 */
abstract public class ControlSystemDropTarget {
    final private DropTarget target;

    /**
     * Initialize 'drop' target
     *
     * @param control
     *            Control onto which items may be dropped
     * @param accepted
     *            (Base) class of accepted items
     */
    public ControlSystemDropTarget(Control control, Class<?>... accepted) {
        target = new DropTarget(control, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);

        var supportedTransfers = new ArrayList<Transfer>();
        for (var clazz : accepted) {
            if (clazz == String.class) {
                supportedTransfers.add(TextTransfer.getInstance());
            }
            if (clazz == File.class) {
                supportedTransfers.add(FileTransfer.getInstance());
            } else {
                var xfer = SerializableItemTransfer.getTransfer(clazz.getName());
                if (xfer != null) {
                    supportedTransfers.add(xfer);
                }
            }
        }
        target.setTransfer(supportedTransfers.toArray(new Transfer[supportedTransfers.size()]));

        target.addDropListener(new DropTargetAdapter() {
            /**
             * Used internally by the system when a DnD operation enters the control.
             */
            @Override
            public void dragEnter(DropTargetEvent event) {
                // Seems DropTarget it is not honoring the order of the transferData:
                // Making sure is right
                var done = false;
                for (var transfer : target.getTransfer()) {
                    for (var data : event.dataTypes) {
                        if (transfer.isSupportedType(data)) {
                            event.currentDataType = data;
                            done = true;
                            break;
                        }
                    }
                    if (done) {
                        break;
                    }
                }

                if ((event.operations & DND.DROP_COPY) != 0) {
                    event.detail = DND.DROP_COPY;
                } else {
                    event.detail = DND.DROP_NONE;
                }
            }

            /**
             * Data was dropped into the target. Check the actual type, handle received data.
             */
            @Override
            public void drop(DropTargetEvent event) {
                handleDrop(event.data);
            }
        });
    }

    /**
     * To be implemented by derived class.
     *
     * Will be called for each 'dropped' item that has the accepted data type
     *
     * @param item
     *            Control system item
     */
    abstract public void handleDrop(Object item);
}
