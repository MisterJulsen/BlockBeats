package de.mrjulsen.blockbeats.net.callbacks.clinet;

import java.util.HashMap;
import java.util.Map;

import de.mrjulsen.dragnsounds.api.Api;

public class ManageFavoritesCallback {
    private static final Map<Long, Runnable> callbacks = new HashMap<>();

    public static long create(Runnable callback) {
        long id;
        do {
            id = Api.id();
        } while (callbacks.containsKey(id));
        callbacks.put(id, callback);
        return id;
    }

    public static void run(long id) {
        if (callbacks.containsKey(id)) {
            callbacks.remove(id).run();
        }
    }
}
