package de.mrjulsen.blockbeats.registry;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create("modid", Registries.CREATIVE_MODE_TAB);
    
    public static final RegistrySupplier<CreativeModeTab> MOD_TAB = TABS.register(new ResourceLocation(BlockBeats.MOD_ID, "tab"), 
            () -> CreativeTabRegistry.create(
                    TextUtils.translate("itemGroup.blockbeats.tab"),
                    () -> new ItemStack(ModBlocks.SOUND_PLAYER.get())
            )
    );

    public static void init() { }
}


