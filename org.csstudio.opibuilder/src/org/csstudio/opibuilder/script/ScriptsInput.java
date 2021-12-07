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

import org.csstudio.opibuilder.properties.ScriptProperty;

/**
 * The value type definition for {@link ScriptProperty}, which describes the input for a Script Property.
 */
public class ScriptsInput {

    private List<ScriptData> scriptList;

    public ScriptsInput(List<ScriptData> scriptDataList) {
        scriptList = scriptDataList;
    }

    public ScriptsInput() {
        scriptList = new ArrayList<ScriptData>();
    }

    /**
     * @return the scriptList
     */
    public List<ScriptData> getScriptList() {
        return scriptList;
    }

    /**
     * @return a total contents copy of this ScriptsInput.
     */
    public ScriptsInput getCopy() {
        var copy = new ScriptsInput();
        for (ScriptData data : scriptList) {
            copy.getScriptList().add(data.getCopy());
        }
        return copy;
    }

    @Override
    public String toString() {
        if (scriptList.size() == 0) {
            return "no script attached";
        }
        if (scriptList.size() == 1) {
            if (scriptList.get(0).isEmbedded()) {
                return scriptList.get(0).getScriptName();
            }
            return scriptList.get(0).getPath().toString();
        }
        return scriptList.size() + " scripts attached";
    }

}
