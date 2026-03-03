package de.mrjulsen.blockbeats.client.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.math.Axis;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.widgets.animated.MouseMotionIndicator;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.render.MapImage;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec2;

public class MapAreaSelectionWidget extends DLGuiComponent {

    private final MutableComponent textDrag = Utils.trans("map", "drag_select");
    private final MutableComponent textSelect = Utils.trans("map", "select");
    private final MutableComponent textMove = Utils.trans("map", "move");
    private final MutableComponent textCancel = Utils.trans("map", "cancel_selection");
    private final MutableComponent textZoom = Utils.trans("map", "zoom");
    private final String keySize = "gui." + BlockBeats.MOD_ID + ".map.selection_size";
    private final String keyPos1 = "gui." + BlockBeats.MOD_ID + ".map.selection_pos1";
    private final String keyPos2 = "gui." + BlockBeats.MOD_ID + ".map.selection_pos2";

    private final MapImage map;
    private final int mapWidth;
    private final int mapHeight;
    private final BiConsumer<Vec2, Vec2> onSelected;

    private double mapX, mapY;
    private int selX1;
    private int selY1;
    private int selX2;
    private int selY2;
    private boolean isSelecting = false;

    private int areaX1 = 0;
    private int areaY1 = 0;
    private int areaX2 = 0;
    private int areaY2 = 0;

    private MouseMotionIndicator mapDragIndicator;
    private MouseMotionIndicator mapSelectIndicator;

    // Cache
    private int dispX1 = -1;
    private int dispY1 = -1;
    private int dispX2 = -1;
    private int dispY2 = -1;

    public MapAreaSelectionWidget(int x, int y, int width, int height, BlockPos center, int mapWidth, int mapHeight, BiConsumer<Vec2, Vec2> onSelected) {
        super(x, y, width, height);
        this.cursor.set(CursorType.CROSSHAIR);
        this.map = new MapImage(Minecraft.getInstance().level, center, center.getY(), mapWidth, mapHeight, false, 4);
        this.onSelected = onSelected;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        
        mapDragIndicator = addComponent(new MouseMotionIndicator(5, 5, 16, GLFW.GLFW_MOUSE_BUTTON_LEFT, false, true));
        mapSelectIndicator = addComponent(new MouseMotionIndicator(5, 5, 16, GLFW.GLFW_MOUSE_BUTTON_LEFT, false, true));

        setArea(0, 0, 0, 0);
        centerViewToPoint(map.getCenterPosOnMap().x, map.getCenterPosOnMap().y);

        addEventListener(DLGuiStandardEvents.MouseDownEvent.class, (s, e) -> {
            if (DLWindowManager.hasShiftDown() && e.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                isSelecting = true;
                selX1 = mouseXMapCoord(e.mouseX());
                selY1 = mouseYMapCoord(e.mouseY());
                selX2 = selX1;
                selY2 = selY1;
            }
            return false;
        });

        addEventListener(DLGuiStandardEvents.DragEvent.class, (s, e) -> {
            if (DLWindowManager.hasShiftDown() && e.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                selX2 = mouseXMapCoord(e.mouseX());
                selY2 = mouseYMapCoord(e.mouseY());
            } else if (e.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                setViewTo(mapX + e.dragX(), mapY + e.dragY());
            }

            return false;
        });

        addEventListener(DLGuiStandardEvents.KeyReleaseEvent.class, (s, e) -> {            
            if (!DLWindowManager.hasShiftDown()) {
                finishSelection(false);
            }
            return false;
        });

        addEventListener(DLGuiStandardEvents.MouseReleaseEvent.class, (s, e) -> {
            finishSelection(true);
            return false;
        });

        addEventListener(DLGuiStandardEvents.ScrollEvent.class, (s, e) -> {
            int coordX = mouseXMapCoord(width() / 2);
            int coordY = mouseYMapCoord(height() / 2);
            this.map.setScale(MathUtils.clamp((int)(map.getScale() - e.deltaY()), 1, 8));
            centerViewToPoint(coordX, coordY);
            return false;
        });
    }
    
    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getAreaX1() {
        return areaX1;
    }

    public int getAreaY1() {
        return areaY1;
    }

    public int getAreaX2() {
        return areaX2;
    }

    public int getAreaY2() {
        return areaY2;
    }
    
    public void setArea(int x1, int y1, int x2, int y2) {
        this.areaX1 = (int)(map.getCenterPosOnMap().x + x1);
        this.areaY1 = (int)(map.getCenterPosOnMap().y + y1);
        this.areaX2 = (int)(map.getCenterPosOnMap().x + x2);
        this.areaY2 = (int)(map.getCenterPosOnMap().y + y2);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        this.map.render(graphics, (int)(mapX), (int)(mapY));

        // Draw area
        int x1 = Math.min(areaX1, areaX2);
        int y1 = Math.min(areaY1, areaY2);
        int x2 = Math.max(areaX1, areaX2);
        int y2 = Math.max(areaY1, areaY2);
        GuiUtils.drawBox(graphics, mapXToAbs(x1), mapYToAbs(y1), (x2 - x1 + 1) * map.getScale(), (y2 - y1 + 1) * map.getScale(), DLColor.fromInt(0x44FF0000), DLColor.fromInt(0xFFFF0000));

        // Draw selection
        if (selX1 > -1 && selY1 > -1) {
            dispX1 = Math.min(selX1, selX2);
            dispY1 = Math.min(selY1, selY2);
            dispX2 = Math.max(selX1, selX2);
            dispY2 = Math.max(selY1, selY2);
            GuiUtils.fill(graphics, mapXToAbs(dispX1), mapYToAbs(dispY1), (dispX2 - dispX1 + 1) * map.getScale(), (dispY2 - dispY1 + 1) * map.getScale(), DLColor.fromInt(0x88FF0000));            
        }
        if (isSelected()) {
            GuiUtils.drawBox(graphics, mouseXToMap(mouseX), mouseYToMap(mouseY), map.getScale(), map.getScale(), DLColor.fromInt(0x44FF0000), DLColor.fromInt(0xFFFF0000));
        }
        
        if (DLWindowManager.hasShiftDown()) {
            //mapSelectIndicator.render(graphics.graphics(), mouseX, mouseY, partialTicks);
            ModGuiIcons.SHIFT_KEY.getAsSprite(16, 16).render(graphics, mapSelectIndicator.x() + 8, mapSelectIndicator.y() + 16);
            GuiUtils.drawString(graphics, graphics.defaultFont(), mapSelectIndicator.x() + mapSelectIndicator.width() + 20, mapSelectIndicator.y() + mapSelectIndicator.height() / 2 - graphics.defaultFont().lineHeight / 2, textDrag, DLColor.WHITE, ETextAlignment.LEFT, true);
            GuiUtils.drawString(graphics, graphics.defaultFont(), mapSelectIndicator.x() + mapSelectIndicator.width() + 20, 16 + mapSelectIndicator.y() + mapSelectIndicator.height() / 2 - graphics.defaultFont().lineHeight / 2, textCancel, DLColor.WHITE, ETextAlignment.LEFT, true);
        } else {
            //mapDragIndicator.render(graphics.graphics(), mouseX, mouseY, partialTicks);
            ModGuiIcons.SHIFT_KEY.getAsSprite(16, 16).render(graphics, mapDragIndicator.x(), mapDragIndicator.y() + 16);
            ModGuiIcons.MOUSE_LEFT.getAsSprite(16, 16).render(graphics, mapDragIndicator.x() + 16, mapDragIndicator.y() + 16);
            ModGuiIcons.MOUSE_MIDDLE.getAsSprite(16, 16).render(graphics, mapDragIndicator.x() + 8, mapDragIndicator.y() + 32);
            GuiUtils.drawString(graphics, graphics.defaultFont(), mapDragIndicator.x() + mapDragIndicator.width() + 20, mapDragIndicator.y() + mapDragIndicator.height() / 2 - graphics.defaultFont().lineHeight / 2, textMove, DLColor.WHITE, ETextAlignment.LEFT, true);
            GuiUtils.drawString(graphics, graphics.defaultFont(), mapDragIndicator.x() + mapDragIndicator.width() + 20, 16 + mapDragIndicator.y() + mapDragIndicator.height() / 2 - graphics.defaultFont().lineHeight / 2, textSelect, DLColor.WHITE, ETextAlignment.LEFT, true);
            GuiUtils.drawString(graphics, graphics.defaultFont(), mapDragIndicator.x() + mapDragIndicator.width() + 20, 32 + mapDragIndicator.y() + mapDragIndicator.height() / 2 - graphics.defaultFont().lineHeight / 2, textZoom, DLColor.WHITE, ETextAlignment.LEFT, true);
        }

        renderMarker(graphics, (int)mouseX, (int)mouseY);
    }

    @Override
    public void renderFrontLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (isSelected()) {            
            int xMapCoord = mouseXMapCoord(mouseX);
            int yMapCoord = mouseYMapCoord(mouseY);
            List<MutableComponent> lines = new LinkedList<>(List.of(
                TextUtils.text(String.format("X: %s, Y: %s", (int)(xMapCoord - map.getCenterPosOnMap().x), (int)(yMapCoord - map.getCenterPosOnMap().y)))
            ));

            if (isDragged() && DLWindowManager.hasShiftDown()) {
                lines.add(TextUtils.translate(keyPos1, (int)(dispX1 - map.getCenterPosOnMap().x), (int)(dispY1 - map.getCenterPosOnMap().y)).withStyle(ChatFormatting.GREEN)
                    .append(TextUtils.text(" - ").withStyle(ChatFormatting.GRAY))
                    .append(TextUtils.translate(keyPos2, (int)(dispX2 - map.getCenterPosOnMap().x), (int)(dispY2 - map.getCenterPosOnMap().y)).withStyle(ChatFormatting.RED))
                );
                lines.add(TextUtils.translate(keySize, dispX2 - dispX1 + 1, dispY2 - dispY1 + 1).withStyle(ChatFormatting.GOLD));
            }

            GuiUtils.drawTooltip(graphics, graphics.defaultFont(), (int)mouseX, (int)mouseY, lines, 200);
        }
    }

    private void renderMarker(DLGuiGraphics graphics, int mouseX, int mouseY) {
        if (isMapPosInBounds((int)map.getCenterPosOnMap().x, (int)map.getCenterPosOnMap().y)) {
            ModGuiIcons.MAP_MARKER.render(graphics, mapXToAbs((int)map.getCenterPosOnMap().x) + map.getScale() / 2 - ModGuiIcons.ICON_SIZE / 2, mapYToAbs((int)map.getCenterPosOnMap().y) + map.getScale() / 2 - ModGuiIcons.ICON_SIZE / 2);
        } else {
            graphics.poseStack().pushPose();
            int rawX = mapXToAbs((int)map.getCenterPosOnMap().x) + map.getScale() / 2;
            int rawY = mapYToAbs((int)map.getCenterPosOnMap().y) + map.getScale() / 2;
            int x = MathUtils.clamp(rawX, 4, width() - 4);
            int y = MathUtils.clamp(rawY, 4, height() - 4);

            graphics.poseStack().translate(x, y, 0);
            
            if (rawX < width() / 4 && rawY < height() / 4) { // top left
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(90 + 45));
            } else if (rawX >= width() / 4 && rawX <= width() / 4 * 3 && rawY < height() / 4) { // top
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(180));
            } else if (rawX > width() / 4 * 3&& rawY < height() / 4) { // top right
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(-90 - 45));
            }
            
            else if (rawX < width() / 4 && rawY >= height() / 4 && rawY <= height() / 4 * 3) { // left
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(90));
            } else if (rawX > width() / 4 * 3 && rawY >= height() / 4 && rawY <= height() / 4 * 3) { // right
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(-90));
            }

            else if (rawX < width() / 4 && rawY > height() / 4 * 3) { // bottom left
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(45));
            } else if (rawX >= width() / 4 && rawX <= width() / 4 * 3 && rawY > height() / 4 * 3) { // bottom
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(0));
            } else if (rawX > width() / 4 * 3 && rawY > height() / 4 * 3) { // bottom right
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(-45));
            }

            ModGuiIcons.MAP_POINTER.render(graphics, -8, -8);
            graphics.poseStack().popPose();
        }
    }

    private int mouseXMapCoord(double mouseX) {
        double relX = mouseX - mapX;
        double corrX = relX - relX % map.getScale();
        return (int)(corrX / map.getScale());
    }

    private int mouseYMapCoord(double mouseY) {
        double relY = mouseY - mapY;
        double corrY = relY - relY % map.getScale();
        return (int)(corrY / map.getScale());
    }

    private int mouseXToMap(double mouseX) {
        double relX = mouseX - mapX;
        double corrX = relX - relX % map.getScale();
        double res = corrX + mapX;
        return (int)(mapX < 0 ? res != (double)(int)res ? res + 1 : res : res);
    }

    private int mouseYToMap(double mouseY) {
        double relY = mouseY - mapY;
        double corrY = relY - relY % map.getScale();
        double res = corrY + mapY;
        return (int)(mapY < 0 ? res != (double)(int)res ? res + 1 : res : res);
    }

    private int mapXToAbs(int mapXCoord) {
        double res = mapXCoord * map.getScale() + mapX;
        return (int)(mapX < 0 ? res != (double)(int)res ? res + 1 : res : res);
    }

    private int mapYToAbs(int mapYCoord) {
        double res = mapYCoord * map.getScale() + mapY;
        return (int)(mapY < 0 ? res != (double)(int)res ? res + 1 : res : res);
    }

    private void centerViewToPoint(double x, double y) {
        setViewTo(-map.getScale() * x + width() / 2, -map.getScale() * y + height() / 2);
    }

    private void setViewTo(double x, double y) {
        mapX = map.getScaledWidth() < width() ? width() / 2 - map.getScaledWidth() / 2 : MathUtils.clamp(x, -map.getScaledWidth() + width(), 0);
        mapY = map.getScaledHeight() < height() ? height() / 2 - map.getScaledHeight() / 2 : MathUtils.clamp(y, -map.getScaledHeight() + height(), 0);
    }

    private boolean isMapPosInBounds(int mapXCoord, int mapYCoord) {
        int x = mapXToAbs(mapXCoord);
        int y = mapYToAbs(mapYCoord);

        return x > 0 && x < width() && y > 0 && y < height();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean b = super.isMouseOver(mouseX, mouseY);
        if (!b) {
            finishSelection(false);
        }
        return b;
    }

    private void finishSelection(boolean accept) {
        if (accept && isSelecting) {
            setArea(selX1, selY1, selX2, selY2);
            onSelected.accept(new Vec2(selX1 - map.getCenterPosOnMap().x, selY1 - map.getCenterPosOnMap().y), new Vec2(selX2 - map.getCenterPosOnMap().x, selY2 - map.getCenterPosOnMap().y));
        }
        isSelecting = false;
        selX1 = -1;
        selY1 = -1;
        selX2 = -1;
        selY2 = -1;
    }

    public void close() {
        map.dispose();
    }
    
}
