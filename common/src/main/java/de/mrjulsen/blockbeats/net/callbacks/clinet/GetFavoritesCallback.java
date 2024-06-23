package de.mrjulsen.blockbeats.net.callbacks.clinet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.mrjulsen.dragnsounds.api.Api;

public class GetFavoritesCallback {
    private static final Map<Long, IGetFavoritesCallback> callbacks = new HashMap<>();

    public static long create(IGetFavoritesCallback callback) {
        long id;
        do {
            id = Api.id();
        } while (callbacks.containsKey(id));
        callbacks.put(id, callback);
        return id;
    }

    public static void run(long id, Set<String> favoritePaths, Map<UUID, String> usernamecache) {
        if (callbacks.containsKey(id)) {
            callbacks.remove(id).run(favoritePaths, usernamecache);
        }
    }

    @FunctionalInterface
    public static interface IGetFavoritesCallback {
        void run(Set<String> favoritePaths, Map<UUID, String> usernamecache);
    }
}
