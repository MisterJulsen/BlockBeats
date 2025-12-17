package de.mrjulsen.blockbeats.net.cts;

import de.mrjulsen.blockbeats.core.data.FavoritesList;
import de.mrjulsen.blockbeats.core.data.Usercache;
import de.mrjulsen.blockbeats.net.callbacks.clinet.GetFavoritesCallback;
import de.mrjulsen.blockbeats.net.stc.GetAdditionalFileDataResponsePacket;
import de.mrjulsen.blockbeats.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class GetAdditionalFileDataPacket extends NetworkPacketData {

	private static final String NBT_REQUEST_ID = "RequestId";

	private long requestId;

	public GetAdditionalFileDataPacket(DLStatus status) { super(status); }

	private GetAdditionalFileDataPacket(long requestId) {
		super(DLStatus.OK);
		this.requestId = requestId;
	}

	public static GetAdditionalFileDataPacket create(GetFavoritesCallback.IGetFavoritesCallback callback) {
		return new GetAdditionalFileDataPacket(GetFavoritesCallback.create(callback));
	}

	@Override
    protected void write(CompoundTag tag) {
		tag.putLong(NBT_REQUEST_ID, requestId);
	}

	@Override
    protected void read(CompoundTag tag) {
		this.requestId = tag.getLong(NBT_REQUEST_ID);
	}

	public static void handle(GetAdditionalFileDataPacket packet, NetworkPacketContext context) {
		ModNetworkManager.RESPONSE_ADDITIONAL_FILE_DATA.send(NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()), new GetAdditionalFileDataResponsePacket(
			packet.requestId,
			FavoritesList.getInstance(context.getPlayer().getServer()).getFavorites(context.getPlayer().getUUID()),
			Usercache.getInstance(context.getPlayer().getServer()).getNamesMapped()
		));
	}
}
