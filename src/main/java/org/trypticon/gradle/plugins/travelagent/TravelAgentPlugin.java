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
        TravelAgentExtension globalExtension = project.getExtensions().create("travelAgent", TravelAgentExtension.class);

        globalExtension.prefer(project.findProperty("travelagent.language"),
                project.findProperty("travelagent.country"),
                project.findProperty("travelagent.timezone"));

        project.getTasks().withType(Test.class).configureEach(task -> {
            // Can't get dependency injection for this one for some reason :(
            TravelAgentTaskExtension taskExtension = task.getExtensions().create("travelAgent", TravelAgentTaskExtension.class,
                    globalExtension, project.getObjects());
            task.getJvmArgumentProviders().add(new TravelAgentArgumentProvider(taskExtension));
        });
    }
}
