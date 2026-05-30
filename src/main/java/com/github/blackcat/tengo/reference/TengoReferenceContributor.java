package com.github.blackcat.tengo.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoIdentifierRef;
import com.github.blackcat.tengo.psi.TengoImportPath;
import com.github.blackcat.tengo.psi.TengoSelectorSuffix;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;

public class TengoReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(TengoIdentifierExpr.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        TengoIdentifierExpr id = (TengoIdentifierExpr) element;
                        // Don't create a reference for the LHS of a `:=` (the declaration itself).
                        if (isDeclarationSite(id)) return PsiReference.EMPTY_ARRAY;
                        return new PsiReference[]{new TengoLocalReference(id)};
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(TengoIdentifierRef.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        TengoSelectorSuffix sel = PsiTreeUtil.getParentOfType(element, TengoSelectorSuffix.class);
                        if (sel == null) return PsiReference.EMPTY_ARRAY;
                        return new PsiReference[]{new TengoSelectorMemberReference(element)};
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(TengoImportPath.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        TengoImportPath path = (TengoImportPath) element;
                        PsiElement str = path.getStringLiteral();
                        if (str == null) str = path.getRawStringLiteral();
                        if (str == null) return PsiReference.EMPTY_ARRAY;
                        return new PsiReference[]{new TengoImportPathReference(path)};
                    }
                });
    }

    private static boolean isDeclarationSite(@NotNull TengoIdentifierExpr id) {
        // The LHS of an assign_stmt whose operator is ':=' counts as the declaration.
        var lhsList = PsiTreeUtil.getParentOfType(id, com.github.blackcat.tengo.psi.TengoLhsList.class);
        if (lhsList == null) return false;
        var assign = (com.github.blackcat.tengo.psi.TengoAssignStmt) lhsList.getParent();
        if (assign == null) return false;
        return com.github.blackcat.tengo.TengoPsiUtil.isDefinition(assign);
    }
}
