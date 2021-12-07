/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.script;

import java.util.List;

/**
 * The ScriptData converted from {@link RuleData}
 */
public class RuleScriptData extends ScriptData {

    private String scriptString;
    private RuleData ruleData;

    public RuleScriptData(RuleData ruleData) {
        this.ruleData = ruleData;
    }

    public RuleData getRuleData() {
        return ruleData;
    }

    /**
     * @return the scriptString
     */
    public final String getScriptString() {
        return scriptString;
    }

    /**
     * @param scriptString
     *            the scriptString to set
     */
    public void setScriptString(String scriptString) {
        this.scriptString = scriptString;
    }

    public void setPVList(List<PVTuple> pvList) {
        this.pvList = pvList;
    }

}
