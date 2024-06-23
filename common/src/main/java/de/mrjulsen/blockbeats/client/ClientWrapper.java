package de.mrjulsen.blockbeats.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.block.entity.SoundPlayerBlockEntity;
import de.mrjulsen.blockbeats.client.screen.PlaylistScreen;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLListBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLListBox.DLListBoxItem;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.mixin.FontAccessor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class ClientWrapper {
    public static void openPlaylistScreen(SoundPlayerBlockEntity blockEntity) {
        DLScreen.setScreen(new PlaylistScreen(blockEntity));
    }

    @SuppressWarnings("resource")
    public static SoundLocation myLocation(String category) {
        return new SoundLocation(Minecraft.getInstance().level, BlockBeats.MOD_ID, String.format("%s/%s", category, Minecraft.getInstance().player.getUUID()));
    }

    @SuppressWarnings("resource")
    public static SoundLocation location(String category) {
        return new SoundLocation(Minecraft.getInstance().level, BlockBeats.MOD_ID, category);
    }



    public static <T> int filterListBox(DLListBox<T> box, Predicate<DLListBoxItem<T>> filter) {
        int count = 0;
        for (DLListBoxItem<T> item : box.getItems()) {
            if (!filter.test(item)) {
                item.set_visible(false);
                continue;
            }
            item.set_visible(true);
            item.setY(box.getY() + 1 + count * box.getItemHeight());
            count++;
        }
        box.getScrollBar().updateMaxScroll(count * box.getItemHeight() + 2);
        return count;
    }
    
    @SuppressWarnings("resource")
    public static MutableComponent textCutOff(MutableComponent text, int maxWidth) {
        MutableComponent dots = TextUtils.text("...");
        Font font = Minecraft.getInstance().font;
        int maxW = maxWidth - font.width(dots);
        
        if (font.width(text) > maxW) {
            return TextUtils.text(font.substrByWidth(text, maxW).getString()).append(dots);
        }
        return text;
    }

    @SuppressWarnings("resource")
    public static <T extends Enum<T> & ITranslatableEnum> List<FormattedText> getEnumTooltipData(String modid, Class<T> enumClass, T selected, int maxWidth) {
        List<FormattedText> c = new ArrayList<>();
        T enumValue = enumClass.getEnumConstants()[0];
        c.addAll(((FontAccessor) Minecraft.getInstance().font).getSplitter()
                .splitLines(TextUtils.translate(enumValue.getEnumDescriptionTranslationKey(modid)), maxWidth, Style.EMPTY));
        c.add(TextUtils.text(" "));
        for (T val : enumClass.getEnumConstants()) {
            String seq1 = String.format("> %s", TextUtils.translate(val.getValueTranslationKey(modid)).getString());
            String seq2 = TextUtils.translate(val.getValueInfoTranslationKey(modid)).getString();
            c.addAll(((FontAccessor)Minecraft.getInstance().font).getSplitter().splitLines(seq1, maxWidth, Style.EMPTY.withBold(true).withColor(val == selected ? ChatFormatting.GOLD : ChatFormatting.WHITE)));
            c.addAll(((FontAccessor)Minecraft.getInstance().font).getSplitter().splitLines(seq2, maxWidth, Style.EMPTY.withColor(val == selected ? ChatFormatting.YELLOW : ChatFormatting.GRAY)));
        }
        
        return c;
    }

    @SuppressWarnings("resource")
    public static List<FormattedText> split(Component text, int maxWidth, Style style) {
        return ((FontAccessor)Minecraft.getInstance().font).getSplitter().splitLines(text, maxWidth, style);
    }
}
