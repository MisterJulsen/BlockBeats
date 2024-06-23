package de.mrjulsen.blockbeats.core.data.playback;

import de.mrjulsen.dragnsounds.api.playback.BoxPlaybackArea;
import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class BoxPlaybackAreaBuilder implements IPlaybackAreaBuilder {

    private static final String NBT_X1 = "X1";
    private static final String NBT_Y1 = "Y1";
    private static final String NBT_Z1 = "Z1";
    private static final String NBT_X2 = "X2";
    private static final String NBT_Y2 = "Y2";
    private static final String NBT_Z2 = "Z2";

    private double x1 = 0;
    private double y1 = 0;
    private double z1 = 0;
    private double x2 = 0;
    private double y2 = 0;
    private double z2 = 0;
    
    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getZ1() {
        return z1;
    }

    public void setZ1(double z1) {
        this.z1 = z1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public double getZ2() {
        return z2;
    }

    public void setZ2(double z2) {
        this.z2 = z2;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble(NBT_X1, x1);
        nbt.putDouble(NBT_Y1, y1);
        nbt.putDouble(NBT_Z1, z1);
        nbt.putDouble(NBT_X2, x2);
        nbt.putDouble(NBT_Y2, y2);
        nbt.putDouble(NBT_Z2, z2);
        return nbt;
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        x1 = nbt.getDouble(NBT_X1);
        y1 = nbt.getDouble(NBT_Y1);
        z1 = nbt.getDouble(NBT_Z1);
        x2 = nbt.getDouble(NBT_X2);
        y2 = nbt.getDouble(NBT_Y2);
        z2 = nbt.getDouble(NBT_Z2);
    }

    @Override
    public IPlaybackArea build(BlockPos pos) {
        return new BoxPlaybackArea(
            new Vec3(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f),
            x1 > 0 ? x1 + 0.5f : x1 - 0.5f,
            y1 > 0 ? y1 + 0.5f : y1 - 0.5f,
            z1 > 0 ? z1 + 0.5f : z1 - 0.5f,
            x2 > 0 ? x2 + 0.5f : x2 - 0.5f,
            y2 > 0 ? y2 + 0.5f : y2 - 0.5f,
            z2 > 0 ? z2 + 0.5f : z2 - 0.5f
        );
    }
    
}
