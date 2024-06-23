package de.mrjulsen.blockbeats.core.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

public class Usercache implements INBTSerializable {
    public static final String INDEX_FILENAME = "usercache.nbt";
    public static final int DATA_VERSION = 1;
    private static final String NBT_VERSION = "Version";
    private static final String NBT_PLAYERS = "Player";

    private static final long MAX_VALID_TIME = TimeUnit.DAYS.toMillis(365);

    private static Usercache instance;
    private final MinecraftServer server;

    private Map<UUID, PlayerInfo> cache = new HashMap<>();
    
    private Usercache(MinecraftServer server) {
        this.server = server;
    }

    public static Usercache getInstance(MinecraftServer server) {
        if (instance == null) {
            try {
                instance = Usercache.open(server);
            } catch (IOException e) {
                BlockBeats.LOGGER.error("Unable to open usercache file.", e);
                instance = new Usercache(server);
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
    
    public String getName(UUID player) {
        return cache.containsKey(player) ? cache.get(player).name() : "unknown";
    }

    public void remove(UUID player) {
        cache.remove(player);
    }

    public void set(Player player) {
        cache.remove(player.getUUID());
        PlayerInfo info = new PlayerInfo(player.getUUID(), player.getGameProfile().getName(), System.currentTimeMillis());
        cache.put(info.id(), info);
        save();
    }

    public Map<UUID, String> getNamesMapped() {
        return cache.entrySet().stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().name()));
    }

    public synchronized void save() {
        CompoundTag nbt = this.serializeNbt();
    
        try {
            NbtIo.writeCompressed(nbt, new File(server.getWorldPath(new LevelResource("data\\" + BlockBeats.MOD_ID + "_" + INDEX_FILENAME)).toString()));
            DragNSounds.LOGGER.info("Saved Usercache List.");
        } catch (IOException var3) {
            DragNSounds.LOGGER.error("Unable to save usercache file.", var3);
        }    
    }
    
    public static Usercache open(MinecraftServer server) throws IOException {        
        File indexFile = new File(server.getWorldPath(new LevelResource("data\\" + BlockBeats.MOD_ID + "_" + INDEX_FILENAME)).toString());
        Usercache file = new Usercache(server);
        if (indexFile.exists()) {
            file.deserializeNbt(NbtIo.readCompressed(indexFile));
        }
        file.cache.entrySet().removeIf(x -> x.getValue().lastRefreshed() + MAX_VALID_TIME < System.currentTimeMillis());
        return file;
    }
    
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_VERSION, DATA_VERSION);
        nbt.put(NBT_PLAYERS, this.saveMapToNBT());
        return nbt;
    }
    
    public void deserializeNbt(CompoundTag nbt) {
        @SuppressWarnings("unused")
        int version = nbt.getInt(NBT_VERSION);
        this.loadMapFromNBT(nbt.getCompound(NBT_PLAYERS));
    }
    
    private CompoundTag saveMapToNBT() {
        CompoundTag compound = new CompoundTag();
        for (Map.Entry<UUID, PlayerInfo> entry : cache.entrySet()) {
            compound.put(entry.getKey().toString(), entry.getValue().toNbt());
        }

        return compound;
    }

    private void loadMapFromNBT(CompoundTag compound) {
        for (String key : compound.getAllKeys()) {
            cache.put(UUID.fromString(key), PlayerInfo.fromNbt(compound.getCompound(key)));
        }
    }
    
    public void close() {
        this.save();    
    }
    
    public int count() {
        return this.cache.size();
    }

    private static record PlayerInfo(UUID id, String name, long lastRefreshed) {

        private static final String NBT_UUID = "UUID";
        private static final String NBT_NAME = "Name";
        private static final String NBT_REFRESHED = "Refreshed";

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID(NBT_UUID, id());
            nbt.putString(NBT_NAME, name());
            nbt.putLong(NBT_REFRESHED, lastRefreshed());
            return nbt;
        }

        public static PlayerInfo fromNbt(CompoundTag nbt) {
            return new PlayerInfo(nbt.getUUID(NBT_UUID), nbt.getString(NBT_NAME), nbt.getLong(NBT_REFRESHED));
        }
    }
}
