package de.mrjulsen.blockbeats.net.cts;

import java.util.HashSet;
import java.util.Set;

import de.mrjulsen.blockbeats.core.data.FavoritesList;
import de.mrjulsen.blockbeats.net.callbacks.clinet.ManageFavoritesCallback;
import de.mrjulsen.blockbeats.net.stc.FavoritesResponsePacket;
import de.mrjulsen.blockbeats.registry.ModNetworkManager;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ManageFavoritesPacket extends NetworkPacketData {

	private static final String NBT_REQUEST_ID = "RequestId";
	private static final String NBT_REMOVE = "Remove";
	private static final String NBT_PATHS = "Paths";

	private long requestId;
	private Set<String> paths;
	private boolean remove;

	public ManageFavoritesPacket(DLStatus status) { super(status); }

	private ManageFavoritesPacket(long requestId, Set<String> paths, boolean remove) {
		super(DLStatus.OK);
		this.requestId = requestId;
		this.paths = paths;
		this.remove = remove;
	}

	public static ManageFavoritesPacket create(Set<String> paths, boolean remove, Runnable callback) {
		return new ManageFavoritesPacket(ManageFavoritesCallback.create(callback), paths, remove);
	}

	@Override
    protected void write(CompoundTag tag) {
		tag.putLong(NBT_REQUEST_ID, requestId);
		tag.putBoolean(NBT_REMOVE, remove);
		ListTag list = new ListTag();
		if (paths != null) for (String p : paths) list.add(StringTag.valueOf(p));
		tag.put(NBT_PATHS, list);
	}

	@Override
    protected void read(CompoundTag tag) {
		this.requestId = tag.getLong(NBT_REQUEST_ID);
		this.remove = tag.getBoolean(NBT_REMOVE);
		ListTag list = tag.getList(NBT_PATHS, Tag.TAG_STRING);
		this.paths = new HashSet<>();
		for (int i = 0; i < list.size(); i++) this.paths.add(list.getString(i));
	}

	public static void handle(ManageFavoritesPacket packet, NetworkPacketContext context) {
		perform(packet, context.getPlayer());
		ModNetworkManager.RESPONSE_FAVORITES.send(NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()), new FavoritesResponsePacket(packet.requestId));
	}

	private static final synchronized void perform(ManageFavoritesPacket packet, Player player) {
		packet.paths.forEach(x -> {
			if (packet.remove) {
				FavoritesList.getInstance(player.getServer()).removeFavorite(player.getUUID(), x);
			} else {
				FavoritesList.getInstance(player.getServer()).addFavorite(player.getUUID(), x);
			}
		});
		FavoritesList.getInstance(player.getServer()).save();
	}
}
