/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

public class MissionDatabase {

    private static final Pattern ARRAY_INDEXES = Pattern.compile("(\\[[\\d+]\\])+");

    private List<ParameterInfo> parameters = new ArrayList<>();
    private List<CommandInfo> commands = new ArrayList<>();

    private Map<NamedObjectId, ParameterInfo> parametersById = new LinkedHashMap<>();
    private Map<String, CommandInfo> commandsByQualifiedName = new LinkedHashMap<>();
    private Map<String, Map<String, CommandInfo>> commandsByNamespaceAndAlias = new LinkedHashMap<>();
    private Map<NamedObjectId, String> unitsById = new ConcurrentHashMap<>();

    public void addParameter(ParameterInfo parameter) {
        parameters.add(parameter);
        var id = NamedObjectId.newBuilder().setName(parameter.getQualifiedName()).build();
        parametersById.put(id, parameter);
        for (var alias : parameter.getAliasList()) {
            parametersById.put(alias, parameter);
        }

        // Update unit index
        if (parameter.hasType() && parameter.getType().getUnitSetCount() > 0) {
            var engUnits = parameter.getType().getUnitSet(0).getUnit();
            unitsById.put(id, engUnits);
            for (var alias : parameter.getAliasList()) {
                unitsById.put(alias, engUnits);
            }
        }

        if (parameter.hasType() && parameter.getType().hasArrayInfo()) {
            var elId = NamedObjectId.newBuilder(id)
                    .setName(id.getName() + "__el")
                    .build();
            var elInfo = parameter.getType().getArrayInfo();
            if (elInfo.hasType() && elInfo.getType().getUnitSetCount() > 0) {
                var elUnits = elInfo.getType().getUnitSet(0).getUnit();
                unitsById.put(elId, elUnits);
                for (var alias : parameter.getAliasList()) {
                    var elAlias = NamedObjectId.newBuilder(alias)
                            .setName(id.getName() + "__el")
                            .build();
                    unitsById.put(elAlias, elUnits);
                }
            }
        }
    }

    public void addCommand(CommandInfo command) {
        commands.add(command);
        commandsByQualifiedName.put(command.getQualifiedName(), command);
        for (var id : command.getAliasList()) {
            var namespace = id.getNamespace();
            var commandsByAlias = commandsByNamespaceAndAlias.computeIfAbsent(namespace, info -> new LinkedHashMap<>());
            commandsByAlias.put(id.getName(), command);
        }
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
        var parts = removeArrayAndAggregateOffset(id.getName());
        if (parts[1] != null) {
            id = NamedObjectId.newBuilder(id).setName(parts[0]).build();
        }
        return parametersById.get(id);
    }

    public CommandInfo getCommandInfo(String qualifiedName) {
        return commandsByQualifiedName.get(qualifiedName);
    }

    public CommandInfo getCommandInfo(String namespace, String alias) {
        var commandsByAlias = commandsByNamespaceAndAlias.get(namespace);
        if (commandsByAlias != null) {
            return commandsByAlias.get(alias);
        }
        return null;
    }

    /**
     * Returns the ParameterTypeInfo for an ID, the ID may also point to an aggregate member or an array entry, the
     * returned ParameterInfo will then match that specific path into the parameter.
     */
    public ParameterTypeInfo getParameterTypeInfo(NamedObjectId id) {
        var suffix = removeArrayAndAggregateOffset(id.getName())[1];

        var parameter = getParameterInfo(id);
        if (parameter == null) {
            return null;
        }

        var qualifiedNameWithSuffix = parameter.getQualifiedName();
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
            for (var member : parent.getMemberList()) {
                var memberType = member.getType();
                var name = parentName + "." + member.getName();
                var match = findMatchingParameterType(memberType, name, qualifiedNameWithSuffix);
                if (match != null) {
                    return match;
                }
            }
            if (parent.hasArrayInfo()) {
                var entryType = parent.getArrayInfo().getType();
                var name = parentName + "\\[[0-9]+\\]";
                var match = findMatchingParameterType(entryType, name, qualifiedNameWithSuffix);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    public String getEngineeringUnits(NamedObjectId id) {
        if (id.getName().endsWith("]")) { // Array
            var matcher = ARRAY_INDEXES.matcher(id.getName());
            var buf = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(buf, "__el");
            }
            matcher.appendTail(buf);

            var elId = NamedObjectId.newBuilder(id)
                    .setName(buf.toString())
                    .build();
            return unitsById.get(elId);
        }

        return unitsById.get(id);
    }

    /**
     * Splits a PV name into the actual parameter name, and the struct or array path within that parameter.
     *
     * For example: "/bla/bloe.f[3].heh" becomes { "/bla/bloe", ".f[3].heh" }
     */
    private static String[] removeArrayAndAggregateOffset(String name) {
        var searchFrom = name.lastIndexOf('/');

        var trimFrom = -1;

        var arrayStart = name.indexOf('[', searchFrom);
        if (arrayStart != -1) {
            trimFrom = arrayStart;
        }

        var memberStart = name.indexOf('.', searchFrom);
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
