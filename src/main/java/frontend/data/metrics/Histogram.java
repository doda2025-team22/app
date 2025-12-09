package frontend.data.metrics;

import java.util.HashMap;
import java.util.Map;

public class Histogram {
    private final String name;
    private final String help;
    private final double[] buckets; // will be added the +Inf bucket at the end
    private final Map<String, Long> bucketCounts = new HashMap<>();
    private long count = 0;
    private double sum = 0;

    public Histogram(String name, String help, double[] buckets) {
        this.name = name;
        this.help = help;
        this.buckets = buckets;

        for (double b : buckets) {
            bucketCounts.put(String.valueOf(b), 0L);
        }
        bucketCounts.put("+Inf", 0L);
    }

    public synchronized void observe(double value) {
        sum += value;
        count++;

        boolean bucketed = false;
        for (double b : buckets) {
            if (value <= b) {
                bucketCounts.put(String.valueOf(b), bucketCounts.get(String.valueOf(b)) + 1);
                bucketed = true;
                break;
            }
        }
        if (!bucketed) {
            bucketCounts.put("+Inf", bucketCounts.get("+Inf") + 1);
        }
    }

    public synchronized long getCount() { return count; }
    public synchronized double getSum() { return sum; }
    public synchronized Map<String, Long> getBuckets() { return new HashMap<>(bucketCounts); }

    public String getName() { return name; }
    public String getHelp() { return help; }
}
