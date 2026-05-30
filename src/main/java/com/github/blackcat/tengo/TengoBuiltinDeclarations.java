package com.github.blackcat.tengo;

import com.github.blackcat.tengo.psi.TengoAssignStmt;
import com.github.blackcat.tengo.psi.TengoExpr;
import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.github.blackcat.tengo.psi.TengoMapEntry;
import com.github.blackcat.tengo.psi.TengoMapLiteral;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
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
 * built-in function and standard-library module (plus every documented module member).
 * References to a builtin name in user code resolve to the corresponding LHS identifier
 * here, which lets Find Usages and navigation work uniformly for built-ins and
 * user-defined names.
 *
 * The synthetic file is navigable (eventSystemEnabled=true) so cmd+click on a builtin
 * opens a read-only in-memory editor with the signature and a one-line doc summary.
 */
@Service(Service.Level.PROJECT)
public final class TengoBuiltinDeclarations {

    private final Project project;
    private volatile State state;

    private static final class State {
        final Map<String, TengoIdentifierExpr> byName = new HashMap<>();
        /** Keyed by "<module>.<member>". */
        final Map<String, PsiElement> stdlibMembers = new HashMap<>();
    }

    public TengoBuiltinDeclarations(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull TengoBuiltinDeclarations getInstance(@NotNull Project project) {
        return project.getService(TengoBuiltinDeclarations.class);
    }

    public @Nullable TengoIdentifierExpr get(@NotNull String name) {
        return state().byName.get(name);
    }

    public @Nullable PsiElement getStdlibMember(@NotNull String module, @NotNull String member) {
        return state().stdlibMembers.get(module + "." + member);
    }

    private State state() {
        State local = state;
        if (local != null) return local;
        synchronized (this) {
            if (state != null) return state;
            PsiFile synthetic = PsiFileFactory.getInstance(project).createFileFromText(
                    "Tengo Builtins.tengo",
                    TengoFileType.INSTANCE,
                    renderSource(),
                    /* modificationStamp */ 0L,
                    /* eventSystemEnabled */ true,
                    /* markAsCopy */ false);
            State built = new State();
            if (synthetic != null) {
                for (TengoAssignStmt assign : PsiTreeUtil.findChildrenOfType(synthetic, TengoAssignStmt.class)) {
                    if (!TengoPsiUtil.isDefinition(assign)) continue;
                    List<TengoIdentifierExpr> lhs = TengoPsiUtil.lhsIdentifiers(assign);
                    if (lhs.isEmpty()) continue;
                    TengoIdentifierExpr decl = lhs.get(0);
                    String name = decl.getName();
                    if (name == null) continue;
                    built.byName.put(name, decl);

                    if (TengoBuiltins.isStdlibModule(name)) {
                        TengoExpr rhs = TengoPsiUtil.firstRhs(assign);
                        TengoMapLiteral map = rhs instanceof TengoMapLiteral
                                ? (TengoMapLiteral) rhs
                                : PsiTreeUtil.findChildOfType(rhs, TengoMapLiteral.class);
                        if (map != null) {
                            for (TengoMapEntry entry : map.getMapEntryList()) {
                                String key = entry.getMapKey() == null ? null
                                        : TengoPsiUtil.unquote(entry.getMapKey().getText());
                                if (key != null) {
                                    built.stdlibMembers.put(name + "." + key, entry.getMapKey());
                                }
                            }
                        }
                    }
                }
            }
            state = built;
            return built;
        }
    }

    private static @NotNull String renderSource() {
        StringBuilder s = new StringBuilder();
        s.append("// Tengo built-in functions and standard-library modules.\n");
        s.append("// Synthesised by the idea-tengo-plugin to back navigation and Find Usages.\n\n");
        for (String name : TengoBuiltins.BUILTIN_FUNCTIONS) {
            TengoDocs.Entry e = TengoDocs.builtin(name);
            if (e != null) {
                s.append("// ").append(e.signature).append('\n');
                s.append("// ").append(e.summary).append('\n');
            }
            s.append(name).append(" := func() {}\n\n");
        }
        for (String mod : TengoBuiltins.STDLIB_MODULES) {
            String summary = TengoDocs.module(mod);
            if (summary != null) {
                s.append("// module ").append(mod).append('\n');
                s.append("// ").append(summary).append('\n');
            }
            s.append(mod).append(" := {\n");
            for (String member : TengoBuiltins.membersOf(mod)) {
                TengoDocs.Entry e = TengoDocs.member(mod, member);
                if (e != null) {
                    s.append("    // ").append(e.signature).append('\n');
                    s.append("    // ").append(e.summary).append('\n');
                }
                s.append("    ").append(member).append(": func() {},\n");
            }
            s.append("}\n\n");
        }
        return s.toString();
    }
}
