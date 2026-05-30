package com.github.blackcat.tengo;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class TengoFileType extends LanguageFileType {
    public static final TengoFileType INSTANCE = new TengoFileType();

    private TengoFileType() {
        super(TengoLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Tengo";
    }

    @Override
    public @NotNull String getDescription() {
        return "Tengo script";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "tengo";
    }

    @Override
    public Icon getIcon() {
        return TengoIcons.FILE;
    }
}
