package de.mrjulsen.blockbeats.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.blockbeats.net.callbacks.clinet.ManageFavoritesCallback;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class FavoritesResponsePacket implements IPacketBase<FavoritesResponsePacket> {

    private long requestId;

    public FavoritesResponsePacket() {}

    public FavoritesResponsePacket(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public void encode(FavoritesResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
    }

    @Override
    public FavoritesResponsePacket decode(FriendlyByteBuf buf) {
        return new FavoritesResponsePacket(buf.readLong());
    }

    @Override
    public void handle(FavoritesResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ManageFavoritesCallback.run(packet.requestId);
        });
    }
    
}
