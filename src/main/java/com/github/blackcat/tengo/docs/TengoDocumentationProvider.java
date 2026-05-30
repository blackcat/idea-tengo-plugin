package com.github.blackcat.tengo.docs;

import com.github.blackcat.tengo.TengoBuiltins;
import com.github.blackcat.tengo.TengoDocs;
import com.github.blackcat.tengo.psi.TengoIdentifierRef;
import com.github.blackcat.tengo.psi.TengoSelectorSuffix;
import com.github.blackcat.tengo.psi.TengoTypes;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TengoDocumentationProvider extends AbstractDocumentationProvider {

    @Override
    public @Nullable String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return renderDoc(element, originalElement);
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        return renderDoc(element, originalElement);
    }

    private @Nullable String renderDoc(@Nullable PsiElement element, @Nullable PsiElement originalElement) {
        PsiElement target = element != null ? element : originalElement;
        if (target == null) return null;

        // Selector member: `<module>.<member>` — show the curated docs.
        TengoSelectorSuffix selector = PsiTreeUtil.getParentOfType(target, TengoSelectorSuffix.class);
        if (selector != null) {
            String receiverName = receiverNameOf(selector);
            String memberName = target instanceof TengoIdentifierRef
                    ? target.getText()
                    : (target.getNode() != null && target.getNode().getElementType() == TengoTypes.IDENTIFIER
                        ? target.getText() : null);
            if (memberName != null && receiverName != null) {
                TengoDocs.Entry e = TengoDocs.member(receiverName, memberName);
                if (e != null) return renderEntry(e);
            }
        }

        // Bare identifier: builtin or stdlib module?
        String text = target.getText();
        if (text != null) {
            if (TengoBuiltins.isBuiltin(text)) {
                TengoDocs.Entry e = TengoDocs.builtin(text);
                if (e != null) return renderEntry(e);
            }
            if (TengoBuiltins.isStdlibModule(text)) {
                String summary = TengoDocs.module(text);
                if (summary != null) return renderModule(text, summary);
            }
        }
        return null;
    }

    private static @Nullable String receiverNameOf(@NotNull TengoSelectorSuffix selector) {
        PsiElement receiver = selector.getPrevSibling();
        while (receiver != null && receiver.getTextLength() == 0) receiver = receiver.getPrevSibling();
        return receiver == null ? null : receiver.getText();
    }

    private static @NotNull String renderEntry(@NotNull TengoDocs.Entry e) {
        return "<pre>" + escape(e.signature) + "</pre>"
                + "<p>" + escape(e.summary) + "</p>";
    }

    private static @NotNull String renderModule(@NotNull String name, @NotNull String summary) {
        return "<pre>module " + escape(name) + "</pre>"
                + "<p>" + escape(summary) + "</p>";
    }

    private static @NotNull String escape(@NotNull String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
