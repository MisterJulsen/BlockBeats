package de.mrjulsen.blockbeats.client.widgets;

import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public class SoundFileInfoPanel extends DLGuiComponent {

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
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite("container").render(graphics, 0, 0, width(), height());

        lineY = 0;
        graphics.poseStack().pushPose();
        float scale = 0.75f;
        graphics.poseStack().scale(scale, scale, 1);
        graphics.poseStack().translate(0, -getScrollOffsetY(), 0);
        final int x = 0;
        final int y = 0;

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

    private void drawHeadline(DLGuiGraphics graphics, int lx, int ly, float scale, MutableComponent key) {
        lineY += LINE_HEIGHT;
        GuiUtils.drawString(graphics, graphics.defaultFont(), lx + 8, ly + 8 + (int)(lineY), key.withStyle(ChatFormatting.BOLD), DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        lineY += LINE_HEIGHT;
    }

    private void drawInfo(DLGuiGraphics graphics, int lx, int ly, float scale, MutableComponent key, String value) {
        GuiUtils.drawString(graphics, graphics.defaultFont(), lx + 12, ly + 8 + (int)(lineY), key, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        lineY += LINE_HEIGHT;
        GuiUtils.drawString(graphics, graphics.defaultFont(), lx + 16, ly + 8 + (int)(lineY), TextUtils.text(value).withStyle(ChatFormatting.ITALIC), DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        lineY += LINE_HEIGHT;
    }

    public int maxRequiredHeight() {
        return LINE_HEIGHT * 2 * (LINES + file.getMetadata().size()) + 16;
    }
    
}
