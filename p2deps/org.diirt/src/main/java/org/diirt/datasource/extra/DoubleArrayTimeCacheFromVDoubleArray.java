/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.extra;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.diirt.datasource.ReadFunction;
import org.diirt.vtype.Display;
import org.diirt.vtype.VNumberArray;
import org.diirt.util.array.ListNumber;

/**
 *
 * @author carcassi
 */
public class DoubleArrayTimeCacheFromVDoubleArray implements DoubleArrayTimeCache {

    private NavigableMap<Instant, VNumberArray> cache = new TreeMap<Instant, VNumberArray>();
    private ReadFunction<? extends List<? extends VNumberArray>> function;
    private Display display;

    public DoubleArrayTimeCacheFromVDoubleArray(ReadFunction<? extends List<? extends VNumberArray>> function) {
        this.function = function;
    }

    public class Data implements DoubleArrayTimeCache.Data {

        private List<Instant> times = new ArrayList<Instant>();
        private List<ListNumber> arrays = new ArrayList<ListNumber>();
        private Instant begin;
        private Instant end;

        private Data(SortedMap<Instant, VNumberArray> subMap, Instant begin, Instant end) {
            this.begin = begin;
            this.end = end;
            for (Map.Entry<Instant, VNumberArray> en : subMap.entrySet()) {
                times.add(en.getKey());
                arrays.add(en.getValue().getData());
            }
        }

        @Override
        public Instant getBegin() {
            return begin;
        }

        @Override
        public Instant getEnd() {
            return end;
        }

        @Override
        public int getNArrays() {
            return times.size();
        }

        @Override
        public ListNumber getArray(int index) {
            return arrays.get(index);
        }

        @Override
        public Instant getTimestamp(int index) {
            return times.get(index);
        }

    }

    private void deleteBefore(Instant Instant) {
        if (cache.isEmpty())
            return;

        // This we want to keep as we need to draw the area
        // from the Instant to the first new value
        Instant firstEntryBeforeInstant = cache.lowerKey(Instant);
        if (firstEntryBeforeInstant == null)
            return;

        // This is the last entry we want to delete
        Instant lastToDelete = cache.lowerKey(firstEntryBeforeInstant);
        if (lastToDelete == null)
            return;

        Instant firstKey = cache.firstKey();
        while (firstKey.compareTo(lastToDelete) <= 0) {
            cache.remove(firstKey);
            firstKey = cache.firstKey();
        }
    }

    @Override
    public DoubleArrayTimeCache.Data getData(Instant begin, Instant end) {
        List<? extends VNumberArray> newValues = function.readValue();
        for (VNumberArray value : newValues) {
            cache.put(value.getTimestamp(), value);
        }
        if (cache.isEmpty())
            return null;

        Instant newBegin = cache.lowerKey(begin);
        if (newBegin == null)
            newBegin = cache.firstKey();

        deleteBefore(begin);
        return data(newBegin, end);
    }

    private DoubleArrayTimeCache.Data data(Instant begin, Instant end) {
        return new Data(cache.subMap(begin, end), begin, end);
    }

    @Override
    public List<DoubleArrayTimeCache.Data> newData(Instant beginUpdate, Instant endUpdate, Instant beginNew, Instant endNew) {
        List<? extends VNumberArray> newValues = function.readValue();

        // No new values, just return the last value
        if (newValues.isEmpty()) {
            return Collections.singletonList(data(cache.lowerKey(endNew), endNew));
        }

        List<Instant> newInstants = new ArrayList<Instant>();
        for (VNumberArray value : newValues) {
            cache.put(value.getTimestamp(), value);
            newInstants.add(value.getTimestamp());
        }
        if (cache.isEmpty())
            return Collections.emptyList();

        Collections.sort(newInstants);
        Instant firstNewValue = newInstants.get(0);

        // We have just one section that start from the oldest update.
        // If the oldest update is too far, we use the start of the update region.
        // If the oldest update is too recent, we start from the being period
        Instant newBegin = firstNewValue;
        if (firstNewValue.compareTo(beginUpdate) < 0) {
            newBegin = beginUpdate;
        }
        if (firstNewValue.compareTo(beginNew) > 0) {
            newBegin = beginNew;
        }


        newBegin = cache.lowerKey(newBegin);
        if (newBegin == null)
            newBegin = cache.firstKey();

        deleteBefore(beginUpdate);
        return Collections.singletonList(data(newBegin, endNew));
    }

    @Override
    public Display getDisplay() {
        if (display == null) {
            display = cache.firstEntry().getValue();
        }

        return display;
    }

}
