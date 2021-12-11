/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util.widgets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.swt.widgets.Composite;

/**
 * An abstract class that handles the pvFormula property.
 *
 */
public class AbstractPVFormulaWidget extends Composite {

    private String pvFormula;
    protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public AbstractPVFormulaWidget(Composite parent, int style) {
        super(parent, style);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public String getPVFormula() {
        return pvFormula;
    }

    public void setPVFormula(String pvFormula) {
        // If new query is the same, don't change -- you may lose the cached result
        if (getPVFormula() != null && getPVFormula().equals(pvFormula)) {
            return;
        }
        if (getPVFormula() == null && pvFormula == null) {
            return;
        }

        var oldValue = this.pvFormula;
        this.pvFormula = pvFormula;
        changeSupport.firePropertyChange("pvFormula", oldValue, pvFormula);
    }

}
