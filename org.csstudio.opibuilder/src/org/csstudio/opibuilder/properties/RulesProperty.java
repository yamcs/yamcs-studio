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
import org.csstudio.opibuilder.properties.support.RulesPropertyDescriptor;
import org.csstudio.opibuilder.script.Expression;
import org.csstudio.opibuilder.script.PVTuple;
import org.csstudio.opibuilder.script.RuleData;
import org.csstudio.opibuilder.script.RulesInput;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;

/**
 * The property for rules.
 */
public class RulesProperty extends AbstractWidgetProperty {

    /**
     * XML ELEMENT name <code>RULE</code>.
     */
    public static final String XML_ELEMENT_RULE = "rule";

    /**
     * XML ATTRIBUTE name <code>NAME</code>.
     */
    public static final String XML_ATTRIBUTE_NAME = "name";

    /**
     * XML ATTRIBUTE name <code>PROPID</code>.
     */
    public static final String XML_ATTRIBUTE_PROPID = "prop_id";

    /**
     * XML ATTRIBUTE name <code>OUTPUTEXPRESSION</code>.
     */
    public static final String XML_ATTRIBUTE_OUTPUTEXPRESSION = "out_exp";

    /**
     * XML ELEMENT name <code>EXPRESSION</code>.
     */
    public static final String XML_ELEMENT_EXPRESSION = "exp";

    /**
     * XML ATTRIBUTE name <code>BOOLEXP</code>.
     */
    public static final String XML_ATTRIBUTE_BOOLEXP = "bool_exp";

    /**
     * XML ELEMENT name <code>VALUE</code>.
     */
    public static final String XML_ELEMENT_VALUE = "value";

    /**
     * XML Element name <code>PV</code>.
     */
    public static final String XML_ELEMENT_PV = "pv";

    public static final String XML_ATTRIBUTE_TRIGGER = "trig";

    /**
     * Rules Property Constructor. The property value type is {@link RulesInput}.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     */
    public RulesProperty(String prop_id, String description, WidgetPropertyCategory category) {
        super(prop_id, description, category, new RulesInput());

    }

    @Override
    public Object checkValue(Object value) {
        if (value == null) {
            return null;
        }
        RulesInput acceptableValue = null;
        if (value instanceof RulesInput) {
            acceptableValue = (RulesInput) value;
        }
        return acceptableValue;
    }

    @Override
    public Object getPropertyValue() {
        if (executionMode == ExecutionMode.RUN_MODE && widgetModel != null) {
            var value = (RulesInput) super.getPropertyValue();
            for (RuleData rd : value.getRuleDataList()) {
                for (Object pv : rd.getPVList().toArray()) {
                    var pvTuple = (PVTuple) pv;
                    var newPV = OPIBuilderMacroUtil.replaceMacros(widgetModel, pvTuple.pvName);
                    if (!newPV.equals(pvTuple.pvName)) {
                        var i = rd.getPVList().indexOf(pv);
                        rd.getPVList().remove(pv);
                        rd.getPVList().add(i, new PVTuple(newPV, pvTuple.trigger));
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
        return new RulesPropertyDescriptor(prop_id, widgetModel, description);
    }

    @Override
    public RulesInput readValueFromXML(Element propElement) throws Exception {
        var result = new RulesInput();
        for (Object oe : propElement.getChildren(XML_ELEMENT_RULE)) {
            var se = (Element) oe;
            var ruleData = new RuleData(widgetModel);
            ruleData.setName(se.getAttributeValue(XML_ATTRIBUTE_NAME));
            ruleData.setPropId(se.getAttributeValue(XML_ATTRIBUTE_PROPID));
            ruleData.setOutputExpValue(Boolean.parseBoolean(se.getAttributeValue(XML_ATTRIBUTE_OUTPUTEXPRESSION)));

            for (Object eo : se.getChildren(XML_ELEMENT_EXPRESSION)) {
                var ee = (Element) eo;
                var booleanExpression = ee.getAttributeValue(XML_ATTRIBUTE_BOOLEXP);
                Object value = "null";
                var valueElement = ee.getChild(XML_ELEMENT_VALUE);
                if (ruleData.isOutputExpValue()) {
                    value = valueElement.getText();
                } else {
                    value = ruleData.getProperty().readValueFromXML(valueElement);
                }
                var exp = new Expression(booleanExpression, value);
                ruleData.addExpression(exp);
            }

            for (Object pvo : se.getChildren(XML_ELEMENT_PV)) {
                var pve = (Element) pvo;
                var trig = true;
                if (pve.getAttribute(XML_ATTRIBUTE_TRIGGER) != null) {
                    trig = Boolean.parseBoolean(pve.getAttributeValue(XML_ATTRIBUTE_TRIGGER));
                }
                ruleData.addPV(new PVTuple(pve.getText(), trig));
            }

            result.getRuleDataList().add(ruleData);
        }
        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        for (RuleData ruleData : ((RulesInput) getPropertyValue()).getRuleDataList()) {
            var ruleElement = new Element(XML_ELEMENT_RULE);
            ruleElement.setAttribute(XML_ATTRIBUTE_NAME, ruleData.getName());
            ruleElement.setAttribute(XML_ATTRIBUTE_PROPID, ruleData.getPropId());
            ruleElement.setAttribute(XML_ATTRIBUTE_OUTPUTEXPRESSION, Boolean.toString(ruleData.isOutputExpValue()));

            for (Expression exp : ruleData.getExpressionList()) {
                var expElement = new Element(XML_ELEMENT_EXPRESSION);
                expElement.setAttribute(XML_ATTRIBUTE_BOOLEXP, exp.getBooleanExpression());
                var valueElement = new Element(XML_ELEMENT_VALUE);
                if (ruleData.isOutputExpValue()) {
                    valueElement.setText(exp.getValue().toString());
                } else {
                    var savedValue = ruleData.getProperty().getPropertyValue();
                    ruleData.getProperty().setPropertyValue_IgnoreOldValue(exp.getValue());
                    ruleData.getProperty().writeToXML(valueElement);
                    ruleData.getProperty().setPropertyValue_IgnoreOldValue(savedValue);
                }
                expElement.addContent(valueElement);
                ruleElement.addContent(expElement);
            }

            for (PVTuple pv : ruleData.getPVList()) {
                var pvElement = new Element(XML_ELEMENT_PV);
                pvElement.setText(pv.pvName);
                pvElement.setAttribute(XML_ATTRIBUTE_TRIGGER, Boolean.toString(pv.trigger));
                ruleElement.addContent(pvElement);
            }
            propElement.addContent(ruleElement);
        }
    }

}
