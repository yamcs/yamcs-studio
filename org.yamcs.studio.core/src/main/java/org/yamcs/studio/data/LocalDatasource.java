/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yamcs.studio.data.vtype.VType;

/**
 * A datasource that provides in-memory PVs.
 */
public class LocalDatasource implements Datasource {

    private Map<IPV, LocalData> pv2data = new HashMap<>();

    // Keep track of local names, to ensure they use same initializers
    private Map<String, LocalData> name2data = new HashMap<>();

    @Override
    public boolean supportsPVName(String pvName) {
        return pvName.startsWith("loc://");
    }

    @Override
    public boolean isConnected(IPV pv) {
        var localData = pv2data.get(pv);
        return localData != null && localData.isConnected();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return true;
    }

    @Override
    public void writeValue(IPV pv, Object value, WriteCallback callback) {
        var localData = pv2data.get(pv);
        if (localData != null) {
            localData.writeValue(value, callback);
        }
    }

    @Override
    public VType getValue(IPV pv) {
        var localData = pv2data.get(pv);
        if (localData != null) {
            return localData.getValue();
        }
        return null;
    }

    @Override
    public void onStarted(IPV pv) {
        var tokens = tokenize(pv.getName());
        var localName = tokens.get(0).toString();

        var localData = name2data.computeIfAbsent(localName, LocalData::new);
        localData.setType((String) tokens.get(1));
        if (tokens.size() > 2) {
            localData.setInitialValue(tokens.get(2));
        }

        pv2data.put(pv, localData);
        localData.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        var localData = pv2data.remove(pv);
        if (localData != null) {
            localData.unregister(pv);
        }
    }

    private static List<Object> tokenize(String pvName) {
        var tokens = FunctionParser.parseFunctionAnyParameter(".+", pvName);
        var nameAndType = tokens.get(0).toString();
        var name = nameAndType;
        String type = null;
        var index = nameAndType.lastIndexOf('<');
        if (nameAndType.endsWith(">") && index != -1) {
            name = nameAndType.substring(0, index);
            type = nameAndType.substring(index + 1, nameAndType.length() - 1);
        }
        List<Object> newTokens = new ArrayList<>();
        newTokens.add(name);
        newTokens.add(type);
        Object initialValue;
        if ("VEnum".equals(type)) {
            List<Object> initialValueList = new ArrayList<>();
            initialValueList.add(tokens.remove(1));
            var labels = FunctionParser.asScalarOrList(tokens.subList(1, tokens.size()));
            if (!(labels instanceof List<?>)) {
                throw new RuntimeException("Invalid format for VEnum channel.");
            }
            initialValueList.add(labels);
            initialValue = initialValueList;
        } else {
            initialValue = FunctionParser.asScalarOrList(tokens.subList(1, tokens.size()));
        }
        if (tokens.size() > 1) {
            newTokens.add(initialValue);
        }
        return newTokens;
    }
}
