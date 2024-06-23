package de.mrjulsen.blockbeats.client.widgets.animated;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class MouseMotionIndicator implements Renderable, ITickable, GuiEventListener, NarratableEntry {
    
    private static final int MAX_SPRITES = 4;
    private static final int TICK_SPEED = 15;
    private int spriteIndex = 0;
    private int spriteIndex2 = 0;

    private int angle = 0;

    private int x;
    private int y;
    private int size;
    private int ticks;

    private final int button;
    private final boolean showWaves;
    private final boolean hold;
    
    public MouseMotionIndicator(int x, int y, int size, int button, boolean showWaves, boolean hold) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.button = button;
        this.showWaves = showWaves;
        this.hold = hold;
    }

    @Override
    public void tick() {
        angle += 6;
        if (angle > 360) {
            angle = 0;
        }

        ticks++;
        if ((ticks %= TICK_SPEED) == 0) {
            spriteIndex++;
            spriteIndex %= MAX_SPRITES;
            if (!hold) {                
                spriteIndex2++;
                spriteIndex2 %= 2;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Graphics graphics = new Graphics(guiGraphics, guiGraphics.pose());
        if (showWaves) {
            switch (spriteIndex) {
                case 0 -> {
                    ModGuiIcons.EMPTY.getAsSprite(size, size).render(graphics, x, y);
                    ModGuiIcons.EMPTY.getAsSprite(size, size).render(graphics, x + size * 3, y);
                }
                case 1 -> {
                    ModGuiIcons.MOTION_WAVE1_LEFT.getAsSprite(size, size).render(graphics, x, y);
                    ModGuiIcons.MOTION_WAVE1_RIGHT.getAsSprite(size, size).render(graphics, x + size * 3, y);
                }
                case 2 -> {
                    ModGuiIcons.MOTION_WAVE2_LEFT.getAsSprite(size, size).render(graphics, x, y);
                    ModGuiIcons.MOTION_WAVE2_RIGHT.getAsSprite(size, size).render(graphics, x + size * 3, y);
                }
                case 3 -> {
                    ModGuiIcons.MOTION_WAVE3_LEFT.getAsSprite(size, size).render(graphics, x, y);
                    ModGuiIcons.MOTION_WAVE3_RIGHT.getAsSprite(size, size).render(graphics, x + size * 3, y);
                }
            }
        }

        double offsetX = Math.sin(Math.toRadians(angle)) * 5;
        ModGuiIcons icon = ModGuiIcons.MOUSE;
        if (spriteIndex2 == 0) {
            switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> icon = ModGuiIcons.MOUSE_LEFT;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> icon = ModGuiIcons.MOUSE_RIGHT;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> icon = ModGuiIcons.MOUSE_MIDDLE;
            }
        }
        icon.getAsSprite(size, size).render(graphics, (int)(x + size * (showWaves ? 1.5F : 0.5f) + offsetX), y);
    }
    

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getWidgetHeight() {
        return size;
    }

    public int getWidgetWidth() {
        return showWaves ? size * 4 : size * 2;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocused(boolean focused) {}
}
