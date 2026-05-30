package com.github.blackcat.tengo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoExpr;
import com.github.blackcat.tengo.psi.TengoFuncLiteral;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoImportExpr;
import com.github.blackcat.tengo.psi.TengoImportPath;
import com.github.blackcat.tengo.psi.TengoLhsList;
import com.github.blackcat.tengo.psi.TengoPrimaryExpr;
import com.github.blackcat.tengo.psi.TengoRhsList;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Common PSI traversal helpers — written against the generated PSI to avoid
 * having to maintain custom mixin classes for every node.
 */
public final class TengoPsiUtil {

    private TengoPsiUtil() {
    }

    /** Returns true if the assign_stmt uses ':=' (declaration form). */
    public static boolean isDefinition(@NotNull TengoAssignStmt assign) {
        for (PsiElement child : assign.getChildren()) {
            // children only contains composite PSI; the ':=' token is a leaf
        }
        for (PsiElement c = assign.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNode().getElementType() == TengoTypes.DEFINE) return true;
        }
        return false;
    }

    /** All identifier-expr LHS targets of an assign_stmt. */
    public static @NotNull List<TengoIdentifierExpr> lhsIdentifiers(@NotNull TengoAssignStmt assign) {
        TengoLhsList lhs = assign.getLhsList();
        if (lhs == null) return List.of();
        List<TengoIdentifierExpr> out = new ArrayList<>();
        for (TengoExpr e : lhs.getExprList()) {
            TengoIdentifierExpr id = asIdentifierExpr(e);
            if (id != null) out.add(id);
        }
        return out;
    }

    /** First RHS expression of an assign_stmt, if any. */
    public static @Nullable TengoExpr firstRhs(@NotNull TengoAssignStmt assign) {
        TengoRhsList rhs = assign.getRhsList();
        if (rhs == null || rhs.getExprList().isEmpty()) return null;
        return rhs.getExprList().get(0);
    }

    /** Unwraps an expression to its primary if it's just a primary expression. */
    public static @Nullable TengoIdentifierExpr asIdentifierExpr(@Nullable TengoExpr expr) {
        if (expr == null) return null;
        if (expr instanceof TengoIdentifierExpr) return (TengoIdentifierExpr) expr;
        return PsiTreeUtil.findChildOfType(expr, TengoIdentifierExpr.class, true);
    }

    /** Unwraps an expression that is exactly a func_literal. */
    public static @Nullable TengoFuncLiteral asFuncLiteral(@Nullable TengoExpr expr) {
        if (expr == null) return null;
        if (expr instanceof TengoFuncLiteral) return (TengoFuncLiteral) expr;
        if (expr instanceof TengoPrimaryExpr) {
            return PsiTreeUtil.findChildOfType(expr, TengoFuncLiteral.class);
        }
        return null;
    }

    /** Unwraps an expression that is exactly an import_expr. */
    public static @Nullable TengoImportExpr asImportExpr(@Nullable TengoExpr expr) {
        if (expr == null) return null;
        if (expr instanceof TengoImportExpr) return (TengoImportExpr) expr;
        return PsiTreeUtil.findChildOfType(expr, TengoImportExpr.class);
    }

    /** Strips surrounding quotes from a string-literal or raw-string-literal element. */
    public static @NotNull String unquote(@NotNull String text) {
        if (text.length() >= 2) {
            char first = text.charAt(0);
            char last = text.charAt(text.length() - 1);
            if ((first == '"' || first == '`' || first == '\'') && first == last) {
                return text.substring(1, text.length() - 1);
            }
        }
        return text;
    }

    /** Returns the path string from an import("...") call, without quotes. */
    public static @Nullable String pathOf(@NotNull TengoImportPath path) {
        PsiElement s = path.getStringLiteral();
        if (s == null) s = path.getRawStringLiteral();
        if (s == null) return null;
        return unquote(s.getText());
    }

    /** True if this PSI leaf is a token of the given type. */
    public static boolean isToken(@Nullable PsiElement element, @NotNull IElementType type) {
        return element != null && element.getNode() != null && element.getNode().getElementType() == type;
    }
}
