/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sim;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.diirt.util.time.TimeInterval;
import org.diirt.vtype.VDouble;

/**
 * Function that reads an xml file and simulates a pv by replaying it.
 *
 * @author carcassi
 */
public class Replay extends Simulation<VDouble> {

    private Duration offset;
    private XmlValues values;

    /**
     * The URI of the file. Any of the standard protocol is supported (file:,
     * http:, ...). Relative uris are allowed, and they will be resolved on the
     * current location in the filesystem.
     *
     * @param uri the location of the playback file
     */
    public Replay(String uri) {
        super(Duration.ofMillis(10), VDouble.class);
        values = ReplayParser.parse(URI.create(uri));
    }

    @Override
    List<VDouble> createValues(TimeInterval interval) {
        offset = Duration.between(interval.getStart(), ((VDouble) values.getValues().get(0)).getTimestamp()).abs();
        TimeInterval originalInterval = interval.minus(offset);
        List<VDouble> newValues = new ArrayList<VDouble>();
        for (ReplayValue value : values.getValues()) {
            if (originalInterval.contains(value.getTimestamp())) {
                ReplayValue copy = value.copy();
                if (values.isAdjustTime()) {
                    copy.adjustTime(offset);
                }
                newValues.add((VDouble) copy);
            }
        }
        return newValues;
    }

}
