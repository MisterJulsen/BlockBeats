package de.mrjulsen.blockbeats.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import de.mrjulsen.blockbeats.BlockBeats;

@Mod(BlockBeats.MOD_ID)
public final class BlockBeatsForge {
    public BlockBeatsForge() {
        EventBuses.registerModEventBus(BlockBeats.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        BlockBeats.init();
    }
}
