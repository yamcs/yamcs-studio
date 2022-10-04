/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_HEIGHT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_VISIBLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_WIDTH;
import static org.csstudio.opibuilder.widgets.model.GroupingContainerModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.TabModel.PROP_ACTIVE_TAB;
import static org.csstudio.opibuilder.widgets.model.TabModel.PROP_HORIZONTAL_TABS;
import static org.csstudio.opibuilder.widgets.model.TabModel.PROP_MINIMUM_TAB_HEIGHT;
import static org.csstudio.opibuilder.widgets.model.TabModel.PROP_TAB_COUNT;

import java.util.LinkedList;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.opibuilder.widgets.model.GroupingContainerModel;
import org.csstudio.opibuilder.widgets.model.TabModel;
import org.csstudio.opibuilder.widgets.model.TabModel.ITabItemHandler;
import org.csstudio.opibuilder.widgets.model.TabModel.TabProperty;
import org.csstudio.swt.widgets.figures.TabFigure;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

/**
 * The editpart of tab widget.
 */
public class TabEditPart extends AbstractContainerEditpart {

    class TabPropertyChangeHandler implements IWidgetPropertyChangeHandler {
        private int tabIndex;
        private TabProperty tabProperty;

        public TabPropertyChangeHandler(int tabIndex, TabProperty tabProperty) {
            this.tabIndex = tabIndex;
            this.tabProperty = tabProperty;
        }

        @Override
        public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
            setTabProperty(tabIndex, tabProperty, newValue);

            return true;
        }
    }

    public static GroupingContainerModel createGroupingContainer() {
        var groupingContainerModel = new GroupingContainerModel();
        groupingContainerModel.setName("Tab");
        groupingContainerModel.setLocation(1, 1);
        groupingContainerModel.setBorderStyle(BorderStyle.NONE);
        groupingContainerModel.setPropertyValue(PROP_TRANSPARENT, true);
        groupingContainerModel.setPropertyValue(PROP_VISIBLE, false);
        return groupingContainerModel;
    }

    private LinkedList<TabItem> tabItemList = new LinkedList<>();

    @Override
    public void activate() {
        getWidgetModel().setTabItemHandler(new ITabItemHandler() {
            @Override
            public void addTab(int index, TabItem tabItem) {
                TabEditPart.this.addTab(index, tabItem);
            }

            @Override
            public void removeTab(int index) {
                TabEditPart.this.removeTab(index);
            }
        });
        super.activate();

        UIBundlingThread.getInstance().addRunnable(() -> {
            // add initial tab
            var j = getTabFigure().getTabAmount();
            while (j < getWidgetModel().getTabsAmount()) {
                addTab();
                j++;
            }
        });

        UIBundlingThread.getInstance().addRunnable(() -> {
            var index = getWidgetModel().getActiveTab();
            getTabFigure().setActiveTabIndex(index);
            getWidgetModel().getChildren().get(index).setPropertyValue(PROP_VISIBLE, true);
        });
    }

    public synchronized void addTab() {
        var tabIndex = getWidgetModel().getChildren().size();
        addTab(tabIndex, new TabItem(getWidgetModel(), tabIndex));
    }

    /**
     * Add a TabItem to the index;
     *
     * @param index
     * @param tabItem
     */
    public synchronized void addTab(int index, TabItem tabItem) {

        if (index < 0 || index > getTabItemCount()) {
            throw new IllegalArgumentException();
        }

        if (index >= TabModel.MAX_TABS_AMOUNT) {
            return;
        }
        var groupingContainerModel = tabItem.getGroupingContainerModel();

        getWidgetModel().addChild(index, groupingContainerModel);

        getTabFigure().addTab((String) tabItem.getPropertyValue(TabProperty.TITLE), index);
        tabItemList.add(index, tabItem);

        initTabLabel(index, tabItem);

        rightShiftTabProperties(index);

        // apply tab properties from TabItem to TabModel
        for (var tabProperty : TabProperty.values()) {
            var propID = TabModel.makeTabPropID(tabProperty.propIDPre, index);
            getWidgetModel().setPropertyValue(propID, tabItem.getPropertyValue(tabProperty));
        }

        // update property sheet
        getWidgetModel().setPropertyValue(PROP_TAB_COUNT, getWidgetModel().getChildren().size(), false);

        for (var tabProperty : TabProperty.values()) {
            var propID = TabModel.makeTabPropID(tabProperty.propIDPre, getWidgetModel().getChildren().size() - 1);
            getWidgetModel().setPropertyVisible(propID, true);
        }

        // update active tab index to the new added tab
        updateTabAreaSize();

        setActiveTabIndex(index);
    }

    /*
     * Overidden, to set the selection behaviour of child EditParts.
     */
    @Override
    protected final EditPart createChild(Object model) {
        var result = super.createChild(model);

        // setup selection behavior for the new child
        if (result instanceof AbstractBaseEditPart) {
            ((AbstractBaseEditPart) result).setSelectable(false);
        }

        return result;
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.CONTAINER_ROLE, null);
        installEditPolicy(EditPolicy.LAYOUT_ROLE, null);
    }

    @Override
    public void deactivate() {
        getTabFigure().dispose();
        super.deactivate();
    }

    @Override
    protected IFigure doCreateFigure() {
        var tabFigure = new TabFigure();
        tabFigure.setHorizontal(getWidgetModel().isHorizontal());
        tabFigure.setMinimumTabHeight(getWidgetModel().getMinimumTabHeight());
        tabFigure.addTabListener((oldIndex, newIndex) -> {
            for (var child : getWidgetModel().getChildren()) {
                child.setPropertyValue(PROP_VISIBLE, false);
            }
            getWidgetModel().getChildren().get(newIndex).setPropertyValue(PROP_VISIBLE, true);
            // if (getExecutionMode() == ExecutionMode.RUN_MODE)
            // setPropertyValue(TabModel.PROP_ACTIVE_TAB, newIndex);
        });

        return tabFigure;
    }

    /**
     * Get the index of the active tab.
     *
     * @return the index of the active tab. Index starts from 0.
     */
    public int getActiveTabIndex() {
        return getTabFigure().getActiveTabIndex();
    }

    @Override
    public IFigure getContentPane() {
        return getTabFigure().getContentPane();
    }

    public GroupingContainerModel getGroupingContainer(int index) {
        return (GroupingContainerModel) getWidgetModel().getChildren().get(index);
    }

    private Dimension getTabAreaSize() {
        var insets = getTabFigure().getInsets();
        if (getWidgetModel().isHorizontal()) {
            return new Dimension(getWidgetModel().getWidth() - 2 - insets.left - insets.right,
                    getWidgetModel().getHeight() - 2 - insets.top - getTabFigure().getTabLabelHeight() - insets.bottom);
        } else {
            return new Dimension(
                    getWidgetModel().getWidth() - 2 - insets.left - getTabFigure().getTabLabelWidth() - insets.right,
                    getWidgetModel().getHeight() - 2 - insets.top - insets.bottom);
        }
    }

    private TabFigure getTabFigure() {
        return (TabFigure) getFigure();
    }

    public TabItem getTabItem(int tabIndex) {
        return tabItemList.get(tabIndex);
    }

    public int getTabItemCount() {
        return tabItemList.size();
    }

    public Label getTabLabel(int index) {
        return getTabFigure().getTabLabel(index);
    }

    @Override
    public TabModel getWidgetModel() {
        return (TabModel) getModel();
    }

    private void initTabLabel(int index, TabItem tabItem) {
        for (var tabProperty : TabProperty.values()) {
            var propValue = tabItem.getPropertyValue(tabProperty);
            setTabFigureProperty(index, tabProperty, propValue);
        }
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        // init tabs
        var i = 0;
        for (var child : getWidgetModel().getChildren()) {
            if (child instanceof GroupingContainerModel) {
                child.setPropertyValue(PROP_VISIBLE, true);
                child.setPropertyValue(PROP_VISIBLE, false);
                getTabFigure().addTab((String) getWidgetModel()
                        .getPropertyValue(TabModel.makeTabPropID(TabProperty.TITLE.propIDPre, i)));
                tabItemList.add(i, new TabItem(getWidgetModel(), i, (GroupingContainerModel) child));
                for (var tabProperty : TabProperty.values()) {
                    setTabProperty(i, tabProperty,
                            getWidgetModel().getPropertyValue(TabModel.makeTabPropID(tabProperty.propIDPre, i)));
                }

                i++;
            }
        }
        IWidgetPropertyChangeHandler relocContainerHandler = (oldValue, newValue, figure) -> {
            updateTabAreaSize();
            refreshVisuals();
            return false;
        };
        setPropertyChangeHandler(PROP_WIDTH, relocContainerHandler);
        setPropertyChangeHandler(PROP_HEIGHT, relocContainerHandler);

        setPropertyChangeHandler(PROP_HORIZONTAL_TABS, (oldValue, newValue, figure) -> {
            ((TabFigure) figure).setHorizontal((Boolean) newValue);
            updateTabAreaSize();
            refreshVisuals();
            return false;
        });

        setPropertyChangeHandler(PROP_ACTIVE_TAB, (oldValue, newValue, figure) -> {
            ((TabFigure) figure).setActiveTabIndex((Integer) newValue);
            updateTabAreaSize();
            refreshVisuals();
            return false;
        });

        setPropertyChangeHandler(PROP_MINIMUM_TAB_HEIGHT, (oldValue, newValue, figure) -> {
            ((TabFigure) figure).setMinimumTabHeight((Integer) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_BORDER_WIDTH, (oldValue, newValue, figure) -> {
            updateTabAreaSize();
            return false;
        });

        registerTabPropertyChangeHandlers();
        registerTabsAmountChangeHandler();
    }

    private void registerTabPropertyChangeHandlers() {
        // set prop handlers and init all the potential tabs
        for (var i = 0; i < TabModel.MAX_TABS_AMOUNT; i++) {
            for (var tabProperty : TabProperty.values()) {
                var propID = TabModel.makeTabPropID(tabProperty.propIDPre, i);
                setPropertyChangeHandler(propID, new TabPropertyChangeHandler(i, tabProperty));
            }
        }

        for (var i = TabModel.MAX_TABS_AMOUNT - 1; i >= getWidgetModel().getTabsAmount(); i--) {
            for (var tabProperty : TabProperty.values()) {
                var propID = TabModel.makeTabPropID(tabProperty.propIDPre, i);
                getWidgetModel().setPropertyVisible(propID, false);
            }
        }
    }

    private void registerTabsAmountChangeHandler() {
        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            var model = getWidgetModel();
            var figure = (TabFigure) refreshableFigure;
            var currentTabAmount = figure.getTabAmount();
            // add tabs
            if ((Integer) newValue > currentTabAmount) {
                for (var i1 = 0; i1 < (Integer) newValue - currentTabAmount; i1++) {
                    for (var tabProperty1 : TabProperty.values()) {
                        var propID1 = TabModel.makeTabPropID(tabProperty1.propIDPre, i1 + currentTabAmount);
                        model.setPropertyVisible(propID1, true);
                    }
                    addTab();
                }
            } else if ((Integer) newValue < currentTabAmount) { // remove tabs
                for (var i2 = currentTabAmount - 1; i2 >= (Integer) newValue; i2--) {
                    for (var tabProperty2 : TabProperty.values()) {
                        var propID2 = TabModel.makeTabPropID(tabProperty2.propIDPre, i2);
                        model.setPropertyVisible(propID2, false);
                    }
                    removeTab();
                }
                setActiveTabIndex(0);
            }
            return true;
        };
        getWidgetModel().getProperty(PROP_TAB_COUNT).addPropertyChangeListener(
                evt -> handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));
    }

    public synchronized void removeTab() {
        removeTab(getTabItemCount() - 1);
        // getWidgetModel().removeChild(
        // getWidgetModel().getChildren().get(getWidgetModel().getChildren().size()-1));
        // getTabFigure().removeTab();
        // updateTabAreaSize();
        // tabItemList.removeLast();
    }

    public synchronized void removeTab(int index) {
        // setActiveTabIndex(index > 0 ? index-1 : getWidgetModel().getChildren().size()-1);
        if (index < 0 || index >= getTabItemCount()) {
            throw new IllegalArgumentException();
        }
        getWidgetModel().removeChild(getWidgetModel().getChildren().get(index));
        getTabFigure().removeTab(index);
        tabItemList.remove(index);

        // left shift tab's properties
        for (var j = index; j < getWidgetModel().getChildren().size(); j++) {
            for (var tabProperty : TabProperty.values()) {
                var propID1 = TabModel.makeTabPropID(tabProperty.propIDPre, j);
                var propID2 = TabModel.makeTabPropID(tabProperty.propIDPre, j + 1);
                getWidgetModel().setPropertyValue(propID1, getWidgetModel().getPropertyValue(propID2));
            }
        }
        // updateTabItemsWithModel();

        // update property sheet
        getWidgetModel().setPropertyValue(PROP_TAB_COUNT, getWidgetModel().getChildren().size(), false);

        for (var tabProperty : TabProperty.values()) {
            var propID = TabModel.makeTabPropID(tabProperty.propIDPre, getWidgetModel().getChildren().size());
            getWidgetModel().setPropertyVisible(propID, false);
        }

        // update active tab index to the new added tab
        updateTabAreaSize();

        setActiveTabIndex(index >= getWidgetModel().getChildren().size() ? index - 1 : index);
    }

    private void rightShiftTabProperties(int index) {
        for (var j = getWidgetModel().getChildren().size() - 1; j > index; j--) {
            for (var tabProperty : TabProperty.values()) {
                var propID1 = TabModel.makeTabPropID(tabProperty.propIDPre, j - 1);
                var propID2 = TabModel.makeTabPropID(tabProperty.propIDPre, j);
                getWidgetModel().setPropertyValue(propID2, getWidgetModel().getPropertyValue(propID1));
            }
        }
    }

    /**
     * Show tab in this index.
     *
     * @param index
     *            the index of the tab to be shown. Index starts from 0.
     */
    public void setActiveTabIndex(int index) {
        getTabFigure().setActiveTabIndex(index);
    }

    private void setTabFigureProperty(int index, TabProperty tabProperty, Object newValue) {
        var label = getTabFigure().getTabLabel(index);
        switch (tabProperty) {
        case TITLE:
            label.setText((String) newValue);
            updateTabAreaSize();
            break;
        case FONT:
            label.setFont(((OPIFont) newValue).getSWTFont());
            updateTabAreaSize();
            break;
        case BACKCOLOR:
            getTabFigure().setTabColor(index, ((OPIColor) newValue).getSWTColor());
            break;
        case FORECOLOR:
            label.setForegroundColor(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            break;
        case ENABLED:
            getTabFigure().setTabEnabled(index, (Boolean) newValue);
            break;
        case ICON_PATH:
            var iconPath = Path.fromPortableString((String) newValue);
            getTabFigure().setIconPath(index, getWidgetModel().toAbsolutePath(iconPath), e -> {
                var message = "Failed to load image from " + newValue + "\n" + e;
                OPIBuilderPlugin.getLogger().log(Level.SEVERE, message, e);
            });
            break;
        default:
            break;
        }
    }

    private void setTabProperty(int index, TabProperty tabProperty, Object newValue) {
        setTabFigureProperty(index, tabProperty, newValue);
        getTabItem(index).setPropertyValue(tabProperty, newValue);
    }

    private void updateTabAreaSize() {
        UIBundlingThread.getInstance().addRunnable(() -> {
            var tabAreaSize = getTabAreaSize();
            for (var child : getWidgetModel().getChildren()) {
                child.setSize(tabAreaSize);
            }
        });
    }
}
