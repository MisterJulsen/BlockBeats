package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

public class RenameSoundFilePopup extends PopupWidget {

    private final MutableComponent title = Utils.trans("rename_file", "title");
    private final MutableComponent textRename = Utils.trans("rename_file", "rename");
    private final MutableComponent hintEnterName = Utils.trans("rename_file", "enter_name");

    private static final int WIN_WIDTH = 200;
    private static final int WIN_HEIGHT = 78;

    private int guiLeft, guiTop;

    public RenameSoundFilePopup(DLPopupScreen parent, int layer, SoundFile file, Supplier<FileBrowserContainer> container, int width, int height, Consumer<PopupWidget> close) {
        super(parent, layer, width, height, close);

        guiLeft = width / 2 - WIN_WIDTH / 2;
        guiTop = height / 2 - WIN_HEIGHT / 2;
        AtomicReference<DLButton> doneBtn = new AtomicReference<>();

        DLEditBox box = addRenderableWidget(new DLEditBox(font, guiLeft + 8, guiTop + 25, WIN_WIDTH - 16, 18, hintEnterName));
        box.setMaxLength(BlockBeats.MAX_FILENAME_LENGTH);
        box.setValue(file.getDisplayName());
        box.withHint(hintEnterName);
        box.setResponder((text) -> {
            if (doneBtn.get() != null) {
                doneBtn.get().active = !text.isBlank();
            }
        });

        DLButton cancelBtn = addRenderableWidget(new DLButton(width / 2 + 2, guiTop + 25 + 25, 80, 20, CommonComponents.GUI_CANCEL, b -> close()));
        cancelBtn.setRenderStyle(AreaStyle.DRAGONLIB);

        doneBtn.set(addRenderableWidget(new DLButton(width / 2 - 2 - 80, guiTop + 25 + 25, 80, 20, textRename, b -> {
            file.updateMetadata(Map.of(SoundFile.META_DISPLAY_NAME, box.getValue()));
            if (container.get() != null) {
                container.get().refresh();
            }
            close();
        })));
        doneBtn.get().setRenderStyle(AreaStyle.DRAGONLIB);
        doneBtn.get().setBackColor(DragonLib.PRIMARY_BUTTON_COLOR);
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIN_WIDTH, WIN_HEIGHT);
        GuiUtils.drawString(graphics, font, guiLeft + 6, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
    }
}
