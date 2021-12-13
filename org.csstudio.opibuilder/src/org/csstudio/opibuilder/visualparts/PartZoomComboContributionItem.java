/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Tweak {@link ZoomComboContributionItem} to dedicate to a part. A ControlContribution that uses a
 * {@link org.eclipse.swt.widgets.Combo} as its control
 */
public class PartZoomComboContributionItem extends ContributionItem implements ZoomListener {

    private boolean forceSetText;
    private Combo combo;
    private String[] initStrings;
    private ToolItem toolitem;
    private ZoomManager zoomManager;
    @SuppressWarnings("unused")
    private IPartService service;
    private IWorkbenchPart part;

    /**
     * Constructor for ComboToolItem.
     *
     * @param partService
     *            used to add a PartListener
     */
    public PartZoomComboContributionItem(IPartService partService) {
        this(partService, "8888%");
    }

    /**
     * Constructor for ComboToolItem.
     *
     * @param partService
     *            used to add a PartListener
     * @param initString
     *            the initial string displayed in the combo
     */
    public PartZoomComboContributionItem(IPartService partService, String initString) {
        this(partService, new String[] { initString });
    }

    /**
     * Constructor for ComboToolItem.
     *
     * @param partService
     *            used to add a PartListener
     * @param initStrings
     *            the initial string displayed in the combo
     */
    public PartZoomComboContributionItem(IPartService partService, String[] initStrings) {
        super(GEFActionConstants.ZOOM_TOOLBAR_WIDGET);
        this.initStrings = initStrings;
        service = partService;
        Assert.isNotNull(partService);
    }

    private void refresh(boolean repopulateCombo) {
        if (combo == null || combo.isDisposed()) {
            return;
        }
        // $TODO GTK workaround
        try {
            if (zoomManager == null) {
                combo.setEnabled(false);
                combo.setText("");
            } else {
                if (repopulateCombo) {
                    combo.setItems(getZoomManager().getZoomLevelsAsText());
                }
                var zoom = getZoomManager().getZoomAsText();
                var index = combo.indexOf(zoom);
                if (index == -1 || forceSetText) {
                    combo.setText(zoom);
                } else {
                    combo.select(index);
                }
                combo.setEnabled(true);
            }
        } catch (SWTException exception) {
            if (!SWT.getPlatform().equals("gtk")) {
                throw exception;
            }
        }
    }

    /**
     * Computes the width required by control
     *
     * @param control
     *            The control to compute width
     * @return int The width required
     */
    protected int computeWidth(Control control) {
        return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
    }

    /**
     * Creates and returns the control for this contribution item under the given parent composite.
     *
     * @param parent
     *            the parent composite
     * @return the new control
     */
    protected Control createControl(Composite parent) {
        combo = new Combo(parent, SWT.DROP_DOWN);
        combo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleWidgetSelected(e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                handleWidgetDefaultSelected(e);
            }
        });
        combo.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // do nothing
            }

            @Override
            public void focusLost(FocusEvent e) {
                refresh(false);
            }
        });

        // Initialize width of combo
        combo.setItems(initStrings);
        toolitem.setWidth(computeWidth(combo));
        refresh(true);
        return combo;
    }

    @Override
    public void dispose() {
        if (zoomManager != null) {
            zoomManager.removeZoomListener(this);
            zoomManager = null;
        }
        service = null;
        part = null;
        combo = null;
    }

    /**
     * The control item implementation of this <code>IContributionItem</code> method calls the
     * <code>createControl</code> framework method. Subclasses must implement <code>createControl</code> rather than
     * overriding this method.
     */
    @Override
    public void fill(Composite parent) {
        createControl(parent);
    }

    /**
     * The control item implementation of this <code>IContributionItem</code> method throws an exception since controls
     * cannot be added to menus.
     */
    @Override
    public void fill(Menu parent, int index) {
        Assert.isTrue(false, "Can't add a control to a menu");
    }

    /**
     * The control item implementation of this <code>IContributionItem</code> method calls the
     * <code>createControl</code> framework method to create a control under the given parent, and then creates a new
     * tool item to hold it. Subclasses must implement <code>createControl</code> rather than overriding this method.
     */
    @Override
    public void fill(ToolBar parent, int index) {
        toolitem = new ToolItem(parent, SWT.SEPARATOR, index);
        var control = createControl(parent);
        toolitem.setControl(control);
    }

    public ZoomManager getZoomManager() {
        return zoomManager;
    }

    public void setZoomManager(ZoomManager zm) {
        if (zoomManager == zm) {
            return;
        }
        if (zoomManager != null) {
            zoomManager.removeZoomListener(this);
        }

        zoomManager = zm;
        refresh(true);

        if (zoomManager != null) {
            zoomManager.addZoomListener(this);
        }
    }

    private void handleWidgetDefaultSelected(SelectionEvent event) {
        if (zoomManager != null) {
            if (combo.getSelectionIndex() >= 0) {
                zoomManager.setZoomAsText(combo.getItem(combo.getSelectionIndex()));
            } else {
                zoomManager.setZoomAsText(combo.getText());
            }
        }
        /*
         * There are several cases where invoking setZoomAsText (above) will not
         * result in zoomChanged being fired (the method below), such as when
         * the user types "asdf" as the zoom level and hits enter, or when they
         * type in 1%, which is below the minimum limit, and the current zoom is
         * already at the minimum level. Hence, there is no guarantee that
         * refresh() will always be invoked. But we need to invoke it to clear
         * out the invalid text and show the current zoom level. Hence, an
         * (often redundant) invocation to refresh() is made below.
         */
        refresh(false);
    }

    private void handleWidgetSelected(SelectionEvent event) {
        forceSetText = true;
        handleWidgetDefaultSelected(event);
        forceSetText = false;
    }

    @Override
    public void zoomChanged(double zoom) {
        refresh(false);
    }

    /**
     * @param part
     *            a part which must have a ZoomManager Adapter.
     */
    public void setPart(IWorkbenchPart part) {
        if (this.part == part) {
            return;
        }
        this.part = part;
        var newZoomManager = part.getAdapter(ZoomManager.class);
        if (newZoomManager != null) {
            setZoomManager(newZoomManager);
        }
    }
}
