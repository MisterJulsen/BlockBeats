package de.mrjulsen.blockbeats.core.data;

import java.util.Arrays;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.IIterableEnum;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.DLColor;

public enum ERedstoneMode implements ITranslatableEnum, IIterableEnum<ERedstoneMode> {
    NO_REDSTONE(0, "no_redstone", ModGuiIcons.NO_REDSTONE, DragonLib.BUTTON_COLOR_DEFAULT_DARK),
    REDSTONE(1, "redstone", ModGuiIcons.REDSTONE, DragonLib.BUTTON_COLOR_CANCEL),
    REDSTONE_IMPLUSE(2, "redstone_impulse", ModGuiIcons.REDSTONE_IMPULSE, DragonLib.BUTTON_COLOR_CANCEL);

    private int index;
    private String name;
    private ModGuiIcons icon;
    private DLColor color;

    private ERedstoneMode(int index, String name, ModGuiIcons icon, DLColor color) {
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

    public DLColor getButtonColor() {
        return color;
    }

    public static ERedstoneMode getByIndex(int id) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == id).findFirst().orElse(NO_REDSTONE);
    }

    @Override
    public ERedstoneMode[] getValues() {
        return values();
    }

    @Override
    public Data getTranslationData() {
        return new Data(BlockBeats.MOD_ID, "redstone_mode", name);
    }
    
}
