package com.github.blackcat.tengo;

import com.github.blackcat.tengo.inspections.TengoShadowedNameInspection;
import com.github.blackcat.tengo.inspections.TengoUnreachableCodeInspection;
import com.github.blackcat.tengo.inspections.TengoUnusedParameterInspection;
import com.github.blackcat.tengo.inspections.TengoUnusedVariableInspection;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

public class TengoInspectionTest extends BasePlatformTestCase {

    private List<HighlightInfo> highlightsWith(Class<? extends LocalInspectionTool> inspection, String source) {
        myFixture.enableInspections(inspection);
        myFixture.configureByText("a.tengo", source);
        return myFixture.doHighlighting();
    }

    private long countDescriptionContains(List<HighlightInfo> highlights, String substring) {
        return highlights.stream()
                .filter(h -> h.getDescription() != null && h.getDescription().contains(substring))
                .count();
    }

    public void testUnusedVariable_doesNotFlagReferenced() {
        var hl = highlightsWith(TengoUnusedVariableInspection.class,
                "x := 1\n" +
                "y := 2\n" +
                "z := x + y\n" +
                "fmt := import(\"fmt\")\n" +
                "fmt.println(z)\n");
        assertEquals(0, countDescriptionContains(hl, "is never used"));
    }

    public void testUnusedVariable_flagsTrulyUnused() {
        var hl = highlightsWith(TengoUnusedVariableInspection.class,
                "x := 1\n" +
                "y := 2\n");
        assertEquals(2, countDescriptionContains(hl, "is never used"));
    }

    public void testUnusedVariable_skipsUnderscore() {
        var hl = highlightsWith(TengoUnusedVariableInspection.class, "_ := 1\n");
        assertEquals(0, countDescriptionContains(hl, "is never used"));
    }

    public void testUnusedParameter_flagsUnreadParam() {
        var hl = highlightsWith(TengoUnusedParameterInspection.class,
                "f := func(a, b) { return a }\n");
        assertEquals(1, countDescriptionContains(hl, "Parameter 'b' is never used"));
    }

    public void testUnusedParameter_skipsUnderscorePrefix() {
        var hl = highlightsWith(TengoUnusedParameterInspection.class,
                "f := func(_unused, used) { return used }\n");
        assertEquals(0, countDescriptionContains(hl, "is never used"));
    }

    public void testUnusedParameter_flagsForInVar() {
        var hl = highlightsWith(TengoUnusedParameterInspection.class,
                "fmt := import(\"fmt\")\n" +
                "m := {a: 1}\n" +
                "for k, v in m { fmt.println(v) }\n");
        assertEquals(1, countDescriptionContains(hl, "Loop variable 'k' is never used"));
    }

    public void testShadowedName_warnsInnerRedeclaration() {
        var hl = highlightsWith(TengoShadowedNameInspection.class,
                "x := 1\n" +
                "f := func() {\n" +
                "    x := 2\n" +
                "    return x\n" +
                "}\n" +
                "y := f() + x\n");
        assertEquals(1, countDescriptionContains(hl, "shadows a declaration"));
    }

    public void testShadowedName_doesNotWarnSameScope() {
        var hl = highlightsWith(TengoShadowedNameInspection.class,
                "x := 1\n" +
                "y := x\n");
        assertEquals(0, countDescriptionContains(hl, "shadows a declaration"));
    }

    public void testUnreachableCode_afterReturn() {
        var hl = highlightsWith(TengoUnreachableCodeInspection.class,
                "f := func() {\n" +
                "    return 1\n" +
                "    x := 2\n" +
                "    return x\n" +
                "}\n");
        assertEquals(1, countDescriptionContains(hl, "Unreachable code"));
    }

    public void testUnreachableCode_afterBreak() {
        var hl = highlightsWith(TengoUnreachableCodeInspection.class,
                "for {\n" +
                "    break\n" +
                "    x := 1\n" +
                "}\n");
        assertEquals(1, countDescriptionContains(hl, "Unreachable code"));
    }

    public void testUnreachableCode_clean() {
        var hl = highlightsWith(TengoUnreachableCodeInspection.class,
                "f := func() {\n" +
                "    x := 1\n" +
                "    return x\n" +
                "}\n");
        assertEquals(0, countDescriptionContains(hl, "Unreachable code"));
    }
}
