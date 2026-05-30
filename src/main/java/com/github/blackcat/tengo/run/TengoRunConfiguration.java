package com.github.blackcat.tengo.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TengoRunConfiguration extends LocatableConfigurationBase<TengoRunConfiguration> {

    private static final String SCRIPT_PATH = "TENGO_SCRIPT_PATH";
    private static final String ARGUMENTS = "TENGO_ARGUMENTS";
    private static final String WORKING_DIRECTORY = "TENGO_WORKING_DIR";

    private String scriptPath = "";
    private String arguments = "";
    private String workingDirectory = "";

    protected TengoRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @NotNull String name) {
        super(project, factory, name);
    }

    public String getScriptPath() {
        return scriptPath == null ? "" : scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath == null ? "" : scriptPath;
    }

    public String getArguments() {
        return arguments == null ? "" : arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments == null ? "" : arguments;
    }

    public String getWorkingDirectory() {
        return workingDirectory == null ? "" : workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory == null ? "" : workingDirectory;
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new TengoRunConfigurationEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return new TengoCommandLineState(environment, this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (getScriptPath().isBlank()) {
            throw new RuntimeConfigurationException("Tengo script path is empty");
        }
        if (TengoSettings.getInstance().getTengoBinaryPath().isBlank()) {
            throw new RuntimeConfigurationException(
                    "Tengo binary is not configured. Set it in Settings → Tools → Tengo.");
        }
    }

    @Override
    public void readExternal(@NotNull Element element) throws com.intellij.openapi.util.InvalidDataException {
        super.readExternal(element);
        scriptPath = JDOMExternalizerUtil.readField(element, SCRIPT_PATH, "");
        arguments = JDOMExternalizerUtil.readField(element, ARGUMENTS, "");
        workingDirectory = JDOMExternalizerUtil.readField(element, WORKING_DIRECTORY, "");
    }

    @Override
    public void writeExternal(@NotNull Element element) throws com.intellij.openapi.util.WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, SCRIPT_PATH, getScriptPath());
        JDOMExternalizerUtil.writeField(element, ARGUMENTS, getArguments());
        JDOMExternalizerUtil.writeField(element, WORKING_DIRECTORY, getWorkingDirectory());
    }
}
