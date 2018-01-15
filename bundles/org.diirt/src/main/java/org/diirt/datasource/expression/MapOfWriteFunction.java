/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.expression;

import org.diirt.datasource.WriteFunction;
import org.diirt.datasource.QueueCollector;
import java.util.HashMap;
import java.util.Map;

/**
 * A function that takes a map and writes the value to a different expression
 *
 * @author carcassi
 */
class MapOfWriteFunction<T> implements WriteFunction<Map<String, T>> {

    private final Map<String, WriteFunction<T>> functions = new HashMap<>();
    private final QueueCollector<MapUpdate<T>> mapUpdateCollector;

    public MapOfWriteFunction(QueueCollector<MapUpdate<T>> mapUpdateCollector) {
        this.mapUpdateCollector = mapUpdateCollector;
    }

    @Override
    public void writeValue(Map<String, T> newValue) {
        for (MapUpdate<T> mapUpdate : mapUpdateCollector.readValue()) {
            for (String name : mapUpdate.getExpressionsToDelete()) {
                functions.remove(name);
            }
            functions.putAll(mapUpdate.getWriteFunctionsToAdd());
        }

        for (Map.Entry<String, T> entry : newValue.entrySet()) {
            WriteFunction<T> function = functions.get(entry.getKey());
            if (function != null) {
                function.writeValue(entry.getValue());
            }
        }
    }

    public QueueCollector<MapUpdate<T>> getMapUpdateCollector() {
        return mapUpdateCollector;
    }

}
