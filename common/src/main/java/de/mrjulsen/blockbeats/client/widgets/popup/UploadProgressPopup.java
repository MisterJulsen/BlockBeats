package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.function.Consumer;

import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadState;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLProgressBar;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

public class UploadProgressPopup extends PopupWidget {

    private final MutableComponent textTitle = TextUtils.translate("gui." + DragNSounds.MOD_ID + ".upload.title");
    private final String keyConvert = "gui." + DragNSounds.MOD_ID + ".upload.convert";
    private final String keyUpload = "gui." + DragNSounds.MOD_ID + ".upload.upload";

    protected DLProgressBar progressBar;
    protected final long uploadId;
    protected DLButton cancelButton;

    protected UploadState currentState;

    public UploadProgressPopup(DLPopupScreen parent, int layer, long uploadId, int width, int height, Consumer<PopupWidget> close) {
        super(parent, layer, width, height, close);
        this.uploadId = uploadId;

        this.progressBar = addRenderableOnly(new DLProgressBar(width / 2 - 80, height / 2, 160, 0, 100, 0));
        this.progressBar.setBackColor(0xFF000000);
        this.progressBar.setBorderColor(DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
        this.progressBar.setBarColor(0xFFA4EB34);
        this.progressBar.setBufferBarColor(DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);

        cancelButton = addRenderableWidget(GuiUtils.createButton(width / 2 - 50, height / 2 + 20, 100, 20, CommonComponents.GUI_CANCEL, (btn) -> {
            ClientApi.cancelUpload(uploadId);            
        }));
        cancelButton.setRenderStyle(AreaStyle.DRAGONLIB);
    }

    @Override
    public void tick() {
        super.tick();
        cancelButton.active = ClientApi.canCancelUpload(uploadId);
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, new GuiAreaDefinition(width / 2 - 100, height / 2 - 50, 200, 100));
        GuiUtils.drawString(graphics, font, width / 2, height / 2 - 40, textTitle, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.CENTER, false);
        GuiUtils.drawString(graphics, font, width / 2, height / 2 - 20, currentState == UploadState.CONVERT ? TextUtils.translate(keyConvert, (int)progressBar.getValue()) : TextUtils.translate(keyUpload, (int)progressBar.getValue()), DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.CENTER, false);
    }
}
