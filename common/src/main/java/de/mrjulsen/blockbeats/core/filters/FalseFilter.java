package de.mrjulsen.blockbeats.core.filters;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.AbstractFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import net.minecraft.resources.ResourceLocation;

public class FalseFilter extends AbstractFilter<SoundFile> {

    public FalseFilter() {
        super("", "", ECompareOperation.EQUALS);
    }

    public FalseFilter(String key, String value, ECompareOperation operation) {
        super("", "", ECompareOperation.NOT);
    }

    @Override
    public ResourceLocation getFilterId() {
        return ResourceLocation.fromNamespaceAndPath(BlockBeats.MOD_ID, "false_filter");
    }

    @Override
    public boolean isValid(SoundFile file) {
        return false;
    }
    
}
