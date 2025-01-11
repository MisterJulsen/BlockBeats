package de.mrjulsen.blockbeats.core.filters;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.AbstractFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import net.minecraft.resources.ResourceLocation;

public class SoundPlaylistFilter extends AbstractFilter<SoundFile> {

    public SoundPlaylistFilter(Collection<String> files) {
        super("PlaylistFilter", files.stream().collect(Collectors.joining(BlockBeats.META_SHARED_SEPARATOR)), ECompareOperation.EQUALS);
    }

    public SoundPlaylistFilter(String key, String value, ECompareOperation operation) {
        super(key, value, operation);
    }

    @Override
    public ResourceLocation getFilterId() {
        return ResourceLocation.fromNamespaceAndPath(BlockBeats.MOD_ID, "playlist_filter");
    }

    @Override
    public boolean isValid(SoundFile file) {
        Collection<String> allowed = Arrays.stream(value().split(BlockBeats.META_SHARED_SEPARATOR)).toList();
        return allowed.contains(file.toString());
    }
    
}
