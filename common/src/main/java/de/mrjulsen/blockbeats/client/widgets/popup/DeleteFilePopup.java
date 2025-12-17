package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

public class DeleteFilePopup extends PopupWindow {

    private final MutableComponent title = Utils.trans("delete", "title");
    private final String keyRename = "gui." + BlockBeats.MOD_ID + ".delete.instruction";

    private static final int WIN_WIDTH = 200;
    private static final int WIN_HEIGHT = 100;

    private MultiLineLabel instructionLabel;

    public DeleteFilePopup(DLWindowManager manager, SoundFile file, Supplier<FileBrowserContainer> container, int width, int height) {
        super(manager, WIN_WIDTH, WIN_HEIGHT);

        instructionLabel = MultiLineLabel.create(Minecraft.getInstance().font, TextUtils.translate(keyRename, file.getDisplayName()), WIN_WIDTH - 20);

        DLButton noBtn = addComponent(new DLButton(width() / 2 + 2, WIN_HEIGHT - 30, 50, 20));
        noBtn.text.set(CommonComponents.GUI_NO);
        noBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });

        DLButton yesBtn = addComponent(new DLButton(width / 2 - 2 - 50, WIN_HEIGHT - 30, 50, 20));
        yesBtn.text.set(CommonComponents.GUI_YES);
        yesBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            ClientApi.deleteSound(file, (status) -> {
                if (container.get() != null) {
                    container.get().refresh();
                }
            });
            getWindowManager().closeWindow(this);
            return false;
        });
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite(DefaultGuiTextures.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        instructionLabel.renderLeftAlignedNoShadow(graphics.graphics(), 10, 25, graphics.defaultFont().lineHeight, DragonLib.VANILLA_UI_FONT_COLOR.getAsARGB());
        GuiUtils.drawString(graphics, graphics.defaultFont(), 6, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }
}
