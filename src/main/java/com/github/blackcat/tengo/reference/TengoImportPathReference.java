package com.github.blackcat.tengo.reference;

import com.github.blackcat.tengo.TengoFile;
import com.github.blackcat.tengo.psi.TengoImportPath;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TengoImportPathReference extends PsiReferenceBase<TengoImportPath> {

    public TengoImportPathReference(@NotNull TengoImportPath path) {
        super(path, innerStringRange(path));
    }

    /** Compute the inner-string-content range relative to the TengoImportPath element.
     *  TengoImportPath wraps just the string literal (with quotes), so the inner range
     *  drops the leading and trailing quote characters. */
    private static TextRange innerStringRange(@NotNull TengoImportPath path) {
        int len = path.getTextLength();
        if (len <= 2) return TextRange.from(0, len);
        return TextRange.from(1, len - 2);
    }

    @Override
    public @Nullable PsiElement resolve() {
        String path = getValue();
        if (path.isEmpty()) return null;
        PsiFile containing = myElement.getContainingFile();
        if (containing == null) return null;
        VirtualFile from = containing.getVirtualFile();
        if (from == null) return null;
        VirtualFile target = resolvePath(from, path);
        if (target == null) return null;
        PsiFile psi = PsiManager.getInstance(myElement.getProject()).findFile(target);
        return psi instanceof TengoFile ? psi : null;
    }

    /**
     * Tengo import path resolution supports three forms:
     *   - `:<name>`             → sibling lookup `<name>.lib.tengo` (then `.tengo`) in the
     *                              current file's directory, then walking up the source tree.
     *   - `./<path>` / `../<path>` / `<rel>` → standard relative file lookup.
     *   - bare `<name>` matching a Tengo stdlib module is handled elsewhere.
     */
    static @Nullable VirtualFile resolvePath(@NotNull VirtualFile from, @NotNull String path) {
        VirtualFile dir = from.getParent();
        if (dir == null) return null;

        if (path.startsWith(":")) {
            return resolveColonLibrary(dir, path.substring(1));
        }

        String[] parts = path.split("/");
        VirtualFile current = dir;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                current = current.getParent();
                if (current == null) return null;
                continue;
            }
            boolean last = (i == parts.length - 1);
            VirtualFile next = current.findChild(part);
            if (last && next == null) {
                next = findTengoFile(current, part);
            }
            if (next == null) return null;
            current = next;
        }
        return current.isDirectory() ? null : current;
    }

    /** Walk up from {@code dir} looking for `<name>.lib.tengo` (preferred) or `<name>.tengo`. */
    private static @Nullable VirtualFile resolveColonLibrary(@NotNull VirtualFile dir, @NotNull String name) {
        VirtualFile current = dir;
        while (current != null) {
            VirtualFile hit = findTengoFile(current, name);
            if (hit != null) return hit;
            current = current.getParent();
        }
        return null;
    }

    /** Try `<name>.lib.tengo`, then `<name>.tengo`, then `<name>` (if already complete). */
    private static @Nullable VirtualFile findTengoFile(@NotNull VirtualFile dir, @NotNull String name) {
        VirtualFile hit = dir.findChild(name + ".lib.tengo");
        if (hit != null && !hit.isDirectory()) return hit;
        hit = dir.findChild(name + ".tengo");
        if (hit != null && !hit.isDirectory()) return hit;
        hit = dir.findChild(name);
        if (hit != null && !hit.isDirectory()) return hit;
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return EMPTY_ARRAY;
    }
}
