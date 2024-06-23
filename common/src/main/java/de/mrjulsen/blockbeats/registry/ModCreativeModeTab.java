package de.mrjulsen.blockbeats.registry;

import de.mrjulsen.blockbeats.BlockBeats;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {

    public static final CreativeModeTab MOD_TAB = CreativeTabRegistry.create(new ResourceLocation(BlockBeats.MOD_ID, "tab"), () -> new ItemStack(ModBlocks.SOUND_PLAYER.get()));

    public static void init() { }
}


