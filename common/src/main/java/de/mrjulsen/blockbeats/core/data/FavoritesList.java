package de.mrjulsen.blockbeats.core.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;

public class FavoritesList implements INBTSerializable {
    public static final String INDEX_FILENAME = "favorites.nbt";
    public static final int DATA_VERSION = 1;
    private static final String NBT_VERSION = "Version";
    private static final String NBT_FILES = "Favorites";
    private static FavoritesList instance;
    private final MinecraftServer server;

    private Map<UUID, Set<String>> files = new HashMap<>();
    
    private FavoritesList(MinecraftServer server) {
        this.server = server;
    }

    public static FavoritesList getInstance(MinecraftServer server) {
        if (instance == null) {
            try {
                instance = FavoritesList.open(server);
            } catch (IOException e) {
                BlockBeats.LOGGER.error("Unable to open favorite file.", e);
                instance = new FavoritesList(server);
            }
        }
        return instance;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.close();
        }
        instance = null;
    }
    
    public Set<String> getFavorites(UUID player) {
        return files.containsKey(player) ? files.get(player) : Set.of();
    }

    public void clearFavorites(UUID player) {
        files.remove(player);
    }

    public void removeFavorite(UUID player, String path) {
        if (files.containsKey(player)) {
            files.get(player).remove(path);
        }
    }

    public boolean isFavorite(UUID player, String path) {
        return files.containsKey(player) && files.get(player).contains(path);
    }

    public void addFavorite(UUID player, String path) {
        files.computeIfAbsent(player, x -> new LinkedHashSet<>()).add(path);
    }

    public synchronized void save() {
        CompoundTag nbt = this.serializeNbt();
    
        try {
            NbtIo.writeCompressed(nbt, new File(SoundLocation.getModDirectory(server.overworld()).toString() + "/" + BlockBeats.MOD_ID + "/" + INDEX_FILENAME));
            DragNSounds.LOGGER.info("Saved Favorites List.");
        } catch (IOException var3) {
            DragNSounds.LOGGER.error("Unable to save favorite file.", var3);
        }    
    }
    
    public static FavoritesList open(MinecraftServer server) throws IOException {        
        File indexFile = new File(SoundLocation.getModDirectory(server.overworld()).toString() + "/" + BlockBeats.MOD_ID + "/" + INDEX_FILENAME);
        FavoritesList file = new FavoritesList(server);
        if (indexFile.exists()) {
            file.deserializeNbt(NbtIo.readCompressed(indexFile));
        }
        return file;
    }
    
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_VERSION, DATA_VERSION);
        nbt.put(NBT_FILES, this.saveMapToNBT());
        return nbt;
    }
    
    public void deserializeNbt(CompoundTag nbt) {
        @SuppressWarnings("unused")
        int version = nbt.getInt(NBT_VERSION);
        this.loadMapFromNBT(nbt.getCompound(NBT_FILES));
    }
    
    private CompoundTag saveMapToNBT() {
        CompoundTag compound = new CompoundTag();
        for (Map.Entry<UUID, Set<String>> entry : files.entrySet()) {
            ListTag list = new ListTag();
            list.addAll(entry.getValue().stream().map(x -> StringTag.valueOf(x)).toList());
            compound.put(entry.getKey().toString(), list);
        }

        return compound;
    }

    private void loadMapFromNBT(CompoundTag compound) {
        for (String key : compound.getAllKeys()) {
            files.put(UUID.fromString(key), new LinkedHashSet<>(compound.getList(key, Tag.TAG_STRING).stream().map(x -> x.getAsString()).collect(Collectors.toSet())));
        }
    }
    
    public void close() {
        this.save();    
    }
    
    public int count() {
        return this.files.size();
    }
}
