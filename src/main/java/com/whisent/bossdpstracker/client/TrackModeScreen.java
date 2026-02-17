package com.whisent.bossdpstracker.client;

import com.whisent.bossdpstracker.BossDpsTracker;
import com.whisent.bossdpstracker.Config;
import com.whisent.bossdpstracker.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrackModeScreen extends Screen {
    private final Screen parent;
    private EditBox hpThresholdInput;
    private Button bossModeButton;
    private Button hpModeButton;
    private Config.TrackMode selectedMode;
    private int hpThreshold;
    
    // 界面尺寸常量
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 200;
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 20;
    private static final int INPUT_WIDTH = 80;
    private static final int INPUT_HEIGHT = 20;

    public TrackModeScreen(Screen parent) {
        super(Component.translatable("screen.bossdpstracker.trackmode_settings"));
        this.parent = parent;
        this.selectedMode = Config.trackMode;
        this.hpThreshold = Config.trackHpThreshold;
    }

    @Override
    protected void init() {
        super.init();
        
        // 计算居中位置
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = centerY - PANEL_HEIGHT / 2;
        
        // Boss模式按钮
        bossModeButton = this.addRenderableWidget(Button.builder(
            Component.translatable("screen.bossdpstracker.boss_mode"), 
            btn -> {
                selectedMode = Config.TrackMode.BOSS;
                updateButtonStates();
            })
            .pos(panelX + 20, panelY + 30)
            .size(BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
        
        // HP模式按钮
        hpModeButton = this.addRenderableWidget(Button.builder(
            Component.translatable("screen.bossdpstracker.hp_mode"), 
            btn -> {
                selectedMode = Config.TrackMode.HP;
                updateButtonStates();
            })
            .pos(panelX + 20, panelY + 60)
            .size(BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
        
        // HP阈值输入框
        hpThresholdInput = new EditBox(this.font, panelX + 100, panelY + 90, INPUT_WIDTH, INPUT_HEIGHT,
            Component.translatable("screen.bossdpstracker.hp_threshold"));
        hpThresholdInput.setValue(String.valueOf(hpThreshold));
        hpThresholdInput.setFilter(s -> s.matches("\\d*")); // 只允许数字
        hpThresholdInput.setResponder(value -> {
            try {
                if (!value.isEmpty()) {
                    hpThreshold = Integer.parseInt(value);
                    if (hpThreshold < 1) hpThreshold = 1;
                    if (hpThreshold > 10000) hpThreshold = 10000;
                } else {
                    hpThreshold = 200; // 默认值
                }
            } catch (NumberFormatException e) {
                hpThreshold = 200; // 默认值
            }
        });
        this.addRenderableWidget(hpThresholdInput);
        
        // 应用按钮
        this.addRenderableWidget(Button.builder(Component.translatable("screen.bossdpstracker.apply"), 
            btn -> applySettings())
            .pos(panelX + 20, panelY + 165)
            .size(85, BUTTON_HEIGHT)
            .build());
            
        // 取消按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), 
            btn -> onClose())
            .pos(panelX + 115, panelY + 165)
            .size(85, BUTTON_HEIGHT)
            .build());
        
        // 初始化按钮状态
        updateButtonStates();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
        // 计算面板位置
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = centerY - PANEL_HEIGHT / 2;

        // 绘制背景面板
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF000000);
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, 0xFFFFFFFF); // 上边框
        guiGraphics.fill(panelX, panelY, panelX + 1, panelY + PANEL_HEIGHT, 0xFFFFFFFF); // 左边框
        guiGraphics.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFFFFFFF); // 右边框
        guiGraphics.fill(panelX, panelY + PANEL_HEIGHT - 1, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFFFFFFF); // 下边框

        // 绘制标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 10, 0xFFFFFF);

        // 绘制说明文本
        guiGraphics.drawString(this.font, Component.translatable("screen.bossdpstracker.select_mode"),
                panelX + 20, panelY + 20, 0xAAAAAA);

        // HP阈值标签
        guiGraphics.drawString(this.font, Component.translatable("screen.bossdpstracker.hp_threshold_label"), 
            panelX + 60, panelY + 95, 0xFFFFFF);
        
        // 当前设置预览
        Component previewText = Component.translatable("screen.bossdpstracker.current_setting", 
            Component.translatable(selectedMode == Config.TrackMode.BOSS ? "screen.bossdpstracker.boss_mode" : "screen.bossdpstracker.hp_mode"),
            hpThreshold);
        guiGraphics.drawString(this.font, previewText, panelX + 20, panelY + 120, 0x55FF55);
        
        // 添加提示文本
        if (selectedMode == Config.TrackMode.HP) {
            Component hintText = Component.translatable("screen.bossdpstracker.hp_hint", hpThreshold);
            guiGraphics.drawString(this.font, hintText, panelX + 20, panelY + 135, 0xAAAAAA);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
    
    private void updateButtonStates() {
        // 更新按钮样式以反映选中状态
        if (selectedMode == Config.TrackMode.BOSS) {
            bossModeButton.setMessage(Component.translatable("screen.bossdpstracker.boss_mode_selected"));
            hpModeButton.setMessage(Component.translatable("screen.bossdpstracker.hp_mode"));
        } else {
            bossModeButton.setMessage(Component.translatable("screen.bossdpstracker.boss_mode"));
            hpModeButton.setMessage(Component.translatable("screen.bossdpstracker.hp_mode_selected"));
        }
        
        // 根据模式启用/禁用HP输入框
        hpThresholdInput.active = selectedMode == Config.TrackMode.HP;
        // 在Boss模式下将输入框变暗但不完全隐藏
        hpThresholdInput.setAlpha(selectedMode == Config.TrackMode.HP ? 255 : 128);
    }
    
    private void applySettings() {
        try {
            hpThreshold = Integer.parseInt(hpThresholdInput.getValue());
            if (hpThreshold < 1 || hpThreshold > 10000) {
                hpThreshold = 200; // 默认值
            }
        } catch (NumberFormatException e) {
            hpThreshold = 200; // 默认值
        }
        
        // 发送到服务器
        NetworkHandler.sendTrackModeUpdate(selectedMode, hpThreshold);
        
        // 关闭界面
        onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}