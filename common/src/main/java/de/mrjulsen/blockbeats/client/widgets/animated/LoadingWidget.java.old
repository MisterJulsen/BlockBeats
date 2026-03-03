package de.mrjulsen.blockbeats.client.widgets.animated;

import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class LoadingWidget implements Renderable, ITickable, GuiEventListener, NarratableEntry {
    
    private static final int MAX_SPRITES = 8;
    private int spriteIndex = 0;

    private int x;
    private int y;
    private int size;
    private int tickSpeed;
    private int ticks;
    
    public LoadingWidget(int x, int y, int size, int tickSpeed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.tickSpeed = Math.max(tickSpeed, 1);
    }

    @Override
    public void tick() {
        ticks++;
        if ((ticks %= tickSpeed) == 0) {
            spriteIndex++;
            spriteIndex %= MAX_SPRITES;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Graphics graphics = new Graphics(guiGraphics, guiGraphics.pose());
        switch (spriteIndex) {
            case 0 -> ModGuiIcons.LOADING1.getAsSprite(size, size).render(graphics, x, y);
            case 1 -> ModGuiIcons.LOADING2.getAsSprite(size, size).render(graphics, x, y);
            case 2 -> ModGuiIcons.LOADING3.getAsSprite(size, size).render(graphics, x, y);
            case 3 -> ModGuiIcons.LOADING4.getAsSprite(size, size).render(graphics, x, y);
            case 4 -> ModGuiIcons.LOADING5.getAsSprite(size, size).render(graphics, x, y);
            case 5 -> ModGuiIcons.LOADING6.getAsSprite(size, size).render(graphics, x, y);
            case 6 -> ModGuiIcons.LOADING7.getAsSprite(size, size).render(graphics, x, y);
            case 7 -> ModGuiIcons.LOADING8.getAsSprite(size, size).render(graphics, x, y);
        }
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

    public int getTickSpeed() {
        return tickSpeed;
    }

    public void setTickSpeed(int tickSpeed) {
        this.tickSpeed = Math.max(tickSpeed, 1);
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
