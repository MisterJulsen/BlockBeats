package de.mrjulsen.blockbeats.core.data;

import java.util.Arrays;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.core.data.playback.BoxPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.EntityRidingPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.IPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.RadiusPlaybackAreaBuilder;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.core.IIterableEnum;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum EPlaybackAreaType implements ITranslatableEnum, StringRepresentable, IIterableEnum<EPlaybackAreaType> {
    RADIUS(0, "radius", ModGuiIcons.RADIUS.getAsSprite(16, 16), RadiusPlaybackAreaBuilder.class),
    BOX(1, "box", ModGuiIcons.BOX.getAsSprite(16, 16), BoxPlaybackAreaBuilder.class),
    ENTITY(2, "entity", ModGuiIcons.CREEPER.getAsSprite(16, 16), EntityRidingPlaybackAreaBuilder.class);

    private int id;
    private String name;
    private Sprite sprite;
    private Class<? extends IPlaybackAreaBuilder> type;

    private EPlaybackAreaType(int id, String name, Sprite sprite, Class<? extends IPlaybackAreaBuilder> clazz) {
        this.id = id;
        this.name = name;
        this.sprite = sprite;
        this.type = clazz;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Sprite getIcon() {
        return sprite;
    }

    public Class<? extends IPlaybackAreaBuilder> getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T extends IPlaybackAreaBuilder> T create() {
        try {
            return (T)type.getConstructor().newInstance();
        } catch (Exception e) {
            BlockBeats.LOGGER.error("Unable to create instance of playback area builder.", e);
            return null;
        }
    }

    public static EPlaybackAreaType getById(int id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(RADIUS);
    }

    public static EPlaybackAreaType getByType(Class<? extends IPlaybackAreaBuilder> input) {
        return Arrays.stream(values()).filter(x -> x.getType().equals(input)).findFirst().orElse(RADIUS);
    }

    @Override
    public String getSerializedName() {
        return getName();
    }

    @Override
    public String getEnumName() {
        return "playback_area_type";
    }

    @Override
    public String getEnumValueName() {
        return getName();
    }

    @Override
    public EPlaybackAreaType[] getValues() {
        return values();
    }
    
}