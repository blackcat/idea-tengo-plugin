package com.github.blackcat.tengo.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.github.blackcat.tengo.psi.TengoTypes;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%public
%class _TengoLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%{
  public _TengoLexer() {
    this((java.io.Reader) null);
  }
%}

EOL             = \r|\n|\r\n
WHITE_CHAR      = [ \t\f]
WS              = ({EOL}|{WHITE_CHAR})+

LINE_COMMENT    = "//" [^\r\n]*
BLOCK_COMMENT   = "/*" ~"*/"

DEC_INT         = [0-9]+
HEX_INT         = 0[xX][0-9a-fA-F]+
OCT_INT         = 0[oO][0-7]+
BIN_INT         = 0[bB][01]+

FLOAT_LIT       = ([0-9]+ "." [0-9]+ ([eE][+-]?[0-9]+)?) | ([0-9]+ [eE][+-]?[0-9]+)

ESCAPE          = \\ ([abfnrtv\\\'\"\/0] | x[0-9a-fA-F]{2} | u[0-9a-fA-F]{4} | U[0-9a-fA-F]{8} | [0-7]{3})
STRING_LIT      = \" ({ESCAPE} | [^\\\"\r\n])* \"
RAW_STRING_LIT  = \` [^\`]* \`
CHAR_LIT        = \' ({ESCAPE} | [^\\\'\r\n]) \'

IDENT           = [A-Za-z_][A-Za-z0-9_]*

%%

<YYINITIAL> {
  {WS}                    { return WHITE_SPACE; }

  {LINE_COMMENT}          { return TengoTypes.LINE_COMMENT; }
  {BLOCK_COMMENT}         { return TengoTypes.BLOCK_COMMENT; }

  "if"                    { return TengoTypes.IF; }
  "else"                  { return TengoTypes.ELSE; }
  "for"                   { return TengoTypes.FOR; }
  "in"                    { return TengoTypes.IN; }
  "return"                { return TengoTypes.RETURN; }
  "break"                 { return TengoTypes.BREAK; }
  "continue"              { return TengoTypes.CONTINUE; }
  "func"                  { return TengoTypes.FUNC; }
  "import"                { return TengoTypes.IMPORT; }
  "export"                { return TengoTypes.EXPORT; }
  "true"                  { return TengoTypes.TRUE; }
  "false"                 { return TengoTypes.FALSE; }
  "undefined"             { return TengoTypes.UNDEFINED; }

  ":="                    { return TengoTypes.DEFINE; }
  "+="                    { return TengoTypes.PLUS_ASSIGN; }
  "-="                    { return TengoTypes.MINUS_ASSIGN; }
  "*="                    { return TengoTypes.MUL_ASSIGN; }
  "/="                    { return TengoTypes.DIV_ASSIGN; }
  "%="                    { return TengoTypes.MOD_ASSIGN; }
  "&^="                   { return TengoTypes.BIT_AND_NOT_ASSIGN; }
  "&="                    { return TengoTypes.BIT_AND_ASSIGN; }
  "|="                    { return TengoTypes.BIT_OR_ASSIGN; }
  "^="                    { return TengoTypes.BIT_XOR_ASSIGN; }
  "<<="                   { return TengoTypes.SHL_ASSIGN; }
  ">>="                   { return TengoTypes.SHR_ASSIGN; }
  "=="                    { return TengoTypes.EQ; }
  "!="                    { return TengoTypes.NEQ; }
  "<="                    { return TengoTypes.LE; }
  ">="                    { return TengoTypes.GE; }
  "<<"                    { return TengoTypes.SHL; }
  ">>"                    { return TengoTypes.SHR; }
  "&&"                    { return TengoTypes.LAND; }
  "||"                    { return TengoTypes.LOR; }
  "++"                    { return TengoTypes.INC; }
  "--"                    { return TengoTypes.DEC; }
  "&^"                    { return TengoTypes.BIT_AND_NOT; }
  "..."                   { return TengoTypes.ELLIPSIS; }

  "="                     { return TengoTypes.ASSIGN; }
  "<"                     { return TengoTypes.LT; }
  ">"                     { return TengoTypes.GT; }
  "!"                     { return TengoTypes.NOT; }
  "+"                     { return TengoTypes.PLUS; }
  "-"                     { return TengoTypes.MINUS; }
  "*"                     { return TengoTypes.MUL; }
  "/"                     { return TengoTypes.DIV; }
  "%"                     { return TengoTypes.MOD; }
  "&"                     { return TengoTypes.BIT_AND; }
  "|"                     { return TengoTypes.BIT_OR; }
  "^"                     { return TengoTypes.BIT_XOR; }

  "("                     { return TengoTypes.LPAREN; }
  ")"                     { return TengoTypes.RPAREN; }
  "{"                     { return TengoTypes.LBRACE; }
  "}"                     { return TengoTypes.RBRACE; }
  "["                     { return TengoTypes.LBRACK; }
  "]"                     { return TengoTypes.RBRACK; }
  ","                     { return TengoTypes.COMMA; }
  ";"                     { return TengoTypes.SEMICOLON; }
  ":"                     { return TengoTypes.COLON; }
  "."                     { return TengoTypes.DOT; }
  "?"                     { return TengoTypes.QUESTION; }

  {FLOAT_LIT}             { return TengoTypes.FLOAT_LITERAL; }
  {HEX_INT}               { return TengoTypes.HEX_LITERAL; }
  {OCT_INT}               { return TengoTypes.OCT_LITERAL; }
  {BIN_INT}               { return TengoTypes.BIN_LITERAL; }
  {DEC_INT}               { return TengoTypes.INTEGER_LITERAL; }

  {STRING_LIT}            { return TengoTypes.STRING_LITERAL; }
  {RAW_STRING_LIT}        { return TengoTypes.RAW_STRING_LITERAL; }
  {CHAR_LIT}              { return TengoTypes.CHAR_LITERAL; }

  {IDENT}                 { return TengoTypes.IDENTIFIER; }
}

[^]                       { return BAD_CHARACTER; }
