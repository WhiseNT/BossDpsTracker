package com.whisent.bossdpstracker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Configuration class for Boss DPS Tracker mod
// Allows customization of the DPS display overlay
@Mod.EventBusSubscriber(modid = BossDpsTracker.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue GUI_WIDTH;
    private static final ForgeConfigSpec.IntValue GUI_OFFSET_X;
    private static final ForgeConfigSpec.IntValue GUI_OFFSET_Y;
    private static final ForgeConfigSpec.IntValue MAX_PLAYERS_SHOWN;
    private static final ForgeConfigSpec.DoubleValue GUI_SCALE;

    static {
        BUILDER.push("GUI Configuration");

        GUI_WIDTH = BUILDER
                .comment("Width of the DPS display GUI in pixels")
                .defineInRange("guiWidth", 100, 50, 500);

        GUI_OFFSET_X = BUILDER
                .comment("Horizontal offset from the left side of the screen")
                .defineInRange("guiOffsetX", 10, 0, 10000);

        GUI_OFFSET_Y = BUILDER
                .comment("Vertical offset from the top of the screen")
                .defineInRange("guiOffsetY", 30, 0, 10000);

        MAX_PLAYERS_SHOWN = BUILDER
                .comment("Maximum number of players to show in the DPS display")
                .defineInRange("maxPlayersShown", 10, 1, 20);
                
        GUI_SCALE = BUILDER
                .comment("Scale of the DPS display GUI")
                .defineInRange("guiScale", 1.0, 0.1, 5.0);

        BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int guiWidth;
    public static int guiOffsetX;
    public static int guiOffsetY;
    public static int maxPlayersShown;
    public static double guiScale;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        guiWidth = GUI_WIDTH.get();
        guiOffsetX = GUI_OFFSET_X.get();
        guiOffsetY = GUI_OFFSET_Y.get();
        maxPlayersShown = MAX_PLAYERS_SHOWN.get();
        guiScale = GUI_SCALE.get();
    }
    
    // 保存配置的方法
    public static void saveConfig() {
        GUI_WIDTH.set(guiWidth);
        GUI_OFFSET_X.set(guiOffsetX);
        GUI_OFFSET_Y.set(guiOffsetY);
        MAX_PLAYERS_SHOWN.set(maxPlayersShown);
        GUI_SCALE.set(guiScale);
    }
}