package de.mrjulsen.blockbeats;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.mrjulsen.blockbeats.core.filters.CaseInsensitiveMetadataFilter;
import de.mrjulsen.blockbeats.core.filters.FalseFilter;
import de.mrjulsen.blockbeats.core.filters.FavoritesFilter;
import de.mrjulsen.blockbeats.core.filters.PlayerFileAccessFilter;
import de.mrjulsen.blockbeats.core.filters.SoundPlaylistFilter;
import de.mrjulsen.blockbeats.events.ClientEvents;
import de.mrjulsen.blockbeats.events.CommonEvents;
import de.mrjulsen.blockbeats.registry.ModBlockEntities;
import de.mrjulsen.blockbeats.registry.ModBlocks;
import de.mrjulsen.blockbeats.registry.ModCreativeModeTab;
import de.mrjulsen.blockbeats.registry.ModItems;
import de.mrjulsen.blockbeats.registry.ModNetworkManager;
import de.mrjulsen.dragnsounds.registry.FilterRegistry;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

public final class BlockBeats {

    public static final String MOD_ID = "blockbeats";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String META_VISIBILITY = "Visibility";
    public static final String META_SHARED = "Shared";
    public static final String META_FAVORITE = "Favorites";
    public static final String META_SHARE_CAN_EDIT = "can_edit";

    public static final String META_SHARED_SEPARATOR = "#";
    public static final String META_SHARE_PROPERTIES_SEPARATOR = "@";
    public static final String PROPERTIES_SEPARATOR = "=";

    public static final int MAX_FILENAME_LENGTH = 64;

    public static final String SOUND_PLAYER_CATEGORY = "sound_player";

    public static void init() {
        FilterRegistry.register(PlayerFileAccessFilter.class);
        FilterRegistry.register(CaseInsensitiveMetadataFilter.class);
        FilterRegistry.register(SoundPlaylistFilter.class);
        FilterRegistry.register(FalseFilter.class);
        FilterRegistry.register(FavoritesFilter.class);

        CommonEvents.init();
        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientEvents.init();
        }

        ModCreativeModeTab.init();
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModNetworkManager.init();
    }
}
