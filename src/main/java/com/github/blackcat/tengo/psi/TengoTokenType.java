package com.github.blackcat.tengo.psi;

import com.intellij.psi.tree.IElementType;
import com.github.blackcat.tengo.TengoLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TengoTokenType extends IElementType {
    public TengoTokenType(@NotNull @NonNls String debugName) {
        super(debugName, TengoLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "TengoTokenType." + super.toString();
    }
}
