package de.mrjulsen.blockbeats.core.data;

import java.util.Arrays;

import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.core.IIterableEnum;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum ERedstoneMode implements ITranslatableEnum, StringRepresentable, IIterableEnum<ERedstoneMode> {
    NO_REDSTONE(0, "no_redstone", ModGuiIcons.NO_REDSTONE, DragonLib.DEFAULT_BUTTON_COLOR),
    REDSTONE(1, "redstone", ModGuiIcons.REDSTONE, DragonLib.ERROR_BUTTON_COLOR),
    REDSTONE_IMPLUSE(2, "redstone_impulse", ModGuiIcons.REDSTONE_IMPULSE, DragonLib.ERROR_BUTTON_COLOR);

    private int index;
    private String name;
    private ModGuiIcons icon;
    private int color;

    private ERedstoneMode(int index, String name, ModGuiIcons icon, int color) {
        this.index = index;
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ModGuiIcons getIcon() {
        return icon;
    }

    public int getButtonColor() {
        return color;
    }

    public static ERedstoneMode getByIndex(int id) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == id).findFirst().orElse(NO_REDSTONE);
    }

    @Override
    public String getSerializedName() {
        return getName();
    }

    @Override
    public String getEnumName() {
        return "redstone_mode";
    }

    @Override
    public String getEnumValueName() {
        return getName();
    }

    @Override
    public ERedstoneMode[] getValues() {
        return values();
    }
    
}
