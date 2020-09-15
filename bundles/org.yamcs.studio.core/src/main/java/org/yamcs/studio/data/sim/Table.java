package org.yamcs.studio.data.sim;

import static org.yamcs.studio.data.vtype.ValueFactory.newVTable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListDouble;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VTable;

/**
 * Function to simulate a signal that generates VTables.
 */
public class Table extends SimFunction<VTable> {

    public Table() {
        this(0.1);
    }

    public Table(Double interval) {
        super(interval);
        if (interval <= 0.0) {
            throw new IllegalArgumentException("Interval must be greater than zero (was " + interval + ")");
        }
    }

    private final List<Class<?>> types = Arrays.asList((Class<?>) String.class, Double.TYPE, Integer.TYPE,
            Instant.class);

    @Override
    VTable nextValue() {
        return newVTable(types, Arrays.asList("Text", "Value", "Index", "Timestamps"),
                Arrays.asList((Object) generateStringColumn(10), generateDoubleColumn(10),
                        generateIntegerColumn(10), generateTimestampColumn(10)));
    }

    private final Random rand = new Random();

    ListInt generateIntegerColumn(int size) {
        int[] column = new int[size];
        for (int i = 0; i < column.length; i++) {
            column[i] = i;
        }
        return new ArrayInt(column);
    }

    ListDouble generateDoubleColumn(int size) {
        double[] column = new double[size];
        for (int i = 0; i < column.length; i++) {
            column[i] = rand.nextGaussian();
        }
        return new ArrayDouble(column);
    }

    List<String> generateStringColumn(int size) {
        String[] column = new String[size];
        for (int i = 0; i < column.length; i++) {
            column[i] = generateString(i);
        }
        return Arrays.asList(column);
    }

    List<Instant> generateTimestampColumn(int size) {
        List<Instant> timestamps = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            timestamps.add(Instant.now());
        }
        return timestamps;
    }

    String generateString(int id) {
        if (id == 0) {
            return "A";
        }

        StringBuilder sb = new StringBuilder();
        while (id != 0) {
            char letter = (char) ('A' + (id % 26));
            sb.insert(0, letter);
            id = id / 26;
        }
        return sb.toString();
    }
}
