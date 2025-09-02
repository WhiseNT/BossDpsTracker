package com.whisent.bossdpstracker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BossDpsOverlay implements IGuiOverlay {
    private static String bossNameCache = "";

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (!ClientBossDpsManager.isDisplay()) return;
        Minecraft mc = Minecraft.getInstance();

        int guiWidth = 100;
        // ✅ 先获取数据，再判空
        ClientBossDpsData data = ClientBossDpsManager.getCurrentBossData();
        if (data == null || data.getBossId() == -1 || data.getPlayerData().isEmpty()) return;

        // ✅ 获取 Boss 实体，判空保护
        Entity bossEntity = null;
        if (mc.level != null) {
            bossEntity = mc.level.getEntity(data.getBossId());
        }
        String bossName;
        if (bossEntity == null) {
            bossName = bossNameCache;
        } else {
            bossName = bossEntity.getType().getDescription().getString();
        }
        if (!bossNameCache.equals(bossName)) bossNameCache = bossName;
        // ✅ 显示类型
        String displayTypeName = switch (ClientEventHandler.changeDataType) {
            case 1 -> "dmg%";
            case 2 -> "dps";
            default -> "dmg";
        };

        int x = 10;
        int y = 30;
        int lineHeight = 14;
        int titleWith = mc.font.width(displayTypeName);
        int factor = 16;
        if (displayTypeName.equals("dmg%")) {
            factor = 15;
        }
        String finalBossName = mc.font.plainSubstrByWidth(bossName, 4 * factor);
        if (finalBossName.length() < bossName.length()) {
            finalBossName += "...";
        }
        guiGraphics.drawString(mc.font, displayTypeName,
                x + guiWidth - 5 - titleWith, y + 4,
                0xffffff);
        guiGraphics.drawString(mc.font, finalBossName,
                x + 6, y + 4, 0xffffff);

        y += 10;
        List<Map.Entry<String, ClientBossDpsData.PlayerDpsInfo>> entries = new ArrayList<>(data.getPlayerData().entrySet());
        entries.sort((o1, o2) -> Float.compare(o2.getValue().totalDamage, o1.getValue().totalDamage));
        int maxToShow = Math.min(10, entries.size());
        // ✅ 绘制半透明黑色背景
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.fill(x, y - 10,
                x + guiWidth, y + 14 * maxToShow + 8, 0x44000000); // ✅ 透明黑背景
        RenderSystem.disableBlend();
        poseStack.popPose();
        int barWidth = (int) (guiWidth * 0.92f);
        // ✅ 绘制每个玩家
        for (int i = 0; i < maxToShow; i++) {
            Map.Entry<String, ClientBossDpsData.PlayerDpsInfo> entry = entries.get(i);
            String name = entry.getKey();

            ClientBossDpsData.PlayerDpsInfo info = entry.getValue();
            var valueText = getValueText(info);
            int textWidth = mc.font.width(valueText);
            int drawX = x + barWidth+3 - textWidth;
            int valueWidth = mc.font.width(valueText);
            int totalWidth = 6 * 14;
            int nameMaxWidth = totalWidth - valueWidth - 3;
            int minNameWidth = 18;
            if (nameMaxWidth < minNameWidth) {
                nameMaxWidth = minNameWidth;
            }
            if (name.equals("chat.bossdpstracker.generic")) {
                name = Component.translatable("chat.bossdpstracker.generic").getString();
            }
            y += lineHeight;
            String displayName = mc.font.plainSubstrByWidth(name,nameMaxWidth);
            if (displayName.length() < name.length()) {
                displayName += "...";
            }
            int percentWidth = (int) (info.percentage / 100.0 * barWidth);
            guiGraphics.fill(x + 5, y + 1,
                    x + 4 + percentWidth, y - 9,
                    getOrCreateColor(name));

            guiGraphics.drawString(mc.font, displayName, x + 8, y - 8, 0xFFFFFF);

            guiGraphics.drawString(mc.font, valueText, drawX, y - 8, 0xFFFFFF);
        }
    }

    private static @NotNull String getValueText(ClientBossDpsData.PlayerDpsInfo info) {
        float displayInfo = switch (ClientEventHandler.changeDataType) {
            case 1 -> info.percentage;
            case 2 -> info.dps;
            default -> info.totalDamage;
        };

        String valueText;
        if (ClientEventHandler.changeDataType == 0 || ClientEventHandler.changeDataType == 2) {
            if (displayInfo >= 1_000_000_000_000_000_000f) {
                valueText = String.format("%.1e", displayInfo);
            } else if (displayInfo >= 1_000_000_000_000f) {
                valueText = String.format("%.1fT", displayInfo / 1_000_000_000_000f);
            } else if (displayInfo >= 1_000_000_000f) {
                valueText = String.format("%.1fB", displayInfo / 1_000_000_000f);
            } else if (displayInfo >= 1_000_000f) {
                valueText = String.format("%.1fM", displayInfo / 1_000_000f);
            } else if (displayInfo >= 1_000f) {
                valueText = String.format("%.1fK", displayInfo / 1_000f);
            } else {
                valueText = String.format("%.0f", displayInfo);
            }
        } else if (ClientEventHandler.changeDataType == 1) {
            valueText = String.format("%.1f%%", displayInfo);
        } else {
            valueText = String.format("%.1f", displayInfo);
        }
        return valueText;
    }

    private static final List<Color> colorPalette = List.of(
            new Color(200,62,62),
            new Color(189,138,62),
            new Color(189,179,62),
            new Color(82,189,62),
            new Color(62,182,189),
            new Color(62,103,189),
            new Color(162,62,189)
    );
    private static final Map<String,Color> playerColors = new java.util.HashMap<>();
    
    public static int getOrCreateColor(String playerId) {
        if (!playerColors.containsKey(playerId)) {
            int index = Math.abs(playerId.hashCode()) % colorPalette.size();
            playerColors.put(playerId, colorPalette.get(index));
            return colorPalette.get(index).getRGB();
        } else {
            return playerColors.get(playerId).getRGB();
        }
    }
}