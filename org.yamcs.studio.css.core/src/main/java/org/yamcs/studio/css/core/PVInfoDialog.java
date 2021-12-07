/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.css.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class PVInfoDialog extends Dialog {

    private Map<String, PVInfo> pvInfoByDisplayname = new LinkedHashMap<>();
    private Map<String, Composite> pvCompositesByDisplayname = new LinkedHashMap<>();

    public PVInfoDialog(Shell parentShell, List<PVInfo> pvInfos) {
        super(parentShell);
        for (PVInfo pvInfo : pvInfos) {
            pvInfoByDisplayname.put(pvInfo.getDisplayName(), pvInfo);
        }
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER);
        setBlockOnOpen(false);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // Nothing
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent.getShell().setText("PV Info");

        if (pvInfoByDisplayname.isEmpty()) {
            return super.createDialogArea(parent);
        }

        parent.setLayout(new GridLayout());

        Combo combo = null;
        if (pvInfoByDisplayname.size() > 1) {
            combo = new Combo(parent, SWT.READ_ONLY);
            var gd = new GridData(GridData.FILL_HORIZONTAL);
            combo.setLayoutData(gd);

            for (Entry<String, PVInfo> entry : pvInfoByDisplayname.entrySet()) {
                combo.add(entry.getKey());
            }
        }

        // Placeholder for any selected PVs
        var pvInfoComposite = new Composite(parent, SWT.NONE);
        var gd = new GridData(GridData.FILL_BOTH);
        pvInfoComposite.setLayoutData(gd);
        pvInfoComposite.setLayout(new GridLayout());

        var first = true;
        for (Entry<String, PVInfo> entry : pvInfoByDisplayname.entrySet()) {
            pvCompositesByDisplayname.put(entry.getKey(), createPVComposite(pvInfoComposite, entry.getValue(), first));
            first = false;
        }

        if (combo != null) {
            var finalCombo = combo; // Grrr
            finalCombo.addListener(SWT.Selection, evt -> {
                List<Composite> pvComposites = new ArrayList<>(pvCompositesByDisplayname.values());
                for (var i = 0; i < pvComposites.size(); i++) {
                    var data = ((GridData) pvComposites.get(i).getLayoutData());

                    data.exclude = (i != finalCombo.getSelectionIndex());

                    pvComposites.get(i).setVisible(!data.exclude);
                }

                // FIXME Commented-out, because i can't get the wrap functional right now
                // pvInfoComposite.layout();
                // pvInfoComposite.getShell().setSize(pvInfoComposite.getShell().computeSize(400, SWT.DEFAULT));
                pvInfoComposite.getShell().pack();
            });

            combo.select(0);
        }

        // pvInfoComposite.getShell().setSize(pvInfoComposite.getShell().computeSize(400, SWT.DEFAULT));

        var screenSize = pvInfoComposite.getDisplay().getPrimaryMonitor().getBounds();
        var shellSize = pvInfoComposite.getShell().getBounds();
        pvInfoComposite.getShell().setLocation((screenSize.width - shellSize.width) / 2,
                (screenSize.height - shellSize.height) / 2);

        return pvInfoComposite;
    }

    private Composite createPVComposite(Composite parent, PVInfo pvInfo, boolean first) {
        var pvWrapper = new PVComposite(parent, SWT.NONE, pvInfo);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        pvWrapper.setLayoutData(gd);

        gd.exclude = !first;
        pvWrapper.setVisible(!gd.exclude);

        var gl = new GridLayout(2, false);
        gl.marginTop = 10;
        pvWrapper.setLayout(gl);

        return pvWrapper;
    }

    @Override
    public boolean close() {
        var ret = super.close();
        for (Composite composite : pvCompositesByDisplayname.values()) {
            composite.dispose();
        }
        pvCompositesByDisplayname.clear();
        return ret;
    }
}
