package de.mrjulsen.blockbeats.net.cts;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.core.data.FavoritesList;
import de.mrjulsen.blockbeats.net.callbacks.clinet.ManageFavoritesCallback;
import de.mrjulsen.blockbeats.net.stc.FavoritesResponsePacket;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ManageFavoritesPacket implements IPacketBase<ManageFavoritesPacket> {

    private long requestId;
    private Set<String> paths;
    private boolean remove;

    public ManageFavoritesPacket() {}

    private ManageFavoritesPacket(long requestId, Set<String> paths, boolean remove) {
        this.requestId = requestId;
        this.paths = paths;
        this.remove = remove;
    }
    
    public static ManageFavoritesPacket create(Set<String> paths, boolean remove, Runnable callback) {
        return new ManageFavoritesPacket(ManageFavoritesCallback.create(callback), paths, remove);
    }

    @Override
    public void encode(ManageFavoritesPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeBoolean(packet.remove);
        buf.writeInt(packet.paths.size());
        for (String str : packet.paths) {
            buf.writeUtf(str);
        }
    }

    @Override
    public ManageFavoritesPacket decode(FriendlyByteBuf buf) {
        long requestId = buf.readLong();
        boolean remove = buf.readBoolean();
        int count = buf.readInt();
        Set<String> paths = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            paths.add(buf.readUtf());
        }
        return new ManageFavoritesPacket(requestId, paths, remove);
    }

    @Override
    public void handle(ManageFavoritesPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            perform(packet, contextSupplier.get().getPlayer());
        });
        BlockBeats.net().sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new FavoritesResponsePacket(packet.requestId));
    }

    private static final synchronized void perform(ManageFavoritesPacket packet, Player player) {
        packet.paths.forEach(x -> {
            if (packet.remove) {
                FavoritesList.getInstance(player.getServer()).removeFavorite(player.getUUID(), x);
            } else {
                FavoritesList.getInstance(player.getServer()).addFavorite(player.getUUID(), x);
            }
        });        
        FavoritesList.getInstance(player.getServer()).save();
    }
    
}
