/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.script;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Data of a rule.
 */
public class RuleData implements IAdaptable {

    public static final String QUOTE = "\"";

    /**
     * The name of the rule.
     */
    private String name = "Rule";

    /**
     * Id of the property which the rule will apply to.
     */
    private String propId = "name";

    private AbstractWidgetModel widgetModel;

    /**
     * Output expression value.
     */
    private boolean outputExpValue;

    private List<Expression> expressionList = new ArrayList<>();

    /**
     * The input PVs of the rule. Which can be accessed in the rule and trigger the rule execution.
     */
    private List<PVTuple> pvList = new ArrayList<>();

    public RuleData(AbstractWidgetModel widgetModel) {
        this.widgetModel = widgetModel;
    }

    public final String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final String getPropId() {
        return propId;
    }

    public void setPropId(String propId) {
        this.propId = propId;
    }

    public void setOutputExpValue(boolean outputExpValue) {
        this.outputExpValue = outputExpValue;
    }

    public boolean isOutputExpValue() {
        return outputExpValue;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    public void addExpression(Expression exp) {
        if (!expressionList.contains(exp)) {
            expressionList.add(exp);
        }
    }

    public void removeExpression(Expression exp) {
        expressionList.remove(exp);
    }

    /**
     * Get the input PVs of the script
     */
    public List<PVTuple> getPVList() {
        return pvList;
    }

    public void addPV(PVTuple pvTuple) {
        if (!pvList.contains(pvTuple)) {
            pvList.add(pvTuple);
        }
    }

    public void removePV(PVTuple pvTuple) {
        pvList.remove(pvTuple);
    }

    /**
     * Generate the Javascript string for this rule.
     */
    public String generateScript() {
        if (expressionList.size() <= 0) {
            return "";
        }
        var sb = new StringBuilder();
        var property = widgetModel.getProperty(propId);
        var needDbl = false;
        var needInt = false;
        var needStr = false;
        var needSev = false;
        for (var exp : expressionList) {
            if (!needDbl) {
                needDbl = containRegex(exp.getBooleanExpression(), "pv\\d")
                        || (outputExpValue && containRegex(exp.getValue().toString(), "pv\\d"));
            }
            if (!needInt) {
                if (exp.getBooleanExpression().contains("pvInt")) {
                    needInt = true;
                }
                if (outputExpValue && exp.getValue().toString().contains("pvInt")) {
                    needInt = true;
                }
            }
            if (!needStr) {
                if (exp.getBooleanExpression().contains("pvStr")) {
                    needStr = true;
                }
                if (outputExpValue && exp.getValue().toString().contains("pvStr")) {
                    needStr = true;
                }
            }
            if (!needSev) {
                if (exp.getBooleanExpression().contains("pvSev")) {
                    needSev = true;
                }
                if (outputExpValue && exp.getValue().toString().contains("pvSev")) {
                    needSev = true;
                }
            }
        }
        for (var i = 0; i < pvList.size(); i++) {
            if (needDbl) {
                sb.append("var pv" + i + " = PVUtil.getDouble(pvs[" + i + "]);\n");
            }
            if (needInt) {
                sb.append("var pvInt" + i + " = PVUtil.getLong(pvs[" + i + "]);\n");
            }
            if (needStr) {
                sb.append("var pvStr" + i + " = PVUtil.getString(pvs[" + i + "]);\n");
            }
            if (needSev) {
                sb.append("var pvSev" + i + " = PVUtil.getSeverity(pvs[" + i + "]);\n");
            }
        }
        var i = 0;
        for (var exp : expressionList) {
            sb.append(i == 0 ? "if (" : "else if (");
            sb.append(expressionList.get(i++).getBooleanExpression());
            sb.append(") {\n");

            sb.append("\twidget.setPropertyValue(\"" + propId + "\",");

            var propValue = generatePropValueString(property, exp);
            sb.append(propValue + ");\n");
            sb.append("}\n");
        }
        sb.append("else {\n");
        sb.append("\twidget.setPropertyValue(\"" + propId + "\"," + generatePropValueString(property, null) + ");\n");
        sb.append("}");

        return sb.toString();
    }

    public RuleData getCopy() {
        var result = new RuleData(widgetModel);
        result.setName(name);
        result.setOutputExpValue(outputExpValue);
        result.setPropId(propId);
        for (var expression : expressionList) {
            result.addExpression(expression.getCopy());
        }
        for (var pvTuple : pvList) {
            result.addPV(pvTuple.getCopy());
        }
        return result;
    }

    private String generatePropValueString(AbstractWidgetProperty property, Expression exp) {
        Object value;
        String propValue;
        if (exp != null && outputExpValue) {
            propValue = exp.getValue().toString();
            return propValue;
        } else {
            if (exp != null) {
                value = exp.getValue();
            } else {
                value = property.getPropertyValue();
            }

            if (value == null) {
                return "null";
            }

            propValue = property.toStringInRuleScript(value);
        }

        return propValue;
    }

    public AbstractWidgetProperty getProperty() {
        return widgetModel.getProperty(propId);
    }

    /**
     * Convert this {@link RuleData} to {@link RuleScriptData} so that the scriptEngine code can be reused for running
     * rules.
     */
    public RuleScriptData convertToScriptData() {
        var ruleScriptData = new RuleScriptData(this);
        ruleScriptData.setPVList(pvList);
        ruleScriptData.setScriptString(generateScript());
        return ruleScriptData;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IWorkbenchAdapter.class) {
            return adapter.cast(new IWorkbenchAdapter() {
                @Override
                public Object getParent(Object o) {
                    return null;
                }

                @Override
                public String getLabel(Object o) {
                    return name;
                }

                @Override
                public ImageDescriptor getImageDescriptor(Object object) {
                    return CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                            "icons/js.gif");
                }

                @Override
                public Object[] getChildren(Object o) {
                    return new Object[0];
                }
            });
        }

        return null;
    }

    public AbstractWidgetModel getWidgetModel() {
        return widgetModel;
    }

    /**
     * If a String contains the regular expression.
     * 
     * @param source
     *            the source string.
     * @param regex
     *            the regular expression.
     * @return true if the source string contains the input regex. false other wise.
     */
    private static boolean containRegex(String source, String regex) {
        var p = Pattern.compile(regex);
        var m = p.matcher(source);
        return m.find();
    }
}
