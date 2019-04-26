package org.trypticon.gradle.plugins.travelagent;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

/**
 * Tests for {@link TravelAgentPlugin}.
 */
public class TestTravelAgentPlugin {

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private File projectDir;

    @Before
    public void setUp() throws Exception
    {
        projectDir = temp.newFolder("test-project");
    }

    /**
     * Convenience method to write a file.
     *
     * @param relativePath the relative path to the file from the project directory.
     * @param content      the content to write into the file.
     * @throws Exception if an error occurs.
     */
    private void write(String relativePath, String... content) throws Exception
    {
        Path filePath = projectDir.toPath().resolve(relativePath);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, ImmutableList.copyOf(content),
                StandardCharsets.UTF_8);
    }

    /**
     * Gets a classpath string suitable for inserting into a Gradle build.
     *
     * @return the classpath string.
     */
    private String getGradleClassPath()
    {
        return Splitter.on(File.pathSeparator).splitToList(System.getProperty("java.class.path")).stream()
                .map(element -> '\"' + element + '\"')
                .collect(Collectors.joining(", "));
    }

    /**
     * Creates a runner to run Gradle.
     *
     * @param extraBuildFileContent extra test-specific content for the build file.
     * @return the Gradle runner.
     * @throws Exception if an error occurs.
     */
    private GradleRunner createRunner(String... extraBuildFileContent) throws Exception
    {
        write("settings.gradle.kts", "rootProject.name = \"plugin-test-project\"");

        // Normally we'd be including the plugin using the `plugins` block but there is some issue
        // where it can't be loaded from the classpath using that.
        String[] buildFileHeader = {
                "import org.trypticon.gradle.plugins.travelagent.*",
                "",
                "buildscript {",
                "    dependencies {",
                "        classpath(files(" + getGradleClassPath() + "))",
                "    }",
                "}",
                "",
                "repositories {",
                "    jcenter()",
                "}",
                "",
                "apply<JavaPlugin>()",
                "apply<TravelAgentPlugin>()",
                "",
                "dependencies {",
                "    \"testImplementation\"(\"junit:junit:4.12\")",
                "    \"testImplementation\"(\"org.hamcrest:java-hamcrest:2.0.0.0\")",
                "    \"testImplementation\"(\"org.hamcrest:hamcrest-junit:2.0.0.0\")",
                "}",
        };
        write("build.gradle.kts", ObjectArrays.concat(buildFileHeader, extraBuildFileContent, String.class));

        return GradleRunner.create().withProjectDir(projectDir);
    }

    private void writeSampleCode() throws Exception {
        write("src/main/java/acme/Something.java",
                "package acme;",
                "",
                "public class Something {",
                "    public String manipulate(String value) {",
                "        return value.toLowerCase();", // Common bug
                "    }",
                "}");

        write("src/test/java/acme/TestSomething.java",
                "package acme;",
                "",
                "import org.junit.Test;",
                "",
                "import static org.hamcrest.Matchers.*;",
                "import static org.junit.Assert.assertThat;",
                "",
                "public class TestSomething {",
                "    @Test",
                "    public void testManipulate() {",
                "        assertThat(new Something().manipulate(\"FISH\"), is(equalTo(\"fish\")));",
                "    }",
                "}");
    }

    @Test
    public void testAvailableTrips() throws Exception {
        writeSampleCode();

        GradleRunner runner = createRunner(
                "configure<TravelAgentExtension> {",
                "    availableTrips.empty()",
                "    availableTrips.add(Trip(\"en\", \"AU\", \"Australia/Sydney\"))",
                "    availableTrips.add(providers.provider { Trip(\"en\", \"AU\", \"Australia/Melbourne\") })",
                "}");

        BuildResult result = runner.withArguments("test", "--stacktrace").build();

        assertThat(result.task(":test").getOutcome(), is(TaskOutcome.SUCCESS));
    }

    @Test
    public void testKnownFailing() throws Exception {
        writeSampleCode();

        GradleRunner runner = createRunner(
                "configure<TravelAgentExtension> {",
                "    knownFailing { trip -> trip.language == \"tr\" }",
                "}");

        BuildResult result = runner.withArguments("test", "--stacktrace").build();

        assertThat(result.task(":test").getOutcome(), is(TaskOutcome.SUCCESS));
    }

    @Test
    public void testForceSettings() throws Exception {
        writeSampleCode();

        GradleRunner runner = createRunner();

        BuildResult result = runner.withArguments("test", "-Ptravelagent.language=tr", "--stacktrace").buildAndFail();

        assertThat(result.getOutput(), containsString("Taking a trip to:\n" +
                "    Language:   tr (Turkish)\n" +
                "    Country:    TR (Turkey)\n" +
                "    Time Zone:  Asia/Istanbul (Eastern European Time)\n" +
                "    To reproduce manually:\n" +
                "        -Ptravelagent.language=tr -Ptravelagent.country=TR -Ptravelagent.timezone=Asia/Istanbul"));

        // And of course...
        assertThat(result.task(":test").getOutcome(), is(TaskOutcome.FAILED));
    }

    @Test
    public void testUpToDate_SameProperties() throws Exception {
        writeSampleCode();

        GradleRunner runner = createRunner();

        runner.withArguments("test", "-Ptravelagent.language=en", "--stacktrace").build();
        BuildResult result = runner.withArguments("test", "-Ptravelagent.language=en", "--stacktrace").build();

        assertThat(result.task(":test").getOutcome(), is(TaskOutcome.UP_TO_DATE));
    }

    @Test
    public void testNotUpToDate_DifferentProperties() throws Exception {
        writeSampleCode();

        GradleRunner runner = createRunner();

        runner.withArguments("test", "-Ptravelagent.language=en", "--stacktrace").build();
        BuildResult result = runner.withArguments("test", "-Ptravelagent.language=es", "--stacktrace").build();

        assertThat(result.task(":test").getOutcome(), is(TaskOutcome.SUCCESS));
    }

    @Test
    public void testNotUpToDate_FailedLastTime() throws Exception {
        writeSampleCode();

        GradleRunner runner = createRunner();

        runner.withArguments("test", "-Ptravelagent.language=tr", "--stacktrace").buildAndFail();
        BuildResult result = runner.withArguments("test", "-Ptravelagent.language=tr", "--stacktrace").buildAndFail();

        assertThat(result.task(":test").getOutcome(), is(TaskOutcome.FAILED));
    }
}
