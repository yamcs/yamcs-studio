/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.processor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.client.InstanceFilter;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.YamcsInstance;
import org.yamcs.protobuf.YamcsInstance.InstanceState;
import org.yamcs.studio.core.YamcsPlugin;

public class SwitchProcessorDialog extends TitleAreaDialog {

    private TableViewer processorsTable;
    private ProcessorInfo processorInfo;

    public SwitchProcessorDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Choose a different processor");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var area = (Composite) super.createDialogArea(parent);

        var composite = new Composite(area, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        var tableWrapper = new Composite(composite, SWT.NONE);
        var tcl = new TableColumnLayout();
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableWrapper.setLayout(tcl);

        processorsTable = new TableViewer(tableWrapper, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        processorsTable.getTable().setHeaderVisible(true);
        processorsTable.getTable().setLinesVisible(true);

        var instanceColumn = new TableViewerColumn(processorsTable, SWT.NONE);
        instanceColumn.getColumn().setText("Instance");
        instanceColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var info = (ProcessorInfo) element;
                return info.getInstance();
            }
        });
        tcl.setColumnData(instanceColumn.getColumn(), new ColumnPixelData(100));

        var nameColumn = new TableViewerColumn(processorsTable, SWT.NONE);
        nameColumn.getColumn().setText("Processor");
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var info = (ProcessorInfo) element;
                return info.getName();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(100));

        var typeColumn = new TableViewerColumn(processorsTable, SWT.NONE);
        typeColumn.getColumn().setText("Type");
        typeColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var info = (ProcessorInfo) element;
                return info.getType();
            }
        });
        tcl.setColumnData(typeColumn.getColumn(), new ColumnPixelData(100));

        processorsTable.setContentProvider(ArrayContentProvider.getInstance());
        processorsTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        processorsTable.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 == null || e2 == null) {
                    return 0;
                }
                var p1 = (ProcessorInfo) e1;
                var p2 = (ProcessorInfo) e2;
                var c = p1.getInstance().compareTo(p2.getInstance());
                return (c != 0) ? c : p1.getName().compareTo(p2.getName());
            }
        });
        processorsTable.addSelectionChangedListener(event -> {
            var okButton = getButton(IDialogConstants.OK_ID);
            okButton.setEnabled(!event.getSelection().isEmpty());
        });

        var client = YamcsPlugin.getYamcsClient();
        var filter = new InstanceFilter();
        filter.setState(InstanceState.RUNNING);
        client.listInstances(filter).whenComplete((response, exc) -> {
            parent.getDisplay().asyncExec(() -> {
                if (exc == null) {
                    List<ProcessorInfo> processors = new ArrayList<>();
                    for (YamcsInstance instance : response.getInstancesList()) {
                        processors.addAll(instance.getProcessorsList());
                    }
                    processorsTable.setInput(processors);
                }
            });
        });

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        var okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(false);
    }

    @Override
    protected void okPressed() {
        var sel = (IStructuredSelection) processorsTable.getSelection();
        if (!sel.isEmpty()) {
            processorInfo = (ProcessorInfo) sel.getFirstElement();
        }
        super.okPressed();
    }

    public ProcessorInfo getProcessorInfo() {
        return processorInfo;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
}
