package de.mrjulsen.blockbeats.core.filters;

import java.util.UUID;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.core.data.FavoritesList;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.AbstractFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class FavoritesFilter extends AbstractFilter<SoundFile> {

    public FavoritesFilter(String key, String value, ECompareOperation operation) {
        super(key, value, operation);
    }

    public static FavoritesFilter of(Player player, ECompareOperation operation) {
        return new FavoritesFilter("FavoritesFilter", player.getUUID().toString(), operation);
    }
    
    @Override
    public ResourceLocation getFilterId() {
        return new ResourceLocation(BlockBeats.MOD_ID, "favorites_filter");
    }

    @Override
    public boolean isValid(SoundFile arg) {
        switch (compareOperation()) {
            case NOT:
                return !FavoritesList.getInstance(ServerEvents.getCurrentServer()).isFavorite(UUID.fromString(value()), arg.toString());
            default:
                return FavoritesList.getInstance(ServerEvents.getCurrentServer()).isFavorite(UUID.fromString(value()), arg.toString());
        }
    }
    
}
