
repositories {
    jcenter()
}

plugins {
    id("java-gradle-plugin")
}

group = "org.trypticon.gradle.plugins"
version = "0.1.0"

dependencies {
    "implementation"("com.google.guava:guava:27.0-jre")

    "testImplementation"(gradleTestKit())
    "testImplementation"("junit:junit:4.12")
    "testImplementation"("org.hamcrest:java-hamcrest:2.0.0.0")
    "testImplementation"("org.hamcrest:hamcrest-junit:2.0.0.0")
}

configure<GradlePluginDevelopmentExtension> {
    plugins {
        register("travel-agent") {
            id = "org.trypticon.gradle.plugins.travel-agent"
            implementationClass = "org.trypticon.gradle.plugins.travelagent.TravelAgentPlugin"
        }
    }
}
