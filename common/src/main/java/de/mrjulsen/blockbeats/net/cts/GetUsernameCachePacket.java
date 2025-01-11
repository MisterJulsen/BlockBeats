package de.mrjulsen.blockbeats.net.cts;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.core.data.Usercache;
import de.mrjulsen.blockbeats.net.callbacks.clinet.GetUsernameCacheCallback;
import de.mrjulsen.blockbeats.net.stc.GetUsernameCacheResponsePacket;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.mcdragonlib.net.DLNetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class GetUsernameCachePacket extends BaseNetworkPacket<GetUsernameCachePacket> {

    private long requestId;

    public GetUsernameCachePacket() {}

    private GetUsernameCachePacket(long requestId) {
        this.requestId = requestId;
    }

    public static GetUsernameCachePacket create(Consumer<Map<UUID, String>> callback) {
        return new GetUsernameCachePacket(GetUsernameCacheCallback.create(callback));
    }

    @Override
    public void encode(GetUsernameCachePacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
    }

    @Override
    public GetUsernameCachePacket decode(RegistryFriendlyByteBuf buf) {
        return new GetUsernameCachePacket(buf.readLong());
    }

    @Override
    public void handle(GetUsernameCachePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {            
            DLNetworkManager.sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new GetUsernameCacheResponsePacket(
                packet.requestId,
                Usercache.getInstance(contextSupplier.get().getPlayer().getServer()).getNamesMapped()
            ));
        });
    }
    
}
