/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import static java.util.Arrays.asList;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_STYLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_HEIGHT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_SCALE_OPTIONS;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_TOOLTIP;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_XPOS;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_YPOS;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_ALARM_PULSING;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_BACKCOLOR_ALARMSENSITIVE;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_BORDER_ALARMSENSITIVE;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_FORECOLOR_ALARMSENSITIVE;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.ArrayModel.PROP_ARRAY_LENGTH;
import static org.csstudio.opibuilder.widgets.model.ArrayModel.PROP_DATA_TYPE;
import static org.csstudio.opibuilder.widgets.model.ArrayModel.PROP_HORIZONTAL;
import static org.csstudio.opibuilder.widgets.model.ArrayModel.PROP_SHOW_SCROLLBAR;
import static org.csstudio.opibuilder.widgets.model.ArrayModel.PROP_SHOW_SPINNER;
import static org.csstudio.opibuilder.widgets.model.ArrayModel.PROP_SPINNER_WIDTH;
import static org.csstudio.opibuilder.widgets.model.ArrayModel.PROP_VISIBLE_ELEMENTS_COUNT;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.csstudio.csdata.ProcessVariable;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.csstudio.opibuilder.editparts.ConnectionHandler;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.editparts.IPVWidgetEditpart;
import org.csstudio.opibuilder.editparts.PVWidgetConnectionHandler;
import org.csstudio.opibuilder.editparts.PVWidgetEditpartDelegate;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.IWidgetInfoProvider;
import org.csstudio.opibuilder.model.NonExistPropertyException;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.widgets.model.ArrayModel;
import org.csstudio.opibuilder.widgets.model.ArrayModel.ArrayDataType;
import org.csstudio.swt.widgets.datadefinition.ByteArrayWrapper;
import org.csstudio.swt.widgets.datadefinition.DoubleArrayWrapper;
import org.csstudio.swt.widgets.datadefinition.FloatArrayWrapper;
import org.csstudio.swt.widgets.datadefinition.IPrimaryArrayWrapper;
import org.csstudio.swt.widgets.datadefinition.IntArrayWrapper;
import org.csstudio.swt.widgets.datadefinition.LongArrayWrapper;
import org.csstudio.swt.widgets.datadefinition.ShortArrayWrapper;
import org.csstudio.swt.widgets.figures.ArrayFigure;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.MouseEvent;
import org.yamcs.studio.data.BasicDataType;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.VString;
import org.yamcs.studio.data.vtype.VStringArray;
import org.yamcs.studio.data.vtype.VType;

/**
 * Editpart for array widget.
 */
public class ArrayEditPart extends AbstractContainerEditpart implements IPVWidgetEditpart {

    private static final String EMPTY_STRING = "";

    /**
     * A flag that is held to true when it is in sync.
     */
    private volatile boolean inSync, keepElementSize;

    private PVWidgetEditpartDelegate delegate;

    private Object valueArray;
    private Object elementDefaultValue;

    IPVListener pvDataTypeListener;

    private static List<String> NONE_SYNCABLE_PROPIDS = asList(
            PROP_XPOS,
            PROP_YPOS,
            PROP_PVVALUE,
            PROP_TOOLTIP);

    private static List<String> INVISIBLE_CHILD_PROPIDS = asList(
            PROP_XPOS,
            PROP_YPOS,
            PROP_SCALE_OPTIONS,
            PROP_PVNAME,
            PROP_TOOLTIP,
            PROP_BACKCOLOR_ALARMSENSITIVE,
            PROP_BORDER_ALARMSENSITIVE,
            PROP_FORECOLOR_ALARMSENSITIVE,
            PROP_ALARM_PULSING);

    private List<String> unSyncablePropIDsFromChild;
    private PropertyChangeListener syncPropertiesListener = evt -> syncAllChildrenProperties(evt.getPropertyName(),
            evt.getNewValue());

    public ArrayEditPart() {
        delegate = new PVWidgetEditpartDelegate(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doActivate() {
        delegate.markAsControlPV(PROP_PVNAME, PROP_PVVALUE);
        delegate.setUpdateSuppressTime(500);
        super.doActivate();
        delegate.doActivate();
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            if (!getChildren().isEmpty()) {
                elementDefaultValue = ((AbstractBaseEditPart) getChildren().get(0)).getValue();
                initValueArray();
                setValue(getValue());
                var provider = getWidgetModel().getChildren().get(0)
                        .getAdapter(IWidgetInfoProvider.class);
                if (provider != null) {
                    var info = provider.getInfo(ArrayModel.ARRAY_UNIQUEPROP_ID);
                    if (info != null && info instanceof List<?>) {
                        unSyncablePropIDsFromChild = new ArrayList<>((List<String>) info);
                        unSyncablePropIDsFromChild.addAll(NONE_SYNCABLE_PROPIDS);
                    }
                }
            }
            registerLoadPVDataTypeListener();
        }
    }

    @Override
    public void activate() {
        super.activate();
        delegate.startPVs();
    }

    protected void initValueArray() {
        var length = getWidgetModel().getArrayLength();
        switch (getWidgetModel().getDataType()) {
        case STRING_ARRAY:
            if (valueArray != null && valueArray instanceof String[] && ((String[]) valueArray).length == length) {
                break;
            }
            valueArray = new String[length];
            Arrays.fill((String[]) valueArray, EMPTY_STRING);
            break;
        case OBJECT_ARRAY:
            if (valueArray != null && valueArray instanceof Object[] && ((Object[]) valueArray).length == length) {
                break;
            }
            valueArray = new Object[length];
            Arrays.fill((Object[]) valueArray, elementDefaultValue);
            break;
        case BYTE_ARRAY:
            if (valueArray != null && valueArray instanceof byte[] && ((byte[]) valueArray).length == length) {
                break;
            }
            valueArray = new byte[length];
            break;
        case DOUBLE_ARRAY:
            if (valueArray != null && valueArray instanceof double[] && ((double[]) valueArray).length == length) {
                break;
            }
            valueArray = new double[length];
            break;
        case FLOAT_ARRAY:
            if (valueArray != null && valueArray instanceof float[] && ((float[]) valueArray).length == length) {
                break;
            }
            valueArray = new float[length];
            break;
        case INT_ARRAY:
            if (valueArray != null && valueArray instanceof int[] && ((int[]) valueArray).length == length) {
                break;
            }
            valueArray = new int[length];
            break;
        case LONG_ARRAY:
            if (valueArray != null && valueArray instanceof long[] && ((long[]) valueArray).length == length) {
                break;
            }
            valueArray = new long[length];
            break;
        case SHORT_ARRAY:
            if (valueArray != null && valueArray instanceof short[] && ((short[]) valueArray).length == length) {
                break;
            }
            valueArray = new short[length];
            break;
        default:
            break;
        }
    }

    private void registerLoadPVDataTypeListener() {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            var model = getWidgetModel();
            var pv = getPV();
            if (pv != null) {
                if (pvDataTypeListener == null) {
                    pvDataTypeListener = new IPVListener() {
                        @Override
                        public void valueChanged(IPV pv) {
                            var value = pv.getValue();
                            if (value != null) {
                                model.setArrayLength(VTypeHelper.getSize(value));
                                var dataType = VTypeHelper.getBasicDataType(value);
                                model.setPropertyValue(PROP_DATA_TYPE,
                                        mapBasicDataTypeToArrayType(dataType));

                            }
                        }
                    };
                }
                pv.addListener(pvDataTypeListener);
            }
        }
    }

    @Override
    public Border calculateBorder() {
        var border = delegate.calculateBorder();
        if (border == null) {
            return super.calculateBorder();
        } else {
            return border;
        }
    }

    @Override
    protected EditPart createChild(Object model) {
        var child = (AbstractWidgetModel) model;
        for (var propId : child.getAllPropertyIDs()) {
            child.getProperty(propId).addPropertyChangeListener(syncPropertiesListener);
        }
        var result = super.createChild(model);
        UIBundlingThread.getInstance().addRunnable(getViewer().getControl().getDisplay(),
                () -> hookChild(result, getChildren().indexOf(result), true));

        return result;
    }

    @Override
    protected void removeChild(EditPart child) {
        super.removeChild(child);
        var childModel = ((AbstractBaseEditPart) child).getWidgetModel();
        // recover property visibility
        if (getExecutionMode() == ExecutionMode.EDIT_MODE) {
            for (var propId : INVISIBLE_CHILD_PROPIDS) {
                try {
                    childModel.setPropertyVisibleAndSavable(propId, true, true);
                } catch (NonExistPropertyException e) {
                }
            }
        }
    }

    /**
     * Hook child with array index
     *
     * @param editPart
     */
    protected void hookChild(EditPart editPart, int indexOfArrayChild, boolean directChild) {
        if (editPart instanceof AbstractContainerEditpart) {
            for (var grandChild : ((AbstractContainerEditpart) editPart).getChildren()) {
                hookChild((EditPart) grandChild, indexOfArrayChild, false);
            }
        }
        var childModel = ((AbstractBaseEditPart) editPart).getWidgetModel();
        if (directChild) {
            if (getExecutionMode() == ExecutionMode.EDIT_MODE) {
                for (var propId : INVISIBLE_CHILD_PROPIDS) {
                    try {
                        childModel.setPropertyVisibleAndSavable(propId, false, true);
                    } catch (NonExistPropertyException e) {
                    }
                }
            }
            try {
                childModel.setScaleOptions(false, false, false);
                childModel.setPropertyValue(PROP_PVNAME, "");
                childModel.setPropertyValue(PROP_BORDER_ALARMSENSITIVE, false);
            } catch (NonExistPropertyException e) {
            }
        }

        if (getExecutionMode() == ExecutionMode.RUN_MODE && editPart instanceof IPVWidgetEditpart) {
            ((IPVWidgetEditpart) editPart).addSetPVValueListener((pvPropId, value) -> {
                var index = getArrayFigure().getIndex() + indexOfArrayChild;
                try {
                    var dataType = getWidgetModel().getDataType();
                    switch (dataType) {
                    case OBJECT_ARRAY:
                        ((Object[]) valueArray)[index] = value;
                        break;
                    case DOUBLE_ARRAY:
                        double doubleValue;
                        if (value instanceof Number) {
                            doubleValue = ((Number) value).doubleValue();
                        } else {
                            doubleValue = Double.valueOf(value.toString());
                        }
                        ((double[]) valueArray)[index] = doubleValue;
                        break;
                    case BYTE_ARRAY:
                        byte byteValue;
                        if (value instanceof Number) {
                            byteValue = ((Number) value).byteValue();
                        } else {
                            byteValue = Byte.valueOf(value.toString());
                        }
                        ((byte[]) valueArray)[index] = byteValue;
                        break;
                    case INT_ARRAY:
                        int intValue;
                        if (value instanceof Number) {
                            intValue = ((Number) value).intValue();
                        } else {
                            intValue = Byte.valueOf(value.toString());
                        }
                        ((int[]) valueArray)[index] = intValue;
                        break;
                    case SHORT_ARRAY:
                        short shortValue;
                        if (value instanceof Number) {
                            shortValue = ((Number) value).shortValue();
                        } else {
                            shortValue = Short.valueOf(value.toString());
                        }
                        ((short[]) valueArray)[index] = shortValue;
                        break;
                    case FLOAT_ARRAY:
                        float floatValue;
                        if (value instanceof Number) {
                            floatValue = ((Number) value).floatValue();
                        } else {
                            floatValue = Float.valueOf(value.toString());
                        }
                        ((float[]) valueArray)[index] = floatValue;
                        break;
                    case LONG_ARRAY:
                        long longValue;
                        if (value instanceof Number) {
                            longValue = ((Number) value).longValue();
                        } else {
                            longValue = Long.valueOf(value.toString());
                        }
                        ((long[]) valueArray)[index] = longValue;
                        break;
                    case STRING_ARRAY:
                        ((String[]) valueArray)[index] = value.toString();
                        break;
                    default:
                        break;
                    }
                    if (getPV() != null) {
                        // TODO this code is a patch for utility pv, because it will convert a int[] and short[] to
                        // long[],
                        // but EPICS PV doesn't support write long[]. should be removed after switched to pv manager
                        if (valueArray instanceof long[]) {
                            var temp = new int[((long[]) valueArray).length];
                            for (var i = 0; i < ((long[]) valueArray).length; i++) {
                                temp[i] = (int) ((long[]) valueArray)[i];
                            }
                            setPVValue(PROP_PVNAME, temp);
                        } else {
                            setPVValue(PROP_PVNAME, valueArray);
                        }
                    }
                } catch (NumberFormatException e) {
                    var msg = NLS.bind("Writing failed: The input data {0} is not compatible with array data type.",
                            value.toString());
                    // recover the original data in children widgets.
                    setValue(getValue());
                    ErrorHandlerUtil.handleError(msg, e);
                }
            });
        }
    }

    @Override
    protected ConnectionHandler createConnectionHandler() {
        return new PVWidgetConnectionHandler(this);
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        if (getExecutionMode() == ExecutionMode.EDIT_MODE) {
            installEditPolicy(EditPolicy.LAYOUT_ROLE, new ArrayLayoutEditPolicy());
            installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new ContainerHighlightEditPolicy());
        }
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ArraySpinnerDirectEditPolicy());
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            delegate.doDeActivate();
            super.deactivate();
        }
    }

    @Override
    protected IFigure doCreateFigure() {
        var figure = new ArrayFigure();
        var model = getWidgetModel();
        figure.setArrayLength(model.getArrayLength());
        figure.setHorizontal(model.isHorizontal());
        figure.setSpinnerWidth(model.getSpinnerWidth());
        figure.setShowSpinner(model.isShowSpinner());
        figure.setShowScrollbar(model.isShowScrollbar());
        figure.setIndex(0);
        figure.addIndexChangeListener(newValue -> setValue(getValue()));
        updatePropSheet();
        return figure;
    }

    private void updatePropSheet() {
        getWidgetModel().setPropertyVisible(PROP_DATA_TYPE, getPVName().trim().isEmpty());
        getWidgetModel().setPropertyVisible(PROP_ARRAY_LENGTH, getPVName().trim().isEmpty());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class key) {
        // for direct edit manager
        if (key == ITextFigure.class) {
            return getArrayFigure().getSpinner().getLabelFigure();
        }
        if (key == ProcessVariable.class) {
            return new ProcessVariable(getPVName());
        }
        return super.getAdapter(key);
    }

    /**
     * @return A String array with all PV names from PV properties.
     */
    @Override
    public String[] getAllPVNames() {
        return delegate.getAllPVNames();
    }

    public ArrayFigure getArrayFigure() {
        return (ArrayFigure) getFigure();
    }

    @Override
    public IFigure getContentPane() {
        return ((ArrayFigure) getFigure()).getContentPane();
    }

    /**
     * @return the control PV. null if no control PV on this widget.
     */
    @Override
    public IPV getControlPV() {
        return delegate.getControlPV();
    }

    @Override
    public DragTracker getDragTracker(Request request) {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            return new SelectEditPartTracker(this) {
                @Override
                protected boolean handleButtonUp(int button) {
                    if (button == 1) {
                        // make widget in edit mode by single click
                        performOpen();
                    }
                    return super.handleButtonUp(button);
                }

                @Override
                public void mouseDoubleClick(MouseEvent me, EditPartViewer viewer) {
                    if (((GraphicalEditPart) getRoot()).getFigure().findFigureAt(me.x, me.y) == getArrayFigure()
                            .getSpinner().getLabelFigure()) {
                        super.mouseDoubleClick(me, viewer);
                    }
                }

                @Override
                public void mouseUp(MouseEvent me, EditPartViewer viewer) {
                    if (((GraphicalEditPart) getRoot()).getFigure().findFigureAt(me.x, me.y) == getArrayFigure()
                            .getSpinner().getLabelFigure()) {
                        super.mouseUp(me, viewer);
                    }
                }
            };
        } else {
            return super.getDragTracker(request);
        }
    }

    @Override
    protected List<AbstractWidgetModel> getModelChildren() {
        return getWidgetModel().getAllChildren();
    }

    protected List<String> getUnSyncablePropIds() {
        if (unSyncablePropIDsFromChild != null) {
            return unSyncablePropIDsFromChild;
        } else {
            return NONE_SYNCABLE_PROPIDS;
        }
    }

    /**
     * Get the PV corresponding to the <code>PV Name</code> property. It is same as calling
     * <code>getPV("pv_name")</code>.
     *
     * @return the PV corresponding to the <code>PV Name</code> property. null if PV Name is not configured for this
     *         widget.
     */
    @Override
    public IPV getPV() {
        return delegate.getPV();
    }

    /**
     * Get the pv by PV property id.
     *
     * @param pvPropId
     *            the PV property id.
     * @return the corresponding pv for the pvPropId. null if the pv doesn't exist.
     */
    @Override
    public IPV getPV(String pvPropId) {
        return delegate.getPV(pvPropId);
    }

    /**
     * @return the first PV name.
     */
    @Override
    public String getPVName() {
        return delegate.getPVName();
    }

    /**
     * Get value from one of the attached PVs.
     *
     * @param pvPropId
     *            the property id of the PV. It is "pv_name" for the main PV.
     * @return the value of the PV.
     */
    @Override
    public VType getPVValue(String pvPropId) {
        return delegate.getPVValue(pvPropId);
    }

    @Override
    public ArrayModel getWidgetModel() {
        return (ArrayModel) super.getWidgetModel();
    }

    /**
     * Get the array index on the child widget.
     *
     * @param child
     * @return the array index on the child widget. -1 if the child is not an array element or it is not a child of the
     *         array widget.
     */
    public int getIndex(AbstractBaseEditPart child) {
        return getChildren().indexOf(child);
    }

    @Override
    protected void initFigure(IFigure figure) {
        super.initFigure(figure);
        delegate.initFigure(figure);
    }

    protected void performDirectEdit() {
        new TextEditManager(this, new LabelCellEditorLocator(getArrayFigure().getSpinner().getLabelFigure()), false)
                .show();
    }

    @Override
    public void performRequest(Request request) {
        if (((request.getType() == RequestConstants.REQ_DIRECT_EDIT && getExecutionMode() != ExecutionMode.RUN_MODE)
                || request.getType() == RequestConstants.REQ_OPEN)) {
            performDirectEdit();
        }
    }

    @Override
    protected void registerBasePropertyChangeHandlers() {
        super.registerBasePropertyChangeHandlers();
        delegate.registerBasePropertyChangeHandlers();
    }

    @Override
    protected void registerPropertyChangeHandlers() {

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, figure) -> {
            for (var child : getWidgetModel().getAllChildren()) {
                child.setEnabled((Boolean) newValue);
            }
            return true;
        };

        setPropertyChangeHandler(AbstractWidgetModel.PROP_ENABLED, handler);

        handler = (oldValue, newValue, figure) -> {
            var value = (VType) newValue;

            if (value instanceof VNumberArray) {
                var wrappedArray = VTypeHelper.getWrappedArray((value));
                if (wrappedArray != null) {
                    setValue(wrappedArray);
                } else {
                    setValue(VTypeHelper.getDoubleArray(value));
                }
            } else {
                if (value instanceof VEnum) {
                    setValue(new String[] { ((VEnum) value).getValue() });
                } else if (value instanceof VString) {
                    setValue(new String[] { ((VString) value).getValue() });
                } else if (value instanceof VStringArray) {
                    setValue(((VStringArray) value).getData().toArray());
                } else {
                    setValue(VTypeHelper.getDoubleArray(value));
                }

            }
            return false;
        };

        setPropertyChangeHandler(PROP_PVVALUE, handler);

        getWidgetModel().getProperty(PROP_PVNAME).addPropertyChangeListener(evt -> updatePropSheet());

        handler = (oldValue, newValue, figure) -> {
            registerLoadPVDataTypeListener();
            return false;
        };
        setPropertyChangeHandler(PROP_PVNAME, handler);

        getWidgetModel().getProperty(PROP_DATA_TYPE).addPropertyChangeListener(evt -> initValueArray());

        handler = (oldValue, newValue, figure) -> {
            updateWidgetSize();
            return false;
        };
        setPropertyChangeHandler(PROP_BORDER_STYLE, handler);
        setPropertyChangeHandler(PROP_BORDER_WIDTH, handler);

        handler = (oldValue, newValue, figure) -> {
            getArrayFigure().setArrayLength((Integer) newValue);
            return false;
        };
        setPropertyChangeHandler(PROP_ARRAY_LENGTH, handler);

        handler = (oldValue, newValue, figure) -> {
            ((ArrayFigure) figure).setSpinnerWidth((Integer) newValue);
            keepElementSize = true;
            getWidgetModel().setSize(((ArrayFigure) figure).getPreferredSize());
            keepElementSize = false;
            return false;
        };
        setPropertyChangeHandler(PROP_SPINNER_WIDTH, handler);

        handler = (oldValue, newValue, figure) -> {
            ((ArrayFigure) figure).setHorizontal((Boolean) newValue);
            keepElementSize = true;
            getWidgetModel().setSize(((ArrayFigure) figure).getPreferredSize());
            keepElementSize = false;
            return false;
        };
        setPropertyChangeHandler(PROP_HORIZONTAL, handler);

        handler = (oldValue, newValue, figure) -> {
            ((ArrayFigure) figure).setShowScrollbar((Boolean) newValue);
            keepElementSize = true;
            getWidgetModel().setSize(((ArrayFigure) figure).getPreferredSize());
            keepElementSize = false;
            return false;
        };
        setPropertyChangeHandler(PROP_SHOW_SCROLLBAR, handler);

        handler = (oldValue, newValue, figure) -> {
            ((ArrayFigure) figure).setShowSpinner((Boolean) newValue);
            keepElementSize = true;
            getWidgetModel().setSize(((ArrayFigure) figure).getPreferredSize());
            keepElementSize = false;
            return false;
        };
        setPropertyChangeHandler(PROP_SHOW_SPINNER, handler);

        PropertyChangeListener sizePropertyChangeListener = evt -> {
            if (getWidgetModel().getChildren().size() == 0 || inSync || keepElementSize) {
                return;
            }
            var elementSize = getArrayFigure().getElementSize();
            var delta = (Integer) evt.getNewValue() - (Integer) evt.getOldValue();
            var elementWH = elementSize.height;
            var propID = evt.getPropertyName();
            var elementCountDirection = false;
            if (getWidgetModel().isHorizontal() && propID.equals(PROP_WIDTH)) {
                elementWH = elementSize.width;
                elementCountDirection = true;
            } else if (!getWidgetModel().isHorizontal() && propID.equals(PROP_HEIGHT)) {
                elementWH = elementSize.height;
                elementCountDirection = true;
            }
            if (elementCountDirection) {
                var deltaElementsCount = Math.round((float) delta / elementWH);
                var idelta = (Math.round((float) delta / elementWH) * elementWH);
                setPropertyValue(propID, (Integer) evt.getOldValue() + idelta, false);
                var visibleElementsCount = getArrayFigure().getVisibleElementsCount() + deltaElementsCount;
                setPropertyValue(PROP_VISIBLE_ELEMENTS_COUNT, visibleElementsCount);
                setValue(getValue());
            } else {
                int wh;
                if (propID.equals(PROP_WIDTH)) {
                    wh = getArrayFigure().getElementSize().width;
                } else {
                    wh = getArrayFigure().getElementSize().height;
                }
                syncAllChildrenProperties(propID, wh + delta);
            }
        };
        getWidgetModel().getProperty(PROP_WIDTH).addPropertyChangeListener(sizePropertyChangeListener);
        getWidgetModel().getProperty(PROP_HEIGHT).addPropertyChangeListener(sizePropertyChangeListener);
    }

    public void setIgnoreOldPVValue(boolean ignoreOldValue) {
        delegate.setIgnoreOldPVValue(ignoreOldValue);
    }

    /**
     * Set PV to given value. Should accept Double, Double[], Integer, String, maybe more.
     */
    @Override
    public void setPVValue(String pvPropId, Object value) {
        delegate.setPVValue(pvPropId, value);
    }

    @Override
    public Object getValue() {
        return valueArray;
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            return;
        }
        if (value.getClass().isArray()) {
            var index = getArrayFigure().getIndex();
            valueArray = value;
            if (value instanceof String[]) {
                var a = (String[]) value;
                setChildrenValue(index, asList(a), ArrayDataType.STRING_ARRAY);
            } else if (value instanceof Object[]) {
                var a = (Object[]) value;
                setChildrenValue(index, asList(a), ArrayDataType.OBJECT_ARRAY);
            } else if (value instanceof double[]) {
                setChildrenValue(index, new DoubleArrayWrapper((double[]) value), ArrayDataType.DOUBLE_ARRAY);
            } else if (value instanceof long[]) {
                setChildrenValue(index, new LongArrayWrapper((long[]) value), ArrayDataType.LONG_ARRAY);
            } else if (value instanceof byte[]) {
                setChildrenValue(index, new ByteArrayWrapper((byte[]) value), ArrayDataType.BYTE_ARRAY);
            } else if (value instanceof float[]) {
                setChildrenValue(index, new FloatArrayWrapper((float[]) value), ArrayDataType.FLOAT_ARRAY);
            } else if (value instanceof short[]) {
                setChildrenValue(index, new ShortArrayWrapper((short[]) value), ArrayDataType.SHORT_ARRAY);
            } else if (value instanceof int[]) {
                setChildrenValue(index, new IntArrayWrapper((int[]) value), ArrayDataType.INT_ARRAY);
            }
            return;
        }
        super.setValue(value);
    }

    /**
     * @param dataList
     *            must be {@link IPrimaryArrayWrapper} or List
     */
    private void setChildrenValue(int index, Object dataList, ArrayDataType arrayDataType) {
        var w = false;
        if (dataList instanceof IPrimaryArrayWrapper) {
            w = true;
        }

        var arrayLength = w ? ((IPrimaryArrayWrapper) dataList).getSize() : ((List<?>) dataList).size();
        getWidgetModel().setArrayLength(arrayLength);
        getWidgetModel().setDataType(arrayDataType);
        for (var child : getChildren()) {
            if (index < arrayLength) {
                var o = w ? ((IPrimaryArrayWrapper) dataList).get(index++) : ((List<?>) dataList).get(index++);
                try {
                    ((AbstractBaseEditPart) child).setValue(o);
                    ((AbstractBaseEditPart) child).getWidgetModel().setTooltip(o.toString());
                } catch (Exception e2) {
                    continue;
                }
            } else {
                try {
                    ((AbstractBaseEditPart) child).getWidgetModel().setTooltip(EMPTY_STRING);
                    ((AbstractBaseEditPart) child).setValue(EMPTY_STRING);
                } catch (Exception e) {
                    try {
                        ((AbstractBaseEditPart) child).setValue(0);
                    } catch (Exception e1) {
                        try {
                            ((AbstractBaseEditPart) child).setValue(elementDefaultValue);
                        } catch (Exception e2) {
                        }
                    }
                }
            }
        }
    }

    private void syncAllChildrenProperties(String propId, Object newValue) {
        if (inSync) {
            return;
        }
        inSync = true;
        if (!getUnSyncablePropIds().contains(propId)) {
            for (var child : getWidgetModel().getAllChildren().toArray()) {
                ((AbstractWidgetModel) child).setPropertyValue(propId, newValue);
            }
        }
        if (propId.equals(PROP_WIDTH) || propId.equals(PROP_HEIGHT)) {
            updateWidgetSize();
        }
        inSync = false;
    }

    protected void updateWidgetSize() {
        var containerSize = getArrayFigure().calcWidgetSizeForElements(getArrayFigure().getVisibleElementsCount(),
                getWidgetModel().getAllChildren().get(0).getSize());
        getWidgetModel().setSize(containerSize);
    }

    @Override
    public void addSetPVValueListener(ISetPVValueListener listener) {
        delegate.addSetPVValueListener(listener);
    }

    @Override
    public boolean isPVControlWidget() {
        return delegate.isPVControlWidget();
    }

    private int mapBasicDataTypeToArrayType(BasicDataType dataType) {
        int r;
        switch (dataType) {
        case DOUBLE:
        case DOUBLE_ARRAY:
            r = ArrayDataType.DOUBLE_ARRAY.ordinal();
            break;
        case BYTE:
        case BYTE_ARRAY:
            r = ArrayDataType.BYTE_ARRAY.ordinal();
            break;
        case CHAR:
        case CHAR_ARRAY:
        case SHORT:
        case SHORT_ARRAY:
            r = ArrayDataType.SHORT_ARRAY.ordinal();
            break;
        case ENUM:
        case ENUM_ARRAY:
        case STRING:
        case STRING_ARRAY:
            r = ArrayDataType.STRING_ARRAY.ordinal();
            break;
        case INT:
        case INT_ARRAY:
            r = ArrayDataType.INT_ARRAY.ordinal();
            break;
        case LONG:
        case LONG_ARRAY:
            r = ArrayDataType.LONG_ARRAY.ordinal();
            break;
        case FLOAT:
        case FLOAT_ARRAY:
            r = ArrayDataType.FLOAT_ARRAY.ordinal();
            break;
        default:
            r = ArrayDataType.OBJECT_ARRAY.ordinal();
            break;
        }
        return r;
    }

    @Override
    public void setControlEnabled(boolean enabled) {
        delegate.setControlEnabled(enabled);
    }
}
