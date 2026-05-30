package com.github.blackcat.tengo.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoBlock;
import com.github.blackcat.tengo.psi.TengoForInClause;
import com.github.blackcat.tengo.psi.TengoForInVar;
import com.github.blackcat.tengo.psi.TengoForStmt;
import com.github.blackcat.tengo.psi.TengoFuncLiteral;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoParam;
import com.github.blackcat.tengo.psi.TengoParamList;
import com.github.blackcat.tengo.TengoBuiltinDeclarations;
import com.github.blackcat.tengo.TengoBuiltins;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TengoLocalReference extends PsiPolyVariantReferenceBase<TengoIdentifierExpr> {

    public TengoLocalReference(@NotNull TengoIdentifierExpr element) {
        super(element, identifierRangeIn(element));
    }

    private static TextRange identifierRangeIn(@NotNull TengoIdentifierExpr element) {
        PsiElement id = element.getIdentifier();
        if (id == null) return TextRange.from(0, element.getTextLength());
        int offset = id.getStartOffsetInParent();
        return TextRange.from(offset, id.getTextLength());
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        String name = getValue();
        if (name.isEmpty()) return ResolveResult.EMPTY_ARRAY;

        List<PsiElement> targets = new ArrayList<>();
        PsiElement scope = myElement.getParent();
        while (scope != null) {
            if (scope instanceof TengoFuncLiteral) {
                TengoParamList params = ((TengoFuncLiteral) scope).getParamList();
                if (params != null) {
                    for (TengoParam p : params.getParamList()) {
                        if (p.getIdentifier() != null && name.equals(p.getIdentifier().getText())) {
                            targets.add(p);
                        }
                    }
                }
            }
            if (scope instanceof TengoForStmt) {
                TengoForInClause inClause = PsiTreeUtil.findChildOfType(scope, TengoForInClause.class);
                if (inClause != null) {
                    for (TengoForInVar v : inClause.getForInVarList()) {
                        if (name.equals(v.getText())) targets.add(v);
                    }
                }
            }
            if (scope instanceof TengoBlock || scope instanceof TengoFile) {
                collectAssignsInScope(scope, name, targets);
            }
            scope = scope.getParent();
        }

        if (targets.isEmpty() && (TengoBuiltins.isBuiltin(name) || TengoBuiltins.isStdlibModule(name))) {
            TengoIdentifierExpr builtin = TengoBuiltinDeclarations
                    .getInstance(myElement.getProject()).get(name);
            if (builtin != null) targets.add(builtin);
        }

        if (targets.isEmpty()) return ResolveResult.EMPTY_ARRAY;

        ResolveResult[] out = new ResolveResult[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            out[i] = new PsiElementResolveResult(targets.get(i));
        }
        return out;
    }

    private void collectAssignsInScope(@NotNull PsiElement scope, @NotNull String name, @NotNull List<PsiElement> targets) {
        for (PsiElement assignPsi : PsiTreeUtil.findChildrenOfType(scope, TengoAssignStmt.class)) {
            TengoAssignStmt assign = (TengoAssignStmt) assignPsi;
            if (!TengoPsiUtil.isDefinition(assign)) continue;
            // Only declarations at this scope level (not in nested function bodies).
            PsiElement enclosingScope = enclosingScopeOf(assign);
            if (enclosingScope != scope) continue;
            // Forward-declaration rule: in a block, only declarations before the reference are visible.
            if (scope instanceof TengoBlock && assign.getTextOffset() > myElement.getTextOffset()) continue;
            for (TengoIdentifierExpr lhs : TengoPsiUtil.lhsIdentifiers(assign)) {
                if (lhs.getIdentifier() != null && name.equals(lhs.getIdentifier().getText())) {
                    targets.add(lhs);
                }
            }
        }
    }

    private static @Nullable PsiElement enclosingScopeOf(@NotNull PsiElement node) {
        PsiElement p = node.getParent();
        while (p != null) {
            if (p instanceof TengoBlock || p instanceof TengoFile) return p;
            p = p.getParent();
        }
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        // The completion contributor already handles variant collection; references
        // are only used for resolution, not autopopulation.
        return PsiPolyVariantReference.EMPTY_ARRAY;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        myElement.setName(newElementName);
        return myElement;
    }
}
