package de.mrjulsen.blockbeats.core.data.playback;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class EntityRidingPlaybackAreaBuilder implements IPlaybackAreaBuilder {

    private static final String NBT_ENTITY = "Entities";
    private static final String NBT_RADIUS = "Radius";

    private final Set<ResourceLocation> entityIds = new LinkedHashSet<>();
    private double radius = 32;
    
    public Set<ResourceLocation> getEntityIds() {
        return entityIds;
    }

    public void addEntityId(ResourceLocation entityId) {
        this.entityIds.add(entityId);
    }

    public void removeEntityId(ResourceLocation entityId) {
        this.entityIds.remove(entityId);
    }

    public void addEntityIds(Collection<ResourceLocation> entityIds) {
        this.entityIds.addAll(entityIds);
    }

    public void setEntityIds(Collection<ResourceLocation> entityIds) {
        this.entityIds.clear();
        this.entityIds.addAll(entityIds);
    }

    public boolean contains(ResourceLocation id) {
        return entityIds.contains(id);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        ListTag ids = new ListTag();
        ids.addAll(entityIds.stream().map(x -> StringTag.valueOf(x.toString())).toList());

        nbt.putDouble(NBT_RADIUS, radius);
        nbt.put(NBT_ENTITY, ids);
        return nbt;
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        entityIds.addAll(nbt.getList(NBT_ENTITY, Tag.TAG_STRING).stream().map(x -> new ResourceLocation(x.getAsString())).toList());
        radius = nbt.getDouble(NBT_RADIUS);
    }

    @Override
    public IPlaybackArea build(BlockPos pos) {
        return new EntityRidingPlaybackArea(getEntityIds(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), getRadius());
    }
    
}
