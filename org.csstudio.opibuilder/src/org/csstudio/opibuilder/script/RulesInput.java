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

import java.util.ArrayList;
import java.util.List;

/**
 * The data definition for RulesProperty.
 */
public class RulesInput {

    private List<RuleData> ruleDataList;

    public RulesInput() {
        ruleDataList = new ArrayList<RuleData>();
    }

    public RulesInput(List<RuleData> ruleDataList) {
        this.ruleDataList = ruleDataList;
    }

    public List<RuleData> getRuleDataList() {
        return ruleDataList;
    }

    public RulesInput getCopy() {
        var copy = new RulesInput();
        for (RuleData ruleData : ruleDataList) {
            copy.getRuleDataList().add(ruleData.getCopy());
        }
        return copy;
    }

    @Override
    public String toString() {
        if (ruleDataList.size() == 0) {
            return "no rule attached";
        }
        if (ruleDataList.size() == 1) {
            return ruleDataList.get(0).getName();
        }
        return ruleDataList.size() + " rules attached";
    }

}
