package com.github.blackcat.tengo.psi.impl;

import com.github.blackcat.tengo.psi.TengoTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TengoForInVarMixin extends TengoPsiElementBase implements PsiNameIdentifierOwner {

    public TengoForInVarMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode id = getNode().findChildByType(TengoTypes.IDENTIFIER);
        return id == null ? null : id.getPsi();
    }

    @Override
    public @Nullable String getName() {
        PsiElement id = getNameIdentifier();
        return id == null ? null : id.getText();
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        return TengoIdentifierMixinUtil.renameIdentifierChild(this, newName);
    }

    @Override
    public int getTextOffset() {
        PsiElement id = getNameIdentifier();
        return id == null ? super.getTextOffset() : id.getTextOffset();
    }
}
