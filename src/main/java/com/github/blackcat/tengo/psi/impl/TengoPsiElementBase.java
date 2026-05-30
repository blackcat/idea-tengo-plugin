package com.github.blackcat.tengo.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for every generated Tengo PSI element. The only thing it adds on top of
 * {@link ASTWrapperPsiElement} is wiring {@link #getReferences()} into the
 * {@link ReferenceProvidersRegistry}, so {@code psi.referenceContributor} registrations
 * are actually consulted (the platform's default ASTWrapperPsiElement does not).
 */
public abstract class TengoPsiElementBase extends ASTWrapperPsiElement {
    public TengoPsiElementBase(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public @Nullable PsiReference getReference() {
        PsiReference[] refs = getReferences();
        return refs.length == 0 ? null : refs[0];
    }
}
