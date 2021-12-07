/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.yamcs.studio.autocomplete.impl.DataSourceProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.IContentParser;
import org.yamcs.studio.autocomplete.preferences.Preferences;

/**
 * Service which handles content parsing (see {@link IContentParser}) and requesting proposals from defined providers
 * (see {@link IAutoCompleteProvider}.
 */
public class AutoCompleteService {

    /**
     * Flag that controls the printing of debug info.
     */
    public static final boolean DEBUG = false;

    private class ProviderTask implements Runnable {

        private final Long uniqueId;
        private final Integer index;
        private final ContentDescriptor desc;
        private final ProviderSettings settings;
        private final IAutoCompleteResultListener listener;
        private boolean canceled = false;

        public ProviderTask(Long uniqueId, Integer index, ContentDescriptor desc, ProviderSettings settings,
                IAutoCompleteResultListener listener) {
            this.index = index;
            this.uniqueId = uniqueId;
            this.desc = desc;
            this.settings = settings;
            this.listener = listener;
        }

        @Override
        public void run() {
            var result = settings.getProvider().listResult(desc, settings.getMaxResults());
            if (result != null && !settings.getName().equals(DataSourceProvider.NAME)) {
                // TODO: find a better solution to hide DataSourceProvider...
                result.setProvider(settings.getName());
            }
            if (!canceled) {
                listener.handleResult(uniqueId, index, result);
            }
            synchronized (workQueue) {
                workQueue.remove(this);
            }
        }

        public void cancel() {
            settings.getProvider().cancel();
            canceled = true;
        }

        @Override
        public int hashCode() {
            var prime = 31;
            var result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((index == null) ? 0 : index.hashCode());
            result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            var other = (ProviderTask) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (index == null) {
                if (other.index != null) {
                    return false;
                }
            } else if (!index.equals(other.index)) {
                return false;
            }
            if (uniqueId == null) {
                if (other.uniqueId != null) {
                    return false;
                }
            } else if (!uniqueId.equals(other.uniqueId)) {
                return false;
            }
            return true;
        }

        private AutoCompleteService getOuterType() {
            return AutoCompleteService.this;
        }

        @Override
        public String toString() {
            return "ProviderTask [uniqueId=" + uniqueId + ", index=" + index + ", desc=" + desc + ", settings="
                    + settings + ", canceled=" + canceled + "]";
        }
    }

    private class ScheduledContent implements Comparable<ScheduledContent> {
        public ProviderSettings settings;
        public ContentDescriptor desc;

        @Override
        public String toString() {
            return "ScheduledContent [settings=" + settings + ", desc=" + desc + "]";
        }

        @Override
        public int compareTo(ScheduledContent sc) {
            return this.settings.compareTo(sc.settings);
        }
    }

    private static AutoCompleteService instance;
    private Map<String, ProviderSettings> providerByName;
    private Map<String, List<ProviderSettings>> providersByType;
    private ProviderSettings defaultProvider;
    private List<ProviderTask> workQueue;
    private List<IContentParser> parsers;

    private AutoCompleteService() {
        try {
            providerByName = getOSGIProviders();
            if (providerByName.get("History") != null) {
                defaultProvider = providerByName.get("History");
            }
            parsers = getOSGIParsers();
        } catch (Exception e) {
            if (providerByName == null) {
                providerByName = new TreeMap<>();
            }
            if (parsers == null) {
                parsers = new ArrayList<>();
            }
        }
        providersByType = new TreeMap<>();
        workQueue = new ArrayList<>();
    }

    public static AutoCompleteService getInstance() {
        if (null == instance) {
            instance = new AutoCompleteService();
        }
        return instance;
    }

    public int get(Long uniqueId, AutoCompleteType acType, String content, IAutoCompleteResultListener listener) {
        AutoCompletePlugin.getLogger().log(Level.FINE,
                ">> ChannelNameService get: " + content + " for type: " + acType.value() + " <<");

        if (content == null || content.isEmpty()) {
            return 0; // no result
        }

        // Useful to handle default data source
        var desc = new ContentDescriptor();
        IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.csstudio.utility.pv");
        // Note: "default_type" is a common setting between pv, pv.ui, pvmanager and pvmanager.ui
        // They need to be kept synchronized.
        if (store != null) {
            desc.setDefaultDataSource(store.getString("default_type") + "://");
        }
        desc.setContentType(ContentType.Undefined);
        desc.setAutoCompleteType(acType);
        desc.setOriginalContent(content);
        desc.setValue(content);

        var descList = parseContent(desc);
        if (DEBUG) {
            System.out.println("=============================================");
            System.out.println("--- ContentDescriptor list ---");
            for (ContentDescriptor ct : descList) {
                System.out.println(ct);
            }
        }

        var index = 0; // Useful to keep the order
        var providerList = retrieveProviders(acType, descList);
        if (DEBUG) {
            System.out.println("--- Associated Content ---");
        }
        // Execute them in parallel
        for (ScheduledContent sc : providerList) {
            if (DEBUG) {
                System.out.println(sc.settings + " => " + sc.desc);
            }
            var task = new ProviderTask(uniqueId, index, sc.desc, sc.settings, listener);
            synchronized (workQueue) {
                workQueue.add(task);
            }
            new Thread(task).start();
            index++;
        }
        return index;
    }

    public void cancel(String type) {
        AutoCompletePlugin.getLogger().log(Level.FINE, ">> ChannelNameService canceled for type: " + type + " <<");
        synchronized (workQueue) {
            for (ProviderTask task : workQueue) {
                task.cancel();
            }
        }
    }

    public boolean hasProviders(String type) {
        return !providersByType.get(type).isEmpty();
    }

    /* Get providers from OSGI services */
    private Map<String, ProviderSettings> getOSGIProviders() throws InvalidSyntaxException {
        Map<String, ProviderSettings> map = new TreeMap<>();

        var context = AutoCompletePlugin.getBundleContext();
        Collection<ServiceReference<IAutoCompleteProvider>> references = context
                .getServiceReferences(IAutoCompleteProvider.class, null);

        for (ServiceReference<IAutoCompleteProvider> ref : references) {
            var provider = (IAutoCompleteProvider) context.getService(ref);
            var name = (String) ref.getProperty("component.name");
            var highLevelProvider = false;
            var prop = (String) ref.getProperty("highLevelProvider");
            if (prop != null && !prop.isEmpty()) {
                highLevelProvider = Boolean.valueOf(prop);
            }
            map.put(name, new ProviderSettings(name, provider, highLevelProvider));
        }
        return map;
    }

    /* Get providers from OSGI services */
    private List<IContentParser> getOSGIParsers() throws InvalidSyntaxException {
        List<IContentParser> list = new ArrayList<>();

        var context = AutoCompletePlugin.getBundleContext();
        Collection<ServiceReference<IContentParser>> references = context.getServiceReferences(IContentParser.class,
                null);

        for (ServiceReference<IContentParser> ref : references) {
            var parser = (IContentParser) context.getService(ref);
            list.add(parser);
        }
        return list;
    }

    /* Read the list of providers from preference string */
    private List<ProviderSettings> parseProviderList(String pref) {
        List<ProviderSettings> providerList = new ArrayList<>();

        if (pref != null && !pref.isEmpty()) {
            var index = -1;
            var st_provider = new StringTokenizer(pref, ";");
            while (st_provider.hasMoreTokens()) {
                var token_provider = st_provider.nextToken();

                if (token_provider.contains(",")) {
                    var name = token_provider.substring(0, token_provider.indexOf(',')).trim();
                    var max_results = Integer.parseInt(
                            token_provider.substring(token_provider.indexOf(',') + 1, token_provider.length()).trim());
                    if (providerByName.get(name) != null) {
                        providerList.add(new ProviderSettings(providerByName.get(name), ++index, max_results));
                    }
                } else {
                    var name = token_provider.trim();
                    if (providerByName.get(name) != null) {
                        providerList.add(new ProviderSettings(providerByName.get(name), ++index));
                    }
                }
            }
        }

        // add default provider
        if (providerList.isEmpty() && defaultProvider != null) {
            providerList.add(defaultProvider);
        }

        // add high level providers
        // TODO: all type have high level providers defined
        // => need restrictions ?
        for (ProviderSettings ps : providerByName.values()) {
            if (ps.isHighLevelProvider() && !providerList.contains(ps)) {
                providerList.add(new ProviderSettings(ps));
            }
        }

        Collections.sort(providerList);
        return providerList;
    }

    /* Associate 1 provider per descriptor */
    private List<ScheduledContent> retrieveProviders(AutoCompleteType acType, List<ContentDescriptor> tokens) {
        // retrieve the list from preferences
        var type = acType.value();
        if (providersByType.get(type) == null) {
            providersByType.put(type, parseProviderList(Preferences.getProviders(type)));
        }
        List<ProviderSettings> definedProviderList = new ArrayList<>(providersByType.get(type));

        // associate descriptor to a provider
        List<ScheduledContent> acceptedProviderList = new ArrayList<>();
        for (ContentDescriptor desc : tokens) {
            var it = definedProviderList.iterator();
            while (it.hasNext()) {
                var settings = it.next();
                if (settings.getProvider().accept(desc.getContentType())) {
                    var sc = new ScheduledContent();
                    sc.desc = desc;
                    sc.settings = settings;
                    acceptedProviderList.add(sc);
                    it.remove();
                }
            }
        }
        Collections.sort(acceptedProviderList);
        return acceptedProviderList;
    }

    /* Handle recursive parsing of a content desc. */
    private List<ContentDescriptor> parseContent(ContentDescriptor desc) {
        List<ContentDescriptor> tokenList = new ArrayList<>();

        ContentDescriptor newDesc = null;
        // backup data
        var startIndex = desc.getStartIndex();
        var endIndex = desc.getEndIndex();
        var acType = desc.getAutoCompleteType();
        var defaultDatasource = desc.getDefaultDataSource();
        var originalContent = desc.getOriginalContent();
        // cancel replay
        desc.setReplay(false);

        for (IContentParser parser : parsers) {
            if (parser.accept(desc) && (newDesc = parser.parse(desc)) != null) {
                newDesc.setAutoCompleteType(acType);
                newDesc.setDefaultDataSource(defaultDatasource);
                newDesc.setOriginalContent(originalContent);
                // update indexes
                newDesc.setStartIndex(newDesc.getStartIndex() + startIndex);
                newDesc.setEndIndex(newDesc.getEndIndex() + endIndex);
                if (newDesc.isReplay()) { // recursive
                    tokenList.addAll(parseContent(newDesc));
                } else {
                    tokenList.add(newDesc);
                }
            }
        }
        if (tokenList.isEmpty()) {
            desc.setContentType(ContentType.Undefined);
            tokenList.add(desc);
        }
        return tokenList;
    }

}
