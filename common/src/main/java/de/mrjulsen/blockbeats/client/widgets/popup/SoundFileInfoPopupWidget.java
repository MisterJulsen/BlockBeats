package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.function.Consumer;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.SoundFileInfoPanel;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLVerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;

public class SoundFileInfoPopupWidget extends PopupWidget {

    private final MutableComponent title = Utils.trans("sound_properties", "title");
    private final MutableComponent textShowFolder = Utils.trans("sound_properties", "show_folder");
    private final GuiAreaDefinition definition;

    public SoundFileInfoPopupWidget(DLPopupScreen parent, int layer, SoundFile file, int width, int height, Consumer<PopupWidget> close) {
        super(parent, layer, width, height, close);
        definition = new GuiAreaDefinition(width / 2 - 94, height / 2 - 80 + 16, 180, 160 - 16 - 30);
        DLButton closeBtn = addRenderableWidget(new DLButton(width / 2 + (DragNSounds.hasServer() ? 2 : -40), height / 2 + 53, 80, 20, DragonLib.TEXT_CLOSE, b -> close()));
        closeBtn.setRenderStyle(AreaStyle.DRAGONLIB);
        if (DragNSounds.hasServer()) {
            DLButton locationBtn = addRenderableWidget(new DLButton(width / 2 - 82, height / 2 + 53, 80, 20, textShowFolder, b -> {
                try {
                    file.getLocation().setLevel(ServerEvents.getCurrentServer().overworld());
                    Util.getPlatform().openFile(file.getLocation().resolve().orElse(SoundLocation.getModDirectory(ServerEvents.getCurrentServer().overworld())).toFile());
                } catch (Exception e) {
                    BlockBeats.LOGGER.error("Unable to open file location.", e);
                }
                close();
            }));
            locationBtn.setRenderStyle(AreaStyle.DRAGONLIB);
        }
        SoundFileInfoPanel container = addRenderableOnly(new SoundFileInfoPanel(file, definition.getX() + 1, definition.getY() + 1, definition.getWidth() - 1 - 8, definition.getHeight() - 2));
        addRenderableWidget(new DLVerticalScrollBar(definition.getRight(), definition.getY(), 8, definition.getHeight(), null))
            .setAutoScrollerSize(true)
            .setScreenSize(container.getHeight())
            .setStepSize(8)
            .updateMaxScroll(container.maxRequiredHeight() - container.getHeight())
            .withOnValueChanged((scrollbar) -> container.setYScrollOffset(scrollbar.getScrollValue()))
        ;
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, width / 2 - 100, height / 2 - 80, 200, 160);
        DynamicGuiRenderer.renderContainerBackground(graphics, definition);
        GuiUtils.drawString(graphics, font, width / 2 - 94, height / 2 - 80 + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
    }
}
