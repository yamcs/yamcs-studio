package org.yamcs.studio.core.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.yamcs.api.ws.WebSocketClientCallback;
import org.yamcs.protobuf.Mdb.ListParametersResponse;
import org.yamcs.protobuf.Mdb.MemberInfo;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Web.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.ParameterWebSocketRequest;
import org.yamcs.studio.core.client.YamcsStudioClient;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Keeps track of the parameter model. This does not currently include parameter values.
 */
public class ParameterCatalogue implements Catalogue, WebSocketClientCallback {

    private static final Logger log = Logger.getLogger(ParameterCatalogue.class.getName());

    private Set<ParameterListener> parameterListeners = new CopyOnWriteArraySet<>();

    private List<ParameterInfo> metaParameters = Collections.emptyList();
    private Map<NamedObjectId, ParameterInfo> parametersById = new LinkedHashMap<>();

    // Index for faster repeat access
    private Map<NamedObjectId, String> unitsById = new ConcurrentHashMap<>();

    public static ParameterCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(ParameterCatalogue.class);
    }

    public void addParameterListener(ParameterListener listener) {
        parameterListeners.add(listener);
    }

    public void removeParameterListener(ParameterListener listener) {
        parameterListeners.remove(listener);
    }

    @Override
    public void onYamcsConnected() {
        loadMetaParameters();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        clearState();
        loadMetaParameters();
    }

    @Override
    public void onYamcsDisconnected() {
        clearState();
    }

    private void clearState() {
        metaParameters = Collections.emptyList();
        unitsById.clear();
    }

    private synchronized void processMetaParameters(List<ParameterInfo> metaParameters) {
        log.info(String.format("Loaded %d parameters", metaParameters.size()));
        this.metaParameters = new ArrayList<>(metaParameters);
        this.metaParameters.sort((p1, p2) -> {
            return p1.getQualifiedName().compareTo(p2.getQualifiedName());
        });

        for (ParameterInfo p : this.metaParameters) {
            NamedObjectId id = NamedObjectId.newBuilder().setName(p.getQualifiedName()).build();
            parametersById.put(id, p);
            for (NamedObjectId alias : p.getAliasList()) {
                parametersById.put(alias, p);
            }

            // Update unit index
            if (p != null && p.hasType() && p.getType().getUnitSetCount() > 0) {
                String combinedUnit = p.getType().getUnitSet(0).getUnit();
                for (int i = 1; i < p.getType().getUnitSetCount(); i++) {
                    combinedUnit += " " + p.getType().getUnitSet(i).getUnit();
                }
                unitsById.put(id, combinedUnit);
            }
        }

        parameterListeners.forEach(ParameterListener::mdbUpdated);
    }

    private void loadMetaParameters() {
        Job job = Job.create("Loading parameters", monitor -> {
            log.fine("Fetching available parameters");
            YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            int pageSize = 500;
            List<ParameterInfo> parameters = new ArrayList<>();

            String next = null;
            while (true) {
                String url = "/mdb/" + instance + "/parameters?details&limit=" + pageSize;
                if (next != null) {
                    url += "&next=" + next;
                }
                try {
                    byte[] data = yamcsClient.get(url, null).get();
                    try {
                        ListParametersResponse response = ListParametersResponse.parseFrom(data);
                        parameters.addAll(response.getParameterList());
                        if (response.hasContinuationToken()) {
                            next = response.getContinuationToken();
                        } else {
                            break;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        log.log(Level.SEVERE, "Failed to decode server response", e);
                        break;
                    }
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    log.log(Level.SEVERE, "Exception while loading parameters: " + cause.getMessage(), cause);
                    return Status.OK_STATUS;
                }
            }
            processMetaParameters(parameters);
            return Status.OK_STATUS;
        });
        job.setPriority(Job.LONG);
        job.schedule(1000L);
    }

    public CompletableFuture<byte[]> requestParameterDetail(String qualifiedName) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        String path = encodePath("/mdb/" + instance + "/parameters" + qualifiedName);
        return yamcsClient.get(path, null);
    }

    public CompletableFuture<byte[]> fetchParameterValue(String instance, String qualifiedName) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String path = encodePath("/archive/" + instance + "/parameters2" + qualifiedName);
        return yamcsClient.get(path + "?limit=1", null);
    }

    public CompletableFuture<byte[]> setParameter(String processor, NamedObjectId id, Value value) {
        String pResource = toURISegments(id);
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        String path = encodePath("/processors/" + instance + "/" + processor + "/parameters" + pResource);
        return yamcsClient.put(path, value);
    }

    public void subscribeParameters(NamedObjectList idList) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        if (yamcsClient.isConnected()) {
            yamcsClient.subscribe(new ParameterWebSocketRequest("subscribe", idList), this);
        }
    }

    /**
     * This encodes the path only (not the query string). This was introduced to encode square brackets in a qualified
     * name of an array entry.
     */
    private static String encodePath(String arg) {
        try {
            String[] segments = arg.split("/");
            for (int i = 0; i < segments.length; i++) {
                segments[i] = URLEncoder.encode(segments[i], "UTF-8");
            }
            return String.join("/", segments);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void onMessage(WebSocketSubscriptionData msg) {
        if (msg.hasParameterData()) {
            ParameterData pdata = msg.getParameterData();
            log.finest(String.format("Sending %s parameters to %s listeners",
                    pdata.getParameterCount(), parameterListeners.size()));
            parameterListeners.forEach(l -> l.onParameterData(pdata));
        }
    }

    public void unsubscribeParameters(NamedObjectList idList) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        if (yamcsClient.isConnected()) {
            yamcsClient.sendWebSocketMessage(new ParameterWebSocketRequest("unsubscribe", idList));
        }
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

    public List<ParameterInfo> getMetaParameters() {
        return new ArrayList<>(metaParameters);
    }

    private String toURISegments(NamedObjectId id) {
        if (!id.hasNamespace()) {
            return id.getName();
        } else {
            return "/" + id.getNamespace() + "/" + id.getName();
        }
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
