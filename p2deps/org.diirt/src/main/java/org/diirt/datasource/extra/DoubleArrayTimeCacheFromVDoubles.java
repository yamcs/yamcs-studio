/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.extra;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.diirt.datasource.ReadFunction;
import org.diirt.vtype.Display;
import org.diirt.vtype.VNumber;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.CollectionNumbers;
import org.diirt.util.array.ListNumber;
import org.diirt.util.time.TimeInterval;

/**
 *
 * @author carcassi
 */
public class DoubleArrayTimeCacheFromVDoubles implements DoubleArrayTimeCache {

    private NavigableMap<Instant, ArrayDouble> cache = new TreeMap<Instant, ArrayDouble>();
    private List<? extends ReadFunction<? extends List<? extends VNumber>>> functions;
    private Display display;
    private Duration tolerance = Duration.ofMillis(1);

    public DoubleArrayTimeCacheFromVDoubles(List<? extends ReadFunction<? extends List<? extends VNumber>>> functions) {
        this.functions = functions;
    }

    public class Data implements DoubleArrayTimeCache.Data {

        private List<Instant> times = new ArrayList<Instant>();
        private List<ArrayDouble> arrays = new ArrayList<ArrayDouble>();
        private Instant begin;
        private Instant end;

        private Data(SortedMap<Instant, ArrayDouble> subMap, Instant begin, Instant end) {
            this.begin = begin;
            this.end = end;
            for (Map.Entry<Instant, ArrayDouble> en : subMap.entrySet()) {
                times.add(en.getKey());
                arrays.add(en.getValue());
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

    /**
     * Finds the array in the cache that is within the tolerance from the
     * given Instant. If not found, it creates a new array and adds
     * it to the cache.
     *
     * @param Instant a time
     * @return the array for that time
     */
    private ArrayDouble arrayFor(Instant Instant) {
        // Try to find the array at the exact time
        ArrayDouble array = cache.get(Instant);
        if (array != null)
            return array;

        // See if the array after the Instant is in range
        Instant newTime = cache.higherKey(Instant);
        if (newTime != null && newTime.minus(tolerance).compareTo(Instant) <= 0) {
            return cache.get(newTime);
        }

        // See if the array before the Instant is in range
        newTime = cache.lowerKey(Instant);
        if (newTime != null && newTime.plus(tolerance).compareTo(Instant) >= 0) {
            return cache.get(newTime);
        }

        // Nothing found. Create a new array and initialize it with
        // the previous data (if any)
        if (newTime != null) {
            array = new ArrayDouble(Arrays.copyOf(CollectionNumbers.wrappedDoubleArray(cache.get(newTime)), functions.size()), false);
        } else {
            double[] blank = new double[functions.size()];
            Arrays.fill(blank, Double.NaN);
            array = new ArrayDouble(blank, false);
        }
        cache.put(Instant, array);
        return array;
    }

    @Override
    public DoubleArrayTimeCache.Data getData(Instant begin, Instant end) {
        // Let's do it in a crappy way first...
        for (int n = 0; n < functions.size(); n++) {
            List<? extends VNumber> vDoubles = functions.get(n).readValue();
            for (VNumber vNumber : vDoubles) {
                if (display == null)
                    display = vNumber;
                ArrayDouble array = arrayFor(vNumber.getTimestamp());
                double oldValue = array.getDouble(n);
                array.setDouble(n, vNumber.getValue().doubleValue());

                // Fix the following values
                for (Map.Entry<Instant, ArrayDouble> en : cache.tailMap(vNumber.getTimestamp().plus(tolerance)).entrySet()) {
                    // If no value or same value as before, replace it
                    if (Double.isNaN(en.getValue().getDouble(n)) || en.getValue().getDouble(n) == oldValue)
                        en.getValue().setDouble(n, vNumber.getValue().doubleValue());
                }
            }
        }

        if (cache.isEmpty())
            return null;

        Instant newBegin = cache.lowerKey(begin);
        if (newBegin == null)
            newBegin = cache.firstKey();

        deleteBefore(begin);
        return data(newBegin, end);
    }

    private List<TimeInterval> update() {
        // Let's do it in a crappy way first...
        // Only keep track of first and last change
        Instant firstChange = null;
        Instant lastChange = null;
        for (int n = 0; n < functions.size(); n++) {
            List<? extends VNumber> vNumbers = functions.get(n).readValue();
            for (VNumber vNumber : vNumbers) {
                if (display == null)
                    display = vNumber;
                ArrayDouble array = arrayFor(vNumber.getTimestamp());
                double oldValue = array.getDouble(n);
                array.setDouble(n, vNumber.getValue().doubleValue());
                if (firstChange == null) {
                    firstChange = vNumber.getTimestamp();
                }
                if (lastChange == null) {
                    lastChange = vNumber.getTimestamp();
                }
                firstChange = min(firstChange, vNumber.getTimestamp());
                lastChange = max(lastChange, vNumber.getTimestamp());

                // Fix the following values
                for (Map.Entry<Instant, ArrayDouble> en : cache.tailMap(vNumber.getTimestamp().plus(tolerance)).entrySet()) {
                    // If no value or same value as before, replace it
                    if (Double.isNaN(en.getValue().getDouble(n)) || en.getValue().getDouble(n) == oldValue)
                        en.getValue().setDouble(n, vNumber.getValue().doubleValue());
                }
            }
        }

        if (firstChange == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(TimeInterval.between(firstChange.minus(tolerance), lastChange));
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

    private DoubleArrayTimeCache.Data data(Instant begin, Instant end) {
        return new Data(cache.subMap(begin, end), begin, end);
    }

    private <T extends Comparable<T>> T max(T a, T b) {
        if (a.compareTo(b) > 0) {
            return a;
        } else {
            return b;
        }
    }

    private <T extends Comparable<T>> T min(T a, T b) {
        if (a.compareTo(b) < 0) {
            return a;
        } else {
            return b;
        }
    }

    @Override
    public List<DoubleArrayTimeCache.Data> newData(Instant beginUpdate, Instant endUpdate, Instant beginNew, Instant endNew) {
        List<TimeInterval> updates = update();
        if (updates.isEmpty())
            return Collections.singletonList(data(cache.lowerKey(beginNew), endNew));

        TimeInterval updateInterval = updates.get(0);
        Instant newBegin = max(beginUpdate, updateInterval.getStart());
        newBegin = min(newBegin, beginNew);
        deleteBefore(beginUpdate);
        return Collections.singletonList(data(newBegin, endNew));
    }

    @Override
    public Display getDisplay() {
        return display;
    }

}
