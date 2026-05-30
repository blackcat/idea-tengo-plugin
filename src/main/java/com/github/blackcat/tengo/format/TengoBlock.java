package com.github.blackcat.tengo.format;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TengoBlock extends AbstractBlock {

    private final SpacingBuilder spacingBuilder;
    private final Indent indent;

    public TengoBlock(@NotNull ASTNode node,
                      @Nullable Wrap wrap,
                      @Nullable Alignment alignment,
                      @NotNull SpacingBuilder spacingBuilder,
                      @NotNull Indent indent) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
        this.indent = indent;
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = myNode.getFirstChildNode();
        while (child != null) {
            if (child.getElementType() != TokenType.WHITE_SPACE && !child.getText().isEmpty()) {
                blocks.add(new TengoBlock(child, null, null, spacingBuilder, childIndent(myNode, child)));
            }
            child = child.getTreeNext();
        }
        return blocks;
    }

    private static Indent childIndent(@NotNull ASTNode parent, @NotNull ASTNode child) {
        IElementType pt = parent.getElementType();
        IElementType ct = child.getElementType();
        if (pt == TengoTypes.BLOCK
                || pt == TengoTypes.MAP_LITERAL
                || pt == TengoTypes.ARRAY_LITERAL
                || pt == TengoTypes.PAREN_EXPR
                || pt == TengoTypes.ARG_LIST
                || pt == TengoTypes.PARAM_LIST) {
            if (ct == TengoTypes.LBRACE || ct == TengoTypes.RBRACE
                    || ct == TengoTypes.LBRACK || ct == TengoTypes.RBRACK
                    || ct == TengoTypes.LPAREN || ct == TengoTypes.RPAREN) {
                return Indent.getNoneIndent();
            }
            return Indent.getNormalIndent();
        }
        return Indent.getNoneIndent();
    }

    @Override
    public @NotNull Indent getIndent() {
        return indent;
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
        IElementType type = myNode.getElementType();
        if (type == TengoTypes.BLOCK
                || type == TengoTypes.MAP_LITERAL
                || type == TengoTypes.ARRAY_LITERAL
                || type == TengoTypes.ARG_LIST) {
            return new ChildAttributes(Indent.getNormalIndent(), null);
        }
        return new ChildAttributes(Indent.getNoneIndent(), null);
    }
}
