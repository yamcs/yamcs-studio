/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.ScriptPropertyDescriptor;
import org.csstudio.opibuilder.script.PVTuple;
import org.csstudio.opibuilder.script.ScriptData;
import org.csstudio.opibuilder.script.ScriptService.ScriptType;
import org.csstudio.opibuilder.script.ScriptsInput;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.CDATA;
import org.jdom.Element;

/**
 * The property for script.
 */
public class ScriptProperty extends AbstractWidgetProperty {

    /**
     * XML ELEMENT name <code>PATH</code>.
     */
    public static final String XML_ELEMENT_PATH = "path";

    /**
     * XML ATTRIBUTE name <code>PATHSTRING</code>.
     */
    public static final String XML_ATTRIBUTE_PATHSTRING = "pathString";

    public static final String XML_ATTRIBUTE_CHECKCONNECT = "checkConnect";

    public static final String XML_ATTRIBUTE_STOP_EXECUTE_ON_ERROR = "seoe";

    public static final String EMBEDDEDJS = "EmbeddedJs";
    public static final String EMBEDDEDPY = "EmbeddedPy";

    /**
     * XML Element name <code>PV</code>.
     */
    public static final String XML_ELEMENT_PV = "pv";

    public static final String XML_ATTRIBUTE_TRIGGER = "trig";

    public static final String XML_ELEMENT_SCRIPT_TEXT = "scriptText";

    private static final String XML_ELEMENT_SCRIPT_NAME = "scriptName";

    /**
     * Script Property Constructor. The property value type is {@link ScriptsInput}.
     * 
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     */
    public ScriptProperty(String prop_id, String description, WidgetPropertyCategory category) {
        super(prop_id, description, category, new ScriptsInput());

    }

    @Override
    public Object checkValue(Object value) {
        if (value == null) {
            return null;
        }
        ScriptsInput acceptableValue = null;
        if (value instanceof ScriptsInput) {
            acceptableValue = (ScriptsInput) value;
        }

        return acceptableValue;
    }

    @Override
    public Object getPropertyValue() {
        if (executionMode == ExecutionMode.RUN_MODE && widgetModel != null) {
            var value = (ScriptsInput) super.getPropertyValue();
            for (ScriptData sd : value.getScriptList()) {
                for (Object pv : sd.getPVList().toArray()) {
                    var pvTuple = (PVTuple) pv;
                    var newPV = OPIBuilderMacroUtil.replaceMacros(widgetModel, pvTuple.pvName);
                    if (!newPV.equals(pvTuple.pvName)) {
                        var i = sd.getPVList().indexOf(pv);
                        sd.getPVList().remove(pv);
                        sd.getPVList().add(i, new PVTuple(newPV, pvTuple.trigger));
                    }
                }
            }
            return value;
        } else {
            return super.getPropertyValue();
        }
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new ScriptPropertyDescriptor(prop_id, widgetModel, description);
    }

    @Override
    public ScriptsInput readValueFromXML(Element propElement) {
        var result = new ScriptsInput();
        for (Object oe : propElement.getChildren(XML_ELEMENT_PATH)) {
            var se = (Element) oe;
            var sd = new ScriptData();
            if (se.getAttributeValue(XML_ATTRIBUTE_PATHSTRING).equals(EMBEDDEDJS)) {
                sd.setEmbedded(true);
                sd.setScriptType(ScriptType.JAVASCRIPT);
                sd.setScriptText(se.getChildText(XML_ELEMENT_SCRIPT_TEXT));
                sd.setScriptName(se.getChildText(XML_ELEMENT_SCRIPT_NAME));
            } else if (se.getAttributeValue(XML_ATTRIBUTE_PATHSTRING).equals(EMBEDDEDPY)) {
                sd.setEmbedded(true);
                sd.setScriptType(ScriptType.PYTHON);
                sd.setScriptText(se.getChildText(XML_ELEMENT_SCRIPT_TEXT));
                sd.setScriptName(se.getChildText(XML_ELEMENT_SCRIPT_NAME));
            } else {
                sd = new ScriptData(new Path(se.getAttributeValue(XML_ATTRIBUTE_PATHSTRING)));
            }
            if (se.getAttributeValue(XML_ATTRIBUTE_CHECKCONNECT) != null) {
                sd.setCheckConnectivity(Boolean.parseBoolean(se.getAttributeValue(XML_ATTRIBUTE_CHECKCONNECT)));
            }
            if (se.getAttributeValue(XML_ATTRIBUTE_STOP_EXECUTE_ON_ERROR) != null) {
                sd.setStopExecuteOnError(
                        Boolean.parseBoolean(se.getAttributeValue(XML_ATTRIBUTE_STOP_EXECUTE_ON_ERROR)));
            }
            for (Object o : se.getChildren(XML_ELEMENT_PV)) {
                var pve = (Element) o;
                var trig = true;
                if (pve.getAttribute(XML_ATTRIBUTE_TRIGGER) != null) {
                    trig = Boolean.parseBoolean(pve.getAttributeValue(XML_ATTRIBUTE_TRIGGER));
                }
                sd.addPV(new PVTuple(pve.getText(), trig));
            }
            result.getScriptList().add(sd);
        }
        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        for (ScriptData scriptData : ((ScriptsInput) getPropertyValue()).getScriptList()) {
            var pathElement = new Element(XML_ELEMENT_PATH);
            String pathString = null;
            if (scriptData.isEmbedded()) {
                if (scriptData.getScriptType() == ScriptType.JAVASCRIPT) {
                    pathString = EMBEDDEDJS;
                } else if (scriptData.getScriptType() == ScriptType.PYTHON) {
                    pathString = EMBEDDEDPY;
                }
                var scriptNameElement = new Element(XML_ELEMENT_SCRIPT_NAME);
                scriptNameElement.setText(scriptData.getScriptName());
                pathElement.addContent(scriptNameElement);
                var scriptTextElement = new Element(XML_ELEMENT_SCRIPT_TEXT);
                scriptTextElement.setContent(new CDATA(scriptData.getScriptText()));
                pathElement.addContent(scriptTextElement);
            } else {
                pathString = scriptData.getPath().toPortableString();
            }
            pathElement.setAttribute(XML_ATTRIBUTE_PATHSTRING, pathString);
            pathElement.setAttribute(XML_ATTRIBUTE_CHECKCONNECT, Boolean.toString(scriptData.isCheckConnectivity()));
            pathElement.setAttribute(XML_ATTRIBUTE_STOP_EXECUTE_ON_ERROR,
                    Boolean.toString(scriptData.isStopExecuteOnError()));
            for (PVTuple pv : scriptData.getPVList()) {
                var pvElement = new Element(XML_ELEMENT_PV);
                pvElement.setText(pv.pvName);
                pvElement.setAttribute(XML_ATTRIBUTE_TRIGGER, Boolean.toString(pv.trigger));
                pathElement.addContent(pvElement);
            }
            propElement.addContent(pathElement);
        }
    }

}
