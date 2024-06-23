package de.mrjulsen.blockbeats.core.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;

public final class SharingUtils {
    public static record ShareData(UUID playerId, Map<String, String> meta) {}

    public static Map<UUID, ShareData> deserialize(SoundFile file) {
        try {
            String[] shareEntries = file.getMetadataSafe(BlockBeats.META_SHARED).split(BlockBeats.META_SHARED_SEPARATOR);
            Map<UUID, ShareData> sharing = new LinkedHashMap<>(shareEntries.length);
            for (String str : shareEntries) {
                if (str.isBlank()) {
                    continue;
                }

                String[] data = str.split(BlockBeats.META_SHARE_PROPERTIES_SEPARATOR);
                UUID playerId = UUID.fromString(data[0]);
                Map<String, String> meta = new LinkedHashMap<>();
                for (int i = 1; i < data.length; i++) {
                    String[] kv = data[i].split(BlockBeats.PROPERTIES_SEPARATOR);
                    if (kv.length <= 1) {
                        BlockBeats.LOGGER.warn("'" + data[i] + "' is no valid sharing property.");
                        continue;
                    }
                    meta.put(kv[0], kv[1]);
                }
                sharing.put(playerId, new ShareData(playerId, meta));
            }
            return sharing;
        } catch (Exception e) {
            BlockBeats.LOGGER.error("Unable to process sharing text: " + file.getMetadataSafe(BlockBeats.META_SHARED), e);
            return Map.of();
        }
    }

    public static void shareWith(Map<UUID, ShareData> data, UUID playerId, Map<String, String> initialMeta) {
        ShareData instance = data.computeIfAbsent(playerId, x -> new ShareData(playerId, new LinkedHashMap<>()));
        instance.meta().putAll(initialMeta);
    }

    public static void stopSharingWith(Map<UUID, ShareData> data, UUID playerId) {
        data.remove(playerId);
    }

    public static void addOrUpdateMeta(Map<UUID, ShareData> data, UUID playerId, Map<String, String> meta) {
        ShareData instance = data.computeIfAbsent(playerId, x -> new ShareData(playerId, new LinkedHashMap<>()));
        meta.entrySet().forEach(x -> {
            if (instance.meta().containsKey(x.getKey())) {
                instance.meta().remove(x.getKey());
            }
        });
        instance.meta().putAll(meta);
    }

    public static void removeMeta(Map<UUID, ShareData> data, UUID playerId, Set<String> keys) {
        ShareData instance = data.computeIfAbsent(playerId, x -> new ShareData(playerId, new LinkedHashMap<>()));
        keys.forEach(k -> instance.meta().remove(k));
    }

    public static String getMetaSafe(Map<UUID, ShareData> data, UUID playerId, String key) {
        ShareData instance = data.computeIfAbsent(playerId, x -> new ShareData(playerId, new LinkedHashMap<>()));
        return instance.meta().containsKey(key) ? instance.meta().get(key) : "";
    }

    public static String serialize(Map<UUID, ShareData> data) {
        return data.entrySet().stream().map(x -> 
            x.getKey().toString() + BlockBeats.META_SHARE_PROPERTIES_SEPARATOR + x.getValue().meta().entrySet().stream().map(y -> y.getKey() + BlockBeats.PROPERTIES_SEPARATOR + y.getValue()).collect(Collectors.joining(BlockBeats.META_SHARE_PROPERTIES_SEPARATOR))
        ).collect(Collectors.joining(BlockBeats.META_SHARED_SEPARATOR));
    }
}
