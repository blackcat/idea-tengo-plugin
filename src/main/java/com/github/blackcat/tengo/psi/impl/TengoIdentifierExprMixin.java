package com.github.blackcat.tengo.psi.impl;

import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoLhsList;
import com.github.blackcat.tengo.psi.TengoTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Mixin for {@code identifier_expr}. Identifier expressions appear in BOTH declaration
 * positions (LHS of {@code :=}) and usage positions. We only present ourselves as a
 * {@link PsiNameIdentifierOwner} at declaration sites — otherwise the IDE treats every
 * occurrence as its own declaration, which breaks navigation, rename, and find-usages.
 */
public abstract class TengoIdentifierExprMixin extends TengoPsiElementBase implements PsiNameIdentifierOwner {

    public TengoIdentifierExprMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        if (!isDeclaration()) return null;
        return identifierLeaf();
    }

    @Override
    public @Nullable String getName() {
        PsiElement leaf = identifierLeaf();
        return leaf == null ? null : leaf.getText();
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        return TengoIdentifierMixinUtil.renameIdentifierChild(this, newName);
    }

    @Override
    public int getTextOffset() {
        PsiElement id = identifierLeaf();
        return id == null ? super.getTextOffset() : id.getTextOffset();
    }

    @Override
    public @Nullable Icon getIcon(int flags) {
        return null;
    }

    private @Nullable PsiElement identifierLeaf() {
        ASTNode id = getNode().findChildByType(TengoTypes.IDENTIFIER);
        return id == null ? null : id.getPsi();
    }

    private boolean isDeclaration() {
        TengoLhsList lhsList = PsiTreeUtil.getParentOfType(this, TengoLhsList.class);
        if (lhsList == null) return false;
        PsiElement assignParent = lhsList.getParent();
        if (!(assignParent instanceof TengoAssignStmt)) return false;
        return TengoPsiUtil.isDefinition((TengoAssignStmt) assignParent);
    }
}
