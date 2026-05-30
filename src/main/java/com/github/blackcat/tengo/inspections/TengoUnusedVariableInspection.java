package com.github.blackcat.tengo.inspections;

import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoImportExpr;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Flags `:=` declarations whose LHS name is never referenced anywhere else in the file.
 * Skips:
 *   - the blank-identifier convention `_`
 *   - imports (handled by {@link TengoUnusedImportInspection})
 */
public class TengoUnusedVariableInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (!(element instanceof TengoAssignStmt)) return;
                TengoAssignStmt assign = (TengoAssignStmt) element;
                if (!TengoPsiUtil.isDefinition(assign)) return;

                // Imports are handled by TengoUnusedImportInspection.
                if (TengoPsiUtil.asImportExpr(TengoPsiUtil.firstRhs(assign)) != null) return;

                for (TengoIdentifierExpr lhs : TengoPsiUtil.lhsIdentifiers(assign)) {
                    if (lhs.getIdentifier() == null) continue;
                    String name = lhs.getIdentifier().getText();
                    if (name == null || name.isEmpty() || "_".equals(name)) continue;
                    if (isReferencedElsewhere(name, lhs)) continue;

                    holder.registerProblem(
                            lhs,
                            "Variable '" + name + "' is never used",
                            ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                            new RemoveDeclarationFix());
                }
            }
        };
    }

    private static boolean isReferencedElsewhere(@NotNull String name, @NotNull TengoIdentifierExpr declaration) {
        PsiFile file = declaration.getContainingFile();
        if (file == null) return false;
        for (TengoIdentifierExpr id : PsiTreeUtil.findChildrenOfType(file, TengoIdentifierExpr.class)) {
            if (id == declaration) continue;
            if (id.getIdentifier() == null) continue;
            if (!name.equals(id.getIdentifier().getText())) continue;
            // Same name on the LHS of a different `:=` is a re-declaration in a deeper
            // scope — not a usage of this declaration. Anything else counts.
            if (isLhsOfDefinition(id)) continue;
            return true;
        }
        return false;
    }

    private static boolean isLhsOfDefinition(@NotNull TengoIdentifierExpr id) {
        var lhsList = PsiTreeUtil.getParentOfType(id, com.github.blackcat.tengo.psi.TengoLhsList.class);
        if (lhsList == null) return false;
        PsiElement parent = lhsList.getParent();
        if (!(parent instanceof TengoAssignStmt)) return false;
        return TengoPsiUtil.isDefinition((TengoAssignStmt) parent);
    }

    private static final class RemoveDeclarationFix implements LocalQuickFix {
        @Override
        public @Nls @NotNull String getFamilyName() {
            return "Remove unused declaration";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            TengoAssignStmt assign = PsiTreeUtil.getParentOfType(element, TengoAssignStmt.class);
            if (assign == null) return;
            // Only safe to drop the whole statement when there's a single LHS.
            List<TengoIdentifierExpr> lhs = TengoPsiUtil.lhsIdentifiers(assign);
            if (lhs.size() != 1) return;
            PsiElement next = assign.getNextSibling();
            assign.delete();
            if (next instanceof PsiWhiteSpace) next.delete();
        }
    }
}
