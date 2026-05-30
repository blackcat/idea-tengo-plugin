package com.github.blackcat.tengo.inspections;

import com.github.blackcat.tengo.psi.TengoBlock;
import com.github.blackcat.tengo.psi.TengoForInClause;
import com.github.blackcat.tengo.psi.TengoForInVar;
import com.github.blackcat.tengo.psi.TengoForStmt;
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

/**
 * Flags function parameters and for-in loop variables that are never read inside
 * their owning function body or loop body. Names starting with `_` opt out (Tengo
 * convention for blank identifiers).
 */
public class TengoUnusedParameterInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof TengoFuncLiteral) {
                    checkFuncParams((TengoFuncLiteral) element, holder);
                } else if (element instanceof TengoForStmt) {
                    checkForInVars((TengoForStmt) element, holder);
                }
            }
        };
    }

    private static void checkFuncParams(@NotNull TengoFuncLiteral fn, @NotNull ProblemsHolder holder) {
        TengoParamList params = fn.getParamList();
        TengoBlock body = fn.getBlock();
        if (params == null || body == null) return;
        for (TengoParam param : params.getParamList()) {
            if (param.getIdentifier() == null) continue;
            String name = param.getIdentifier().getText();
            if (name.isEmpty() || name.startsWith("_")) continue;
            if (isReadInside(name, body, param.getIdentifier())) continue;
            holder.registerProblem(
                    param,
                    "Parameter '" + name + "' is never used",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL);
        }
    }

    private static void checkForInVars(@NotNull TengoForStmt forStmt, @NotNull ProblemsHolder holder) {
        TengoForInClause inClause = PsiTreeUtil.findChildOfType(forStmt, TengoForInClause.class);
        if (inClause == null) return;
        for (TengoForInVar var : inClause.getForInVarList()) {
            String name = var.getText();
            if (name.isEmpty() || name.startsWith("_")) continue;
            if (isReadInside(name, forStmt, var)) continue;
            holder.registerProblem(
                    var,
                    "Loop variable '" + name + "' is never used",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL);
        }
    }

    /** True if any identifier_expr with this name appears inside `scope`, excluding the
     *  declaration itself. */
    private static boolean isReadInside(@NotNull String name, @NotNull PsiElement scope, @NotNull PsiElement declarationLeaf) {
        for (TengoIdentifierExpr id : PsiTreeUtil.findChildrenOfType(scope, TengoIdentifierExpr.class)) {
            if (id.getIdentifier() == null) continue;
            if (id.getIdentifier() == declarationLeaf) continue;
            if (name.equals(id.getIdentifier().getText())) return true;
        }
        return false;
    }
}
