package org.trypticon.gradle.plugins.travelagent;

import com.google.common.collect.ImmutableList;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.Input;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.security.SecureRandom;
import java.util.List;

/**
 * Extension holding per-task configuration for Travel Agent.
 */
public class TravelAgentTaskExtension {

    /**
     * The extension holding global configuration.
     */
    private final TravelAgentExtension globalExtension;

    /**
     * Lazy property for enabled status.
     */
    private final Property<Boolean> enabled;

    /**
     * Lazy list of available trips.
     */
    private final ListProperty<Trip> availableTrips;

    /**
     * Spec to match trips to accept.
     */
    private Spec<? super Trip> filter = Specs.SATISFIES_ALL;

    /**
     * Lazy list of acceptable trips.
     */
    private final Provider<List<Trip>> acceptableTrips;


    /**
     * Constructs the extension.
     *
     * @param globalExtension the extension holding global configuration.
     * @param objectFactory the object factory.
     */
    @Inject
    public TravelAgentTaskExtension(@Nonnull TravelAgentExtension globalExtension, @Nonnull ObjectFactory objectFactory) {
        this.globalExtension = globalExtension;

        enabled = objectFactory.property(Boolean.class);
        enabled.set(globalExtension.getEnabled());

        availableTrips = objectFactory.listProperty(Trip.class);
        availableTrips.set(globalExtension.getAvailableTrips());

        acceptableTrips = availableTrips.map(trips ->
                trips.stream()
                        .filter(filter::isSatisfiedBy)
                        .filter(globalExtension.getFilter()::isSatisfiedBy)
                        .collect(ImmutableList.toImmutableList()));
    }


    /**
     * Gets whether the travel agent is enabled.
     *
     * @return {@code true} if enabled, {@code false} if disabled.
     */
    public Property<Boolean> getEnabled() {
        return enabled;
    }

    /**
     * Gets the available trips.
     *
     * @return the available trips.
     */
    public ListProperty<Trip> getAvailableTrips() {
        return availableTrips;
    }

    /**
     * Gets the trips satisfying the filter.
     *
     * Accessible primarily for the benefit of Gradle's up-to-date checks.
     *
     * As far as up-to-date checking is concerned, we only care about the list of acceptable trips,
     * not which one was actually chosen at random. Even if you wanted to be purist about this and say
     * that the chosen trip is the one which matters, all that happens is your test task will never be
     * up-to-date.
     *
     * @return the trips satisfying the filter.
     */
    @Input
    public Provider<List<Trip>> getAcceptableTrips()
    {
        return acceptableTrips;
    }

    /**
     * Adds a spec for a known failing combination. Sometimes this sort of thing can't be avoided.
     * For instance, Gradle themselves seemingly refuse to fix their own issues with running in Turkish,
     * which makes our own integration tests fail since they are running Gradle.
     *
     * @param spec the spec to match failing trips.
     */
    public void knownFailing(Spec<Trip> spec) {
        filter = Specs.intersect(filter, Specs.negate(spec));
    }

    /**
     * Suggests a trip which matches the requirements.
     *
     * @return the trip.
     */
    Trip suggestTrip() {
        List<Trip> acceptableTrips = this.acceptableTrips.get();
        int randomIndex = new SecureRandom().nextInt(acceptableTrips.size());
        return acceptableTrips.get(randomIndex);
    }
}
