package de.mrjulsen.blockbeats.core.data;

import java.util.Arrays;

import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.core.IIterableEnum;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum ELoopMode implements ITranslatableEnum, StringRepresentable, IIterableEnum<ELoopMode> {
    NO_LOOP(0, "no_loop", ModGuiIcons.NO_LOOP, DragonLib.DEFAULT_BUTTON_COLOR),
    LOOP(1, "loop", ModGuiIcons.LOOP, DragonLib.PRIMARY_BUTTON_COLOR),
    SINGLE_LOOP(2, "single_loop", ModGuiIcons.LOOP_SINGLE, DragonLib.PRIMARY_BUTTON_COLOR);

    private int index;
    private String name;
    private ModGuiIcons icon;
    private int color;

    private ELoopMode(int index, String name, ModGuiIcons icon, int color) {
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

    public static ELoopMode getByIndex(int id) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == id).findFirst().orElse(NO_LOOP);
    }

    @Override
    public String getSerializedName() {
        return getName();
    }

    @Override
    public String getEnumName() {
        return "loop_mode";
    }

    @Override
    public String getEnumValueName() {
        return getName();
    }

    @Override
    public ELoopMode[] getValues() {
        return values();
    }
    
}