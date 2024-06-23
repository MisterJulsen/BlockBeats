package de.mrjulsen.blockbeats.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.blockbeats.block.entity.SoundPlayerBlockEntity;
import de.mrjulsen.blockbeats.core.data.ERedstoneMode;
import de.mrjulsen.blockbeats.core.data.Playlist;
import de.mrjulsen.blockbeats.core.data.playback.IPlaybackAreaBuilder;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

public class SoundPlayerPacket implements IPacketBase<SoundPlayerPacket> {

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

    public SoundPlayerPacket() {}

    public SoundPlayerPacket(BlockPos pos, Playlist playlist, IPlaybackAreaBuilder playbackArea, ERedstoneMode redstone, float volume, float pitch, int attenuationDistance, boolean bgm, boolean showLabel, boolean locked) {
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
    public void encode(SoundPlayerPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeNbt(packet.playlist.serializeNbt());
        buf.writeInt(packet.redstone.getIndex());
        buf.writeNbt(IPlaybackAreaBuilder.serialize(packet.playbackArea));
        buf.writeFloat(packet.volume);
        buf.writeFloat(packet.pitch);
        buf.writeInt(packet.attenuationDistance);
        buf.writeBoolean(packet.bgm);
        buf.writeBoolean(packet.showLabel);
        buf.writeBoolean(packet.locked);
    }

    @Override
    public SoundPlayerPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Playlist playlist = Playlist.empty();
        playlist.deserializeNbt(buf.readNbt());
        ERedstoneMode redstone = ERedstoneMode.getByIndex(buf.readInt());
        IPlaybackAreaBuilder playbackArea = IPlaybackAreaBuilder.deserialize(buf.readNbt());
        return new SoundPlayerPacket(pos, playlist, playbackArea, redstone, buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public void handle(SoundPlayerPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            Level level = contextSupplier.get().getPlayer().level();
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
