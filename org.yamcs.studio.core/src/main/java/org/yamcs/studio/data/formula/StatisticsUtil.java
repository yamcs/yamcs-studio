package org.yamcs.studio.data.formula;

import java.util.List;

import org.yamcs.studio.data.vtype.CollectionNumber;

/**
 * Utility class to calculate statistical information.
 */
public class StatisticsUtil {

    private static class StatisticsImpl extends Statistics {

        private final int count;
        private final Range range;
        private double average;
        private double stdDev;

        public StatisticsImpl(Range range, int count, double average, double stdDev) {
            this.count = count;
            this.range = range;
            this.average = average;
            this.stdDev = stdDev;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Range getRange() {
            return range;
        }

        @Override
        public double getAverage() {
            return average;
        }

        @Override
        public double getStdDev() {
            return stdDev;
        }

    }

    /**
     * Calculates data statistics, excluding NaN values.
     *
     * @param data
     *            the data
     * @return the calculated statistics
     */
    public static Statistics statisticsOf(CollectionNumber data) {
        var iterator = data.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        var count = 0;
        var min = iterator.nextDouble();
        while (Double.isNaN(min)) {
            if (!iterator.hasNext()) {
                return null;
            } else {
                min = iterator.nextDouble();
            }
        }
        var max = min;
        var total = min;
        var totalSquare = min * min;
        count++;

        while (iterator.hasNext()) {
            var value = iterator.nextDouble();
            if (!Double.isNaN(value)) {
                if (value > max) {
                    max = value;
                }
                if (value < min) {
                    min = value;
                }
                total += value;
                totalSquare += value * value;
                count++;
            }
        }

        var average = total / count;
        var stdDev = Math.sqrt(totalSquare / count - average * average);

        return new StatisticsImpl(Ranges.range(min, max), count, average, stdDev);
    }

    /**
     * Aggregates statistical information.
     *
     * @param data
     *            a list of statistical information
     * @return the aggregate of all
     */
    public static Statistics statisticsOf(List<Statistics> data) {
        if (data.isEmpty()) {
            return null;
        }

        var iterator = data.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        Statistics first = null;
        while (first == null && iterator.hasNext()) {
            first = iterator.next();
        }
        if (first == null) {
            return null;
        }

        var count = first.getCount();
        var range = first.getRange();
        var total = first.getAverage() * first.getCount();
        var totalSquare = (first.getStdDev() * first.getStdDev() + first.getAverage() * first.getAverage())
                * first.getCount();

        while (iterator.hasNext()) {
            var stats = iterator.next();
            range = range.combine(stats.getRange());
            total += stats.getAverage() * stats.getCount();
            totalSquare += (stats.getStdDev() * stats.getStdDev() + stats.getAverage() * stats.getAverage())
                    * stats.getCount();
            count += stats.getCount();
        }

        var average = total / count;
        var stdDev = Math.sqrt(totalSquare / count - average * average);

        return new StatisticsImpl(range, count, average, stdDev);
    }

    /**
     * Creates the statistics, excluding NaN values, but the values are actually calculated when requested.
     *
     * @param data
     *            the data
     * @return the calculated statistics
     */
    public static Statistics lazyStatisticsOf(CollectionNumber data) {
        return new LazyStatistics(data);
    }

    private static class LazyStatistics extends Statistics {

        private Statistics stats;
        private CollectionNumber data;

        public LazyStatistics(CollectionNumber data) {
            this.data = data;
        }

        private void calculateStats() {
            if (stats == null) {
                stats = statisticsOf(data);
                data = null;
            }
        }

        @Override
        public int getCount() {
            calculateStats();
            return stats.getCount();
        }

        @Override
        public double getAverage() {
            calculateStats();
            return stats.getAverage();
        }

        @Override
        public double getStdDev() {
            calculateStats();
            return stats.getStdDev();
        }

        @Override
        public Range getRange() {
            calculateStats();
            return stats.getRange();
        }

    }
}
