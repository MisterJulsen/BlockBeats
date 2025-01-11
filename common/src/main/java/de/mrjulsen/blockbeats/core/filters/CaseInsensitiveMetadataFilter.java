package de.mrjulsen.blockbeats.core.filters;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.AbstractFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import net.minecraft.resources.ResourceLocation;

public class CaseInsensitiveMetadataFilter extends AbstractFilter<SoundFile> {

    public CaseInsensitiveMetadataFilter(String key, String value, ECompareOperation operation) {
        super(key, value, operation);
    }

    @Override
    public ResourceLocation getFilterId() {
        return ResourceLocation.fromNamespaceAndPath(BlockBeats.MOD_ID, "caseinsensitive_metadata_filter");
    }

    @Override
    public boolean isValid(SoundFile file) {
        boolean b = false;
        b = this.compareOperation() == ECompareOperation.NOT ? file.getMetadata().entrySet().stream().allMatch((e) -> {
            return this.compareOperation().compare((String)e.getKey(), this.key()) || !this.value().isBlank() && this.compareOperation().compare((String)e.getValue().toLowerCase(), this.value().toLowerCase());
        }) : file.getMetadata().entrySet().stream().anyMatch((e) -> {
            return this.compareOperation().compare((String)e.getKey(), this.key()) && (this.value().isBlank() || this.compareOperation().compare((String)e.getValue().toLowerCase(), this.value().toLowerCase()));
        });
        return b;
    }
    
}
