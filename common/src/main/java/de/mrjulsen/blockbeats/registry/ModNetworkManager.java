package de.mrjulsen.blockbeats.registry;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.blockbeats.net.cts.SoundPlayerPacket;
import de.mrjulsen.blockbeats.net.cts.ManageFavoritesPacket;
import de.mrjulsen.blockbeats.net.cts.GetUsernameCachePacket;
import de.mrjulsen.blockbeats.net.cts.GetAdditionalFileDataPacket;
import de.mrjulsen.blockbeats.net.stc.GetUsernameCacheResponsePacket;
import de.mrjulsen.blockbeats.net.stc.GetAdditionalFileDataResponsePacket;
import de.mrjulsen.blockbeats.net.stc.FavoritesResponsePacket;

public class ModNetworkManager {
    
    public static final DLNetworkManager NETWORK = new DLNetworkManager(DLUtils.resourceLocation(BlockBeats.MOD_ID, "network"), "1");

    // C2S packets (client -> server)
    public static final NetworkPacketType.Send<NetworkDirection.C2S, SoundPlayerPacket> SOUND_PLAYER =
        NETWORK.registerSendOnlyPacket("sound_player", NetworkDirection.C2S, SoundPlayerPacket::handle, SoundPlayerPacket::new);

    public static final NetworkPacketType.Send<NetworkDirection.C2S, ManageFavoritesPacket> MANAGE_FAVORITES =
        NETWORK.registerSendOnlyPacket("manage_favorites", NetworkDirection.C2S, ManageFavoritesPacket::handle, ManageFavoritesPacket::new);

    public static final NetworkPacketType.Send<NetworkDirection.C2S, GetUsernameCachePacket> GET_USERNAME_CACHE =
        NETWORK.registerSendOnlyPacket("get_username_cache", NetworkDirection.C2S, GetUsernameCachePacket::handle, GetUsernameCachePacket::new);

    public static final NetworkPacketType.Send<NetworkDirection.C2S, GetAdditionalFileDataPacket> GET_ADDITIONAL_FILE_DATA =
        NETWORK.registerSendOnlyPacket("get_additional_file_data", NetworkDirection.C2S, GetAdditionalFileDataPacket::handle, GetAdditionalFileDataPacket::new);

    // S2C packets (server -> client)
    public static final NetworkPacketType.Send<NetworkDirection.S2C, GetUsernameCacheResponsePacket> RESPONSE_USERNAME_CACHE =
        NETWORK.registerSendOnlyPacket("response_username_cache", NetworkDirection.S2C, GetUsernameCacheResponsePacket::handle, GetUsernameCacheResponsePacket::new);

    public static final NetworkPacketType.Send<NetworkDirection.S2C, GetAdditionalFileDataResponsePacket> RESPONSE_ADDITIONAL_FILE_DATA =
        NETWORK.registerSendOnlyPacket("response_additional_file_data", NetworkDirection.S2C, GetAdditionalFileDataResponsePacket::handle, GetAdditionalFileDataResponsePacket::new);

    public static final NetworkPacketType.Send<NetworkDirection.S2C, FavoritesResponsePacket> RESPONSE_FAVORITES =
        NETWORK.registerSendOnlyPacket("response_favorites", NetworkDirection.S2C, FavoritesResponsePacket::handle, FavoritesResponsePacket::new);


    public static void init() {}
    
}
