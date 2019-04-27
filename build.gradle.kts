
repositories {
    jcenter()
}

plugins {
    id("java-gradle-plugin")
    id("jacoco")
    id("pl.droidsonroids.jacoco.testkit") version "1.0.3"
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configure<GradlePluginDevelopmentExtension> {
    plugins {
        register("travel-agent") {
            id = "org.trypticon.gradle.plugins.travel-agent"
            implementationClass = "org.trypticon.gradle.plugins.travelagent.TravelAgentPlugin"
        }
    }
}

tasks {
    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal.ONE
                }
            }
        }
    }
    check {
        dependsOn(jacocoTestCoverageVerification)
    }
}
