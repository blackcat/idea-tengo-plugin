package com.github.blackcat.tengo;

import com.intellij.lang.Language;

public final class TengoLanguage extends Language {
    public static final TengoLanguage INSTANCE = new TengoLanguage();

    private TengoLanguage() {
        super("Tengo");
    }
}
