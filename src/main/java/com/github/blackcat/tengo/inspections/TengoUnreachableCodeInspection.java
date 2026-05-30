package com.github.blackcat.tengo.inspections;

import com.github.blackcat.tengo.psi.TengoBlock;
import com.github.blackcat.tengo.psi.TengoBreakStmt;
import com.github.blackcat.tengo.psi.TengoContinueStmt;
import com.github.blackcat.tengo.psi.TengoReturnStmt;
import com.github.blackcat.tengo.psi.TengoStatement;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Flags statements that follow a {@code return}, {@code break}, or {@code continue}
 * within the same block — they will never execute.
 */
public class TengoUnreachableCodeInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof TengoBlock) {
                    check((TengoBlock) element, holder);
                } else if (element instanceof PsiFile) {
                    // Top-level statements form an implicit block.
                    check(element, holder);
                }
            }
        };
    }

    private static void check(@NotNull PsiElement block, @NotNull ProblemsHolder holder) {
        boolean terminated = false;
        for (PsiElement child = block.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof PsiWhiteSpace || child instanceof PsiComment) continue;
            // Only inspect direct child statements; nested blocks are handled by the
            // visitor when it recurses into them.
            TengoStatement stmt = (child instanceof TengoStatement) ? (TengoStatement) child : null;
            if (stmt == null) continue;
            if (terminated) {
                holder.registerProblem(
                        stmt,
                        "Unreachable code",
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        new RemoveUnreachableFix());
                return;
            }
            if (isFlowTerminator(stmt)) terminated = true;
        }
    }

    private static boolean isFlowTerminator(@NotNull TengoStatement stmt) {
        PsiElement inner = stmt.getFirstChild();
        return inner instanceof TengoReturnStmt
                || inner instanceof TengoBreakStmt
                || inner instanceof TengoContinueStmt;
    }

    private static final class RemoveUnreachableFix implements LocalQuickFix {
        @Override
        public @Nls @NotNull String getFamilyName() {
            return "Remove unreachable code";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement first = descriptor.getPsiElement();
            PsiElement parent = first.getParent();
            if (parent == null) return;
            // Delete this statement and every following statement-or-trivia in the same
            // block, up to but not including any closing brace.
            List<PsiElement> toDelete = new ArrayList<>();
            for (PsiElement n = first; n != null; n = n.getNextSibling()) {
                if (n.getText().equals("}")) break;
                toDelete.add(n);
            }
            for (PsiElement e : toDelete) e.delete();
        }
    }
}
