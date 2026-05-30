package com.github.blackcat.tengo.psi.impl;

import com.github.blackcat.tengo.TengoFileType;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers shared by Tengo PSI mixin classes for renaming identifiers.
 * Parses `<newName> := 0` into a throwaway file to extract a fresh
 * identifier leaf, then replaces the existing leaf with it.
 */
final class TengoIdentifierMixinUtil {

    private TengoIdentifierMixinUtil() {
    }

    @Nullable
    static PsiElement renameIdentifierChild(@NotNull PsiElement owner, @NotNull String newName) {
        ASTNode node = owner.getNode().findChildByType(TengoTypes.IDENTIFIER);
        if (node == null) return owner;
        PsiElement freshLeaf = createIdentifierLeaf(owner.getProject(), newName);
        if (freshLeaf == null) return owner;
        node.getPsi().replace(freshLeaf);
        return owner;
    }

    @Nullable
    private static PsiElement createIdentifierLeaf(@NotNull Project project, @NotNull String name) {
        PsiFile dummy = PsiFileFactory.getInstance(project)
                .createFileFromText("__rename__.tengo", TengoFileType.INSTANCE, name + " := 0\n");
        TengoIdentifierExpr id = PsiTreeUtil.findChildOfType(dummy, TengoIdentifierExpr.class, true);
        if (id == null) return null;
        return id.getFirstChild();
    }
}
