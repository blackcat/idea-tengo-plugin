package com.github.blackcat.tengo.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.github.blackcat.tengo.TengoBuiltins;
import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoExportStmt;
import com.github.blackcat.tengo.psi.TengoExpr;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoImportExpr;
import com.github.blackcat.tengo.psi.TengoImportPath;
import com.github.blackcat.tengo.psi.TengoMapEntry;
import com.github.blackcat.tengo.psi.TengoMapLiteral;
import com.github.blackcat.tengo.psi.TengoSelectorSuffix;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the identifier after a `.` to a member of the receiver:
 *   - If receiver is a known stdlib module, no concrete resolve target (members are virtual).
 *   - If receiver is a name bound to `import("./path")`, resolve to the corresponding
 *     `export { name: ... }` entry in the target file.
 */
public class TengoSelectorMemberReference extends PsiPolyVariantReferenceBase<PsiElement> {

    public TengoSelectorMemberReference(@NotNull PsiElement element) {
        super(element, TextRange.from(0, element.getTextLength()));
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        String memberName = myElement.getText();
        if (memberName == null || memberName.isEmpty()) return ResolveResult.EMPTY_ARRAY;

        TengoSelectorSuffix selector = PsiTreeUtil.getParentOfType(myElement, TengoSelectorSuffix.class);
        if (selector == null) return ResolveResult.EMPTY_ARRAY;

        PsiElement receiver = selector.getPrevSibling();
        while (receiver != null && receiver.getTextLength() == 0) receiver = receiver.getPrevSibling();
        if (receiver == null) return ResolveResult.EMPTY_ARRAY;

        String receiverName = receiver.getText();
        if (TengoBuiltins.isStdlibModule(receiverName)
                && TengoBuiltins.membersOf(receiverName).contains(memberName)) {
            // Synthetic — no target. cmd-click will fail-soft here.
            return ResolveResult.EMPTY_ARRAY;
        }

        TengoFile importedFile = resolveImportedFile(receiverName, myElement);
        if (importedFile == null) return ResolveResult.EMPTY_ARRAY;

        List<PsiElement> targets = new ArrayList<>();
        TengoExpr exported = exportValue(importedFile);
        TengoMapLiteral map = exported instanceof TengoMapLiteral
                ? (TengoMapLiteral) exported
                : PsiTreeUtil.findChildOfType(exported, TengoMapLiteral.class);
        if (map != null) {
            for (TengoMapEntry entry : map.getMapEntryList()) {
                String key = entry.getMapKey().getText();
                key = TengoPsiUtil.unquote(key);
                if (memberName.equals(key)) {
                    targets.add(entry.getMapKey());
                }
            }
        }

        if (targets.isEmpty()) return ResolveResult.EMPTY_ARRAY;
        ResolveResult[] out = new ResolveResult[targets.size()];
        for (int i = 0; i < targets.size(); i++) out[i] = new PsiElementResolveResult(targets.get(i));
        return out;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return EMPTY_ARRAY;
    }

    private static @Nullable TengoFile resolveImportedFile(@NotNull String receiverName, @NotNull PsiElement at) {
        PsiFile file = at.getContainingFile();
        if (!(file instanceof TengoFile)) return null;

        for (TengoAssignStmt assign : PsiTreeUtil.findChildrenOfType(file, TengoAssignStmt.class)) {
            if (!TengoPsiUtil.isDefinition(assign)) continue;
            List<TengoIdentifierExpr> lhs = TengoPsiUtil.lhsIdentifiers(assign);
            if (lhs.isEmpty()) continue;
            if (!receiverName.equals(lhs.get(0).getIdentifier().getText())) continue;
            TengoExpr rhs = TengoPsiUtil.firstRhs(assign);
            TengoImportExpr imp = TengoPsiUtil.asImportExpr(rhs);
            if (imp == null) continue;
            TengoImportPath path = imp.getImportPath();
            if (path == null) continue;
            String pathStr = TengoPsiUtil.pathOf(path);
            if (pathStr == null) return null;
            return resolveRelative(file.getVirtualFile(), pathStr, at);
        }
        return null;
    }

    private static @Nullable TengoFile resolveRelative(@Nullable VirtualFile fromFile, @NotNull String path, @NotNull PsiElement context) {
        if (fromFile == null) return null;
        VirtualFile target = TengoImportPathReference.resolvePath(fromFile, path);
        if (target == null) return null;
        PsiFile psi = PsiManager.getInstance(context.getProject()).findFile(target);
        return psi instanceof TengoFile ? (TengoFile) psi : null;
    }

    private static @Nullable TengoExpr exportValue(@NotNull TengoFile file) {
        TengoExportStmt export = PsiTreeUtil.findChildOfType(file, TengoExportStmt.class);
        return export == null ? null : export.getExpr();
    }
}
