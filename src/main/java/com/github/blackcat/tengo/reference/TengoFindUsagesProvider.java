package com.github.blackcat.tengo.reference;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.github.blackcat.tengo.lexer.TengoLexerAdapter;
import com.github.blackcat.tengo.parser.TengoParserDefinition;
import com.github.blackcat.tengo.psi.TengoForInVar;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoMapKey;
import com.github.blackcat.tengo.psi.TengoParam;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TengoFindUsagesProvider implements FindUsagesProvider {

    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new TengoLexerAdapter(),
                TokenSet.create(TengoTypes.IDENTIFIER),
                TengoParserDefinition.COMMENTS,
                TengoParserDefinition.STRINGS);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement element) {
        return element instanceof TengoIdentifierExpr
                || element instanceof TengoParam
                || element instanceof TengoForInVar
                // map keys are the resolve targets for stdlib members
                // (e.g. `encode` inside `json := { encode: ..., ... }`) AND for
                // user-defined exports (`export { area: ..., sum: ... }`).
                || element instanceof TengoMapKey;
    }

    @Override
    public @Nullable String getHelpId(@NotNull PsiElement psiElement) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @Override
    public @NotNull String getType(@NotNull PsiElement element) {
        if (element instanceof TengoParam) return "parameter";
        if (element instanceof TengoForInVar) return "loop variable";
        if (element instanceof TengoMapKey) return "member";
        return "variable";
    }

    @Override
    public @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        return element.getText();
    }

    @Override
    public @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return element.getText();
    }
}
