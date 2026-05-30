package com.github.blackcat.tengo.inspections;

import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoExpr;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nls;

import java.util.List;

public class TengoUnusedImportInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (!(element instanceof TengoAssignStmt)) return;
                TengoAssignStmt assign = (TengoAssignStmt) element;
                if (!TengoPsiUtil.isDefinition(assign)) return;

                // Only consider top-level imports of the form `name := import("...")`.
                if (!(assign.getParent() != null
                        && assign.getParent().getParent() instanceof TengoFile)) return;

                List<TengoIdentifierExpr> lhs = TengoPsiUtil.lhsIdentifiers(assign);
                if (lhs.size() != 1) return;
                TengoIdentifierExpr name = lhs.get(0);
                if (name.getIdentifier() == null) return;

                TengoExpr rhs = TengoPsiUtil.firstRhs(assign);
                TengoImportExpr imp = TengoPsiUtil.asImportExpr(rhs);
                if (imp == null) return;

                String nameText = name.getIdentifier().getText();
                if (!isUsedElsewhere(nameText, name)) {
                    holder.registerProblem(
                            name,
                            "Import '" + nameText + "' is never used",
                            ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                            new RemoveImportFix());
                }
            }
        };
    }

    private static boolean isUsedElsewhere(@NotNull String name, @NotNull TengoIdentifierExpr declaration) {
        PsiFile file = declaration.getContainingFile();
        if (file == null) return false;
        for (TengoIdentifierExpr id : PsiTreeUtil.findChildrenOfType(file, TengoIdentifierExpr.class)) {
            if (id == declaration) continue;
            if (id.getIdentifier() == null) continue;
            if (name.equals(id.getIdentifier().getText())) return true;
        }
        return false;
    }

    private static final class RemoveImportFix implements LocalQuickFix {
        @Override
        public @Nls @NotNull String getFamilyName() {
            return "Remove unused import";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement nameElement = descriptor.getPsiElement();
            TengoAssignStmt assign = PsiTreeUtil.getParentOfType(nameElement, TengoAssignStmt.class);
            if (assign == null) return;
            // Delete the assign_stmt plus the surrounding whitespace / terminating semicolon.
            PsiElement target = assign;
            PsiElement next = target.getNextSibling();
            target.delete();
            if (next instanceof PsiWhiteSpace || (next != null && ";".equals(next.getText()))) {
                next.delete();
            }
        }
    }
}
