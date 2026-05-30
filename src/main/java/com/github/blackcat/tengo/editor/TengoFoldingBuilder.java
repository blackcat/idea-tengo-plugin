package com.github.blackcat.tengo.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TengoFoldingBuilder extends FoldingBuilderEx implements DumbAware {

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        for (PsiElement element : PsiTreeUtil.findChildrenOfAnyType(root,
                com.intellij.psi.PsiElement.class)) {

            IElementType type = element.getNode().getElementType();
            TextRange range = element.getTextRange();
            if (range.getLength() < 2) continue;
            if (!spansMultipleLines(document, range)) continue;

            if (type == TengoTypes.BLOCK
                    || type == TengoTypes.MAP_LITERAL
                    || type == TengoTypes.ARRAY_LITERAL
                    || type == TengoTypes.BLOCK_COMMENT) {
                descriptors.add(new FoldingDescriptor(element.getNode(), range));
            }
        }
        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }

    private static boolean spansMultipleLines(@NotNull Document document, @NotNull TextRange range) {
        return document.getLineNumber(range.getStartOffset()) != document.getLineNumber(range.getEndOffset());
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        IElementType type = node.getElementType();
        if (type == TengoTypes.BLOCK) return "{...}";
        if (type == TengoTypes.MAP_LITERAL) return "{...}";
        if (type == TengoTypes.ARRAY_LITERAL) return "[...]";
        if (type == TengoTypes.BLOCK_COMMENT) return "/*...*/";
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
