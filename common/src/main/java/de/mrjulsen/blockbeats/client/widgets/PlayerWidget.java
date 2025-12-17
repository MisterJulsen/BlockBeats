package de.mrjulsen.blockbeats.client.widgets;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.FlatButtonRenderer;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class PlayerWidget extends DLButton {

    public static final int HEIGHT = 18;

    private boolean picked;

    private final UUID playerId;
    private final DLSprite icon;
    private final String name;

    public PlayerWidget(int pX, int pY, int pWidth, UUID playerId, String name, @Nullable ResourceLocation skinLocation, Consumer<PlayerWidget> pOnPress, Collection<TaskBuilder> fileTasks) {
        super(pX, pY, pWidth, HEIGHT);
        text.set(TextUtils.EMPTY);
        addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            pOnPress.accept(this);
            return false;
        });
           
        this.playerId = playerId;
        this.name = name;
        this.icon = getSkinIcon(skinLocation);

        FlowLayout layout = new FlowLayout();
        layout.flowDirection.set(Direction.HORIZONTAL);
        layout.wrap.set(false);
        this.layout.set(layout);

        for (TaskBuilder task : fileTasks) {                
            addTask(task.sprite(), task.text(), task.action());
        }

        addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {            
            picked = !picked;
            return false;
        });
    }

    private DLSprite getSkinIcon(ResourceLocation skinLocation) {
        if (skinLocation != null) {
            return new DLSprite(new DLTexture(skinLocation, 64, 64), 16, 16, 8, 8, 8, 8);
        }
        return ModGuiIcons.PLAYER.getAsSprite(16, 16);
    }

    public void addTask(DLSprite sprite, MutableComponent text, Consumer<PlayerWidget> action) {
        DLButton btn = addComponent(new DLButton(0, 0, 18, 18));
        btn.layoutContraint.set(FlowLayout.FlowConstraint.END);
        btn.componentRenderer.set(FlatButtonRenderer.INSTANCE);
        btn.text.set(TextUtils.empty());
        btn.tooltip.set(new DLTooltip(List.of(text), 200));
        btn.icon.set(sprite);
        btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            action.accept(this);
            return false;
        });
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean isPicked() {
        return picked;
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        icon.render(graphics, x() + 1, y() + 1);
        if (isSelected()) {
            GuiUtils.drawBox(graphics, 0, 0, width(), height(), DLColor.fromInt(0x339E9E9E), DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR);
        }
        if (isSelected()) {
            GuiUtils.drawBox(graphics, 0, 0, width(), height(), DLColor.fromInt(0x33FFFFFF), DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR);
        }
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5 + icon.getWidth(), height() / 2 - graphics.defaultFont().lineHeight / 2, name, isSelected() || isPicked() ? DragonLib.VANILLA_BUTTON_HIGHLIGHTED_FONT_COLOR : DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        icon.render(graphics, 1, 1);
    }

    public void pick(boolean b) {
        picked = b;
    }

    public static record TaskBuilder(DLSprite sprite, MutableComponent text, Consumer<PlayerWidget> action) {}    
}
