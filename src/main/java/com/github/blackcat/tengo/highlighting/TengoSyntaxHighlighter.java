package com.github.blackcat.tengo.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.github.blackcat.tengo.lexer.TengoLexerAdapter;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TengoSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey[] EMPTY = new TextAttributesKey[0];

    private static final TokenSet KEYWORDS = TokenSet.create(
            TengoTypes.IF, TengoTypes.ELSE, TengoTypes.FOR, TengoTypes.IN,
            TengoTypes.RETURN, TengoTypes.BREAK, TengoTypes.CONTINUE,
            TengoTypes.FUNC, TengoTypes.IMPORT, TengoTypes.EXPORT);

    private static final TokenSet CONSTANTS = TokenSet.create(
            TengoTypes.TRUE, TengoTypes.FALSE, TengoTypes.UNDEFINED);

    private static final TokenSet NUMBERS = TokenSet.create(
            TengoTypes.INTEGER_LITERAL, TengoTypes.FLOAT_LITERAL,
            TengoTypes.HEX_LITERAL, TengoTypes.OCT_LITERAL, TengoTypes.BIN_LITERAL);

    private static final TokenSet STRINGS = TokenSet.create(
            TengoTypes.STRING_LITERAL, TengoTypes.RAW_STRING_LITERAL);

    private static final TokenSet OPERATORS = TokenSet.create(
            TengoTypes.DEFINE, TengoTypes.ASSIGN,
            TengoTypes.PLUS_ASSIGN, TengoTypes.MINUS_ASSIGN, TengoTypes.MUL_ASSIGN,
            TengoTypes.DIV_ASSIGN, TengoTypes.MOD_ASSIGN,
            TengoTypes.BIT_AND_ASSIGN, TengoTypes.BIT_OR_ASSIGN, TengoTypes.BIT_XOR_ASSIGN,
            TengoTypes.BIT_AND_NOT_ASSIGN, TengoTypes.SHL_ASSIGN, TengoTypes.SHR_ASSIGN,
            TengoTypes.EQ, TengoTypes.NEQ, TengoTypes.LE, TengoTypes.GE,
            TengoTypes.LT, TengoTypes.GT,
            TengoTypes.LAND, TengoTypes.LOR, TengoTypes.NOT,
            TengoTypes.SHL, TengoTypes.SHR,
            TengoTypes.BIT_AND, TengoTypes.BIT_OR, TengoTypes.BIT_XOR, TengoTypes.BIT_AND_NOT,
            TengoTypes.PLUS, TengoTypes.MINUS, TengoTypes.MUL, TengoTypes.DIV, TengoTypes.MOD,
            TengoTypes.INC, TengoTypes.DEC,
            TengoTypes.ELLIPSIS, TengoTypes.QUESTION);

    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

    static {
        for (IElementType t : KEYWORDS.getTypes()) ATTRIBUTES.put(t, TengoTextAttributes.KEYWORD);
        for (IElementType t : CONSTANTS.getTypes()) ATTRIBUTES.put(t, TengoTextAttributes.CONSTANT);
        for (IElementType t : NUMBERS.getTypes()) ATTRIBUTES.put(t, TengoTextAttributes.NUMBER);
        for (IElementType t : STRINGS.getTypes()) ATTRIBUTES.put(t, TengoTextAttributes.STRING);
        for (IElementType t : OPERATORS.getTypes()) ATTRIBUTES.put(t, TengoTextAttributes.OPERATOR);

        ATTRIBUTES.put(TengoTypes.CHAR_LITERAL, TengoTextAttributes.CHAR);
        ATTRIBUTES.put(TengoTypes.LINE_COMMENT, TengoTextAttributes.LINE_COMMENT);
        ATTRIBUTES.put(TengoTypes.BLOCK_COMMENT, TengoTextAttributes.BLOCK_COMMENT);

        ATTRIBUTES.put(TengoTypes.LPAREN, TengoTextAttributes.PAREN);
        ATTRIBUTES.put(TengoTypes.RPAREN, TengoTextAttributes.PAREN);
        ATTRIBUTES.put(TengoTypes.LBRACE, TengoTextAttributes.BRACE);
        ATTRIBUTES.put(TengoTypes.RBRACE, TengoTextAttributes.BRACE);
        ATTRIBUTES.put(TengoTypes.LBRACK, TengoTextAttributes.BRACKET);
        ATTRIBUTES.put(TengoTypes.RBRACK, TengoTextAttributes.BRACKET);
        ATTRIBUTES.put(TengoTypes.COMMA, TengoTextAttributes.COMMA);
        ATTRIBUTES.put(TengoTypes.SEMICOLON, TengoTextAttributes.SEMICOLON);
        ATTRIBUTES.put(TengoTypes.COLON, TengoTextAttributes.OPERATOR);
        ATTRIBUTES.put(TengoTypes.DOT, TengoTextAttributes.DOT);

        ATTRIBUTES.put(TengoTypes.IDENTIFIER, TengoTextAttributes.IDENTIFIER);

        ATTRIBUTES.put(TokenType.BAD_CHARACTER, TextAttributesKey.createTextAttributesKey(
                "TENGO_BAD_CHARACTER",
                com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER));
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new TengoLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey key = ATTRIBUTES.get(tokenType);
        return key == null ? EMPTY : new TextAttributesKey[]{key};
    }
}
