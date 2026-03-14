package de.mrjulsen.blockbeats.client.widgets;

import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLScrollableWidgetContainer;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.MutableComponent;

public class SoundFileInfoPanel extends DLScrollableWidgetContainer {

    private static final int LINE_HEIGHT = 12;

    private static final int LINES = 17;

    private final MutableComponent textDisplayName = Utils.trans("file_info", "display_name");
    private final MutableComponent textTitle = Utils.trans("file_info", "title");
    private final MutableComponent textArtist = Utils.trans("file_info", "artist");
    private final MutableComponent textFileSize = Utils.trans("file_info", "size");
    private final MutableComponent textFileId = Utils.trans("file_info", "id");
    private final MutableComponent textLocation = Utils.trans("file_info", "location");
    private final MutableComponent textOwner = Utils.trans("file_info", "owner");
    private final MutableComponent textUploadTime = Utils.trans("file_info", "upload_time");
    private final MutableComponent textAlbum = Utils.trans("file_info", "album");
    private final MutableComponent textGenre = Utils.trans("file_info", "genre");
    private final MutableComponent textDate = Utils.trans("file_info", "date");
    private final MutableComponent textDuration = Utils.trans("file_info", "duration");

    private final MutableComponent textHeadlineFile = Utils.trans("file_info", "headline_file_info");
    private final MutableComponent textHeadlineAudio = Utils.trans("file_info", "headline_audio");
    private final MutableComponent textHeadlineMetadata = Utils.trans("file_info", "headline_metadata");

    private final SoundFile file;
    private int lineY = 0;


    public SoundFileInfoPanel(SoundFile file, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.file = file;
    }

    @Override
    public void renderMainLayerScrolled(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainLayerScrolled(graphics, mouseX, mouseY, partialTicks);
        lineY = 0;
        graphics.poseStack().pushPose();
        float scale = 0.75f;
        graphics.poseStack().scale(scale, scale, 1);
        final int x = (int)(getX() / scale);
        final int y = (int)(getY() / scale);

        drawInfo(graphics, x, y, scale, textDisplayName, file.getDisplayName());
        drawInfo(graphics, x, y, scale, textTitle, file.getInfo().getOriginalTitle());
        drawInfo(graphics, x, y, scale, textArtist, file.getInfo().getArtist());
        drawInfo(graphics, x, y, scale, textDuration, String.valueOf(Utils.formatDurationMs(file.getInfo().getDuration())));
        drawHeadline(graphics, x, y, scale, textHeadlineFile);
        drawInfo(graphics, x, y, scale, textFileSize, IOUtils.formatBytes(file.getInfo().getSize()));
        drawInfo(graphics, x, y, scale, textFileId, file.getId().toString());
        drawInfo(graphics, x, y, scale, textLocation, file.getLocation().toString());
        drawInfo(graphics, x, y, scale, textOwner, file.getInfo().getOwnerId().toString());
        drawInfo(graphics, x, y, scale, textUploadTime, file.getInfo().getUploadTimeFormatted());
        drawHeadline(graphics, x, y, scale, textHeadlineAudio);
        drawInfo(graphics, x, y, scale, textTitle, file.getInfo().getOriginalTitle());
        drawInfo(graphics, x, y, scale, textArtist, file.getInfo().getArtist());
        drawInfo(graphics, x, y, scale, textAlbum, file.getInfo().getAlbum());
        drawInfo(graphics, x, y, scale, textGenre, file.getInfo().getGenre());
        drawInfo(graphics, x, y, scale, textDate, file.getInfo().getDate());
        drawHeadline(graphics, x, y, scale, textHeadlineMetadata);
        file.getMetadata().forEach((k, v) -> {            
            drawInfo(graphics, x, y, scale, TextUtils.text(k), v);
        });

        graphics.poseStack().popPose();
    }

    private void drawHeadline(Graphics graphics, int lx, int ly, float scale, MutableComponent key) {
        lineY += LINE_HEIGHT;
        GuiUtils.drawString(graphics, font, lx + 8, ly + 8 + (int)(lineY), key.withStyle(ChatFormatting.BOLD), DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);
        lineY += LINE_HEIGHT;
    }

    private void drawInfo(Graphics graphics, int lx, int ly, float scale, MutableComponent key, String value) {
        GuiUtils.drawString(graphics, font, lx + 12, ly + 8 + (int)(lineY), key, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);
        lineY += LINE_HEIGHT;
        GuiUtils.drawString(graphics, font, lx + 16, ly + 8 + (int)(lineY), TextUtils.text(value).withStyle(ChatFormatting.ITALIC), DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.LEFT, false);
        lineY += LINE_HEIGHT;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput var1) {
    }

    public int maxRequiredHeight() {
        return LINE_HEIGHT * 2 * (LINES + file.getMetadata().size()) + 16;
    }

    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }
    
}
