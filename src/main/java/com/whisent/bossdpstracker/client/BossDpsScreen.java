package com.whisent.bossdpstracker.client;

import com.whisent.bossdpstracker.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BossDpsScreen extends Screen {
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean dragging = false;
    private int lastDragX;
    private int lastDragY;
    private final Screen parent;
    private int originalX;
    private int originalY;
    private boolean isScaling = false;
    private int scaleStartX;
    private double originalScale;
    
    // 保存原始配置值用于取消操作
    private int initialX;
    private int initialY;
    private double initialScale;

    public BossDpsScreen(Screen parent) {
        super(Component.translatable("screen.bossdpstracker.config"));
        this.parent = parent;
        this.initialX = Config.guiOffsetX;
        this.initialY = Config.guiOffsetY;
        this.initialScale = Config.guiScale;
        this.originalX = Config.guiOffsetX;
        this.originalY = Config.guiOffsetY;
        this.lastDragX = Config.guiOffsetX;
        this.lastDragY = Config.guiOffsetY;
    }

    @Override
    protected void init() {
        super.init();
        
        // 添加保存按钮
        this.addRenderableWidget(Button.builder(Component.translatable("screen.bossdpstracker.save"), 
            btn -> saveAndClose())
            .pos(this.width / 2 - 100, this.height - 55)
            .size(95, 20)
            .build());
            
        // 添加取消按钮
        this.addRenderableWidget(Button.builder(Component.translatable("screen.bossdpstracker.cancel"), 
            btn -> cancelAndClose())
            .pos(this.width / 2 + 5, this.height - 55)
            .size(95, 20)
            .build());
            
        // 添加重置按钮
        this.addRenderableWidget(Button.builder(Component.translatable("screen.bossdpstracker.reset"), 
            btn -> resetPosition())
            .pos(this.width / 2 - 100, this.height - 30)
            .size(200, 20)
            .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        // 绘制预览框
        int previewX = dragging && !isScaling ? lastDragX : Config.guiOffsetX;
        int previewY = dragging && !isScaling ? lastDragY : Config.guiOffsetY;
        int previewWidth = (int) (Config.guiWidth * Config.guiScale);
        
        // 绘制拖拽区域提示
        guiGraphics.drawString(this.font, Component.translatable("screen.bossdpstracker.drag_hint"), 
            10, 10, 0xFFFFFF);
            
        // 绘制当前坐标和缩放信息
        guiGraphics.drawString(this.font, Component.translatable("screen.bossdpstracker.current_pos_scale", 
            previewX, previewY, String.format("%.2f", Config.guiScale)), 10, 25, 0xFFFFFF);
        
        // 绘制预览框
        drawPreviewBox(guiGraphics, previewX, previewY, previewWidth);
    }

    private void drawPreviewBox(GuiGraphics guiGraphics, int x, int y, int width) {
        // 绘制一个简单的预览框，模拟DPS显示框的外观
        int height = (int) (100 * Config.guiScale); // 大概高度
        
        // 绘制背景
        guiGraphics.fill(x, y, x + width, y + height, 0x88000000);
        
        // 绘制边框
        guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF); // 上边框
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF); // 左边框
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF); // 右边框
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF); // 下边框
        
        // 绘制标题栏
        guiGraphics.fill(x, y, x + width, y + (int) (15 * Config.guiScale), 0xAA000000);
        guiGraphics.drawString(this.font, Component.translatable("screen.bossdpstracker.config"), 
            x + (int) (5 * Config.guiScale), y + (int) (4 * Config.guiScale), 0xFFFFFF);
        
        // 如果正在拖拽，显示坐标
        if (dragging) {
            guiGraphics.drawString(this.font, "X: " + x + ", Y: " + y, 
                x + (int) (5 * Config.guiScale), y + (int) (20 * Config.guiScale), 0xFFFF00);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 当按键被按下时重绘屏幕以更新提示文本
        this.minecraft.tell(this::init);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // 左键点击
            int previewX = dragging && !isScaling ? lastDragX : Config.guiOffsetX;
            int previewY = dragging && !isScaling ? lastDragY : Config.guiOffsetY;
            int width = (int) (Config.guiWidth * Config.guiScale);
            int height = (int) (100 * Config.guiScale);
            
            // 检查是否点击在预览框内
            if (mouseX >= previewX && mouseX <= previewX + width && 
                mouseY >= previewY && mouseY <= previewY + height) {
                dragging = true;
                
                // 检查是否按住Alt键进行缩放
                if (hasAltDown()) {
                    isScaling = true;
                    scaleStartX = (int) mouseX;
                    originalScale = Config.guiScale;
                } else {
                    isScaling = false;
                    dragOffsetX = (int) (mouseX - previewX);
                    dragOffsetY = (int) (mouseY - previewY);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            if (isScaling) {
                // 缩放逻辑：根据鼠标距离初始点击点的水平距离调整宽度
                int deltaX = (int) (mouseX - scaleStartX);
                // 每20像素变化对应0.1的缩放变化
                double scaleChange = deltaX / 20.0 * 0.1;
                Config.guiScale = Math.max(0.1, Math.min(5.0, originalScale + scaleChange));
            } else {
                lastDragX = (int) (mouseX - dragOffsetX);
                lastDragY = (int) (mouseY - dragOffsetY);
                
                // 边界检查
                if (lastDragX < 0) lastDragX = 0;
                if (lastDragY < 0) lastDragY = 0;
                if (lastDragX > this.width - (int) (Config.guiWidth * Config.guiScale)) lastDragX = this.width - (int) (Config.guiWidth * Config.guiScale);
                if (lastDragY > this.height - (int) (100 * Config.guiScale)) lastDragY = this.height - (int) (100 * Config.guiScale);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            if (!isScaling) {
                // 更新配置位置
                Config.guiOffsetX = lastDragX;
                Config.guiOffsetY = lastDragY;
            }
            isScaling = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void saveAndClose() {
        // 保存配置
        Config.saveClientConfig();
        onClose();
    }
    
    private void cancelAndClose() {
        // 恢复初始配置值
        Config.guiOffsetX = initialX;
        Config.guiOffsetY = initialY;
        Config.guiScale = initialScale;
        onClose();
    }
    
    private void resetPosition() {
        Config.guiOffsetX = 10;
        Config.guiOffsetY = 30;
        Config.guiScale = 1.0;
        lastDragX = 10;
        lastDragY = 30;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

}