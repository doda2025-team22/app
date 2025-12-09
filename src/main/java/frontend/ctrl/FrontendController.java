package frontend.ctrl;

import com.doda.lib_version.VersionUtil;
import frontend.data.metrics.Counter;
import frontend.data.metrics.Gauge;
import frontend.data.metrics.Histogram;
import frontend.data.metrics.MetricsRegistry;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.Objects;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import frontend.data.Sms;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "/sms")
public class FrontendController {

    private String modelHost;

    private RestTemplateBuilder rest;

    // -- metrics fields --

    private final MetricsRegistry registry;

    private final Counter buttonCounter;

    private final Gauge spamGruessPercentage;

    private final Histogram textLengthHistogram;

    private int spamGuessCount;

    private int hamGuessCount;

    public FrontendController(RestTemplateBuilder rest, Environment env, MetricsRegistry registry) {
        this.rest = rest;
        this.modelHost = env.getProperty("MODEL_HOST");
        this.registry = registry;
        this.buttonCounter = registry.createCounter(
            "button_presses_counter",
            "Total numbers of button presses");
        this.spamGruessPercentage = registry.createGauge(
            "spam_guess_gauge",
            "The current percentage of spam guess of the users compared to all predictions");
        this.textLengthHistogram = registry.createHistogram(
            "length_of_text",
            "The distribution of the number of characters of the sms tested by the user",
            new double[]{10,20,30,40,50,60,70,80,90});
        this.spamGuessCount = 0;
        this.hamGuessCount = 0;
        assertModelHost();
    }

    private void assertModelHost() {
        if (modelHost == null || modelHost.strip().isEmpty()) {
            System.err.println("ERROR: ENV variable MODEL_HOST is null or empty");
            System.exit(1);
        }
        modelHost = modelHost.strip();
        if (modelHost.indexOf("://") == -1) {
            var m = "ERROR: ENV variable MODEL_HOST is missing protocol, like \"http://...\" (was: \"%s\")\n";
            System.err.printf(m, modelHost);
            System.exit(1);
        } else {
            System.out.printf("Working with MODEL_HOST=\"%s\"\n", modelHost);
        }
    }

    @GetMapping("")
    public String redirectToSlash(HttpServletRequest request) {
        // relative REST requests in JS will end up on / and not on /sms
        return "redirect:" + request.getRequestURI() + "/";
    }

    @GetMapping("/")
    public String index(Model m) {
        m.addAttribute("hostname", modelHost);
        return "sms/index";
    }

    @GetMapping("/library-version")
    public String getLibraryVersion(Model m) {
        System.out.println("Library version is " + VersionUtil.getVersion());
        m.addAttribute("version",VersionUtil.getVersion());
        return "sms/library-version";
    }
    @PostMapping("/library-version")
    public String postLibraryVersionButton(Model m) {
        // -- metrics start --
        System.out.println("Clicked the button in version!");
        buttonCounter.inc("library_version_button");
        // -- metrics end --

        m.addAttribute("version",VersionUtil.getVersion());
        return "sms/library-version";
    }

    @PostMapping({ "", "/" })
    @ResponseBody
    public Sms predict(@RequestBody Sms sms) {
        // -- metrics start --
        System.out.println("Clicked the button for prediction!");
        buttonCounter.inc("prediction_button");

        if (Objects.equals(sms.guess, "ham")) {
            hamGuessCount++;
        } else {
            spamGuessCount++;
        }
        spamGruessPercentage.set("prediction", (double) spamGuessCount * 100 / (hamGuessCount + spamGuessCount));
        textLengthHistogram.observe(sms.sms.length());
        // -- metrics end --

        sms.result = getPrediction(sms);

        System.out.printf("Prediction: %s\n", sms.result);
        return sms;
    }

    private String getPrediction(Sms sms) {
        try {
            var url = new URI(modelHost + "/predict");
            var c = rest.build().postForEntity(url, sms, Sms.class);
            return c.getBody().result.trim();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // --- Exposing metrics ---
    // Gauge -> num of active sessions
    // Counter -> number of predictions gotten
    // Histogram -> duration for how long it takes to make a prediction, distribution of that

    @GetMapping(value = "/metrics", produces = "text/plain; version=0.0.4")
    @ResponseBody
    public String exposeMetrics() {
        return registry.expose();
    }
}