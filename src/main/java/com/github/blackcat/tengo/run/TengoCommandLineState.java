package com.github.blackcat.tengo.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TengoCommandLineState extends CommandLineState {

    private final TengoRunConfiguration configuration;

    public TengoCommandLineState(@NotNull ExecutionEnvironment env, @NotNull TengoRunConfiguration cfg) {
        super(env);
        this.configuration = cfg;
    }

    @Override
    protected @NotNull ProcessHandler startProcess() throws ExecutionException {
        String binaryPath = TengoSettings.getInstance().getTengoBinaryPath().trim();
        if (binaryPath.isEmpty()) {
            throw new ExecutionException("Tengo binary path is not set (Settings → Tools → Tengo).");
        }

        GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setCharset(java.nio.charset.StandardCharsets.UTF_8);
        cmd.setExePath(binaryPath);

        ParametersList args = cmd.getParametersList();
        String extra = configuration.getArguments();
        if (!extra.isBlank()) {
            args.addParametersString(extra);
        }
        args.add(configuration.getScriptPath());

        String workDir = configuration.getWorkingDirectory();
        if (workDir.isBlank()) {
            Path scriptPath = Paths.get(configuration.getScriptPath());
            Path parent = scriptPath.toAbsolutePath().getParent();
            if (parent != null) cmd.setWorkDirectory(parent.toFile());
        } else {
            cmd.setWorkDirectory(workDir);
        }

        KillableProcessHandler handler = new KillableProcessHandler(cmd);
        ProcessTerminatedListener.attach(handler);
        return handler;
    }
}
