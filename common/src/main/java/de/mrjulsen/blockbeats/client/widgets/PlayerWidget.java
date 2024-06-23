package de.mrjulsen.blockbeats.client.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ScrollableWidgetContainer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class PlayerWidget extends DLButton {

    public static final int HEIGHT = 18;

    private boolean selected;

    private final UUID playerId;
    private final Sprite icon;
    private final String name;
    private final DLPopupScreen parent;
    private final ScrollableWidgetContainer parentContainer;

    // Buttons
    private int taskIndex = 1;
    private Collection<Task> tasks = new ArrayList<>();

    public PlayerWidget(DLPopupScreen parent, ScrollableWidgetContainer parentContainer, int pX, int pY, int pWidth, UUID playerId, String name, @Nullable ResourceLocation skinLocation, Consumer<PlayerWidget> pOnPress, Collection<TaskBuilder> fileTasks) {
        super(pX, pY, pWidth, HEIGHT, TextUtils.empty(), pOnPress);        
        this.parent = parent;
        this.playerId = playerId;
        this.name = name;
        this.parentContainer = parentContainer;
        this.icon = getSkinIcon(skinLocation);

        for (TaskBuilder task : fileTasks) {                
            addTask(task.sprite(), task.text(), task.action());
        }
    }

    private Sprite getSkinIcon(ResourceLocation skinLocation) {
        if (skinLocation != null) {
            return new Sprite(skinLocation, 64, 64, 8, 8, 8, 8, 16, 16);
        }
        return ModGuiIcons.PLAYER.getAsSprite(16, 16);
    }

    public void addTask(Sprite sprite, MutableComponent text, Consumer<PlayerWidget> action) {
        tasks.add(new Task(getParent(), new GuiAreaDefinition(x() + width - 18 * taskIndex, y() + height / 2 - 9, 18, 18), sprite, DLTooltip.of(text).assignedTo(this).withMaxWidth(width), action));
        taskIndex++;
    }

    public DLPopupScreen getParent() {
        return parent;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        icon.render(graphics, x() + 1, y() + 1);
        if (isSelected()) {
            GuiUtils.drawBox(graphics, GuiAreaDefinition.of(this), 0x339E9E9E, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
        }
        if (isMouseSelected()) {
            GuiUtils.drawBox(graphics, GuiAreaDefinition.of(this), 0x33FFFFFF, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE);
            tasks.forEach(x -> x.render(graphics, mouseX, mouseY));
        }
        GuiUtils.drawString(graphics, font, x() + 5 + icon.getWidth(), y() + getHeight() / 2 - font.lineHeight / 2, name, isMouseSelected() || isSelected() ? DragonLib.NATIVE_BUTTON_FONT_COLOR_HIGHLIGHT : DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);
    }    

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        if (getParent().getAllowedLayer() == parentContainer.getWidgetLayerIndex()) {
            tasks.forEach(x -> x.renderTooltip(graphics, mouseX, mouseY, (int)parentContainer.getXScrollOffset(), (int)parentContainer.getYScrollOffset()));
        }
    }

    @Override
    public void onClick(double d, double e) {
        selected = !selected;
        super.onClick(d, e);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button)) {
            Optional<Task> task = tasks.stream().filter(x -> x.isOver((int)mouseX, (int)mouseY)).findFirst();
            if (task.isPresent()) {
                task.get().run(this);
                GuiUtils.playButtonSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void select(boolean b) {
        selected = b;
    }

    public static class Task {
        private final GuiAreaDefinition area;
        private final Sprite sprite;
        private final Consumer<PlayerWidget> action;
        private final DLPopupScreen parent;
        private final DLTooltip tooltip;
        
        public Task(DLPopupScreen parent, GuiAreaDefinition area, Sprite sprite, DLTooltip tooltip, Consumer<PlayerWidget> action) {
            this.area = area;
            this.sprite = sprite;
            this.action = action;
            this.parent = parent;
            this.tooltip = tooltip;
        }

        public boolean isOver(int mouseX, int mouseY) {
            return area.isInBounds(mouseX, mouseY);
        }

        public void run(PlayerWidget widget) {
            action.accept(widget);
        }

        public void render(Graphics graphics, int mouseX, int mouseY) {
            sprite.render(graphics, area.getX() + area.getWidth() / 2 - ModGuiIcons.ICON_SIZE / 2, area.getY() + area.getHeight() / 2 - ModGuiIcons.ICON_SIZE / 2);
            if (isOver(mouseX, mouseY)) {                
                GuiUtils.drawBox(graphics, area, 0x44FFFFFF, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE);
            }
        }        

        public void renderTooltip(Graphics graphics, int mouseX, int mouseY, int xOffset, int yOffset) {
            if (isOver(mouseX + xOffset, mouseY + yOffset)) {
                GuiUtils.renderTooltipAt(parent, GuiAreaDefinition.of(tooltip.getAssignedWidget()), tooltip.getLines(), tooltip.getMaxWidth(), graphics, mouseX + 8, mouseY - 16, mouseX + xOffset, mouseY + yOffset, 0, 0);
            }
        }
    }

    public static record TaskBuilder(Sprite sprite, MutableComponent text, Consumer<PlayerWidget> action) {}    
}
