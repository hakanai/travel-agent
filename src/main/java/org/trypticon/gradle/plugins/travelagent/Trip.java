package org.trypticon.gradle.plugins.travelagent;

import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * Models a locale, time zone and any other related information for a single location in the world.
 */
@Immutable
public final class Trip implements Serializable {
    private final String language;
    private final String country;
    private final String timeZone;

    /**
     * Constructs the trip.
     *
     * @param language the language code.
     * @param country the country or region code.
     * @param timeZone the time zone ID.
     */
    public Trip(String language, String country, String timeZone) {
        this.language = language;
        this.country = country;
        this.timeZone = timeZone;
    }

    /**
     * Gets the language code.
     *
     * @return the language code.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Gets the country code.
     *
     * @return the country code.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Gets the time zone ID.
     *
     * @return the time zone ID.
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Converts the trip to command-line arguments for the JVM.
     *
     * @return the command-line arguments.
     */
    Iterable<String> toCommandLineArguments() {
        return ImmutableList.of(
                "-Duser.language=" + language,
                "-Duser.country=" + country,
                "-Duser.timezone=" + timeZone);
    }
}
