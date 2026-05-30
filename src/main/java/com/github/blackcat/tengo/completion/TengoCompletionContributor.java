package com.github.blackcat.tengo.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.github.blackcat.tengo.TengoBuiltins;
import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.TengoFileType;
import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoExpr;
import com.github.blackcat.tengo.psi.TengoForInClause;
import com.github.blackcat.tengo.psi.TengoForInVar;
import com.github.blackcat.tengo.psi.TengoFuncLiteral;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoImportPath;
import com.github.blackcat.tengo.psi.TengoParam;
import com.github.blackcat.tengo.psi.TengoParamList;
import com.github.blackcat.tengo.psi.TengoSelectorSuffix;
import com.github.blackcat.tengo.psi.TengoTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TengoCompletionContributor extends CompletionContributor {

    private static final List<String> KEYWORDS = List.of(
            "if", "else", "for", "in", "return", "break", "continue",
            "func", "import", "export", "true", "false", "undefined");

    public TengoCompletionContributor() {
        // Identifier-position completion: keywords, builtins, stdlib modules, locals.
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(TengoTypes.IDENTIFIER),
                new IdentifierCompletionProvider());

        // After-the-dot member completion for stdlib modules.
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(TengoTypes.IDENTIFIER)
                        .withSuperParent(2, TengoSelectorSuffix.class),
                new SelectorCompletionProvider());

        // Path completion inside import("...").
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(TengoTypes.STRING_LITERAL)
                        .withParent(TengoImportPath.class),
                new ImportPathCompletionProvider());
    }

    private static class IdentifierCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            PsiElement position = parameters.getPosition();

            // Skip if we're after a dot — that's handled by SelectorCompletionProvider.
            if (PsiTreeUtil.getParentOfType(position, TengoSelectorSuffix.class) != null) {
                return;
            }

            for (String kw : KEYWORDS) {
                result.addElement(PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(kw)
                                .withBoldness(true)
                                .withIcon(AllIcons.Nodes.Static),
                        50));
            }

            for (String name : TengoBuiltins.BUILTIN_FUNCTIONS) {
                result.addElement(PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(name)
                                .withIcon(AllIcons.Nodes.Method)
                                .withTypeText("builtin", true),
                        40));
            }

            for (String name : TengoBuiltins.STDLIB_MODULES) {
                result.addElement(PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(name)
                                .withIcon(AllIcons.Nodes.PpLib)
                                .withTypeText("stdlib", true),
                        45));
            }

            Set<String> locals = new LinkedHashSet<>();
            collectLocalNames(position, locals);
            for (String name : locals) {
                result.addElement(PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(name)
                                .withIcon(AllIcons.Nodes.Variable),
                        100));
            }
        }
    }

    private static class SelectorCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            PsiElement position = parameters.getPosition();
            TengoSelectorSuffix selector = PsiTreeUtil.getParentOfType(position, TengoSelectorSuffix.class);
            if (selector == null) return;

            // The receiver is the expression just before this selector.
            PsiElement parent = selector.getParent();
            if (parent == null) return;
            PsiElement receiver = selector.getPrevSibling();
            while (receiver != null && receiver.getTextLength() == 0) receiver = receiver.getPrevSibling();
            if (receiver == null) return;

            String receiverText = receiver.getText();
            // Direct stdlib reference, e.g. `math.<caret>`.
            if (TengoBuiltins.isStdlibModule(receiverText)) {
                for (String member : TengoBuiltins.membersOf(receiverText)) {
                    result.addElement(LookupElementBuilder.create(member)
                            .withIcon(AllIcons.Nodes.Method)
                            .withTypeText(receiverText, true));
                }
                return;
            }

            // Imported module: find `<receiverText> := import("<name>")` and offer that module's members.
            String moduleName = importedModuleNameFor(receiverText, position);
            if (moduleName != null && TengoBuiltins.isStdlibModule(moduleName)) {
                for (String member : TengoBuiltins.membersOf(moduleName)) {
                    result.addElement(LookupElementBuilder.create(member)
                            .withIcon(AllIcons.Nodes.Method)
                            .withTypeText(moduleName, true));
                }
            }
        }
    }

    private static class ImportPathCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            PsiFile file = parameters.getOriginalFile();
            if (!(file instanceof TengoFile)) return;
            VirtualFile vFile = file.getVirtualFile();
            if (vFile == null) return;
            VirtualFile parent = vFile.getParent();
            if (parent == null) return;

            for (String mod : TengoBuiltins.STDLIB_MODULES) {
                result.addElement(LookupElementBuilder.create("\"" + mod + "\"")
                        .withPresentableText(mod)
                        .withIcon(AllIcons.Nodes.PpLib)
                        .withTypeText("stdlib", true));
            }

            for (VirtualFile child : parent.getChildren()) {
                if (child.isDirectory()) continue;
                if (!"tengo".equals(child.getExtension())) continue;
                if (child.equals(vFile)) continue;
                String name = "./" + child.getNameWithoutExtension();
                result.addElement(LookupElementBuilder.create("\"" + name + "\"")
                        .withPresentableText(name)
                        .withIcon(TengoFileType.INSTANCE.getIcon()));
            }
        }
    }

    static void collectLocalNames(@NotNull PsiElement position, @NotNull Set<String> out) {
        PsiElement scope = position;
        Set<PsiElement> seen = new HashSet<>();
        while (scope != null) {
            if (scope instanceof TengoFuncLiteral) {
                TengoParamList params = ((TengoFuncLiteral) scope).getParamList();
                if (params != null) {
                    for (TengoParam p : params.getParamList()) {
                        if (p.getIdentifier() != null) {
                            out.add(p.getIdentifier().getText());
                        }
                    }
                }
            }
            if (scope instanceof TengoForInClause) {
                for (TengoForInVar v : ((TengoForInClause) scope).getForInVarList()) {
                    out.add(v.getText());
                }
            }
            for (PsiElement assignNode : PsiTreeUtil.findChildrenOfType(scope, TengoAssignStmt.class)) {
                if (!seen.add(assignNode)) continue;
                TengoAssignStmt assign = (TengoAssignStmt) assignNode;
                if (!TengoPsiUtil.isDefinition(assign)) continue;
                if (assign.getTextOffset() > position.getTextOffset()) continue;
                for (TengoIdentifierExpr lhs : TengoPsiUtil.lhsIdentifiers(assign)) {
                    if (lhs.getIdentifier() != null) {
                        out.add(lhs.getIdentifier().getText());
                    }
                }
            }
            scope = scope.getParent();
        }
    }

    private static String importedModuleNameFor(@NotNull String name, @NotNull PsiElement position) {
        PsiFile file = position.getContainingFile();
        if (!(file instanceof TengoFile)) return null;
        for (TengoAssignStmt assign : PsiTreeUtil.findChildrenOfType(file, TengoAssignStmt.class)) {
            if (!TengoPsiUtil.isDefinition(assign)) continue;
            List<TengoIdentifierExpr> lhs = TengoPsiUtil.lhsIdentifiers(assign);
            if (lhs.isEmpty()) continue;
            if (!name.equals(lhs.get(0).getIdentifier().getText())) continue;
            TengoExpr rhs = TengoPsiUtil.firstRhs(assign);
            var importExpr = TengoPsiUtil.asImportExpr(rhs);
            if (importExpr == null || importExpr.getImportPath() == null) continue;
            return TengoPsiUtil.pathOf(importExpr.getImportPath());
        }
        return null;
    }
}
