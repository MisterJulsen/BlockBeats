package de.mrjulsen.blockbeats.net.stc;

import de.mrjulsen.blockbeats.net.callbacks.clinet.ManageFavoritesCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

public class FavoritesResponsePacket extends NetworkPacketData {

	private static final String NBT_REQUEST_ID = "RequestId";

	private long requestId;

	public FavoritesResponsePacket(DLStatus status) { super(status); }
    
	public FavoritesResponsePacket(long requestId) {
		super(DLStatus.OK);
		this.requestId = requestId;
	}

	@Override
    protected void write(CompoundTag tag) {
		tag.putLong(NBT_REQUEST_ID, requestId);
	}

	@Override
    protected void read(CompoundTag tag) {
		this.requestId = tag.getLong(NBT_REQUEST_ID);
	}

	public static void handle(FavoritesResponsePacket packet, NetworkPacketContext context) {
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
			ManageFavoritesCallback.run(packet.requestId);
		});
	}
}
