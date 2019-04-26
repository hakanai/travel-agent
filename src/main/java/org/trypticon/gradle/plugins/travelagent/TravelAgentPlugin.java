package org.trypticon.gradle.plugins.travelagent;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;

import javax.annotation.Nonnull;

/**
 * Main entry point for plugin.
 */
public class TravelAgentPlugin implements Plugin<Project> {
    @Override
    public void apply(@Nonnull Project project) {
        TravelAgentExtension extension = project.getExtensions().create("travelAgent", TravelAgentExtension.class);

        extension.prefer(project.findProperty("travelagent.language"),
                project.findProperty("travelagent.country"),
                project.findProperty("travelagent.timezone"));

        project.getTasks().withType(Test.class).configureEach(task -> {
            task.getJvmArgumentProviders().add(new TravelAgentArgumentProvider(extension));
        });
    }

}
