package de.mrjulsen.blockbeats.core.data.playback;

import de.mrjulsen.blockbeats.core.data.EPlaybackAreaType;
import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public interface IPlaybackAreaBuilder extends INBTSerializable {

    public static String NBT_ID = "Id";

    IPlaybackArea build(BlockPos src);

    public static CompoundTag serialize(IPlaybackAreaBuilder builder) {        
        CompoundTag nbt = builder.serializeNbt();
        nbt.putInt(NBT_ID, EPlaybackAreaType.getByType(builder.getClass()).getId());
        return nbt;
    }

    public static IPlaybackAreaBuilder deserialize(CompoundTag nbt) {
        IPlaybackAreaBuilder builder = EPlaybackAreaType.getById(nbt.getInt(NBT_ID)).create();
        builder.deserializeNbt(nbt);
        return builder;
    }
}
