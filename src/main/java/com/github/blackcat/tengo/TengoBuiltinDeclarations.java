package com.github.blackcat.tengo;

import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A per-project synthesised Tengo file that contains a trivial declaration for every
 * built-in function and standard-library module. References to a builtin name in user
 * code resolve to the corresponding LHS identifier here, which lets Find Usages and
 * navigation work uniformly for built-ins and user-defined names.
 *
 * The file is non-physical (in-memory only). It's never visible to the user but it IS
 * indexed by the Find Usages word index because PsiFileFactory associates it with the
 * project.
 */
@Service(Service.Level.PROJECT)
public final class TengoBuiltinDeclarations {

    private final Project project;
    private volatile Map<String, TengoIdentifierExpr> byName;

    public TengoBuiltinDeclarations(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull TengoBuiltinDeclarations getInstance(@NotNull Project project) {
        return project.getService(TengoBuiltinDeclarations.class);
    }

    public @Nullable TengoIdentifierExpr get(@NotNull String name) {
        return cache().get(name);
    }

    private Map<String, TengoIdentifierExpr> cache() {
        Map<String, TengoIdentifierExpr> local = byName;
        if (local != null) return local;
        synchronized (this) {
            if (byName != null) return byName;
            StringBuilder source = new StringBuilder();
            for (String name : TengoBuiltins.BUILTIN_FUNCTIONS) {
                source.append(name).append(" := func() {}\n");
            }
            for (String mod : TengoBuiltins.STDLIB_MODULES) {
                source.append(mod).append(" := {}\n");
            }
            PsiFile synthetic = PsiFileFactory.getInstance(project)
                    .createFileFromText("__tengo_builtins__.tengo", TengoFileType.INSTANCE, source.toString());
            Map<String, TengoIdentifierExpr> map = new HashMap<>();
            for (TengoAssignStmt assign : PsiTreeUtil.findChildrenOfType(synthetic, TengoAssignStmt.class)) {
                if (!TengoPsiUtil.isDefinition(assign)) continue;
                List<TengoIdentifierExpr> lhs = TengoPsiUtil.lhsIdentifiers(assign);
                if (lhs.isEmpty()) continue;
                String key = lhs.get(0).getName();
                if (key != null) map.put(key, lhs.get(0));
            }
            byName = map;
            return map;
        }
    }
}
