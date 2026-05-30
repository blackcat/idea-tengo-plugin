package com.github.blackcat.tengo.psi.impl;

import com.github.blackcat.tengo.TengoPsiUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin for {@code map_key} elements.
 *  - Exposes {@link #getName()} so {@code ReferencesSearch} knows which word to look up in
 *    the index — without this, the search runs against a null name and returns nothing.
 *  - For map keys inside the synthetic "Tengo Builtins.tengo" file, advertises project-wide
 *    useScope so Find Usages locates user-code call sites of stdlib members.
 */
public abstract class TengoMapKeyMixin extends TengoPsiElementBase implements PsiNamedElement {

    private static final String SYNTHETIC_FILE_NAME = "Tengo Builtins.tengo";

    public TengoMapKeyMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        return TengoPsiUtil.unquote(getText());
    }

    @Override
    public PsiElement setName(@NotNull String newName) throws IncorrectOperationException {
        return this;
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        PsiFile file = getContainingFile();
        if (file != null && SYNTHETIC_FILE_NAME.equals(file.getName())) {
            return GlobalSearchScope.projectScope(getProject());
        }
        return super.getUseScope();
    }
}
