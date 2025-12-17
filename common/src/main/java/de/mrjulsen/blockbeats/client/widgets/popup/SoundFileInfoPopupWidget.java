package de.mrjulsen.blockbeats.client.widgets.popup;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.widgets.SoundFileInfoPanel;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;

public class SoundFileInfoPopupWidget extends PopupWindow {

    private final MutableComponent title = Utils.trans("sound_properties", "title");
    private final MutableComponent textShowFolder = Utils.trans("sound_properties", "show_folder");
    private final Rectangle definition;

    public SoundFileInfoPopupWidget(DLWindowManager manager, SoundFile file) {
        super(manager, 200, 180);
        definition = Rectangle.withSize(width() / 2 - 94, height() / 2 - 80 + 16, 180, 160 - 16 - 30);
        
        DLButton closeBtn = addComponent(new DLButton(width() / 2 + (DragNSounds.hasServer() ? 2 : -40), height() / 2 + 53, 80, 20));
        closeBtn.text.set(TextUtils.TEXT_CLOSE);
        closeBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });
        
        if (DragNSounds.hasServer()) {
            DLButton locationBtn = addComponent(new DLButton(width() / 2 - 82, height() / 2 + 53, 80, 20));
            locationBtn.text.set(textShowFolder);
            locationBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                try {
                    file.getLocation().setLevel(ServerEvents.getCurrentServer().overworld());
                    Util.getPlatform().openFile(file.getLocation().resolve().orElse(SoundLocation.getModDirectory(ServerEvents.getCurrentServer().overworld())).toFile());
                } catch (Exception ex) {
                    BlockBeats.LOGGER.error("Unable to open file location.", ex);
                }
                getWindowManager().closeWindow(this);
                return false;
            });
        }
        SoundFileInfoPanel container = addComponent(new SoundFileInfoPanel(file, (int)definition.x() + 1, (int)definition.y() + 1, (int)definition.width() - 1 - 8, (int)definition.height() - 2));

        DLScrollBar scrollBar = addComponent(new DLScrollBar((int)definition.right(), (int)definition.y(), 8, (int)definition.height(), Orientation.VERTICAL));
        scrollBar.scrollerSize.set(-1);
        scrollBar.screenSize.set(container.height());
        scrollBar.scrollSteps.set(15);
        scrollBar.max.set(container.maxRequiredHeight());
        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            container.setScrollOffsetY(scrollBar.value.get().intValue());
            return false;
        });
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite(DefaultGuiTextures.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2 - 94, height() / 2 - 80 + 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }
}
