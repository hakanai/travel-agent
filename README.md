
Travel Agent Plugin
===================

Travel Agent is a Gradle plugin to deliberately exacerbate globalisation bugs when running tests.
Globalisation preferences are randomly selected from a collection of different locales when the
test run is started.

This can help to find bugs which are somehow not caught by other tools such as [Forbidden API Checker][1],
whether it be because the code is touching new APIs which aren't yet known to be evil, or because the developer
has knowingly subverted the checking by performing some kind of incorrect workaround.


Applying the Plugin
-------------------

To include, add the following to your build script:

(Groovy)

```groovy
plugins {
    id 'org.trypticon.gradle.plugins.travel-agent' version '3.0.0'
}
```

(Kotlin)

```kotlin
plugins {
    id("org.trypticon.gradle.plugins.travel-agent") version "3.0.0"
}
```

Or if you cannot use the `plugins` block for some reason:

(Groovy)

```groovy
buildscript {
    repositories { jcenter() }
    dependencies { classpath 'org.trypticon.gradle.plugins:travel-agent:0.1.0+' }
}

apply plugin: org.trypticon.gradle.plugins.travelagent.TravelAgentPlugin
```

(Kotlin)

```kotlin
buildscript {
    repositories { jcenter() }
    dependencies { classpath("org.trypticon.gradle.plugins:travel-agent:0.1.0+") }
}

apply<org.trypticon.gradle.plugins.travelagent.TravelAgentPlugin>()
```


Configuration
-------------

Adding trips to the list of available trips:

(If you do find an interesting one, we'd like to hear about it too. Maybe it could be in the predefined trips?)

(Groovy)

```groovy
travelAgent {
    availableTrips.empty()
    availableTrips.add(Trip('en', 'AU', 'Australia/Sydney'))
    availableTrips.add(providers.provider { new Trip('en', 'AU', 'Australia/Melbourne') })
}
```

(Kotlin)

```kotlin
configure<TravelAgentExtension> {
    availableTrips.empty()
    availableTrips.add(Trip("en", "AU", "Australia/Sydney"))
    availableTrips.add(providers.provider { Trip("en", "AU", "Australia/Melbourne") })
}
```

Filtering out a trip which is known to cause failures:

(Never a great idea, but sometimes you have no choice. For instance, if you're testing Gradle builds,
Gradle itself doesn't work when run in Turkish locale.)

(Groovy)

```groovy
travelAgent {
    knownFailing { trip -> trip.language == 'tr' }
}
```

(Kotlin)

```kotlin
configure<TravelAgentExtension> {
    knownFailing { trip -> trip.language == "tr" }
}
```


Parameters
----------

If a test fails in a specific region, you're going to want to go back there to investigate why.
In this situation, you can force the settings as follows:

* `-Ptravelagent.language=...`
* `-Ptravelagent.country=...`
* `-Ptravelagent.timezone=...`

Passing only some of these parameters and not others works as expected - the provided ones are
used as a filter to narrow down the accepted trips.




[1]: https://github.com/policeman-tools/forbidden-apis
