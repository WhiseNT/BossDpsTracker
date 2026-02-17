package com.whisent.bossdpstracker;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

// Configuration class for Boss DPS Tracker mod
// Allows customization of the DPS display overlay
@Mod.EventBusSubscriber(modid = BossDpsTracker.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    // 追踪模式枚举
    public enum TrackMode {
        BOSS,  // 只追踪Boss
        HP     // 追踪高于指定血量的生物
    }
    
    // ==================== 客户端配置 ====================
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    
    private static final ForgeConfigSpec.IntValue GUI_WIDTH;
    private static final ForgeConfigSpec.IntValue GUI_OFFSET_X;
    private static final ForgeConfigSpec.IntValue GUI_OFFSET_Y;
    private static final ForgeConfigSpec.IntValue MAX_PLAYERS_SHOWN;
    private static final ForgeConfigSpec.DoubleValue GUI_SCALE;
    
    static {
        CLIENT_BUILDER.push("GUI Configuration");

        GUI_WIDTH = CLIENT_BUILDER
                .comment("Width of the DPS display GUI in pixels")
                .defineInRange("guiWidth", 100, 50, 500);

        GUI_OFFSET_X = CLIENT_BUILDER
                .comment("Horizontal offset from the left side of the screen")
                .defineInRange("guiOffsetX", 10, 0, 10000);

        GUI_OFFSET_Y = CLIENT_BUILDER
                .comment("Vertical offset from the top of the screen")
                .defineInRange("guiOffsetY", 30, 0, 10000);

        MAX_PLAYERS_SHOWN = CLIENT_BUILDER
                .comment("Maximum number of players to show in the DPS display")
                .defineInRange("maxPlayersShown", 10, 1, 20);
                
        GUI_SCALE = CLIENT_BUILDER
                .comment("Scale of the DPS display GUI")
                .defineInRange("guiScale", 1.0, 0.1, 5.0);

        CLIENT_BUILDER.pop();
    }
    
    static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
    
    // ==================== 服务器配置 ====================
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    
    private static final ForgeConfigSpec.EnumValue<TrackMode> TRACK_MODE;
    private static final ForgeConfigSpec.IntValue TRACK_HP_THRESHOLD;
    
    static {
        SERVER_BUILDER.push("Tracking Configuration");

        TRACK_MODE = SERVER_BUILDER
                .comment("Tracking mode: BOSS for boss-only tracking, HP for HP-based tracking")
                .defineEnum("trackMode", TrackMode.BOSS);

        TRACK_HP_THRESHOLD = SERVER_BUILDER
                .comment("HP threshold for tracking when in HP mode")
                .defineInRange("trackHpThreshold", 200, 1, 10000);

        SERVER_BUILDER.pop();
    }
    
    static final ForgeConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    // ==================== 客户端配置变量 ====================
    public static int guiWidth;
    public static int guiOffsetX;
    public static int guiOffsetY;
    public static int maxPlayersShown;
    public static double guiScale;
    
    // ==================== 服务器配置变量 ====================
    public static TrackMode trackMode;
    public static int trackHpThreshold;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 根据配置类型加载不同的配置
        if (event.getConfig().getSpec() == CLIENT_SPEC) {
            guiWidth = GUI_WIDTH.get();
            guiOffsetX = GUI_OFFSET_X.get();
            guiOffsetY = GUI_OFFSET_Y.get();
            maxPlayersShown = MAX_PLAYERS_SHOWN.get();
            guiScale = GUI_SCALE.get();
        } else if (event.getConfig().getSpec() == SERVER_SPEC) {
            trackMode = TRACK_MODE.get();
            trackHpThreshold = TRACK_HP_THRESHOLD.get();
        }
    }
    
    // 保存客户端配置的方法
    public static void saveClientConfig() {
        GUI_WIDTH.set(guiWidth);
        GUI_OFFSET_X.set(guiOffsetX);
        GUI_OFFSET_Y.set(guiOffsetY);
        MAX_PLAYERS_SHOWN.set(maxPlayersShown);
        GUI_SCALE.set(guiScale);
    }
    
    // 保存服务器配置的方法
    public static void saveServerConfig() {
        TRACK_MODE.set(trackMode);
        TRACK_HP_THRESHOLD.set(trackHpThreshold);
    }
}