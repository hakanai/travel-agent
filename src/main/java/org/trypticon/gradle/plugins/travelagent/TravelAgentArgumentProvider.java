package org.trypticon.gradle.plugins.travelagent;

import org.gradle.api.Named;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Nested;
import org.gradle.process.CommandLineArgumentProvider;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Provides command-line arguments to set the location to travel to.
 */
class TravelAgentArgumentProvider implements CommandLineArgumentProvider, Named {
    private static final Logger logger = Logging.getLogger(TravelAgentArgumentProvider.class);

    private final TravelAgentExtension extension;

    TravelAgentArgumentProvider(TravelAgentExtension extension) {
        this.extension = extension;
    }

    /**
     * Gets the extension.
     * This method exists primarily to give Gradle access to the nested properties.
     *
     * @return the extension.
     */
    @Nested
    public TravelAgentExtension getExtension() {
        return extension;
    }

    @Override
    public Iterable<String> asArguments() {
        Trip trip = extension.suggestTrip();
        Iterable<String> result = trip.toCommandLineArguments();

        if (logger.isLifecycleEnabled()) {
            String language = trip.getLanguage();
            String country = trip.getCountry();
            Locale locale = new Locale(language, country);
            String displayLanguage = locale.getDisplayLanguage(Locale.ROOT);
            String displayCountry = locale.getDisplayCountry(Locale.ROOT);
            String timeZone = trip.getTimeZone();
            String displayTimeZone = TimeZone.getTimeZone(timeZone).getDisplayName(Locale.ROOT);

            logger.lifecycle("Taking a trip to:\n" +
                            "    Language:   {} ({})\n" +
                            "    Country:    {} ({})\n" +
                            "    Time Zone:  {} ({})\n" +
                            "    To reproduce manually:\n" +
                            "        -Ptravelagent.language={} -Ptravelagent.country={} -Ptravelagent.timezone={}",
                    language, displayLanguage, country, displayCountry, timeZone, displayTimeZone,
                    language, country, timeZone);
        }

        return result;
    }

    @Override
    public String getName() {
        // Returning a name helps ensure that up-to-date checks on the Test task don't flip around
        // unstably due to the command-line arguments changing their order.
        return "travelAgent";
    }
}
