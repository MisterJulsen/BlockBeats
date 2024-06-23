package de.mrjulsen.blockbeats.core.filters;

import java.util.Collection;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.core.ESoundVisibility;
import de.mrjulsen.blockbeats.core.data.SharingUtils;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.AbstractFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class PlayerFileAccessFilter extends AbstractFilter<SoundFile> {

    public PlayerFileAccessFilter(Player player) {
        this("PlayerFileAccess", player.getUUID().toString(), ECompareOperation.EQUALS);
    }

    public PlayerFileAccessFilter(String key, String value, ECompareOperation operation) {
        super(key, value, operation);
    }

    @Override
    public ResourceLocation getFilterId() {
        return new ResourceLocation(BlockBeats.MOD_ID, "player_access_filter");
    }

    @Override
    public boolean isValid(SoundFile file) {
        try {
            ESoundVisibility visibility = ESoundVisibility.getByName(file.getMetadataSafe(BlockBeats.META_VISIBILITY));
            Collection<String> sharedWith = SharingUtils.deserialize(file).keySet().stream().map(x -> x.toString()).toList();
            return visibility != ESoundVisibility.PRIVATE || file.getInfo().getOwnerId().toString().equals(value()) || sharedWith.contains(value());
        } catch (Exception e) {
            BlockBeats.LOGGER.warn("Unable to apply file access filter.", e);
            return false;
        }
    }
    
}
