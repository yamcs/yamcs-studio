package org.yamcs.studio.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.MemberInfo;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

public class MissionDatabase {

    private List<ParameterInfo> parameters = new ArrayList<>();
    private List<CommandInfo> commands = new ArrayList<>();

    private Map<NamedObjectId, ParameterInfo> parametersById = new LinkedHashMap<>();
    private Map<String, CommandInfo> commandsByQualifiedName = new LinkedHashMap<>();
    private Map<NamedObjectId, String> unitsById = new ConcurrentHashMap<>();

    public void addParameter(ParameterInfo parameter) {
        parameters.add(parameter);
        NamedObjectId id = NamedObjectId.newBuilder().setName(parameter.getQualifiedName()).build();
        parametersById.put(id, parameter);
        for (NamedObjectId alias : parameter.getAliasList()) {
            parametersById.put(alias, parameter);
        }

        // Update unit index
        if (parameter.hasType() && parameter.getType().getUnitSetCount() > 0) {
            String combinedUnit = parameter.getType().getUnitSet(0).getUnit();
            for (int i = 1; i < parameter.getType().getUnitSetCount(); i++) {
                combinedUnit += " " + parameter.getType().getUnitSet(i).getUnit();
            }
            unitsById.put(id, combinedUnit);
        }
    }

    public void addCommand(CommandInfo command) {
        commands.add(command);
        commandsByQualifiedName.put(command.getQualifiedName(), command);
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public int getCommandCount() {
        return commands.size();
    }

    public List<ParameterInfo> getParameters() {
        List<ParameterInfo> parameters = new ArrayList<>(this.parameters);
        Collections.sort(parameters, (p1, p2) -> p1.getQualifiedName().compareTo(p2.getQualifiedName()));
        return parameters;
    }

    public List<CommandInfo> getCommands() {
        List<CommandInfo> commands = new ArrayList<>(this.commands);
        Collections.sort(commands, (c1, c2) -> c1.getQualifiedName().compareTo(c2.getQualifiedName()));
        return commands;
    }

    /**
     * Returns the ParameterInfo for an ID, the ID may also point to an aggregate member or an array entry, the returned
     * ParameterInfo will then match the containing parameter.
     */
    public ParameterInfo getParameterInfo(NamedObjectId id) {
        String[] parts = removeArrayAndAggregateOffset(id.getName());
        if (parts[1] != null) {
            id = NamedObjectId.newBuilder(id).setName(parts[0]).build();
        }
        return parametersById.get(id);
    }

    public CommandInfo getCommandInfo(String qualifiedName) {
        return commandsByQualifiedName.get(qualifiedName);
    }

    /**
     * Returns the ParameterTypeInfo for an ID, the ID may also point to an aggregate member or an array entry, the
     * returned ParameterInfo will then match that specific path into the parameter.
     */
    public ParameterTypeInfo getParameterTypeInfo(NamedObjectId id) {
        String suffix = removeArrayAndAggregateOffset(id.getName())[1];

        ParameterInfo parameter = getParameterInfo(id);
        if (parameter == null) {
            return null;
        }

        String qualifiedNameWithSuffix = parameter.getQualifiedName();
        if (suffix != null) {
            qualifiedNameWithSuffix += suffix;
        }

        return findMatchingParameterType(parameter.getType(), parameter.getQualifiedName(), qualifiedNameWithSuffix);
    }

    private ParameterTypeInfo findMatchingParameterType(ParameterTypeInfo parent, String parentName,
            String qualifiedNameWithSuffix) {
        if (parent == null) {
            return null;
        } else if (qualifiedNameWithSuffix.matches(parentName)) {
            return parent;
        } else {
            for (MemberInfo member : parent.getMemberList()) {
                ParameterTypeInfo memberType = member.getType();
                String name = parentName + "." + member.getName();
                ParameterTypeInfo match = findMatchingParameterType(memberType, name, qualifiedNameWithSuffix);
                if (match != null) {
                    return match;
                }
            }
            if (parent.hasArrayInfo()) {
                ParameterTypeInfo entryType = parent.getArrayInfo().getType();
                String name = parentName + "\\[[0-9]+\\]";
                ParameterTypeInfo match = findMatchingParameterType(entryType, name, qualifiedNameWithSuffix);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    public String getCombinedUnit(NamedObjectId id) {
        return unitsById.get(id);
    }

    /**
     * Splits a PV name into the actual parameter name, and the struct or array path within that parameter.
     * 
     * For example: "/bla/bloe.f[3].heh" becomes { "/bla/bloe", ".f[3].heh" }
     */
    private static String[] removeArrayAndAggregateOffset(String name) {
        int searchFrom = name.lastIndexOf('/');

        int trimFrom = -1;

        int arrayStart = name.indexOf('[', searchFrom);
        if (arrayStart != -1) {
            trimFrom = arrayStart;
        }

        int memberStart = name.indexOf('.', searchFrom);
        if (memberStart != -1) {
            if (trimFrom >= 0) {
                trimFrom = Math.min(trimFrom, memberStart);
            } else {
                trimFrom = memberStart;
            }
        }

        if (trimFrom >= 0) {
            return new String[] { name.substring(0, trimFrom), name.substring(trimFrom) };
        } else {
            return new String[] { name, null };
        }
    }
}
