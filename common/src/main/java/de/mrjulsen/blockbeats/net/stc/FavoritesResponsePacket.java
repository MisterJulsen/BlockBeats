package de.mrjulsen.blockbeats.net.stc;

import java.util.function.Supplier;

import de.mrjulsen.blockbeats.net.callbacks.clinet.ManageFavoritesCallback;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class FavoritesResponsePacket extends BaseNetworkPacket<FavoritesResponsePacket> {

    private long requestId;

    public FavoritesResponsePacket() {}

    public FavoritesResponsePacket(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public void encode(FavoritesResponsePacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
    }

    @Override
    public FavoritesResponsePacket decode(RegistryFriendlyByteBuf buf) {
        return new FavoritesResponsePacket(buf.readLong());
    }

    @Override
    public void handle(FavoritesResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ManageFavoritesCallback.run(packet.requestId);
        });
    }
    
}
