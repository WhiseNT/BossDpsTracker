package com.whisent.bossdpstracker.client;

import com.whisent.bossdpstracker.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BossDpsMainMenu extends Screen {
    private final Screen parent;

    public BossDpsMainMenu(Screen parent) {
        super(Component.translatable("screen.bossdpstracker.main_menu"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 10;
        int startY = this.height / 3;
        
        // 显示设置按钮
        this.addRenderableWidget(Button.builder(Component.translatable("screen.bossdpstracker.display_settings"), 
            btn -> this.minecraft.setScreen(new BossDpsScreen(this)))
            .pos(centerX - buttonWidth / 2, startY)
            .size(buttonWidth, buttonHeight)
            .build());
            
        // 追踪模式设置按钮 - 检查权限
        boolean hasTrackModePermission = hasTrackModePermission();
        var button = Button.builder(Component.translatable("screen.bossdpstracker.trackmode_settings"),
                        btn -> this.minecraft.setScreen(new TrackModeScreen(this)))
                .pos(centerX - buttonWidth / 2, startY + buttonHeight + spacing)
                .size(buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(button);
        
        // 如果没有权限，禁用按钮并添加提示
        if (!hasTrackModePermission) {
            button.active = false;
        }
        
        // 返回按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), 
            btn -> onClose())
            .pos(centerX - buttonWidth / 2, startY + (buttonHeight + spacing) * 2)
            .size(buttonWidth, buttonHeight)
            .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        // 绘制标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
        
        // 绘制当前追踪模式信息
        String trackModeText = "当前追踪模式: " + 
            (Config.trackMode == Config.TrackMode.BOSS ? "Boss模式" : "血量模式(" + Config.trackHpThreshold + "血)");
        guiGraphics.drawCenteredString(this.font, trackModeText, this.width / 2, 60, 0xAAAAAA);
        
        // 如果没有权限，显示提示
        if (!hasTrackModePermission()) {
            guiGraphics.drawCenteredString(this.font, "在多人服务器中需要管理员权限才能更改追踪模式", this.width / 2, 80, 0xFF5555);
        }
    }
    
    private boolean hasTrackModePermission() {
        // 单人游戏中的玩家或服务器管理员都有权限
        return this.minecraft != null && 
               (this.minecraft.hasSingleplayerServer() || 
                (this.minecraft.player != null && this.minecraft.player.hasPermissions(2)));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}