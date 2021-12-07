/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

import java.util.HashMap;
import java.util.Map;

import org.yamcs.studio.data.vtype.VType;

public class FormulaDatasource implements Datasource {

    private Map<String, FormulaData> name2data = new HashMap<>();
    private Map<IPV, FormulaData> pv2data = new HashMap<>();

    @Override
    public boolean supportsPVName(String pvName) {
        return pvName.startsWith("=");
    }

    @Override
    public boolean isConnected(IPV pv) {
        var formulaData = pv2data.get(pv);
        return formulaData != null && formulaData.isConnected();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return false;
    }

    @Override
    public VType getValue(IPV pv) {
        var formulaData = pv2data.get(pv);
        if (formulaData != null) {
            return formulaData.getValue();
        }
        return null;
    }

    @Override
    public void writeValue(IPV pv, Object value, WriteCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onStarted(IPV pv) {
        var formulaString = pv.getName();
        var formulaData = name2data.computeIfAbsent(formulaString, FormulaData::new);
        pv2data.put(pv, formulaData);
        formulaData.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        var formulaData = pv2data.remove(pv);
        if (formulaData != null) {
            formulaData.unregister(pv);
        }
    }
}
