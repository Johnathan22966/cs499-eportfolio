package com.example.nexventory;

import android.content.Context;
import android.content.Intent;

/** Keeps account access separate from screen navigation. */
public final class SessionManager {
    private static final String FILE = "account_session";
    private static final String KEY = "signed_in";
    private final Context context;

    public SessionManager(Context context) { this.context = context; }

    public boolean isSignedIn() {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY, false);
    }

    public void signIn() {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY, true).apply();
    }

    public void signOut() {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().remove(KEY).apply();
        Intent login = new Intent(context, LoginActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(login);
    }

    public boolean requireSignIn() {
        if (isSignedIn()) return true;
        Intent login = new Intent(context, LoginActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(login);
        return false;
    }
}
