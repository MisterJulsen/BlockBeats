package de.mrjulsen.blockbeats.net.stc;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.net.callbacks.clinet.GetUsernameCacheCallback;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class GetUsernameCacheResponsePacket extends BaseNetworkPacket<GetUsernameCacheResponsePacket> {

    private long requestId;
    private Map<UUID, String> usernamecache;

    public GetUsernameCacheResponsePacket() {}

    public GetUsernameCacheResponsePacket(long requestId, Map<UUID, String> usernamecache) {
        this.requestId = requestId;
        this.usernamecache = usernamecache;
    }

    @Override
    public void encode(GetUsernameCacheResponsePacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeMap(packet.usernamecache, (b, k) -> b.writeUUID(k), (b, v) -> b.writeUtf(v));
    }

    @Override
    public GetUsernameCacheResponsePacket decode(RegistryFriendlyByteBuf buf) {
        long requestId = buf.readLong();
        Map<UUID, String> usernamecache = buf.readMap(b -> b.readUUID(), b -> b.readUtf());
        return new GetUsernameCacheResponsePacket(requestId, usernamecache);
    }

    @Override
    public void handle(GetUsernameCacheResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            GetUsernameCacheCallback.run(packet.requestId, packet.usernamecache);
        });
    }
    
}
