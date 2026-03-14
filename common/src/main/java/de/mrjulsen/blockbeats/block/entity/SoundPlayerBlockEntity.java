package de.mrjulsen.blockbeats.block.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.core.data.ERedstoneMode;
import de.mrjulsen.blockbeats.core.data.Playlist;
import de.mrjulsen.blockbeats.core.data.playback.IPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.RadiusPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.registry.ModBlockEntities;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ServerApi;
import de.mrjulsen.dragnsounds.core.data.ESoundType;
import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundInstance;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundSource;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.Cache;
import de.mrjulsen.mcdragonlib.data.DataCache;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SoundPlayerBlockEntity extends TickableBlockEntity<SoundPlayerBlockEntity> {

    private static final String NBT_PLAYLIST = "Playlist";
    private static final String NBT_REDSTONE = "RedstoneMode";
    private static final String NBT_RUNNING = "IsRunning";
    private static final String NBT_START_TIME = "StartTime";
    private static final String NBT_DURATION = "Duration";
    private static final String NBT_SOUND_ID = "SoundId";
    private static final String NBT_OWNER = "Owner";
    private static final String NBT_LOCKED = "Locked";
    private static final String NBT_POWERED = "Powered";
    private static final String NBT_PLAYBACK_AREA = "PlaybackArea";
    private static final String NBT_VOLUME = "Volume";
    private static final String NBT_PITCH = "Pitch";
    private static final String NBT_ATTENUATION_DISTANCE = "AttenuationDistance";
    private static final String NBT_BGM = "BGM";
    private static final String NBT_SHOW_LABEL = "ShowLabel";

    private Playlist playlist = Playlist.empty();
    private IPlaybackAreaBuilder playbackAreaBuilder = new RadiusPlaybackAreaBuilder();
    private Cache<IPlaybackArea> playbackArea = new Cache<>(() -> playbackAreaBuilder == null ? null : playbackAreaBuilder.build(this.getBlockPos()));
    private ERedstoneMode redstone = ERedstoneMode.NO_REDSTONE;

    private boolean running = false;
    private long startTimeMillis = 0;
    private long durationMillis = 0;
    private long soundId = 0;
    private boolean locked = false;
    private boolean powered = false;
    private UUID owner = DragNSounds.ZERO_UUID;
    private float volume = CustomSoundInstance.VOLUME_DEFAULT;
    private float pitch = CustomSoundInstance.PITCH_DEFAULT;
    private int attenuationDistance = 32;
    private boolean bgm = false;
    private boolean showLabel = false;

    // Cache
    private final Set<Player> knownPlayers = new LinkedHashSet<>();
    private final Map<UUID, Long> playerSoundIds = new HashMap<>();
    private final DataCache<Optional<SoundFile>, String> fileCache = new DataCache<>((filePath) -> {
        if (filePath == null) {
            return Optional.empty();
        }
        int splitIndex = filePath.lastIndexOf("/");
        String locationStr = filePath.substring(0, splitIndex);
        String id = filePath.substring(splitIndex + 1);
        SoundLocation location = new SoundLocation(getLevel(), locationStr);
        try {
            return ServerApi.getSoundFile(location, id);
        } catch (Exception e) {
            BlockBeats.LOGGER.error("Could not get sound file: " + location + "/" + id, e);
        }
        return Optional.empty();
    });

    protected SoundPlayerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SoundPlayerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUND_PLAYER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put(NBT_PLAYLIST, playlist.serializeNbt());
        nbt.putInt(NBT_REDSTONE, getRedstone().getIndex());
        nbt.putBoolean(NBT_RUNNING, isRunning());
        nbt.putLong(NBT_START_TIME, startTimeMillis);
        nbt.putLong(NBT_DURATION, durationMillis);
        nbt.putLong(NBT_SOUND_ID, soundId);
        nbt.putUUID(NBT_OWNER, getOwner());
        nbt.putBoolean(NBT_LOCKED, isLocked());
        nbt.putBoolean(NBT_POWERED, isPowered());
        nbt.put(NBT_PLAYBACK_AREA, IPlaybackAreaBuilder.serialize(getPlaybackAreaBuilder()));
        nbt.putFloat(NBT_VOLUME, getVolume());
        nbt.putFloat(NBT_PITCH, getPitch());
        nbt.putInt(NBT_ATTENUATION_DISTANCE, getAttenuationDistance());
        nbt.putBoolean(NBT_BGM, isBgm());
        nbt.putBoolean(NBT_SHOW_LABEL, isShowLabel());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.playlist.deserializeNbt(nbt.getCompound(NBT_PLAYLIST));
        this.redstone = ERedstoneMode.getByIndex(nbt.getInt(NBT_REDSTONE));
        running = nbt.getBoolean(NBT_RUNNING);
        startTimeMillis = nbt.getLong(NBT_START_TIME);
        durationMillis = nbt.getLong(NBT_DURATION);
        soundId = nbt.getLong(NBT_SOUND_ID);
        owner = nbt.getUUID(NBT_OWNER);
        locked = nbt.getBoolean(NBT_LOCKED);
        powered = nbt.getBoolean(NBT_POWERED);
        volume = nbt.getFloat(NBT_VOLUME);
        pitch = nbt.getFloat(NBT_PITCH);
        attenuationDistance = nbt.getInt(NBT_ATTENUATION_DISTANCE);
        bgm = nbt.getBoolean(NBT_BGM);
        showLabel = nbt.getBoolean(NBT_SHOW_LABEL);
        setPlaybackAreaBuilder(IPlaybackAreaBuilder.deserialize(nbt.getCompound(NBT_PLAYBACK_AREA)));
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!isRunning() || level.isClientSide) {
            return;
        }

        if (System.currentTimeMillis() > startTimeMillis + (long)((float)durationMillis / getPitch())) {
            nextTrack(false);
        } else if (getPlaylist().getCurrentFile() != null && !getPlaylist().getCurrentFile().isEmpty()) {
            checkPlayers(false);
        }        
    }

    private void clearCache() {
        knownPlayers.clear();
        playerSoundIds.clear();
        fileCache.clear();
    }

    private void checkPlayers(boolean newTrack) {
        Set<Player> playersInArea = Arrays.stream(ServerApi.selectPlayers(getLevel(), playbackArea.get())).collect(Collectors.toSet());
        knownPlayers.retainAll(getLevel().players());
        playersInArea.removeAll(knownPlayers);
        playTrack(getPlaylist().getCurrentFile(), newTrack ? 0 : playbackProgressTicksNow(), playersInArea.stream().toArray(ServerPlayer[]::new), newTrack);
        knownPlayers.addAll(playersInArea);
    }

    public int playbackProgressTicksNow() {
        int millisPerTick = (int)(TimeUnit.SECONDS.toMillis(1) / DragonLib.mcTps());
        long progressMillis = System.currentTimeMillis() - startTimeMillis;
        int progressTicks = (int)(progressMillis / millisPerTick / getPitch());
        return progressTicks;
    }

    public boolean isAccessible(Player player) {
        return !isLocked() || getOwner().equals(player.getUUID());
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public ERedstoneMode getRedstone() {
        return redstone;
    }

    public void setRedstone(ERedstoneMode redstone) {
        this.redstone = redstone;
    } 

    public void setRunning(boolean b) {
        this.running = b;
        if (b) {
            nextTrack(true);
        } else {
            stop();
            clearCache();
            startTimeMillis = 0;
            durationMillis = 0;
            soundId = 0;
        }
        notifyUpdate();
    }

    public boolean isRunning() {
        return running;
    }

    public void nextTrack(boolean restart) {
        String filePath = null;
        if (restart) {
            filePath = getPlaylist().restart();
        } else {
            filePath = getPlaylist().next(true);
        }

        if (filePath == null) {
            setRunning(false);
            return;
        }
        clearCache();
        checkPlayers(true);
    }

    private void playTrack(String filePath, int offset, ServerPlayer[] players, boolean newTrack) {
        Optional<SoundFile> optFile = fileCache.get(filePath);
        if (optFile != null && optFile.isPresent()) {
            if (newTrack) {                
                startTimeMillis = System.currentTimeMillis();
                durationMillis = optFile.get().getInfo().getDuration();                
                notifyUpdate();
                stop();
            }

            long soundId = players.length > 0 ? ServerApi.playSound(
                optFile.get(),
                new PlaybackConfig(
                    isBgm() ? ESoundType.UI : ESoundType.WORLD,
                    CustomSoundSource.CUSTOM.getSerializedName(),
                    getVolume(),
                    getPitch(),
                    new Vec3(worldPosition.getX() + 0.5f, worldPosition.getY() + 0.5f, worldPosition.getZ() + 0.5f),
                    getAttenuationDistance(),
                    false,
                    offset,
                    isShowLabel()
                ),
                players, (player, sId, status) -> {

                }
            ) : 0;

            for (ServerPlayer player : players) {
                playerSoundIds.computeIfAbsent(player.getUUID(), x -> soundId);
            }

            if (newTrack) {
                this.soundId = soundId;
            }
        }
    }

    public void stop() {
        for (Player player : getLevel().players()) {
            if (playerSoundIds.containsKey(player.getUUID())) {
                ServerApi.stopSound(playerSoundIds.get(player.getUUID()), new ServerPlayer[] {(ServerPlayer)player});
            }
        }
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean b) {
        this.locked = b;
    }

    public void setPowered(boolean b) {
        this.powered = b;
        if (getRedstone() == ERedstoneMode.NO_REDSTONE) {
            return;
        }

        if (b) {
            nextTrack(true);
        } else if (getRedstone() == ERedstoneMode.REDSTONE) {
            stop();
        }
    }

    public boolean isPowered() {
        return powered;
    }

    public IPlaybackAreaBuilder getPlaybackAreaBuilder() {
        return playbackAreaBuilder;
    }

    public void setPlaybackAreaBuilder(IPlaybackAreaBuilder playbackAreaBuilder) {
        this.playbackAreaBuilder = playbackAreaBuilder;
        this.playbackArea.clear();
    }

    public IPlaybackArea getPlaybackArea() {
        return playbackArea.get();
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getAttenuationDistance() {
        return attenuationDistance;
    }

    public void setAttenuationDistance(int attenuationDistance) {
        this.attenuationDistance = attenuationDistance;
    }

    public boolean isBgm() {
        return bgm;
    }

    public void setBgm(boolean bgm) {
        this.bgm = bgm;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }    
    
}
