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
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.WidgetContainer;
import de.mrjulsen.mcdragonlib.client.render.MapImage;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec2;

public class MapAreaSelectionWidget extends WidgetContainer {

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

    private final DLScreen parent;

    @SuppressWarnings("resource")
    public MapAreaSelectionWidget(DLScreen parent, int x, int y, int width, int height, BlockPos center, int mapWidth, int mapHeight, BiConsumer<Vec2, Vec2> onSelected) {
        super(x, y, width, height);
        this.parent = parent;
        this.map = new MapImage(Minecraft.getInstance().level, center, center.getY(), mapWidth, mapHeight, false, 4);
        this.onSelected = onSelected;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        
        mapDragIndicator = addWidget(new MouseMotionIndicator(getX() + 5, getY() + 5, 16, GLFW.GLFW_MOUSE_BUTTON_LEFT, false, true));
        mapSelectIndicator = addWidget(new MouseMotionIndicator(getX() + 5, getY() + 5, 16, GLFW.GLFW_MOUSE_BUTTON_LEFT, false, true));

        setArea(0, 0, 0, 0);
        centerViewToPoint(map.getCenterPosOnMap().x, map.getCenterPosOnMap().y);
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
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        GuiUtils.enableScissor(graphics, getX(), getY(), getWidth(), getHeight());
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
        this.map.render(graphics, (int)(getX() + mapX), (int)(getY() + mapY));

        // Draw area
        int x1 = Math.min(areaX1, areaX2);
        int y1 = Math.min(areaY1, areaY2);
        int x2 = Math.max(areaX1, areaX2);
        int y2 = Math.max(areaY1, areaY2);
        GuiUtils.drawBox(graphics, new GuiAreaDefinition(mapXToAbs(x1), mapYToAbs(y1), (x2 - x1 + 1) * map.getScale(), (y2 - y1 + 1) * map.getScale()), 0x44FF0000, 0xFFFF0000);

        // Draw selection
        if (selX1 > -1 && selY1 > -1) {
            dispX1 = Math.min(selX1, selX2);
            dispY1 = Math.min(selY1, selY2);
            dispX2 = Math.max(selX1, selX2);
            dispY2 = Math.max(selY1, selY2);
            GuiUtils.fill(graphics, mapXToAbs(dispX1), mapYToAbs(dispY1), (dispX2 - dispX1 + 1) * map.getScale(), (dispY2 - dispY1 + 1) * map.getScale(), 0x88FF0000);            
        }
        if (isMouseSelected()) {
            GuiUtils.drawBox(graphics, new GuiAreaDefinition(mouseXToMap(mouseX), mouseYToMap(mouseY), map.getScale(), map.getScale()), 0x44FF0000, 0xFFFF0000);
        }
        
        if (DLScreen.hasShiftDown()) {
            mapSelectIndicator.render(graphics.graphics(), mouseX, mouseY, partialTicks);
            ModGuiIcons.SHIFT_KEY.getAsSprite(16, 16).render(graphics,  mapSelectIndicator.getX() + 8, mapSelectIndicator.getY() + 16);
            GuiUtils.drawString(graphics, font, mapSelectIndicator.getX() + mapSelectIndicator.getWidgetWidth(), mapSelectIndicator.getY() + mapSelectIndicator.getWidgetHeight() / 2 - font.lineHeight / 2, textDrag, 0xFFFFFFFF, EAlignment.LEFT, true);
            GuiUtils.drawString(graphics, font, mapSelectIndicator.getX() + mapSelectIndicator.getWidgetWidth(), 16 + mapSelectIndicator.getY() + mapSelectIndicator.getWidgetHeight() / 2 - font.lineHeight / 2, textCancel, 0xFFFFFFFF, EAlignment.LEFT, true);
        } else {
            mapDragIndicator.render(graphics.graphics(), mouseX, mouseY, partialTicks);
            ModGuiIcons.SHIFT_KEY.getAsSprite(16, 16).render(graphics, mapDragIndicator.getX(), mapDragIndicator.getY() + 16);
            ModGuiIcons.MOUSE_LEFT.getAsSprite(16, 16).render(graphics, mapDragIndicator.getX() + 16, mapDragIndicator.getY() + 16);
            ModGuiIcons.MOUSE_MIDDLE.getAsSprite(16, 16).render(graphics, mapDragIndicator.getX() + 8, mapDragIndicator.getY() + 32);
            GuiUtils.drawString(graphics, font, mapDragIndicator.getX() + mapDragIndicator.getWidgetWidth(), mapDragIndicator.getY() + mapDragIndicator.getWidgetHeight() / 2 - font.lineHeight / 2, textMove, 0xFFFFFFFF, EAlignment.LEFT, true);
            GuiUtils.drawString(graphics, font, mapDragIndicator.getX() + mapDragIndicator.getWidgetWidth(), 16 + mapDragIndicator.getY() + mapDragIndicator.getWidgetHeight() / 2 - font.lineHeight / 2, textSelect, 0xFFFFFFFF, EAlignment.LEFT, true);
            GuiUtils.drawString(graphics, font, mapDragIndicator.getX() + mapDragIndicator.getWidgetWidth(), 32 + mapDragIndicator.getY() + mapDragIndicator.getWidgetHeight() / 2 - font.lineHeight / 2, textZoom, 0xFFFFFFFF, EAlignment.LEFT, true);
        }

        renderMarker(graphics, mouseX, mouseY, partialTicks);
        GuiUtils.disableScissor(graphics);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);

        if (isMouseSelected()) {            
            int xMapCoord = mouseXMapCoord(mouseX);
            int yMapCoord = mouseYMapCoord(mouseY);
            List<MutableComponent> lines = new LinkedList<>(List.of(
                TextUtils.text(String.format("X: %s, Y: %s", (int)(xMapCoord - map.getCenterPosOnMap().x), (int)(yMapCoord - map.getCenterPosOnMap().y)))
            ));

            if (isDragging() && DLScreen.hasShiftDown()) {
                lines.add(TextUtils.translate(keyPos1, (int)(dispX1 - map.getCenterPosOnMap().x), (int)(dispY1 - map.getCenterPosOnMap().y)).withStyle(ChatFormatting.GREEN)
                    .append(TextUtils.text(" - ").withStyle(ChatFormatting.GRAY))
                    .append(TextUtils.translate(keyPos2, (int)(dispX2 - map.getCenterPosOnMap().x), (int)(dispY2 - map.getCenterPosOnMap().y)).withStyle(ChatFormatting.RED))
                );
                lines.add(TextUtils.translate(keySize, dispX2 - dispX1 + 1, dispY2 - dispY1 + 1).withStyle(ChatFormatting.GOLD));
            }

            GuiUtils.renderTooltip(parent, GuiAreaDefinition.of(this), lines, width, graphics, (int)mouseX, (int)mouseY);
        }
    }

    private void renderMarker(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMapPosInBounds((int)map.getCenterPosOnMap().x, (int)map.getCenterPosOnMap().y)) {
            ModGuiIcons.MAP_MARKER.render(graphics, mapXToAbs((int)map.getCenterPosOnMap().x) + map.getScale() / 2 - ModGuiIcons.ICON_SIZE / 2, mapYToAbs((int)map.getCenterPosOnMap().y) + map.getScale() / 2 - ModGuiIcons.ICON_SIZE / 2);
        } else {
            graphics.poseStack().pushPose();
            int rawX = mapXToAbs((int)map.getCenterPosOnMap().x) + map.getScale() / 2;
            int rawY = mapYToAbs((int)map.getCenterPosOnMap().y) + map.getScale() / 2;
            int x = MathUtils.clamp(rawX, getX() + 4, getX() + getWidth() - 4);
            int y = MathUtils.clamp(rawY, getY() + 4, getY() + getHeight() - 4);

            graphics.poseStack().translate(x, y, 0);
            
            if (rawX < getX() + getWidth() / 4 && rawY < getY() + getHeight() / 4) { // top left
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(90 + 45));
            } else if (rawX >= getX() + getWidth() / 4 && rawX <= getX() + getWidth() / 4 * 3 && rawY < getY() + getHeight() / 4) { // top
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(180));
            } else if (rawX > getX() + getWidth() / 4 * 3&& rawY < getY() + getHeight() / 4) { // top right
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(-90 - 45));
            }
            
            else if (rawX < getX() + getWidth() / 4 && rawY >= getY() + getHeight() / 4 && rawY <= getY() + getHeight() / 4 * 3) { // left
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(90));
            } else if (rawX > getX() + getWidth() / 4 * 3 && rawY >= getY() + getHeight() / 4 && rawY <= getY() + getHeight() / 4 * 3) { // right
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(-90));
            }

            else if (rawX < getX() + getWidth() / 4 && rawY > getY() + getHeight() / 4 * 3) { // bottom left
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(45));
            } else if (rawX >= getX() + getWidth() / 4 && rawX <= getX() + getWidth() / 4 * 3 && rawY > getY() + getHeight() / 4 * 3) { // bottom
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(0));
            } else if (rawX > getX() + getWidth() / 4 * 3 && rawY > getY() + getHeight() / 4 * 3) { // bottom right
                graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(-45));
            }

            ModGuiIcons.MAP_POINTER.render(graphics, -8, -8);
            graphics.poseStack().popPose();
        }
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (DLScreen.hasShiftDown() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            selX2 = mouseXMapCoord(mouseX);
            selY2 = mouseYMapCoord(mouseY);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setViewTo(mapX + dragX, mapY + dragY);
        }

        return true;
    }

    private int mouseXMapCoord(double mouseX) {
        double relX = mouseX - getX() - mapX;
        double corrX = relX - relX % map.getScale();
        return (int)(corrX / map.getScale());
    }

    private int mouseYMapCoord(double mouseY) {
        double relY = mouseY - getY() - mapY;
        double corrY = relY - relY % map.getScale();
        return (int)(corrY / map.getScale());
    }

    private int mouseXToMap(double mouseX) {
        double relX = mouseX - getX() - mapX;
        double corrX = relX - relX % map.getScale();
        double res = corrX + mapX + getX();
        return (int)(mapX < -getX() ? res != (double)(int)res ? res + 1 : res : res);
    }

    private int mouseYToMap(double mouseY) {
        double relY = mouseY - getY() - mapY;
        double corrY = relY - relY % map.getScale();
        double res = corrY + mapY + getY();
        return (int)(mapY < -getY() ? res != (double)(int)res ? res + 1 : res : res);
    }

    private int mapXToAbs(int mapXCoord) {
        double res = mapXCoord * map.getScale() + getX() + mapX;
        return (int)(mapX < -getX() ? res != (double)(int)res ? res + 1 : res : res);
    }

    private int mapYToAbs(int mapYCoord) {
        double res = mapYCoord * map.getScale() + getY() + mapY;
        return (int)(mapY < -getY() ? res != (double)(int)res ? res + 1 : res : res);
    }

    private void centerViewToPoint(double x, double y) {
        setViewTo(-map.getScale() * x + getWidth() / 2, -map.getScale() * y + getHeight() / 2);
    }

    private void setViewTo(double x, double y) {
        mapX = map.getScaledWidth() < width ? width / 2 - map.getScaledWidth() / 2 : MathUtils.clamp(x, -map.getScaledWidth() + getWidth(), 0);
        mapY = map.getScaledHeight() < height ? height / 2 - map.getScaledHeight() / 2 : MathUtils.clamp(y, -map.getScaledHeight() + getHeight(), 0);
    }

    private boolean isMapPosInBounds(int mapXCoord, int mapYCoord) {
        int x = mapXToAbs(mapXCoord);
        int y = mapYToAbs(mapYCoord);

        return x > getX() && x < getX() + getWidth() && y > getY() && y < getY() + getHeight();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean b = super.isMouseOver(mouseX, mouseY);
        if (!b) {
            finishSelection(false);
        }
        return b;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (DLScreen.hasShiftDown() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isSelecting = true;
            selX1 = mouseXMapCoord(mouseX);
            selY1 = mouseYMapCoord(mouseY);
            selX2 = selX1;
            selY2 = selY1;
        }
        
        this.setDragging(true);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int code, int scan, int mod) {
        if (!DLScreen.hasShiftDown()) {
            finishSelection(false);
        }
        return super.keyReleased(code, scan, mod);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        finishSelection(true);
        return super.mouseReleased(mouseX, mouseY, button);
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int coordX = mouseXMapCoord(getX() + getWidth() / 2);
        int coordY = mouseYMapCoord(getY() + getHeight() / 2);
        this.map.setScale(MathUtils.clamp((int)(map.getScale() + delta), 1, 8));
        centerViewToPoint(coordX, coordY);
        return true;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return true;
    }

    public void close() {
        map.dispose();
    }
    
}
