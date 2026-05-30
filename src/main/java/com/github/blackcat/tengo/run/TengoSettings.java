package com.github.blackcat.tengo.run;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.APP)
@State(name = "TengoSettings", storages = @Storage("tengo.xml"))
public final class TengoSettings implements PersistentStateComponent<TengoSettings.State> {

    public static final class State {
        public String tengoBinaryPath = "";
    }

    private State state = new State();

    public static @NotNull TengoSettings getInstance() {
        return ApplicationManager.getApplication().getService(TengoSettings.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    public @NotNull String getTengoBinaryPath() {
        return state.tengoBinaryPath == null ? "" : state.tengoBinaryPath;
    }

    public void setTengoBinaryPath(@NotNull String path) {
        state.tengoBinaryPath = path;
    }
}
