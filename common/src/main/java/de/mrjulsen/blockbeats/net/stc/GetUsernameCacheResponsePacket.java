package de.mrjulsen.blockbeats.net.stc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.mrjulsen.blockbeats.net.callbacks.clinet.GetUsernameCacheCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class GetUsernameCacheResponsePacket extends NetworkPacketData {

	private static final String NBT_REQUEST_ID = "RequestId";
	private static final String NBT_CACHE = "Cache";

	private long requestId;
	private Map<UUID, String> usernamecache;

	public GetUsernameCacheResponsePacket(DLStatus status) { super(status); }
    
	public GetUsernameCacheResponsePacket(long requestId, Map<UUID, String> usernamecache) {
		super(DLStatus.OK);
		this.requestId = requestId;
		this.usernamecache = usernamecache;
	}

	@Override
    protected void write(CompoundTag tag) {
		tag.putLong(NBT_REQUEST_ID, requestId);
		ListTag list = new ListTag();
		if (usernamecache != null) for (Map.Entry<UUID, String> e : usernamecache.entrySet()) {
			CompoundTag eTag = new CompoundTag();
			eTag.putUUID("U", e.getKey());
			eTag.putString("N", e.getValue());
			list.add(eTag);
		}
		tag.put(NBT_CACHE, list);
	}

	@Override
    protected void read(CompoundTag tag) {
		this.requestId = tag.getLong(NBT_REQUEST_ID);
		this.usernamecache = new HashMap<>();
		ListTag list = tag.getList(NBT_CACHE, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag e = list.getCompound(i);
			this.usernamecache.put(e.getUUID("U"), e.getString("N"));
		}
	}

	public static void handle(GetUsernameCacheResponsePacket packet, NetworkPacketContext context) {
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
			GetUsernameCacheCallback.run(packet.requestId, packet.usernamecache);
		});
	}
}
