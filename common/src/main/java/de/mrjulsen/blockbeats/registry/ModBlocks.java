package de.mrjulsen.blockbeats.registry;

import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.block.SoundPlayerBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BlockBeats.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<SoundPlayerBlock> SOUND_PLAYER = register("sound_player", () -> new SoundPlayerBlock(Properties.of()));



    public static final <T extends Block> RegistrySupplier<T> registerWithoutItem(String name, Supplier<T> block) {
        RegistrySupplier<T> result = BLOCKS.register(name, block);
        return result;
    }
    
    public static final <T extends Block> RegistrySupplier<T> register(String name, Supplier<T> block) {
        RegistrySupplier<T> result = registerWithoutItem(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(result.get(), new Item.Properties().arch$tab(ModCreativeModeTab.MOD_TAB)));
        return result;
    }

    public static void init() {
        BLOCKS.register();
    }
}
