package de.mrjulsen.blockbeats.net.cts;

import java.util.function.Supplier;

import de.mrjulsen.blockbeats.core.data.FavoritesList;
import de.mrjulsen.blockbeats.core.data.Usercache;
import de.mrjulsen.blockbeats.net.callbacks.clinet.GetFavoritesCallback;
import de.mrjulsen.blockbeats.net.callbacks.clinet.GetFavoritesCallback.IGetFavoritesCallback;
import de.mrjulsen.blockbeats.net.stc.GetAdditionalFileDataResponsePacket;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.mcdragonlib.net.DLNetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class GetAdditionalFileDataPacket extends BaseNetworkPacket<GetAdditionalFileDataPacket> {

    private long requestId;

    public GetAdditionalFileDataPacket() {}

    private GetAdditionalFileDataPacket(long requestId) {
        this.requestId = requestId;
    }

    public static GetAdditionalFileDataPacket create(IGetFavoritesCallback callback) {
        return new GetAdditionalFileDataPacket(GetFavoritesCallback.create(callback));
    }

    @Override
    public void encode(GetAdditionalFileDataPacket packet, RegistryFriendlyByteBuf buf) {
        buf.writeLong(packet.requestId);
    }

    @Override
    public GetAdditionalFileDataPacket decode(RegistryFriendlyByteBuf buf) {
        return new GetAdditionalFileDataPacket(buf.readLong());
    }

    @Override
    public void handle(GetAdditionalFileDataPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {            
            DLNetworkManager.sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new GetAdditionalFileDataResponsePacket(
                packet.requestId,
                FavoritesList.getInstance(contextSupplier.get().getPlayer().getServer()).getFavorites(contextSupplier.get().getPlayer().getUUID()),
                Usercache.getInstance(contextSupplier.get().getPlayer().getServer()).getNamesMapped()
            ));
        });
    }
    
}
