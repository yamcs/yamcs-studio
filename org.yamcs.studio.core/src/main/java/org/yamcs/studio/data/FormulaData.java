package org.yamcs.studio.data;

import java.util.HashSet;
import java.util.Set;

import org.yamcs.studio.data.formula.CompiledFormula;
import org.yamcs.studio.data.vtype.VType;

public class FormulaData implements IPVListener {

    private CompiledFormula formula;

    // PVs that this formula depends on
    private Set<IPV> inputs = new HashSet<>();

    // PVs that depnd on this formula
    private Set<IPV> pvs = new HashSet<>();

    public FormulaData(String formulaString) {
        formula = new CompiledFormula(formulaString);
        for (String pvName : formula.getDependencies()) {
            var pv = PVFactory.getInstance().createPV(pvName);
            pv.addListener(this);
            inputs.add(pv);
        }
    }

    public Set<IPV> getInputs() {
        return inputs;
    }

    public boolean isConnected() {
        for (IPV input : inputs) {
            if (!input.isConnected()) {
                return false;
            }
        }
        return true; // No inputs, or all connected
    }

    public VType getValue() {
        return (VType) formula.execute();
    }

    void register(IPV pv) {
        var startInputPVs = pvs.isEmpty();

        pvs.add(pv);
        if (isConnected()) {
            pv.notifyConnectionChange();
            pv.notifyValueChange();
        }

        if (startInputPVs) {
            for (IPV input : inputs) {
                input.addListener(this);
                input.start();
            }
        }
    }

    void unregister(IPV pv) {
        pvs.remove(pv);

        // Cleanup
        if (pvs.isEmpty()) {
            for (IPV input : inputs) {
                input.removeListener(this);
                input.stop();
            }
            pv.notifyConnectionChange();
            pv.notifyValueChange();
        }
    }

    @Override
    public void connectionChanged(IPV input) {
        pvs.forEach(pv -> pv.notifyConnectionChange());
    }

    @Override
    public void exceptionOccurred(IPV input, Exception exception) {
    }

    @Override
    public void valueChanged(IPV input) {
        formula.updateInput(input.getName(), input.getValue());
        pvs.forEach(pv -> pv.notifyValueChange());
    }

    @Override
    public void writeFinished(IPV input, boolean isWriteSucceeded) {
    }

    @Override
    public void writePermissionChanged(IPV input) {
    }
}
