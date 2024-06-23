package de.mrjulsen.blockbeats.net.stc;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.net.callbacks.clinet.GetUsernameCacheCallback;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class GetUsernameCacheResponsePacket implements IPacketBase<GetUsernameCacheResponsePacket> {

    private long requestId;
    private Map<UUID, String> usernamecache;

    public GetUsernameCacheResponsePacket() {}

    public GetUsernameCacheResponsePacket(long requestId, Map<UUID, String> usernamecache) {
        this.requestId = requestId;
        this.usernamecache = usernamecache;
    }

    @Override
    public void encode(GetUsernameCacheResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeMap(packet.usernamecache, (b, k) -> b.writeUUID(k), (b, v) -> b.writeUtf(v));
    }

    @Override
    public GetUsernameCacheResponsePacket decode(FriendlyByteBuf buf) {
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
