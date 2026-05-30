package com.github.blackcat.tengo;

import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.ParsingTestCase;
import com.github.blackcat.tengo.parser.TengoParserDefinition;

public class TengoParsingTest extends ParsingTestCase {

    public TengoParsingTest() {
        super("", "tengo", new TengoParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }

    public void testTrivial() {
        assertNoErrors("x := 1");
    }

    public void testImport() {
        assertNoErrors("fmt := import(\"fmt\")");
    }

    public void testCallAndSelector() {
        assertNoErrors("fmt := import(\"fmt\")\nfmt.println(\"hi\")\n");
    }

    public void testFuncLiteral() {
        assertNoErrors("add := func(a, b) { return a + b }\n");
    }

    public void testIfElse() {
        assertNoErrors("if x > 0 { return 1 } else { return 0 }\n");
    }

    public void testForC() {
        assertNoErrors("for i := 0; i < 10; i++ { x = x + i }\n");
    }

    public void testForIn() {
        assertNoErrors("for k, v in m { x = v }\n");
    }

    public void testArrayMapLiterals() {
        assertNoErrors("xs := [1, 2, 3]\nm := {a: 1, b: \"x\"}\n");
    }

    public void testOperators() {
        assertNoErrors("x := (1 + 2) * 3 - 4 / 2 % 1 == 0 && !false || true ? 1 : 0\n");
    }

    public void testExport() {
        assertNoErrors("export { area: 1, sum: 2 }\n");
    }

    public void testComprehensiveSample() {
        String source =
                "// import standard library modules\n" +
                "fmt := import(\"fmt\")\n" +
                "math := import(\"math\")\n" +
                "lib := import(\"./lib/util\")\n" +
                "\n" +
                "/* function literal */\n" +
                "area := func(r) {\n" +
                "    return math.pi * r * r\n" +
                "}\n" +
                "\n" +
                "sum := func(a, b, ...rest) {\n" +
                "    total := a + b\n" +
                "    for _, v in rest {\n" +
                "        total += v\n" +
                "    }\n" +
                "    return total\n" +
                "}\n" +
                "\n" +
                "values := [1, 2.0, 0x1F, 0o7, 0b101, \"hello\", `raw`, 'A', true, false, undefined]\n" +
                "m := {a: 1, b: \"x\", \"key\": 3.14}\n" +
                "\n" +
                "if x := 10; x > 5 {\n" +
                "    fmt.println(\"big\")\n" +
                "} else if x == 5 {\n" +
                "    fmt.println(\"medium\")\n" +
                "} else {\n" +
                "    fmt.println(\"small\")\n" +
                "}\n" +
                "\n" +
                "for i := 0; i < 10; i++ {\n" +
                "    if i % 2 == 0 { continue }\n" +
                "    if i == 7 { break }\n" +
                "}\n" +
                "\n" +
                "for v in values { fmt.println(v) }\n" +
                "for k, v in m { fmt.println(k, v) }\n" +
                "\n" +
                "x := (1 + 2) * 3 - 4 / 2 % 1\n" +
                "y := x << 2 | x >> 1 & 0xFF ^ 0\n" +
                "z := !true && false || x == 0\n" +
                "w := x > 0 ? \"pos\" : x < 0 ? \"neg\" : \"zero\"\n" +
                "\n" +
                "first := values[0]\n" +
                "head := values[:3]\n" +
                "tail := values[3:]\n" +
                "mid := values[1:4]\n" +
                "\n" +
                "export {\n" +
                "    area: area,\n" +
                "    sum: sum,\n" +
                "}\n";
        assertNoErrors(source);
    }

    private void assertNoErrors(String source) {
        PsiFile file = createPsiFile("snippet", source);
        ensureParsed(file);
        PsiErrorElement err = PsiTreeUtil.findChildOfType(file, PsiErrorElement.class);
        if (err != null) {
            fail("Parse error at " + err.getTextOffset() + ": " + err.getErrorDescription()
                    + "\n--- input ---\n" + source
                    + "\n--- tree ---\n" + com.intellij.psi.impl.DebugUtil.psiToString(file, true));
        }
    }
}
