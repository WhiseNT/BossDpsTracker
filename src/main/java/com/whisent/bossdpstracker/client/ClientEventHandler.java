package com.whisent.bossdpstracker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.whisent.bossdpstracker.BossDpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = BossDpsTracker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {
    private static int changeDataType = 0;

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (BossDpsTracker.ClientModEvents.DISPLAY.consumeClick()) {
            ClientBossDpsManager.setEnable(!ClientBossDpsManager.isEnable());
        }
        if (BossDpsTracker.ClientModEvents.CHANGE_TYPE.consumeClick()) {
            changeDataType++;
            if (changeDataType > 2) changeDataType = 0;
        }
    }
    private static String bossNameCache = "";
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {

        if (!ClientBossDpsManager.isDisplay()) return;
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics gui = event.getGuiGraphics();
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
        String displayTypeName = switch (changeDataType) {
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
        gui.drawString(mc.font, displayTypeName,
                x + guiWidth - 5 - titleWith, y + 4,
                0xffffff);
        gui.drawString(mc.font, finalBossName,
                x + 6, y + 4, 0xffffff);

        y += 10;
        List<Map.Entry<String, ClientBossDpsData.PlayerDpsInfo>> entries = new ArrayList<>(data.getPlayerData().entrySet());
        entries.sort((o1, o2) -> Float.compare(o2.getValue().totalDamage, o1.getValue().totalDamage));
        int maxToShow = Math.min(10, entries.size());
        // ✅ 绘制半透明黑色背景
        PoseStack poseStack = gui.pose();
        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gui.fill(x, y - 10,
                x + guiWidth, y + 14 * maxToShow + 8, 0x09000000); // ✅ 透明黑背景
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
            gui.fill(x + 5, y + 1,
                    x + 4 + percentWidth, y - 9,
                    getOrCreateColor(name));

            gui.drawString(mc.font, displayName, x + 8, y - 8, 0xFFFFFF);

            gui.drawString(mc.font, valueText, drawX, y - 8, 0xFFFFFF);
        }

    }

    private static @NotNull String getValueText(ClientBossDpsData.PlayerDpsInfo info) {
        float displayInfo = switch (changeDataType) {
            case 1 -> info.percentage;
            case 2 -> info.dps;
            default -> info.totalDamage;
        };

        String valueText;
        if (changeDataType == 0 || changeDataType == 2) {
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
        } else if (changeDataType == 1) {
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
    public static boolean hasColor(String playerId) {
        return playerColors.containsKey(playerId);
    }
    public static int getColor(String player) {
        return playerColors.getOrDefault(player, Color.BLUE).getRGB();
    }
    public static int getOrCreateColor(String playerId) {
        if (!hasColor(playerId)) {
            int index = Math.abs(playerId.hashCode()) % colorPalette.size();
            playerColors.put(playerId, colorPalette.get(index));
            return colorPalette.get(index).getRGB();
        } else {
            return getColor(playerId);
        }

    }
}
