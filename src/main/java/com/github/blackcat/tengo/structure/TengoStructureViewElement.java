package com.github.blackcat.tengo.structure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.TengoPsiUtil;
import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoBlock;
import com.github.blackcat.tengo.psi.TengoExportStmt;
import com.github.blackcat.tengo.psi.TengoExpr;
import com.github.blackcat.tengo.psi.TengoForInVar;
import com.github.blackcat.tengo.psi.TengoFuncLiteral;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoImportExpr;
import com.github.blackcat.tengo.psi.TengoParam;
import com.github.blackcat.tengo.psi.TengoParamList;
import com.github.blackcat.tengo.psi.TengoStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TengoStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final PsiElement element;

    public TengoStructureViewElement(@NotNull PsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (element instanceof NavigatablePsiElement) {
            ((NavigatablePsiElement) element).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return element instanceof NavigatablePsiElement && ((NavigatablePsiElement) element).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        String text = presentText();
        return text == null ? "" : text;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return new PresentationData(presentText(), null, presentIcon(), null);
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        List<TreeElement> children = new ArrayList<>();
        if (element instanceof TengoFile) {
            collectStatementChildren(((TengoFile) element).getChildren(), children);
        } else if (element instanceof TengoFuncLiteral) {
            TengoFuncLiteral func = (TengoFuncLiteral) element;
            TengoParamList params = func.getParamList();
            if (params != null) {
                for (TengoParam p : params.getParamList()) {
                    children.add(new TengoStructureViewElement(p));
                }
            }
            TengoBlock body = func.getBlock();
            if (body != null) {
                collectStatementChildren(body.getChildren(), children);
            }
        } else if (element instanceof TengoAssignStmt) {
            TengoAssignStmt assign = (TengoAssignStmt) element;
            TengoExpr rhs = TengoPsiUtil.firstRhs(assign);
            TengoFuncLiteral fn = TengoPsiUtil.asFuncLiteral(rhs);
            if (fn != null) {
                return new TengoStructureViewElement(fn).getChildren();
            }
        }
        return children.toArray(new TreeElement[0]);
    }

    private void collectStatementChildren(PsiElement[] childPsi, List<TreeElement> out) {
        for (PsiElement child : childPsi) {
            collectFromStatement(child, out);
        }
    }

    private void collectFromStatement(PsiElement node, List<TreeElement> out) {
        TengoStatement stmt = PsiTreeUtil.getNonStrictParentOfType(node, TengoStatement.class);
        if (stmt == null && node instanceof TengoStatement) stmt = (TengoStatement) node;
        if (stmt == null) {
            stmt = PsiTreeUtil.findChildOfType(node, TengoStatement.class, false);
            if (stmt == null) return;
        }
        PsiElement inner = stmt.getFirstChild();
        if (inner == null) return;

        TengoAssignStmt assign = PsiTreeUtil.findChildOfType(inner, TengoAssignStmt.class, false);
        if (assign == null && inner instanceof TengoAssignStmt) assign = (TengoAssignStmt) inner;
        if (assign != null && TengoPsiUtil.isDefinition(assign)) {
            for (TengoIdentifierExpr lhs : TengoPsiUtil.lhsIdentifiers(assign)) {
                if (lhs.getIdentifier() != null) {
                    out.add(new TengoStructureViewElement(assign));
                    break;
                }
            }
            return;
        }

        if (inner instanceof TengoExportStmt) {
            out.add(new TengoStructureViewElement(inner));
        }
    }

    private @Nullable String presentText() {
        if (element instanceof TengoFile) return ((TengoFile) element).getName();
        if (element instanceof TengoFuncLiteral) {
            return "func" + paramSignature((TengoFuncLiteral) element);
        }
        if (element instanceof TengoAssignStmt) {
            TengoAssignStmt assign = (TengoAssignStmt) element;
            List<TengoIdentifierExpr> lhs = TengoPsiUtil.lhsIdentifiers(assign);
            if (lhs.isEmpty()) return assign.getText();
            String name = lhs.get(0).getIdentifier().getText();
            TengoExpr rhs = TengoPsiUtil.firstRhs(assign);
            TengoFuncLiteral fn = TengoPsiUtil.asFuncLiteral(rhs);
            if (fn != null) {
                return name + paramSignature(fn);
            }
            TengoImportExpr importExpr = TengoPsiUtil.asImportExpr(rhs);
            if (importExpr != null) {
                String path = importExpr.getImportPath() == null ? "?"
                        : String.valueOf(TengoPsiUtil.pathOf(importExpr.getImportPath()));
                return name + " = import(\"" + path + "\")";
            }
            return name;
        }
        if (element instanceof TengoExportStmt) {
            return "export";
        }
        if (element instanceof TengoParam) {
            PsiElement id = ((TengoParam) element).getIdentifier();
            return id == null ? "param" : id.getText();
        }
        if (element instanceof TengoForInVar) {
            return ((TengoForInVar) element).getText();
        }
        return element.getText();
    }

    private @Nullable Icon presentIcon() {
        if (element instanceof TengoFile) return AllIcons.FileTypes.Any_type;
        if (element instanceof TengoFuncLiteral) return AllIcons.Nodes.Function;
        if (element instanceof TengoExportStmt) return AllIcons.Nodes.ModuleGroup;
        if (element instanceof TengoParam) return AllIcons.Nodes.Parameter;
        if (element instanceof TengoForInVar) return AllIcons.Nodes.Variable;
        if (element instanceof TengoAssignStmt) {
            TengoExpr rhs = TengoPsiUtil.firstRhs((TengoAssignStmt) element);
            if (TengoPsiUtil.asFuncLiteral(rhs) != null) return AllIcons.Nodes.Function;
            if (TengoPsiUtil.asImportExpr(rhs) != null) return AllIcons.Nodes.PpLib;
            return AllIcons.Nodes.Variable;
        }
        return AllIcons.Nodes.Field;
    }

    private static String paramSignature(@NotNull TengoFuncLiteral fn) {
        TengoParamList list = fn.getParamList();
        if (list == null || list.getParamList().isEmpty()) return "()";
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (TengoParam p : list.getParamList()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(p.getText());
        }
        return sb.append(')').toString();
    }

    @SuppressWarnings("unused")
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        List<StructureViewTreeElement> out = new ArrayList<>();
        for (TreeElement t : getChildren()) {
            if (t instanceof StructureViewTreeElement) {
                out.add((StructureViewTreeElement) t);
            }
        }
        return out;
    }
}
