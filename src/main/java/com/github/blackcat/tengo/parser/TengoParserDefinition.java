package com.github.blackcat.tengo.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.TengoLanguage;
import com.github.blackcat.tengo.lexer.TengoLexerAdapter;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;

public class TengoParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(TengoLanguage.INSTANCE);

    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(TengoTypes.LINE_COMMENT, TengoTypes.BLOCK_COMMENT);
    public static final TokenSet STRINGS = TokenSet.create(
            TengoTypes.STRING_LITERAL,
            TengoTypes.RAW_STRING_LITERAL,
            TengoTypes.CHAR_LITERAL);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new TengoLexerAdapter();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new TengoParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return STRINGS;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return TengoTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new TengoFile(viewProvider);
    }
}
