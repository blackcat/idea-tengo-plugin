package com.github.blackcat.tengo.psi;

import com.intellij.psi.tree.IElementType;
import com.github.blackcat.tengo.TengoLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TengoElementType extends IElementType {
    public TengoElementType(@NotNull @NonNls String debugName) {
        super(debugName, TengoLanguage.INSTANCE);
    }
}
