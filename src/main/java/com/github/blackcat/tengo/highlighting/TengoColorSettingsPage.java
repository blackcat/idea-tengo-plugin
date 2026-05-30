package com.github.blackcat.tengo.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.github.blackcat.tengo.TengoIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Map;

public class TengoColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", TengoTextAttributes.KEYWORD),
            new AttributesDescriptor("Constant", TengoTextAttributes.CONSTANT),
            new AttributesDescriptor("Number", TengoTextAttributes.NUMBER),
            new AttributesDescriptor("String", TengoTextAttributes.STRING),
            new AttributesDescriptor("Character", TengoTextAttributes.CHAR),
            new AttributesDescriptor("Line comment", TengoTextAttributes.LINE_COMMENT),
            new AttributesDescriptor("Block comment", TengoTextAttributes.BLOCK_COMMENT),
            new AttributesDescriptor("Operator", TengoTextAttributes.OPERATOR),
            new AttributesDescriptor("Parentheses", TengoTextAttributes.PAREN),
            new AttributesDescriptor("Braces", TengoTextAttributes.BRACE),
            new AttributesDescriptor("Brackets", TengoTextAttributes.BRACKET),
            new AttributesDescriptor("Comma", TengoTextAttributes.COMMA),
            new AttributesDescriptor("Semicolon", TengoTextAttributes.SEMICOLON),
            new AttributesDescriptor("Dot", TengoTextAttributes.DOT),
            new AttributesDescriptor("Identifier", TengoTextAttributes.IDENTIFIER),
            new AttributesDescriptor("Builtin function", TengoTextAttributes.BUILTIN),
            new AttributesDescriptor("Standard library module", TengoTextAttributes.STDLIB),
            new AttributesDescriptor("Function call", TengoTextAttributes.FUNCTION_CALL),
            new AttributesDescriptor("Function declaration", TengoTextAttributes.FUNCTION_DECLARATION),
            new AttributesDescriptor("Parameter", TengoTextAttributes.PARAMETER),
            new AttributesDescriptor("Local variable", TengoTextAttributes.LOCAL_VARIABLE),
    };

    @Override
    public @Nullable Icon getIcon() {
        return TengoIcons.FILE;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new TengoSyntaxHighlighter();
    }

    @Override
    public @NotNull String getDemoText() {
        return "// Tengo sample\n" +
                "fmt := import(\"fmt\")\n" +
                "math := import(\"math\")\n" +
                "\n" +
                "/* compute the area of a circle */\n" +
                "area := func(r) {\n" +
                "    return math.pi * r * r\n" +
                "}\n" +
                "\n" +
                "values := [1.0, 2.5, 3.14]\n" +
                "for i, v in values {\n" +
                "    if v > 1 {\n" +
                "        fmt.printf(\"%d => %v\\n\", i, area(v))\n" +
                "    } else {\n" +
                "        continue\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "export {\n" +
                "    area: area,\n" +
                "    answer: 42,\n" +
                "    ok: true,\n" +
                "    nothing: undefined,\n" +
                "}\n";
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Tengo";
    }
}
