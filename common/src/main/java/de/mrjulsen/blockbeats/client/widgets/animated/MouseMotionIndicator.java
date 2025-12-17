package de.mrjulsen.blockbeats.client.widgets.animated;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public class MouseMotionIndicator extends DLGuiComponent {
    
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
        super(x, y, size, button);
        this.size = size;
        this.button = button;
        this.showWaves = showWaves;
        this.hold = hold;
    }

    @Override
    public void tick() {
        super.tick();
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
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
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
}
