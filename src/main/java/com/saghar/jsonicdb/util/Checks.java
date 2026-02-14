package com.saghar.jsonicdb.util;

public final class Checks {
    private Checks() {
    }

    public static void require(boolean condition, String message) {
        if (!condition) throw new JsonicException(message);
    }
}
