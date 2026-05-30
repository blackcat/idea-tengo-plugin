package com.github.blackcat.tengo.format;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Indent;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.github.blackcat.tengo.TengoLanguage;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;

public class TengoFormattingModelBuilder implements FormattingModelBuilder {

    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
        CommonCodeStyleSettings common = settings.getCommonSettings(TengoLanguage.INSTANCE.getID());

        SpacingBuilder spacingBuilder = new SpacingBuilder(settings, TengoLanguage.INSTANCE)
                // No space inside parens/brackets/braces
                .after(TengoTypes.LPAREN).none()
                .before(TengoTypes.RPAREN).none()
                .after(TengoTypes.LBRACK).none()
                .before(TengoTypes.RBRACK).none()

                // Comma: no space before, one after
                .before(TengoTypes.COMMA).none()
                .after(TengoTypes.COMMA).spaces(1)

                // Semicolon
                .before(TengoTypes.SEMICOLON).none()
                .after(TengoTypes.SEMICOLON).spaces(1)

                // Dot: no surrounding space
                .around(TengoTypes.DOT).none()

                // Spaces around binary operators
                .around(TengoTypes.ASSIGN).spaces(1)
                .around(TengoTypes.DEFINE).spaces(1)
                .around(TengoTypes.PLUS_ASSIGN).spaces(1)
                .around(TengoTypes.MINUS_ASSIGN).spaces(1)
                .around(TengoTypes.MUL_ASSIGN).spaces(1)
                .around(TengoTypes.DIV_ASSIGN).spaces(1)
                .around(TengoTypes.MOD_ASSIGN).spaces(1)
                .around(TengoTypes.BIT_AND_ASSIGN).spaces(1)
                .around(TengoTypes.BIT_OR_ASSIGN).spaces(1)
                .around(TengoTypes.BIT_XOR_ASSIGN).spaces(1)
                .around(TengoTypes.BIT_AND_NOT_ASSIGN).spaces(1)
                .around(TengoTypes.SHL_ASSIGN).spaces(1)
                .around(TengoTypes.SHR_ASSIGN).spaces(1)
                .around(TengoTypes.EQ).spaces(1)
                .around(TengoTypes.NEQ).spaces(1)
                .around(TengoTypes.LE).spaces(1)
                .around(TengoTypes.GE).spaces(1)
                .around(TengoTypes.LT).spaces(1)
                .around(TengoTypes.GT).spaces(1)
                .around(TengoTypes.LAND).spaces(1)
                .around(TengoTypes.LOR).spaces(1)
                .around(TengoTypes.SHL).spaces(1)
                .around(TengoTypes.SHR).spaces(1)
                .around(TengoTypes.BIT_AND).spaces(1)
                .around(TengoTypes.BIT_OR).spaces(1)
                .around(TengoTypes.BIT_XOR).spaces(1)
                .around(TengoTypes.BIT_AND_NOT).spaces(1)
                .around(TengoTypes.PLUS).spaces(1)
                .around(TengoTypes.MINUS).spaces(1)
                .around(TengoTypes.MUL).spaces(1)
                .around(TengoTypes.DIV).spaces(1)
                .around(TengoTypes.MOD).spaces(1)

                // Colon (in maps, in slices): one space after, none before
                .before(TengoTypes.COLON).none()
                .after(TengoTypes.COLON).spaces(1)

                // Brace placement
                .before(TengoTypes.LBRACE).spaces(1)
                .after(TengoTypes.LBRACE).none()

                // Increment/decrement attach to operand
                .before(TengoTypes.INC).none()
                .before(TengoTypes.DEC).none();

        TengoBlock root = new TengoBlock(
                formattingContext.getNode(),
                null,
                null,
                spacingBuilder,
                Indent.getNoneIndent());

        return FormattingModelProvider.createFormattingModelForPsiFile(
                formattingContext.getContainingFile(),
                root,
                settings);
    }
}
