package com.github.blackcat.tengo.inspections;

import com.github.blackcat.tengo.TengoBuiltins;
import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoLhsList;
import com.github.blackcat.tengo.psi.TengoSelectorSuffix;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class TengoUnresolvedReferenceInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (!(element instanceof TengoIdentifierExpr)) return;
                TengoIdentifierExpr id = (TengoIdentifierExpr) element;

                String name = id.getName();
                if (name == null || name.isEmpty()) return;

                // Skip declaration sites (LHS of `:=`).
                if (isDeclarationSite(id)) return;
                // Skip identifiers that are the receiver position of a selector chain;
                // builtins/stdlib are recognized below.
                if (TengoBuiltins.isBuiltin(name) || TengoBuiltins.isStdlibModule(name)) return;
                // Skip the underscore convention for unused.
                if ("_".equals(name)) return;

                PsiReference ref = id.getReference();
                if (ref == null) return;
                if (ref.resolve() != null) return;
                // Polyvariant: any candidates?
                if (ref instanceof com.intellij.psi.PsiPolyVariantReference pvr) {
                    if (pvr.multiResolve(false).length > 0) return;
                }

                holder.registerProblem(
                        id,
                        "Cannot resolve symbol '" + name + "'",
                        ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
            }
        };
    }

    private static boolean isDeclarationSite(@NotNull TengoIdentifierExpr id) {
        TengoLhsList lhs = PsiTreeUtil.getParentOfType(id, TengoLhsList.class);
        if (lhs == null) return false;
        TengoAssignStmt assign = (TengoAssignStmt) lhs.getParent();
        return assign != null && TengoPsiUtil.isDefinition(assign);
    }
}
