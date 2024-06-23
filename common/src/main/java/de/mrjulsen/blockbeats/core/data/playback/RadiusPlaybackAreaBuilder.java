package de.mrjulsen.blockbeats.core.data.playback;

import de.mrjulsen.dragnsounds.api.playback.RadiusPlaybackArea;
import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class RadiusPlaybackAreaBuilder implements IPlaybackAreaBuilder {

    private static final String NBT_RADIUS = "Radius";

    private double radius = 32;
    
    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble(NBT_RADIUS, radius);
        return nbt;
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        radius = nbt.getDouble(NBT_RADIUS);
    }

    @Override
    public IPlaybackArea build(BlockPos pos) {
        return new RadiusPlaybackArea(new Vec3(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f), (radius + 0.5f) * (radius + 0.5f) + DragonLib.PIXEL);
    }
    
}
