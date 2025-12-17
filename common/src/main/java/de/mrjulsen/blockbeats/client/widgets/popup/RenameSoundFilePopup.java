package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextLabel;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

public class RenameSoundFilePopup extends PopupWindow {

    private final MutableComponent title = Utils.trans("rename_file", "title");
    private final MutableComponent textRename = Utils.trans("rename_file", "rename");
    private final MutableComponent hintEnterName = Utils.trans("rename_file", "enter_name");

    private static final int WIN_WIDTH = 200;
    private static final int WIN_HEIGHT = 78;

    public RenameSoundFilePopup(DLWindowManager manager, SoundFile file, Supplier<FileBrowserContainer> container) {
        super(manager, WIN_WIDTH, WIN_HEIGHT);
        AtomicReference<DLButton> doneBtn = new AtomicReference<>();

        DLRichTextEditBox box = addComponent(new DLRichTextEditBox(8, 25, WIN_WIDTH - 16, 18));
        box.placeholderText.set(hintEnterName);
        box.maxCharacters.set(BlockBeats.MAX_FILENAME_LENGTH);
        box.text.get().set(file.getDisplayName());
        box.addEventListener(DLRichTextLabel.TextChangedEvent.class, (s, e) -> {
            if (doneBtn.get() != null) {
                doneBtn.get().enabled.set(!e.text().getPlainText().isBlank());
            }
            return false;
        });        

        DLButton cancelBtn = addComponent(new DLButton(width() / 2 + 2, 25 + 25, 80, 20));
        cancelBtn.text.set(CommonComponents.GUI_CANCEL);
        cancelBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });

        DLButton doneButton = addComponent(new DLButton(width() / 2 - 2 - 80, 25 + 25, 80, 20));
        doneButton.text.set(textRename);
        doneButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            file.updateMetadata(Map.of(SoundFile.META_DISPLAY_NAME, box.text.get().getPlainText()));
            if (container.get() != null) {
                container.get().refresh();
            }
            getWindowManager().closeWindow(this);
            return false;
        });
        doneBtn.set(doneButton);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite(DefaultGuiTextures.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), 6, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }
}
