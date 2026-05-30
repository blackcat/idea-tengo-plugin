package com.github.blackcat.tengo.reference;

import com.github.blackcat.tengo.TengoBuiltinDeclarations;
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
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Resolves the identifier after a `.` to a member of the receiver:
 *   - Receiver bound to {@code import("./path")} or {@code import(":name")} → resolves to
 *     the corresponding key inside the imported file's {@code export {...}} map.
 *   - Receiver bound to {@code import("<stdlib>")} → resolves to a synthetic stdlib member
 *     declaration in {@link TengoBuiltinDeclarations}.
 *   - Receiver name is itself a known stdlib module (e.g. unqualified {@code math.pi}) →
 *     resolves the same way.
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

        // (1) Receiver is a local variable bound to import(...). Inspect the import path.
        String boundImport = importPathBoundTo(receiverName, myElement);
        if (boundImport != null) {
            if (TengoBuiltins.isStdlibModule(boundImport)) {
                PsiElement target = TengoBuiltinDeclarations.getInstance(myElement.getProject())
                        .getStdlibMember(boundImport, memberName);
                if (target != null) return single(target);
            } else {
                TengoFile importedFile = resolveRelativeFile(boundImport, myElement);
                if (importedFile != null) {
                    PsiElement target = findExportedMember(importedFile, memberName);
                    if (target != null) return single(target);
                }
            }
        }

        // (2) Receiver name is itself a stdlib module (unqualified `math.pi`).
        if (TengoBuiltins.isStdlibModule(receiverName)) {
            PsiElement target = TengoBuiltinDeclarations.getInstance(myElement.getProject())
                    .getStdlibMember(receiverName, memberName);
            if (target != null) return single(target);
        }

        return ResolveResult.EMPTY_ARRAY;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return EMPTY_ARRAY;
    }

    /** If `receiverName := import("X")` at top level, return X. Otherwise null. */
    private static @Nullable String importPathBoundTo(@NotNull String receiverName, @NotNull PsiElement at) {
        PsiFile file = at.getContainingFile();
        if (!(file instanceof TengoFile)) return null;
        for (TengoAssignStmt assign : PsiTreeUtil.findChildrenOfType(file, TengoAssignStmt.class)) {
            if (!TengoPsiUtil.isDefinition(assign)) continue;
            List<TengoIdentifierExpr> lhs = TengoPsiUtil.lhsIdentifiers(assign);
            if (lhs.isEmpty()) continue;
            if (lhs.get(0).getIdentifier() == null) continue;
            if (!receiverName.equals(lhs.get(0).getIdentifier().getText())) continue;
            TengoExpr rhs = TengoPsiUtil.firstRhs(assign);
            TengoImportExpr imp = TengoPsiUtil.asImportExpr(rhs);
            if (imp == null) continue;
            TengoImportPath path = imp.getImportPath();
            if (path == null) continue;
            return TengoPsiUtil.pathOf(path);
        }
        return null;
    }

    private static @Nullable TengoFile resolveRelativeFile(@NotNull String path, @NotNull PsiElement context) {
        PsiFile containing = context.getContainingFile();
        if (containing == null) return null;
        VirtualFile from = containing.getVirtualFile();
        if (from == null) return null;
        VirtualFile target = TengoImportPathReference.resolvePath(from, path);
        if (target == null) return null;
        PsiFile psi = PsiManager.getInstance(context.getProject()).findFile(target);
        return psi instanceof TengoFile ? (TengoFile) psi : null;
    }

    private static @Nullable PsiElement findExportedMember(@NotNull TengoFile file, @NotNull String memberName) {
        TengoExportStmt export = PsiTreeUtil.findChildOfType(file, TengoExportStmt.class);
        if (export == null) return null;
        TengoExpr exportedExpr = export.getExpr();
        TengoMapLiteral map = exportedExpr instanceof TengoMapLiteral
                ? (TengoMapLiteral) exportedExpr
                : PsiTreeUtil.findChildOfType(exportedExpr, TengoMapLiteral.class);
        if (map == null) return null;
        for (TengoMapEntry entry : map.getMapEntryList()) {
            if (entry.getMapKey() == null) continue;
            String key = TengoPsiUtil.unquote(entry.getMapKey().getText());
            if (memberName.equals(key)) return entry.getMapKey();
        }
        return null;
    }

    private static ResolveResult[] single(@NotNull PsiElement target) {
        return new ResolveResult[]{new PsiElementResolveResult(target)};
    }
}
