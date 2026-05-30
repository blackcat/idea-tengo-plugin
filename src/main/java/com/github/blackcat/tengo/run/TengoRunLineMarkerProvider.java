package com.github.blackcat.tengo.run;

import com.github.blackcat.tengo.TengoFile;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adds a green play-arrow gutter icon at the first non-comment leaf of a Tengo file so
 * the user can one-click run it. We attach the marker only to the FIRST such element to
 * avoid duplicate icons.
 */
public class TengoRunLineMarkerProvider extends RunLineMarkerContributor {

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        // Only attach at the file's first leaf (or first non-whitespace leaf).
        if (!(element.getContainingFile() instanceof TengoFile)) return null;
        if (element.getFirstChild() != null) return null;
        PsiElement file = element.getContainingFile();
        if (file == null) return null;

        PsiElement firstLeaf = findFirstNonTrivialLeaf(file);
        if (firstLeaf != element) return null;

        return new Info(
                com.intellij.icons.AllIcons.RunConfigurations.TestState.Run,
                ExecutorAction.getActions(0));
    }

    private static @Nullable PsiElement findFirstNonTrivialLeaf(@NotNull PsiElement root) {
        PsiElement current = root;
        while (current != null) {
            PsiElement first = current.getFirstChild();
            if (first == null) {
                if (current.getTextLength() > 0) return current;
                return null;
            }
            current = skipTrivia(first);
        }
        return null;
    }

    private static @Nullable PsiElement skipTrivia(@Nullable PsiElement element) {
        while (element != null && element.getTextLength() == 0) {
            element = element.getNextSibling();
        }
        return element;
    }
}
