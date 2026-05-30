package com.github.blackcat.tengo.highlighting;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.github.blackcat.tengo.TengoBuiltins;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Re-colors leaf IDENTIFIER tokens whose text matches a known Tengo builtin
 * or stdlib module name. This runs on top of the lexer-driven syntax
 * highlighter and overlays the builtin / stdlib attribute.
 */
public class TengoBuiltinAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getFirstChild() != null) return;
        IElementType type = element.getNode().getElementType();
        if (type != TengoTypes.IDENTIFIER) return;

        String text = element.getText();
        if (TengoBuiltins.isStdlibModule(text)) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(TengoTextAttributes.STDLIB)
                    .create();
        } else if (TengoBuiltins.isBuiltin(text)) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(TengoTextAttributes.BUILTIN)
                    .create();
        }
    }
}
