package de.mrjulsen.blockbeats.net.cts;

import de.mrjulsen.blockbeats.block.entity.SoundPlayerBlockEntity;
import de.mrjulsen.blockbeats.core.data.ERedstoneMode;
import de.mrjulsen.blockbeats.core.data.Playlist;
import de.mrjulsen.blockbeats.core.data.playback.IPlaybackAreaBuilder;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SoundPlayerPacket extends NetworkPacketData {

	private static final String NBT_POS = "Pos";
	private static final String NBT_PLAYLIST = "Playlist";
	private static final String NBT_REDSTONE = "Redstone";
	private static final String NBT_PLAYBACK_AREA = "PlaybackArea";
	private static final String NBT_VOLUME = "Volume";
	private static final String NBT_PITCH = "Pitch";
	private static final String NBT_ATT_DIST = "AttenuationDistance";
	private static final String NBT_BGM = "Bgm";
	private static final String NBT_SHOW_LABEL = "ShowLabel";
	private static final String NBT_LOCKED = "Locked";

	private BlockPos pos;
	private Playlist playlist;
	private ERedstoneMode redstone;
	private IPlaybackAreaBuilder playbackArea;
	private float volume;
	private float pitch;
	private int attenuationDistance;
	private boolean bgm;
	private boolean showLabel;
	private boolean locked;

	public SoundPlayerPacket(DLStatus status) { super(status); }

	public SoundPlayerPacket(BlockPos pos, Playlist playlist, IPlaybackAreaBuilder playbackArea, ERedstoneMode redstone, float volume, float pitch, int attenuationDistance, boolean bgm, boolean showLabel, boolean locked) {
		super(DLStatus.OK);
		this.pos = pos;
		this.playlist = playlist;
		this.playbackArea = playbackArea;
		this.redstone = redstone;
		this.volume = volume;
		this.pitch = pitch;
		this.attenuationDistance = attenuationDistance;
		this.bgm = bgm;
		this.showLabel = showLabel;
		this.locked = locked;
	}

	@Override
    protected void write(CompoundTag tag) {
		tag.putLong(NBT_POS, pos.asLong());
		tag.put(NBT_PLAYLIST, playlist.serializeNbt());
		tag.putInt(NBT_REDSTONE, redstone.getIndex());
		tag.put(NBT_PLAYBACK_AREA, IPlaybackAreaBuilder.serialize(playbackArea));
		tag.putFloat(NBT_VOLUME, volume);
		tag.putFloat(NBT_PITCH, pitch);
		tag.putInt(NBT_ATT_DIST, attenuationDistance);
		tag.putBoolean(NBT_BGM, bgm);
		tag.putBoolean(NBT_SHOW_LABEL, showLabel);
		tag.putBoolean(NBT_LOCKED, locked);
	}

	@Override
    protected void read(CompoundTag tag) {
		this.pos = BlockPos.of(tag.getLong(NBT_POS));
		this.playlist = Playlist.empty();
		this.playlist.deserializeNbt(tag.getCompound(NBT_PLAYLIST));
		this.redstone = ERedstoneMode.getByIndex(tag.getInt(NBT_REDSTONE));
		this.playbackArea = IPlaybackAreaBuilder.deserialize(tag.getCompound(NBT_PLAYBACK_AREA));
		this.volume = tag.getFloat(NBT_VOLUME);
		this.pitch = tag.getFloat(NBT_PITCH);
		this.attenuationDistance = tag.getInt(NBT_ATT_DIST);
		this.bgm = tag.getBoolean(NBT_BGM);
		this.showLabel = tag.getBoolean(NBT_SHOW_LABEL);
		this.locked = tag.getBoolean(NBT_LOCKED);
	}

	public static void handle(SoundPlayerPacket packet, NetworkPacketContext context) {
		context.queue(() -> {
			Level level = context.getPlayer().level();
			if (level.getBlockEntity(packet.pos) instanceof SoundPlayerBlockEntity be) {
				be.setPlaylist(packet.playlist);
				be.setRedstone(packet.redstone);
				be.setPlaybackAreaBuilder(packet.playbackArea);
				be.setVolume(packet.volume);
				be.setPitch(packet.pitch);
				be.setAttenuationDistance(packet.attenuationDistance);
				be.setBgm(packet.bgm);
				be.setShowLabel(packet.showLabel);
				be.setLocked(packet.locked);
				be.notifyUpdate();
			}
		});
	}
	
}
