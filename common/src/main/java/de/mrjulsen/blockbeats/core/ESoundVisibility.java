package de.mrjulsen.blockbeats.core;

import java.util.Arrays;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import net.minecraft.resources.ResourceLocation;

public enum ESoundVisibility implements ITranslatableEnum {
    PRIVATE(0, "private", new ResourceLocation("textures/item/music_disc_mall.png")),
    SHARED(1, "shared", new ResourceLocation("textures/item/music_disc_13.png")),
    PUBLIC(2, "public", new ResourceLocation("textures/item/music_disc_cat.png"));

    private int index;
    private String name;
    private ResourceLocation icon;

    private ESoundVisibility(int index, String name, ResourceLocation icon) {
        this.index = index;
        this.name = name;
        this.icon = icon;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getIconLocation() {
        return icon;
    }

    public static ESoundVisibility getByName(String name) {
        return Arrays.stream(values()).filter(x -> x.getName().equals(name)).findFirst().orElse(PRIVATE);
    }
    
    public static ESoundVisibility getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(PRIVATE);
    }

    @Override
    public Data getTranslationData() {
        return new Data(BlockBeats.MOD_ID, "sound_visibility", name);
    }
}
