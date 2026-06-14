package com.moon.vault.util;

import java.util.UUID;

public final class CodeGenerator {

    private CodeGenerator() {
    }

    public static String generate(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
