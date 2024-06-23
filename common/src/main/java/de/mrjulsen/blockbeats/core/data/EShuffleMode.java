package de.mrjulsen.blockbeats.core.data;

import java.util.Arrays;

import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.core.IIterableEnum;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum EShuffleMode implements ITranslatableEnum, StringRepresentable, IIterableEnum<EShuffleMode> {
    NO_SHUFFLE(0, "no_shuffle", ModGuiIcons.NO_SHUFFLE, DragonLib.DEFAULT_BUTTON_COLOR),
    SHUFFLE(1, "shuffle", ModGuiIcons.SHUFFLE, DragonLib.PRIMARY_BUTTON_COLOR);

    private int index;
    private String name;
    private ModGuiIcons icon;
    private int color;

    private EShuffleMode(int index, String name, ModGuiIcons icon, int color) {
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

    public static EShuffleMode getByIndex(int id) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == id).findFirst().orElse(NO_SHUFFLE);
    }

    @Override
    public String getSerializedName() {
        return getName();
    }

    @Override
    public String getEnumName() {
        return "shuffle_mode";
    }

    @Override
    public String getEnumValueName() {
        return getName();
    }

    @Override
    public EShuffleMode[] getValues() {
        return values();
    }
    
}
