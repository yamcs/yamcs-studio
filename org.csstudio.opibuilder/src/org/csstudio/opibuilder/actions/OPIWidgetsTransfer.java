/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.actions;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * The transfer for clip board related actions.
 */
public class OPIWidgetsTransfer extends ByteArrayTransfer {

    private static OPIWidgetsTransfer instance;

    private static final String TYPE_NAME = "OPIWidgetsTransfer:" + System.currentTimeMillis();

    private static final int TYPEID = registerType(TYPE_NAME);

    public synchronized static OPIWidgetsTransfer getInstance() {
        if (instance == null) {
            instance = new OPIWidgetsTransfer();
        }
        return instance;
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPEID };
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    @Override
    public void javaToNative(Object object, TransferData transferData) {
        if (!isSupportedType(transferData) || !(checkInput(object))) {
            DND.error(DND.ERROR_INVALID_DATA);
        }
        try {
            super.javaToNative((((String) object).getBytes("UTF-8")), transferData);
        } catch (Exception e) {
            ErrorHandlerUtil.handleError("Convert to UTF-8 bytes failed", e);
        }
    }

    @Override
    public Object nativeToJava(TransferData transferData) {
        if (!isSupportedType(transferData)) {
            return null;
        }
        var bytes = (byte[]) super.nativeToJava(transferData);
        if (bytes == null) {
            return null;
        }
        try {
            var displayModel = (DisplayModel) XMLUtil.fillWidgetsFromXMLString(new String(bytes, "UTF-8"), null);
            var widgets = displayModel.getChildren();
            return widgets;
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "Failed to transfer XML to widget", e);
        }
        return null;
    }

    /**
     * Checks the provided input, which must be a non-empty list that contains only objects of type
     * {@link AbstractWidgetModel}.
     *
     * @param input
     *            the input to check
     * @return true, if the input object is valid, false otherwise
     */
    private boolean checkInput(Object input) {

        if (input == null) {
            return false;
        }
        return input instanceof String;
    }
}
