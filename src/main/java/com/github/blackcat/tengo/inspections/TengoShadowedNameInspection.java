package com.github.blackcat.tengo.inspections;

import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoBlock;
import com.github.blackcat.tengo.psi.TengoForInClause;
import com.github.blackcat.tengo.psi.TengoForInVar;
import com.github.blackcat.tengo.psi.TengoFuncLiteral;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoParam;
import com.github.blackcat.tengo.psi.TengoParamList;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Warns when a `:=` declaration introduces a name that's already declared in an
 * enclosing scope (function param, for-in var, or outer `:=`). Shadowing is
 * sometimes intentional, so this is a WEAK_WARNING by default.
 */
public class TengoShadowedNameInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (!(element instanceof TengoAssignStmt)) return;
                TengoAssignStmt assign = (TengoAssignStmt) element;
                if (!TengoPsiUtil.isDefinition(assign)) return;
                for (TengoIdentifierExpr lhs : TengoPsiUtil.lhsIdentifiers(assign)) {
                    if (lhs.getIdentifier() == null) continue;
                    String name = lhs.getIdentifier().getText();
                    if (name == null || name.isEmpty() || "_".equals(name)) continue;
                    PsiElement shadowed = findShadowedDecl(name, lhs);
                    if (shadowed == null) continue;
                    holder.registerProblem(
                            lhs,
                            "'" + name + "' shadows a declaration from an enclosing scope",
                            ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }

    /** Walks parent scopes from this declaration; returns the outer declaration with the
     *  same name, or null. */
    private static @Nullable PsiElement findShadowedDecl(@NotNull String name, @NotNull TengoIdentifierExpr decl) {
        // Start ABOVE the enclosing block/function — we want OUTER scopes.
        PsiElement currentScope = enclosingScopeOf(decl);
        if (currentScope == null) return null;
        PsiElement scope = currentScope.getParent();
        while (scope != null) {
            if (scope instanceof TengoFuncLiteral) {
                TengoParamList params = ((TengoFuncLiteral) scope).getParamList();
                if (params != null) {
                    for (TengoParam p : params.getParamList()) {
                        if (p.getIdentifier() != null && name.equals(p.getIdentifier().getText())) return p;
                    }
                }
            }
            if (scope.getParent() instanceof com.github.blackcat.tengo.psi.TengoForStmt) {
                TengoForInClause inClause = PsiTreeUtil.findChildOfType(scope.getParent(), TengoForInClause.class);
                if (inClause != null) {
                    for (TengoForInVar v : inClause.getForInVarList()) {
                        if (name.equals(v.getText())) return v;
                    }
                }
            }
            if (scope instanceof TengoBlock || scope instanceof TengoFile) {
                PsiElement hit = findDeclInScope(scope, name, decl);
                if (hit != null) return hit;
            }
            scope = scope.getParent();
        }
        return null;
    }

    private static @Nullable PsiElement findDeclInScope(@NotNull PsiElement scope, @NotNull String name, @NotNull TengoIdentifierExpr ignore) {
        for (TengoAssignStmt assign : PsiTreeUtil.findChildrenOfType(scope, TengoAssignStmt.class)) {
            if (!TengoPsiUtil.isDefinition(assign)) continue;
            if (enclosingScopeOf(assign) != scope) continue;
            for (TengoIdentifierExpr lhs : TengoPsiUtil.lhsIdentifiers(assign)) {
                if (lhs == ignore) continue;
                if (lhs.getIdentifier() == null) continue;
                if (name.equals(lhs.getIdentifier().getText())) return lhs;
            }
        }
        return null;
    }

    private static @Nullable PsiElement enclosingScopeOf(@NotNull PsiElement node) {
        PsiElement p = node.getParent();
        while (p != null) {
            if (p instanceof TengoBlock || p instanceof TengoFile) return p;
            p = p.getParent();
        }
        return null;
    }
}
