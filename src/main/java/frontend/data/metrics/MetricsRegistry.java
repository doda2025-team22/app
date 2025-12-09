package frontend.data.metrics;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
// this code's skeleton was generated using AI, referring to the Prometheus documentation
@Component
public class MetricsRegistry {

    private final List<Counter> counters = new ArrayList<>();
    private final List<Gauge> gauges = new ArrayList<>();
    private final List<Histogram> histograms = new ArrayList<>();

    public Counter createCounter(String name, String help) {
        Counter c = new Counter(name, help);
        counters.add(c);
        return c;
    }

    public Gauge createGauge(String name, String help) {
        Gauge g = new Gauge(name, help);
        gauges.add(g);
        return g;
    }

    public Histogram createHistogram(String name, String help, double[] buckets) {
        Histogram h = new Histogram(name, help, buckets);
        histograms.add(h);
        return h;
    }

    public String expose() {
        StringBuilder sb = new StringBuilder();

        // --- COUNTERS ---
        for (Counter c : counters) {
            sb.append("# HELP ").append(c.getName()).append(" ").append(c.getHelp()).append("\n");
            sb.append("# TYPE ").append(c.getName()).append(" counter\n");

            // snapshot returns Map<String, Double> since one label only
            for (var entry : c.snapshot().entrySet()) {
                sb.append(c.getName())
                    .append(formatLabel(entry.getKey()))
                    .append(" ")
                    .append(entry.getValue())
                    .append("\n");
            }
            sb.append("\n");
        }

        // --- GAUGES ---
        for (Gauge g : gauges) {
            sb.append("# HELP ").append(g.getName()).append(" ").append(g.getHelp()).append("\n");
            sb.append("# TYPE ").append(g.getName()).append(" gauge\n");

            for (var entry : g.snapshot().entrySet()) {
                sb.append(g.getName())
                    .append(formatLabel(entry.getKey()))
                    .append(" ")
                    .append(entry.getValue())
                    .append("\n");
            }
            sb.append("\n");
        }

        // --- HISTOGRAMS ---
        for (Histogram h : histograms) {
            sb.append("# HELP ").append(h.getName()).append(" ").append(h.getHelp()).append("\n");
            sb.append("# TYPE ").append(h.getName()).append(" histogram\n");

            for (var bucket : h.getBuckets().entrySet()) {
                sb.append(h.getName()).append("_bucket")
                    .append("{le=\"").append(bucket.getKey()).append("\"}")
                    .append(" ").append(bucket.getValue()).append("\n");
            }

            sb.append(h.getName()).append("_sum ").append(h.getSum()).append("\n");
            sb.append(h.getName()).append("_count ").append(h.getCount()).append("\n\n");
        }

        return sb.toString();
    }

    /** Format a single label */
    private String formatLabel(String value) {
        if (value == null || value.isEmpty()) return "";
        return "{label=\"" + escape(value) + "\"}";
    }

    /** Follow Prometheus escaping rules */
    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"");
    }
}
