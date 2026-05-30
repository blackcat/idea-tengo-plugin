package com.github.blackcat.tengo;

import com.intellij.testFramework.LexerTestCase;
import com.github.blackcat.tengo.lexer.TengoLexerAdapter;

public class TengoLexerTest extends LexerTestCase {

    @Override
    protected TengoLexerAdapter createLexer() {
        return new TengoLexerAdapter();
    }

    @Override
    protected String getDirPath() {
        return "src/test/testData/lexer";
    }

    public void testKeywords() {
        doTest("if else for in return break continue func import export true false undefined",
                "TengoTokenType.if ('if')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.else ('else')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.for ('for')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.in ('in')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.return ('return')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.break ('break')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.continue ('continue')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.func ('func')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.import ('import')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.export ('export')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.true ('true')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.false ('false')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.undefined ('undefined')");
    }

    public void testNumbers() {
        doTest("42 3.14 0xFF 0o17 0b1010 1e6",
                "TengoTokenType.INTEGER_LITERAL ('42')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.FLOAT_LITERAL ('3.14')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.HEX_LITERAL ('0xFF')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.OCT_LITERAL ('0o17')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.BIN_LITERAL ('0b1010')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.FLOAT_LITERAL ('1e6')");
    }

    public void testStrings() {
        doTest("\"hello\" `raw\\nstring` 'A'",
                "TengoTokenType.STRING_LITERAL ('\"hello\"')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.RAW_STRING_LITERAL ('`raw\\nstring`')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.CHAR_LITERAL (''A'')");
    }

    public void testCompoundOperators() {
        doTest(":= += -= *= /= %= &= |= ^= &^= <<= >>= == != <= >= && || << >> &^ ++ -- ...",
                "TengoTokenType.:= (':=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.+= ('+=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.-= ('-=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.*= ('*=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType./= ('/=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.%= ('%=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.&= ('&=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.|= ('|=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.^= ('^=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.&^= ('&^=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.<<= ('<<=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.>>= ('>>=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.== ('==')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.!= ('!=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.<= ('<=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.>= ('>=')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.&& ('&&')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.|| ('||')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.<< ('<<')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.>> ('>>')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.&^ ('&^')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.++ ('++')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.-- ('--')\n" +
                "WHITE_SPACE (' ')\n" +
                "TengoTokenType.... ('...')");
    }

    public void testComments() {
        doTest("// line comment\n/* block\ncomment */",
                "TengoTokenType.LINE_COMMENT ('// line comment')\n" +
                "WHITE_SPACE ('\\n')\n" +
                "TengoTokenType.BLOCK_COMMENT ('/* block\\ncomment */')");
    }
}
