package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.List;
import de.mrjulsen.blockbeats.client.widgets.PlayerShareSelector;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextLabel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.network.chat.MutableComponent;

public class SharePopup extends PopupWindow {

    private final MutableComponent title = Utils.trans("share", "title");
    private final MutableComponent textStopShare = Utils.trans("share", "stop_share");
    private final MutableComponent descriptionStopShare = Utils.trans("share", "stop_share_description");

    private static final int WIN_WIDTH = 200;
    private static final int WIN_HEIGHT = 220;

    private DLButton cancelButton;
    private DLButton stopShareBtn;
    private DLScrollBar scrollBar;

    private final Rectangle definition;

    public SharePopup(DLWindowManager manager, SoundFile file) {
        super(manager, WIN_WIDTH, WIN_HEIGHT);
        
        this.definition = Rectangle.withSize(6, 40, WIN_WIDTH - 20, WIN_HEIGHT - 85);
        
        PlayerShareSelector container = addComponent(new PlayerShareSelector((int)definition.x() + 1, (int)definition.y() + 1, (int)definition.width() - 2, (int)definition.height(), file, () -> scrollBar));

        DLRichTextEditBox searchBox = addComponent(new DLRichTextEditBox((int)definition.x() + 1, (int)definition.y() - 19, (int)definition.width() - 2 + 8, 18));
        searchBox.placeholderText.set(TextUtils.TEXT_SEARCH);
        searchBox.addEventListener(DLRichTextLabel.TextChangedEvent.class, (s, e) -> {
            container.refresh(e.text().getPlainText());
            return false;
        });

        cancelButton = addComponent(new DLButton(WIN_WIDTH - 80 - 6, WIN_HEIGHT - 30, 80, 20));
        cancelButton.text.set(TextUtils.TEXT_CLOSE);
        cancelButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });
        
        stopShareBtn = addComponent(new DLButton(6, WIN_HEIGHT - 30, WIN_WIDTH - 80 - 16, 20));
        stopShareBtn.text.set(textStopShare);
        stopShareBtn.tooltip.set(new DLTooltip(List.of(descriptionStopShare), 200));
        stopShareBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            container.reload("");
            return false;
        });

        scrollBar = addComponent(new DLScrollBar((int)definition.right(), (int)definition.y(), 8, (int)definition.height(), Orientation.VERTICAL));
        scrollBar.scrollerSize.set(-1);
        scrollBar.screenSize.set(container.height());
        scrollBar.scrollSteps.set(15);
        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            container.setScrollOffsetY(scrollBar.value.get().intValue());
            return false;
        });        
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite(DefaultGuiTextures.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), 6, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }
}
