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
        LocalData localData = pv2data.get(pv);
        return localData != null && localData.isConnected();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return true;
    }

    @Override
    public void writeValue(IPV pv, Object value, WriteCallback callback) {
        String localName = toLocalName(pv.getName());
        LocalData localData = name2data.get(localName);
        if (localData != null) {
            localData.writeValue(value, callback);
        }
    }

    @Override
    public VType getValue(String pvName) {
        String localName = toLocalName(pvName);
        LocalData localData = name2data.get(localName);
        if (localData != null) {
            return localData.getValue();
        }
        return null;
    }

    @Override
    public void onStarted(IPV pv) {
        List<Object> tokens = tokenize(pv.getName());
        String localName = tokens.get(0).toString();

        LocalData localData = name2data.computeIfAbsent(localName, x -> new LocalData(x));
        localData.setType((String) tokens.get(1));
        if (tokens.size() > 2) {
            localData.setInitialValue(tokens.get(2));
        }

        pv2data.put(pv, localData);
        localData.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        pv2data.remove(pv);

        String localName = toLocalName(pv.getName());
        LocalData localData = name2data.get(localName);
        if (localData != null) {
            localData.unregister(pv);
        }
    }

    private static String toLocalName(String pvName) {
        List<Object> parsedTokens = tokenize(pvName);
        return parsedTokens.get(0).toString();
    }

    private static List<Object> tokenize(String pvName) {
        List<Object> tokens = FunctionParser.parseFunctionAnyParameter(".+", pvName);
        String nameAndType = tokens.get(0).toString();
        String name = nameAndType;
        String type = null;
        int index = nameAndType.lastIndexOf('<');
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
            Object labels = FunctionParser.asScalarOrList(tokens.subList(1, tokens.size()));
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
