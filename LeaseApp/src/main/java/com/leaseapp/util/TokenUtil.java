package com.leaseapp.util;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    // Generates a URL-safe, unguessable token used in the tenant's secure email link
    public static String generateSecureToken() {
        byte[] bytes = new byte[32]; // 256 bits
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}