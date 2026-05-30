package com.github.blackcat.tengo.run;

import com.github.blackcat.tengo.TengoIcons;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import javax.swing.Icon;

public class TengoConfigurationType implements ConfigurationType {

    public static final String ID = "TengoRunConfiguration";

    private final ConfigurationFactory factory = new TengoConfigurationFactory(this);

    @Override
    public @NotNull String getDisplayName() {
        return "Tengo";
    }

    @Override
    public @Nullable String getConfigurationTypeDescription() {
        return "Run a Tengo script using the configured tengo binary";
    }

    @Override
    public Icon getIcon() {
        return TengoIcons.FILE;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{factory};
    }
}
