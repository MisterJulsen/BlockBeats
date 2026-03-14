package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.PlayerShareSelector;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLVerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.MutableComponent;

public class SharePopup extends PopupWidget {

    private final MutableComponent title = Utils.trans("share", "title");
    private final MutableComponent textStopShare = Utils.trans("share", "stop_share");
    private final MutableComponent descriptionStopShare = Utils.trans("share", "stop_share_description");

    private static final int WIN_WIDTH = 200;
    private static final int WIN_HEIGHT = 220;
    private int guiLeft, guiTop;

    private DLButton cancelButton;
    private DLButton stopShareBtn;
    private DLVerticalScrollBar scrollBar;
    private final Collection<DLTooltip> tooltips = new ArrayList<>();

    private final GuiAreaDefinition definition;

    public SharePopup(DLPopupScreen parent, int layer, int width, int height, SoundFile file, Consumer<PopupWidget> close) {
        super(parent, layer, width, height, close);
        guiLeft = width / 2 - WIN_WIDTH / 2;
        guiTop = height / 2 - WIN_HEIGHT / 2;
        this.definition = new GuiAreaDefinition(guiLeft + 6, guiTop + 40, WIN_WIDTH - 20, WIN_HEIGHT - 85);
        
        PlayerShareSelector container = addRenderableWidget(new PlayerShareSelector(parent, definition.getX() + 1, definition.getY() + 1, definition.getWidth() - 2, definition.getHeight(), file, this::getScrollBar));
        container.setWidgetLayerIndex(this.getWidgetLayerIndex());

        DLEditBox searchBox = addRenderableWidget(new DLEditBox(font, definition.getX() + 1, definition.getY() - 19, definition.getWidth() - 2 + 8, 18, TextUtils.empty()));
        searchBox.setResponder((value) -> {
            container.refresh(value);
        });
        searchBox.withHint(DragonLib.TEXT_SEARCH);

        cancelButton = addRenderableWidget(new DLButton(guiLeft + WIN_WIDTH - 80 - 6, guiTop + WIN_HEIGHT - 30, 80, 20, DragonLib.TEXT_CLOSE, (btn) -> close()));
        cancelButton.setRenderStyle(AreaStyle.DRAGONLIB);
        stopShareBtn = addRenderableWidget(new DLButton(guiLeft + 6, guiTop + WIN_HEIGHT - 30, WIN_WIDTH - 80 - 16, 20, textStopShare,
        (btn) -> {
            container.reload("");
        }));
        stopShareBtn.setRenderStyle(AreaStyle.DRAGONLIB);
        stopShareBtn.setBackColor(DragonLib.ERROR_BUTTON_COLOR);
        tooltips.add(DLTooltip.of(descriptionStopShare).assignedTo(stopShareBtn).withMaxWidth(width / 4));

        scrollBar = addRenderableWidget(new DLVerticalScrollBar(definition.getRight(), definition.getY(), 8, definition.getHeight(), definition))
            .setAutoScrollerSize(true)
            .setScreenSize(container.getHeight())
            .setStepSize(15)
            .setMaxScroll(container.maxRequiredHeight())
            .withOnValueChanged((scrollbar) -> container.setYScrollOffset(scrollbar.getScrollValue()))
        ;
    }

    private DLVerticalScrollBar getScrollBar() {
        return scrollBar;
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIN_WIDTH, WIN_HEIGHT);
        DynamicGuiRenderer.renderContainerBackground(graphics, definition);
        GuiUtils.drawString(graphics, font, guiLeft + 6, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        tooltips.forEach(x -> x.render(getParent(), graphics, mouseX, mouseY));
    }
}
