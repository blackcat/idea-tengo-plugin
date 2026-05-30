package com.github.blackcat.tengo.highlighting;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public final class TengoTextAttributes {
    public static final TextAttributesKey KEYWORD =
            TextAttributesKey.createTextAttributesKey("TENGO_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey NUMBER =
            TextAttributesKey.createTextAttributesKey("TENGO_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING =
            TextAttributesKey.createTextAttributesKey("TENGO_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey CHAR =
            TextAttributesKey.createTextAttributesKey("TENGO_CHAR", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LINE_COMMENT =
            TextAttributesKey.createTextAttributesKey("TENGO_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
            TextAttributesKey.createTextAttributesKey("TENGO_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey OPERATOR =
            TextAttributesKey.createTextAttributesKey("TENGO_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PAREN =
            TextAttributesKey.createTextAttributesKey("TENGO_PAREN", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACE =
            TextAttributesKey.createTextAttributesKey("TENGO_BRACE", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKET =
            TextAttributesKey.createTextAttributesKey("TENGO_BRACKET", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey COMMA =
            TextAttributesKey.createTextAttributesKey("TENGO_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey SEMICOLON =
            TextAttributesKey.createTextAttributesKey("TENGO_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey DOT =
            TextAttributesKey.createTextAttributesKey("TENGO_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey IDENTIFIER =
            TextAttributesKey.createTextAttributesKey("TENGO_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey BUILTIN =
            TextAttributesKey.createTextAttributesKey("TENGO_BUILTIN", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    public static final TextAttributesKey STDLIB =
            TextAttributesKey.createTextAttributesKey("TENGO_STDLIB", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    public static final TextAttributesKey CONSTANT =
            TextAttributesKey.createTextAttributesKey("TENGO_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey FUNCTION_CALL =
            TextAttributesKey.createTextAttributesKey("TENGO_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey FUNCTION_DECLARATION =
            TextAttributesKey.createTextAttributesKey("TENGO_FUNCTION_DECLARATION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey PARAMETER =
            TextAttributesKey.createTextAttributesKey("TENGO_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey LOCAL_VARIABLE =
            TextAttributesKey.createTextAttributesKey("TENGO_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

    private TengoTextAttributes() {
    }
}
