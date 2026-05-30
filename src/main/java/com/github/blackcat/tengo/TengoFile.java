package com.github.blackcat.tengo;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class TengoFile extends PsiFileBase {
    public TengoFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, TengoLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TengoFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Tengo File";
    }
}
