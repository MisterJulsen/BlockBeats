package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.function.Consumer;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

public class DeleteFilePopup extends PopupWidget {

    private final MutableComponent title = Utils.trans("delete", "title");
    private final String keyRename = "gui." + BlockBeats.MOD_ID + ".delete.instruction";

    private static final int WIN_WIDTH = 200;
    private static final int WIN_HEIGHT = 100;

    private int guiLeft, guiTop;

    private MultiLineLabel instructionLabel;

    public DeleteFilePopup(DLPopupScreen parent, int layer, SoundFile file, Supplier<FileBrowserContainer> container, int width, int height, Consumer<PopupWidget> close) {
        super(parent, layer, width, height, close);

        guiLeft = width / 2 - WIN_WIDTH / 2;
        guiTop = height / 2 - WIN_HEIGHT / 2;

        instructionLabel = MultiLineLabel.create(font, TextUtils.translate(keyRename, file.getDisplayName()), WIN_WIDTH - 20);

        DLButton noBtn = addRenderableWidget(new DLButton(width / 2 + 2, guiTop + WIN_HEIGHT - 30, 50, 20, CommonComponents.GUI_NO, b -> close()));
        DLButton yesBtn = addRenderableWidget(new DLButton(width / 2 - 2 - 50, guiTop + WIN_HEIGHT - 30, 50, 20, CommonComponents.GUI_YES, b -> {
            ClientApi.deleteSound(file, (status) -> {
                if (container.get() != null) {
                    container.get().refresh();
                }
            });
            close();
        }));
        noBtn.setRenderStyle(AreaStyle.DRAGONLIB);
        yesBtn.setRenderStyle(AreaStyle.DRAGONLIB);
        yesBtn.setBackColor(DragonLib.ERROR_BUTTON_COLOR);
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIN_WIDTH, WIN_HEIGHT);
        instructionLabel.renderLeftAlignedNoShadow(graphics.poseStack(), guiLeft + 10, guiTop + 25, font.lineHeight, DragonLib.NATIVE_UI_FONT_COLOR);
        GuiUtils.drawString(graphics, font, guiLeft + 6, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
    }
}
