package de.mrjulsen.blockbeats.core;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum ESoundVisibility implements StringRepresentable, ITranslatableEnum {
    PRIVATE(0, "private", ResourceLocation.parse("textures/item/music_disc_mall.png")),
    SHARED(1, "shared", ResourceLocation.parse("textures/item/music_disc_13.png")),
    PUBLIC(2, "public", ResourceLocation.parse("textures/item/music_disc_cat.png"));

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
    public String getEnumName() {
        return "sound_visibility";
    }

    @Override
    public String getEnumValueName() {
        return getName();
    }

    @Override
    public String getSerializedName() {
        return getName();
    }
}
