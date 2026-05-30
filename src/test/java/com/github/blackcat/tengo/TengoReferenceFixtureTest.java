package com.github.blackcat.tengo;

import com.github.blackcat.tengo.psi.TengoIdentifierExpr;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end reference + rename tests running on top of the real IntelliJ Platform
 * (BasePlatformTestCase loads every extension declared in plugin.xml).
 */
public class TengoReferenceFixtureTest extends BasePlatformTestCase {

    public void testUsageResolvesToTopLevelDeclaration() {
        PsiFile file = myFixture.configureByText("a.tengo", "foo := 1\nbar := foo + 2\n");
        List<TengoIdentifierExpr> ids = collect(file);
        // Order: foo(decl), bar(decl), foo(usage)
        assertEquals(3, ids.size());

        TengoIdentifierExpr decl = ids.get(0);
        TengoIdentifierExpr usage = ids.get(2);
        assertEquals("foo", usage.getText());

        // A usage must NOT advertise itself as a named declaration.
        assertNull("usage's getNameIdentifier() must be null so the IDE doesn't treat it as a declaration",
                ((PsiNameIdentifierOwner) usage).getNameIdentifier());

        // It MUST produce a non-empty reference that resolves to the declaration.
        PsiReference ref = usage.getReference();
        assertNotNull("usage.getReference() returned null - reference contributor is not wired", ref);
        PsiElement resolved = ref.resolve();
        assertNotNull("usage.getReference().resolve() returned null - multiResolve failed", resolved);
        assertSame("resolved target must be the LHS declaration", decl, resolved);
    }

    public void testFunctionParameterResolvesFromBody() {
        PsiFile file = myFixture.configureByText("a.tengo",
                "add := func(a, b) {\n" +
                "    return a + b\n" +
                "}\n");
        List<TengoIdentifierExpr> ids = collect(file);
        // ids: add(decl), a(usage in body), b(usage in body)
        TengoIdentifierExpr aUsage = ids.stream()
                .filter(id -> "a".equals(id.getText()))
                .findFirst().orElseThrow();
        PsiReference ref = aUsage.getReference();
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull("parameter usage must resolve to its TengoParam declaration", resolved);
        assertTrue("resolved element must be a named element",
                resolved instanceof PsiNamedElement);
        assertEquals("a", ((PsiNamedElement) resolved).getName());
    }

    public void testForInVarResolvesFromBody() {
        PsiFile file = myFixture.configureByText("a.tengo",
                "xs := [1, 2, 3]\n" +
                "for _, v in xs {\n" +
                "    y := v\n" +
                "}\n");
        TengoIdentifierExpr vUsage = collect(file).stream()
                .filter(id -> "v".equals(id.getText()))
                .findFirst().orElseThrow();
        PsiReference ref = vUsage.getReference();
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull("for-in var usage must resolve", resolved);
        assertEquals("v", ((PsiNamedElement) resolved).getName());
    }

    public void testRenamePropagatesAcrossUsages() {
        myFixture.configureByText("a.tengo",
                "fo<caret>o := 1\n" +
                "bar := foo + foo\n");
        myFixture.renameElementAtCaret("renamed");
        myFixture.checkResult(
                "renamed := 1\n" +
                "bar := renamed + renamed\n");
    }

    public void testRenameFromUsageAlsoUpdatesDeclaration() {
        myFixture.configureByText("a.tengo",
                "foo := 1\n" +
                "bar := fo<caret>o + 2\n");
        myFixture.renameElementAtCaret("renamed");
        myFixture.checkResult(
                "renamed := 1\n" +
                "bar := renamed + 2\n");
    }

    public void testForwardScopingBlocksLaterDeclarations() {
        // In a top-level file Tengo allows forward refs; this test is about
        // the LOCAL scope rule inside a function block.
        PsiFile file = myFixture.configureByText("a.tengo",
                "fn := func() {\n" +
                "    x\n" +              // usage before declaration
                "    x := 1\n" +
                "    return x\n" +
                "}\n");
        List<TengoIdentifierExpr> ids = collect(file);
        TengoIdentifierExpr early = ids.stream()
                .filter(id -> "x".equals(id.getText()))
                .findFirst().orElseThrow();
        PsiReference ref = early.getReference();
        // multiResolve should return empty since x is not declared before this usage.
        assertNotNull(ref);
        assertNull("usage before declaration should NOT resolve under block-scope rules",
                ref.resolve());
    }

    public void testColonLibraryImportResolvesSiblingFile() {
        myFixture.addFileToProject("maps.lib.tengo",
                "getKeys := func(m) { return [] }\n" +
                "export { getKeys: getKeys }\n");
        myFixture.configureByText("canonical.lib.tengo",
                "maps := import(\":maps\")\n" +
                "x := maps.getKeys({a: 1})\n");

        var member = PsiTreeUtil.findChildrenOfType(myFixture.getFile(),
                com.github.blackcat.tengo.psi.TengoIdentifierRef.class).iterator().next();
        PsiReference ref = member.getReference();
        assertNotNull("member reference must exist", ref);
        PsiElement resolved = ref.resolve();
        assertNotNull("`getKeys` after `maps.` must resolve into maps.lib.tengo", resolved);
        assertEquals("getKeys", resolved.getText());
    }

    public void testColonImportPathReferenceItselfResolves() {
        myFixture.addFileToProject("maps.lib.tengo", "export {}\n");
        myFixture.configureByText("canonical.lib.tengo",
                "maps := import(\":maps\")\n");
        var importPath = PsiTreeUtil.findChildOfType(myFixture.getFile(),
                com.github.blackcat.tengo.psi.TengoImportPath.class);
        assertNotNull(importPath);
        PsiReference[] refs = importPath.getReferences();
        assertTrue("import_path should expose a path reference", refs.length > 0);
        PsiElement target = refs[0].resolve();
        assertNotNull("`:maps` must resolve to maps.lib.tengo", target);
        assertEquals("maps.lib.tengo", ((PsiFile) target).getName());
    }

    public void testBuiltinUsageResolvesToSyntheticDeclaration() {
        PsiFile file = myFixture.configureByText("a.tengo",
                "x := [1, 2]\n" +
                "y := is_map(x)\n");
        TengoIdentifierExpr usage = collect(file).stream()
                .filter(id -> "is_map".equals(id.getText()))
                .findFirst().orElseThrow();
        PsiReference ref = usage.getReference();
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull("`is_map` usage must resolve to its synthetic builtin declaration", resolved);
        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("is_map", ((PsiNamedElement) resolved).getName());
    }

    /** Mirrors what Alt+F7 actually invokes (TargetElementUtil → FindUsagesManager).
     *  ReferencesSearch alone bypasses the canFindUsagesFor / handler-factory gates. */
    public void testAltF7OnLocalImportedModule() {
        myFixture.configureByText("a.tengo",
                "json := import(\"json\")\n" +
                "a := json.encode(1)\n" +
                "b := json.decode(2)\n");
        // Place caret on the declaration LHS.
        myFixture.getEditor().getCaretModel().moveToOffset(0);
        var usages = myFixture.findUsages(myFixture.getElementAtCaret());
        assertEquals("Alt+F7 on `json` decl must list its 2 usages",
                2, usages.size());
    }

    /** Mirrors canonical.lib.tengo: a local `encode` function that internally calls
     *  `json.encode`. Alt+F7 on json.encode must return ONLY the json.encode calls,
     *  not the local declaration. */
    public void testAltF7OnStdlibMember_AlongsideLocalShadow() {
        myFixture.configureByText("a.tengo",
                "json := import(\"json\")\n" +
                "encode := func(obj) {\n" +
                "    return json.encode(obj)\n" +
                "}\n" +
                "x := encode(1)\n" +
                "y := json.encode(2)\n");
        int offset = myFixture.getFile().getText().indexOf("json.encode") + "json.".length();
        myFixture.getEditor().getCaretModel().moveToOffset(offset);
        var usages = myFixture.findUsages(myFixture.getElementAtCaret());
        assertEquals("Alt+F7 on `json.encode` must find both json.encode call sites",
                2, usages.size());
    }

    /** Verify references-search vs find-usages independently: separates "did resolution
     *  succeed for each usage" from "did the find-usages scope find them". */
    public void testStdlibMemberReferencesResolveToSyntheticTarget() {
        myFixture.configureByText("a.tengo",
                "json := import(\"json\")\n" +
                "a := json.encode(1)\n" +
                "b := json.encode(3)\n");
        PsiElement target = com.github.blackcat.tengo.TengoBuiltinDeclarations
                .getInstance(getProject()).getStdlibMember("json", "encode");
        assertNotNull("synthetic json.encode target must exist", target);

        // Manually iterate every TengoIdentifierRef named `encode` in the test file
        // and assert its reference reports isReferenceTo(target)=true.
        int hits = 0;
        for (var ref : PsiTreeUtil.findChildrenOfType(myFixture.getFile(),
                com.github.blackcat.tengo.psi.TengoIdentifierRef.class)) {
            if (!"encode".equals(ref.getText())) continue;
            PsiReference psiRef = ref.getReference();
            assertNotNull(psiRef);
            assertTrue("usage at offset " + ref.getTextOffset() + " must isReferenceTo synthetic target",
                    psiRef.isReferenceTo(target));
            hits++;
        }
        assertEquals(2, hits);

        // And ReferencesSearch with an explicit project scope should also see them.
        var refs = com.intellij.psi.search.searches.ReferencesSearch.search(target,
                com.intellij.psi.search.GlobalSearchScope.projectScope(getProject())).findAll();
        assertEquals("ReferencesSearch with project scope must find 2 usages",
                2, refs.size());
    }

    public void testAltF7OnStdlibMember_FromLocalImportedReceiver() {
        myFixture.configureByText("a.tengo",
                "json := import(\"json\")\n" +
                "a := json.encode(1)\n" +
                "b := json.decode(2)\n" +
                "c := json.encode(3)\n");
        // Place caret right on `encode` of the first call.
        int offset = myFixture.getFile().getText().indexOf("encode");
        assertTrue(offset > 0);
        myFixture.getEditor().getCaretModel().moveToOffset(offset);
        var usages = myFixture.findUsages(myFixture.getElementAtCaret());
        assertEquals("Alt+F7 on `encode` (json stdlib member) must find both call sites",
                2, usages.size());
    }

    public void testStdlibImportedAsLocalVariable_FindUsagesAndMemberResolve() {
        PsiFile file = myFixture.configureByText("a.tengo",
                "json := import(\"json\")\n" +
                "a := json.encode(1)\n" +
                "b := json.decode(\"x\")\n");
        List<TengoIdentifierExpr> ids = collect(file);
        // ids: json(decl), a(decl), json(usage1), b(decl), json(usage2)
        TengoIdentifierExpr decl = ids.stream()
                .filter(id -> "json".equals(id.getText()))
                .findFirst().orElseThrow();
        // Usages collected via ReferencesSearch — Find Usages uses this same path.
        var refs = com.intellij.psi.search.searches.ReferencesSearch.search(decl).findAll();
        assertEquals("Find Usages on `json` should see both `json.encode` and `json.decode`",
                2, refs.size());

        // And `encode` itself should resolve to the stdlib synthetic member.
        TengoIdentifierExpr encodeUsage = ids.stream()
                .filter(id -> "encode".equals(id.getText()))
                .findFirst().orElse(null);
        // encode is a selector member, not a top-level identifier — fetch via IdentifierRef.
        com.github.blackcat.tengo.psi.TengoIdentifierRef encodeRef = com.intellij.psi.util.PsiTreeUtil
                .findChildrenOfType(file, com.github.blackcat.tengo.psi.TengoIdentifierRef.class).stream()
                .filter(r -> "encode".equals(r.getText()))
                .findFirst().orElseThrow();
        PsiReference ref = encodeRef.getReference();
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull("`json.encode` (where json is bound to stdlib import) must resolve", resolved);
    }

    public void testBuiltinSyntheticDeclIsNavigable() {
        myFixture.configureByText("a.tengo", "y := is_map(1)\n");
        TengoIdentifierExpr usage = collect(myFixture.getFile()).stream()
                .filter(id -> "is_map".equals(id.getText()))
                .findFirst().orElseThrow();
        PsiElement resolved = usage.getReference().resolve();
        assertNotNull(resolved);
        assertTrue("synthetic builtin decl must be Navigatable",
                resolved instanceof com.intellij.pom.Navigatable);
        com.intellij.pom.Navigatable nav = (com.intellij.pom.Navigatable) resolved;
        assertTrue("synthetic builtin decl must report canNavigate()=true",
                nav.canNavigate());
    }

    private static List<TengoIdentifierExpr> collect(PsiFile file) {
        return new ArrayList<>(PsiTreeUtil.findChildrenOfType(file, TengoIdentifierExpr.class));
    }
}
