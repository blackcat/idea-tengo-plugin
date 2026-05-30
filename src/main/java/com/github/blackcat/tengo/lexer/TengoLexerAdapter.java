package com.github.blackcat.tengo.lexer;

import com.intellij.lexer.FlexAdapter;

public class TengoLexerAdapter extends FlexAdapter {
    public TengoLexerAdapter() {
        super(new _TengoLexer(null));
    }
}
