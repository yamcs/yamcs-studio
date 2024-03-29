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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A UI to display two lists of strings with button to move one or more selected strings between the two groups.
 */
public class StringListSelectionWidget extends Composite {

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private org.eclipse.swt.widgets.List unselected;
    private org.eclipse.swt.widgets.List selected;

    public StringListSelectionWidget(Composite parent, int style) {
        super(parent, style);
        setLayout(new FormLayout());

        var composite = new Composite(this, SWT.CENTER);

        composite.setLayout(new GridLayout(1, false));

        var selectButton = new Button(composite, SWT.NONE);
        selectButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        selectButton.setSize(34, 30);
        selectButton.setText("-->");
        selectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (unselected.getSelectionCount() != 0) {
                    List<String> newSelection = new ArrayList<>(selectedValues);
                    newSelection.addAll(Arrays.asList(unselected.getSelection()));
                    setSelectedValues(newSelection);
                    selected.setSelection(selectedValues.size() - 1);
                }
            }
        });

        var upButton = new Button(composite, SWT.NONE);
        upButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        upButton.setText("Move Up");
        upButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selected.getSelectionCount() != 0) {
                    var oldIndex = selected.getSelectionIndex();
                    if (oldIndex != 0) {
                        List<String> newSelection = new ArrayList<>(selectedValues);
                        newSelection.set(oldIndex - 1, selectedValues.get(oldIndex));
                        newSelection.set(oldIndex, selectedValues.get(oldIndex - 1));
                        setSelectedValues(newSelection);
                        selected.setSelection(oldIndex - 1);
                    }
                }
            }
        });

        var downButton = new Button(composite, SWT.NONE);
        downButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        downButton.setText("Move Down");
        downButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selected.getSelectionCount() != 0) {
                    var oldIndex = selected.getSelectionIndex();
                    if (oldIndex != selectedValues.size() - 1) {
                        List<String> newSelection = new ArrayList<>(selectedValues);
                        newSelection.set(oldIndex + 1, selectedValues.get(oldIndex));
                        newSelection.set(oldIndex, selectedValues.get(oldIndex + 1));
                        setSelectedValues(newSelection);
                        selected.setSelection(oldIndex + 1);
                    }
                }
            }
        });

        var unselectButton = new Button(composite, SWT.NONE);
        unselectButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        unselectButton.setText("<--");
        unselectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selected.getSelectionCount() != 0) {
                    List<String> newSelection = new ArrayList<>(selectedValues);
                    newSelection.removeAll(Arrays.asList(selected.getSelection()));
                    setSelectedValues(newSelection);
                }
            }
        });

        var fd_composite = new FormData();
        fd_composite.left = new FormAttachment(50, 100, -50);
        fd_composite.top = new FormAttachment(50, 100, -60);
        composite.setLayoutData(fd_composite);

        unselected = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        unselected.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                if (unselected.getSelectionCount() != 0) {
                    List<String> newSelection = new ArrayList<>(selectedValues);
                    newSelection.addAll(Arrays.asList(unselected.getSelection()));
                    setSelectedValues(newSelection);
                    selected.setSelection(selectedValues.size() - 1);
                }
            }
        });
        var fd_unselected = new FormData();
        fd_unselected.right = new FormAttachment(composite, -5);
        fd_unselected.bottom = new FormAttachment(100, -5);
        fd_unselected.top = new FormAttachment(0, 30);
        fd_unselected.left = new FormAttachment(0, 5);
        unselected.setLayoutData(fd_unselected);

        selected = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        selected.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                if (selected.getSelectionCount() != 0) {
                    List<String> newSelection = new ArrayList<>(selectedValues);
                    newSelection.removeAll(Arrays.asList(selected.getSelection()));
                    setSelectedValues(newSelection);
                }
            }
        });
        var fd_selected = new FormData();
        fd_selected.bottom = new FormAttachment(100, -5);
        fd_selected.right = new FormAttachment(100, -5);
        fd_selected.top = new FormAttachment(0, 30);
        fd_selected.left = new FormAttachment(composite, 5);
        selected.setLayoutData(fd_selected);

        var lblAvailableOptions = new Label(this, SWT.NONE);
        var fd_lblAvailableOptions = new FormData();
        fd_lblAvailableOptions.top = new FormAttachment(0, 5);
        fd_lblAvailableOptions.left = new FormAttachment(unselected, 0, SWT.LEFT);
        lblAvailableOptions.setLayoutData(fd_lblAvailableOptions);
        lblAvailableOptions.setText("Available:");

        var lblSelectedOptions = new Label(this, SWT.NONE);
        var fd_lblSelectedOptions = new FormData();
        fd_lblSelectedOptions.top = new FormAttachment(0, 5);
        fd_lblSelectedOptions.left = new FormAttachment(selected, 0, SWT.LEFT);
        lblSelectedOptions.setLayoutData(fd_lblSelectedOptions);
        lblSelectedOptions.setText("Selected:");
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    private List<String> possibleValues = new ArrayList<>();
    private List<String> selectedValues = new ArrayList<>();

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(Collection<String> possibleValues) {
        this.possibleValues = new ArrayList<>(possibleValues);
        Collections.sort(this.possibleValues);
    }

    public List<String> getSelectedValues() {
        return selectedValues;
    }

    public void setSelectedValues(List<String> selectedValues) {
        // Make a copy of the old properties, make sure the new ones are in the
        // list
        var oldSelectedValues = this.selectedValues;
        this.selectedValues = new ArrayList<>(selectedValues);
        this.selectedValues.retainAll(possibleValues);

        // Change the lists accordingly
        selected.setItems(selectedValues.toArray(new String[selectedValues.size()]));
        List<String> unselectedValues = new ArrayList<>(possibleValues);
        unselectedValues.removeAll(this.selectedValues);
        unselected.setItems(unselectedValues.toArray(new String[unselectedValues.size()]));
        changeSupport.firePropertyChange("selectedValues", oldSelectedValues, this.selectedValues);
    }
}
