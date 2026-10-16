package com.example.nexventory;

import java.security.SecureRandom;
import android.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
    private static final int ITERATIONS = 120000;
    private PasswordHasher() {}

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return ITERATIONS + ":" + Base64.encodeToString(salt, Base64.NO_WRAP) + ":"
                + Base64.encodeToString(derive(password, salt, ITERATIONS), Base64.NO_WRAP);
    }

    public static boolean matches(String password, String stored) {
        try {
            String[] parts = stored.split(":", 3);
            if (parts.length != 3) return false;
            int rounds = Integer.parseInt(parts[0]);
            if (rounds < 10000 || rounds > 1000000) return false;
            byte[] salt = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] expected = Base64.decode(parts[2], Base64.NO_WRAP);
            byte[] actual = derive(password, salt, rounds);
            return java.security.MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException error) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int rounds) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        } catch (Exception error) {
            throw new IllegalStateException("Password hashing unavailable", error);
        } finally {
            spec.clearPassword();
        }
    }
}
