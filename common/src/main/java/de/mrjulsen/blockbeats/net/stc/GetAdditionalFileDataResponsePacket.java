package de.mrjulsen.blockbeats.net.stc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.mrjulsen.blockbeats.net.callbacks.clinet.GetFavoritesCallback;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class GetAdditionalFileDataResponsePacket extends NetworkPacketData {

	private static final String NBT_REQUEST_ID = "RequestId";
	private static final String NBT_PATHS = "Paths";
	private static final String NBT_CACHE = "Cache";

	private long requestId;
	private Set<String> paths;
	private Map<UUID, String> usernamecache;

	public GetAdditionalFileDataResponsePacket(DLStatus status) { super(status); }

	public GetAdditionalFileDataResponsePacket(long requestId, Set<String> paths, Map<UUID, String> usernamecache) {
		super(DLStatus.OK);
		this.requestId = requestId;
		this.paths = paths;
		this.usernamecache = usernamecache;
	}

	@Override protected void write(CompoundTag tag) {
		tag.putLong(NBT_REQUEST_ID, requestId);
		ListTag pList = new ListTag();
		if (paths != null) for (String s : paths) pList.add(StringTag.valueOf(s));
		tag.put(NBT_PATHS, pList);

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
		ListTag pList = tag.getList(NBT_PATHS, 8);
		this.paths = new HashSet<>();
		for (int i = 0; i < pList.size(); i++) this.paths.add(pList.getString(i));

		this.usernamecache = new HashMap<>();
		ListTag list = tag.getList(NBT_CACHE, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag e = list.getCompound(i);
			this.usernamecache.put(e.getUUID("U"), e.getString("N"));
		}
	}

	public static void handle(GetAdditionalFileDataResponsePacket packet, NetworkPacketContext context) {
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
			GetFavoritesCallback.run(packet.requestId, packet.paths, packet.usernamecache);
		});
	}
}
