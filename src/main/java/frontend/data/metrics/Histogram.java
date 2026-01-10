package frontend.data.metrics;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Histogram {
    private final String name;
    private final String help;
    private final double[] buckets; // sorted ascending, without +Inf
    private final Map<String, Long> bucketCounts = new LinkedHashMap<>();
    private long count = 0;
    private double sum = 0;

    public Histogram(String name, String help, double[] buckets) {
        this.name = name;
        this.help = help;
        this.buckets = buckets.clone();
        Arrays.sort(this.buckets);

        for (double b : this.buckets) {
            bucketCounts.put(String.valueOf(b), 0L);
        }
        // Prometheus histograms always include the +Inf bucket
        bucketCounts.put("+Inf", 0L);
    }

    public synchronized void observe(double value) {
        sum += value;
        count++;

        // Prometheus histogram buckets are cumulative: bucket{le="X"} counts observations <= X
        for (double b : buckets) {
            if (value <= b) {
                bucketCounts.put(String.valueOf(b), bucketCounts.get(String.valueOf(b)) + 1);
            }
        }
        // +Inf bucket must equal total observation count
        bucketCounts.put("+Inf", bucketCounts.get("+Inf") + 1);
    }

    public synchronized long getCount() { return count; }
    public synchronized double getSum() { return sum; }
    public synchronized Map<String, Long> getBuckets() { return new LinkedHashMap<>(bucketCounts); }

    public String getName() { return name; }
    public String getHelp() { return help; }
}