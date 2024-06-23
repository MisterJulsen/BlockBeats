package de.mrjulsen.blockbeats.net.callbacks.clinet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.api.Api;

public class GetUsernameCacheCallback {
    private static final Map<Long, Consumer<Map<UUID, String>>> callbacks = new HashMap<>();

    public static long create(Consumer<Map<UUID, String>> callback) {
        long id;
        do {
            id = Api.id();
        } while (callbacks.containsKey(id));
        callbacks.put(id, callback);
        return id;
    }

    public static void run(long id, Map<UUID, String> usernamecache) {
        if (callbacks.containsKey(id)) {
            callbacks.remove(id).accept(usernamecache);
        }
    }
}
