package de.mrjulsen.blockbeats.client;

import java.util.Arrays;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import net.minecraft.resources.ResourceLocation;

public enum ModGuiIcons {
    DELETE("delete", 1, 0),
    PLAY("play", 2, 0),
    RENAME("rename", 3, 0),
    PROPERTIES("properties", 4, 0),
    INFO("info", 5, 0),
    PAUSE("pause", 6, 0),
    SHARE("share", 7, 0),
    SHARE2("share2", 8, 0),
    DOWNLOAD("download", 9, 0),
    ADD("add", 10, 0),
    REMOVE("remove", 11, 0),
    VISIBLE("visible", 12, 0),
    INVISIBLE("invisible", 13, 0),
    CLOUD("cloud", 14, 0),
    FOLDER("folder", 15, 0),

    CD("cd", 0, 1),
    NOTE("note", 1, 1),
    UPLOAD("upload", 2, 1),    
    REFRESH("refresh", 3, 1),    
    CHECK("check", 4, 1),    
    BULLET("bullet", 5, 1),    
    SORT_DESCENDING("sort_descending", 6, 1),    
    SORT_ASCENDING("sort_ascending", 7, 1),    
    LIST("list", 8, 1),    
    HIERARCHY("hierarchy", 9, 1),    
    SERVER("server", 10, 1),    
    PLAYER("player", 11, 1),    
    COPY("copy", 12, 1),    
    PASTE("paste", 13, 1),    
    CUT("cut", 14, 1),

    PLAY_SMALL("play_small", 15, 1),
    LOADING1("loading1", 0, 2),
    LOADING2("loading2", 1, 2),
    LOADING3("loading3", 2, 2),
    LOADING4("loading4", 3, 2),
    LOADING5("loading5", 4, 2),
    LOADING6("loading6", 5, 2),
    LOADING7("loading7", 6, 2),
    LOADING8("loading8", 7, 2),
    MAP_MARKER("map_marker", 8, 2),
    MAP_POINTER("map_pointer", 9, 2),
    MOUSE("mouse", 10, 2),
    MOUSE_RIGHT("mouse_right", 11, 2),
    MOUSE_LEFT("mouse_left", 12, 2),
    MOUSE_MIDDLE("mouse_middle", 13, 2),
    SHIFT_KEY("shift_key", 14, 2),

    REDSTONE("redstone", 0, 3),
    REDSTONE_IMPULSE("redstone_impulse", 1, 3),
    NO_REDSTONE("no_redstone", 2, 3),
    NO_SHUFFLE("no_shuffle", 3, 3),
    SHUFFLE("shuffle", 4, 3),
    NO_LOOP("no_loop", 5, 3),
    LOOP("loop", 6, 3),
    LOOP_SINGLE("loop_single", 7, 3),
    LOCK("lock", 8, 3),
    UNLOCK("unlock", 9, 3),
    MUTE("mute", 10, 3),
    SOUND("sound", 11, 3),
    STAR_FILLED("star_filled", 12, 3),
    STAR("star", 13, 3),
    EDIT("edit", 14, 3),
    NO_EDIT("no_edit", 15, 3),

    MOTION_WAVE1_LEFT("motion_wave1_left", 0, 4),
    MOTION_WAVE2_LEFT("motion_wave2_left", 1, 4),
    MOTION_WAVE3_LEFT("motion_wave3_left", 2, 4),
    MOTION_WAVE1_RIGHT("motion_wave1_right", 3, 4),
    MOTION_WAVE2_RIGHT("motion_wave2_right", 4, 4),
    MOTION_WAVE3_RIGHT("motion_wave3_right", 5, 4),
    RADIUS("radius", 6, 4),
    BOX("box", 7, 4),
    CREEPER("creeper", 8, 4),

    EMPTY("empty", 0, 0);

    private String id;
    private int u;
    private int v;

    public static final int ICON_SIZE = 16;
    public static final ResourceLocation ICON_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockBeats.MOD_ID, "textures/gui/icons.png");;

    ModGuiIcons(String id, int u, int v) {
        this.id = id;
        this.u = u;
        this.v = v;
    }

    public String getId() {
        return id;
    }

    public int getUMultiplier() {
        return u;
    }

    public int getVMultiplier() {
        return v;
    }

    public int getU() {
        return u * ICON_SIZE;
    }

    public int getV() {
        return v * ICON_SIZE;
    }

    public static ModGuiIcons getByStringId(String id) {
        return Arrays.stream(values()).filter(x -> x.getId().equals(id)).findFirst().orElse(ModGuiIcons.EMPTY);
    }

    public void render(Graphics graphics, int x, int y) {
        GuiUtils.drawTexture(ModGuiIcons.ICON_LOCATION, graphics, x, y, getU(), getV(), ICON_SIZE, ICON_SIZE);
    }

    public Sprite getAsSprite(int renderWidth, int renderHeight) {
        return new Sprite(ICON_LOCATION, 256, 256, getU(), getV(), ICON_SIZE, ICON_SIZE, renderWidth, renderHeight);
    }
}