package com.github.blackcat.tengo.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TengoConfigurationFactory extends ConfigurationFactory {

    public TengoConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull String getId() {
        return "TengoRunConfigurationFactory";
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new TengoRunConfiguration(project, this, "Tengo");
    }

    @Override
    public @NotNull String getName() {
        return "Tengo";
    }
}
