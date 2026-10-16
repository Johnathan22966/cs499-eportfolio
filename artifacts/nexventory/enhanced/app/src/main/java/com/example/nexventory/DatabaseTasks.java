package com.example.nexventory;

import android.app.Activity;
import android.widget.Toast;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Serial background work, followed by a callback on the screen's UI thread. */
public final class DatabaseTasks {
    public interface Callback<T> { void accept(T value); }
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private DatabaseTasks() {}
    public static <T> void run(Activity screen, Callable<T> task, Callback<T> success) {
        run(screen, task, success, message -> Toast.makeText(screen, message, Toast.LENGTH_LONG).show());
    }
    public static <T> void run(Activity screen, Callable<T> task, Callback<T> success, Callback<String> failure) {
        WORKER.execute(() -> {
            try {
                T result = task.call();
                screen.runOnUiThread(() -> { if (!screen.isFinishing() && !screen.isDestroyed()) success.accept(result); });
            } catch (Exception error) {
                String message = error instanceof IllegalArgumentException ? error.getMessage()
                        : "Unable to access saved inventory. No incomplete import was saved.";
                screen.runOnUiThread(() -> { if (!screen.isFinishing() && !screen.isDestroyed()) failure.accept(message); });
            }
        });
    }
}
