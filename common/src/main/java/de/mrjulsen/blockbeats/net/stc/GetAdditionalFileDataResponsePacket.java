package de.mrjulsen.blockbeats.net.stc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.net.callbacks.clinet.GetFavoritesCallback;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class GetAdditionalFileDataResponsePacket extends BaseNetworkPacket<GetAdditionalFileDataResponsePacket> {

    private long requestId;
    private Set<String> paths;
    private Map<UUID, String> usernamecache;

    public GetAdditionalFileDataResponsePacket() {}

    public GetAdditionalFileDataResponsePacket(long requestId, Set<String> paths, Map<UUID, String> usernamecache) {
        this.requestId = requestId;
        this.paths = paths;
        this.usernamecache = usernamecache;
    }

    @Override
    public void encode(GetAdditionalFileDataResponsePacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
        buf.writeInt(packet.paths.size());
        for (String str : packet.paths) {
            buf.writeUtf(str);
        }
        buf.writeMap(packet.usernamecache, (b, k) -> b.writeUUID(k), (b, v) -> b.writeUtf(v));
    }

    @Override
    public GetAdditionalFileDataResponsePacket decode(RegistryFriendlyByteBuf buf) {
        long requestId = buf.readLong();
        int count = buf.readInt();
        Set<String> paths = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            paths.add(buf.readUtf());
        }
        Map<UUID, String> usernamecache = buf.readMap(b -> b.readUUID(), b -> b.readUtf());
        return new GetAdditionalFileDataResponsePacket(requestId, paths, usernamecache);
    }

    @Override
    public void handle(GetAdditionalFileDataResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            GetFavoritesCallback.run(packet.requestId, packet.paths, packet.usernamecache);
        });
    }
    
}
