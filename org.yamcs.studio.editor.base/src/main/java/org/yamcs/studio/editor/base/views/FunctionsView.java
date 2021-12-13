/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.editor.base.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.formula.FormulaFunctionSet;
import org.yamcs.studio.data.formula.FormulaFunctions;
import org.yamcs.studio.data.formula.FormulaRegistry;

public class FunctionsView extends ViewPart {

    private TreeViewer treeViewer;

    @Override
    public void createPartControl(Composite parent) {

        var composite = new Composite(parent, SWT.NONE);
        var tcl_composite = new TreeColumnLayout();
        composite.setLayout(tcl_composite);

        treeViewer = new TreeViewer(composite, SWT.BORDER);
        var tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        var treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        treeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(Object element) {
                return null;
            }

            @Override
            public String getText(Object element) {
                if (element instanceof FormulaFunctionSet) {
                    return ((FormulaFunctionSet) element).getName();
                } else if (element instanceof FormulaFunction) {
                    return FormulaFunctions.formatSignature((FormulaFunction) element);
                }
                return "";
            }
        });
        var trclmnNewColumn = treeViewerColumn.getColumn();
        tcl_composite.setColumnData(trclmnNewColumn, new ColumnWeightData(10, ColumnWeightData.MINIMUM_WIDTH, true));
        trclmnNewColumn.setText("Name");

        var treeViewerColumn_1 = new TreeViewerColumn(treeViewer, SWT.NONE);
        treeViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(Object element) {
                return null;
            }

            @Override
            public String getText(Object element) {
                if (element instanceof FormulaFunction) {
                    return ((FormulaFunction) element).getDescription();
                } else if (element instanceof FormulaFunctionSet) {
                    return ((FormulaFunctionSet) element).getDescription();
                }
                return "";
            }
        });
        var trclmnNewColumn_1 = treeViewerColumn_1.getColumn();
        tcl_composite.setColumnData(trclmnNewColumn_1, new ColumnWeightData(7, ColumnWeightData.MINIMUM_WIDTH, true));
        trclmnNewColumn_1.setText("Description");
        treeViewer.setContentProvider(new FunctionTreeContentProvider());

        List<String> functionSetNames = new ArrayList<>(FormulaRegistry.getDefault().listFunctionSets());
        Collections.sort(functionSetNames);
        List<FormulaFunctionSet> functionSets = new ArrayList<>();
        for (var functionSetName : functionSetNames) {
            functionSets.add(FormulaRegistry.getDefault().findFunctionSet(functionSetName));
        }
        treeViewer.setInput(functionSets);
    }

    @Override
    public void setFocus() {
        treeViewer.getControl().setFocus();
    }
}
