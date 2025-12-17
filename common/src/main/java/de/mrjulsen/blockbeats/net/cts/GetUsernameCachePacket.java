package de.mrjulsen.blockbeats.net.cts;

import java.util.function.Consumer;

import de.mrjulsen.blockbeats.core.data.Usercache;
import de.mrjulsen.blockbeats.net.callbacks.clinet.GetUsernameCacheCallback;
import de.mrjulsen.blockbeats.net.stc.GetUsernameCacheResponsePacket;
import de.mrjulsen.blockbeats.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class GetUsernameCachePacket extends NetworkPacketData {

	private static final String NBT_REQUEST_ID = "RequestId";

	private long requestId;

	public GetUsernameCachePacket(DLStatus status) { super(status); }
    
	private GetUsernameCachePacket(long requestId) {
		super(DLStatus.OK);
		this.requestId = requestId;
	}

	public static GetUsernameCachePacket create(Consumer<java.util.Map<java.util.UUID, String>> callback) {
		return new GetUsernameCachePacket(GetUsernameCacheCallback.create(callback));
	}

	@Override
    protected void write(CompoundTag tag) {
		tag.putLong(NBT_REQUEST_ID, requestId);
	}

	@Override
    protected void read(CompoundTag tag) {
		this.requestId = tag.getLong(NBT_REQUEST_ID);
	}

	public static void handle(GetUsernameCachePacket packet, NetworkPacketContext context) {
		ModNetworkManager.RESPONSE_USERNAME_CACHE.send(NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()), new GetUsernameCacheResponsePacket(
			packet.requestId,
			Usercache.getInstance(context.getPlayer().getServer()).getNamesMapped()
		));
	}
}
