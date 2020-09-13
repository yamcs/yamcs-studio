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
        FormulaData formulaData = pv2data.get(pv);
        return formulaData != null && formulaData.isConnected();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return false;
    }

    @Override
    public VType getValue(IPV pv) {
        FormulaData formulaData = pv2data.get(pv);
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
        String formulaString = pv.getName();
        FormulaData formulaData = name2data.computeIfAbsent(formulaString, FormulaData::new);
        pv2data.put(pv, formulaData);
        formulaData.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        FormulaData formulaData = pv2data.remove(pv);
        if (formulaData != null) {
            formulaData.unregister(pv);
        }
    }
}
