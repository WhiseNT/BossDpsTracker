package com.whisent.bossdpstracker;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.whisent.bossdpstracker.client.BossDpsOverlay;
import com.whisent.bossdpstracker.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.UUID;

@Mod(BossDpsTracker.MODID)
public class BossDpsTracker {
    public static final UUID NON_PLAYER_UUID = UUID.fromString("668afff5-e78c-fe96-3467-f51bd11d3c89");
    public static final String MODID = "bossdpstracker";

    public static final Logger LOGGER = LogUtils.getLogger();

    public BossDpsTracker(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时加载活动Boss数据

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
        
        @SubscribeEvent
        public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("boss_dps", new BossDpsOverlay());
        }

        public static final KeyMapping DISPLAY =
                new KeyMapping("key.bossdpstracker.display",
                        KeyConflictContext.IN_GAME,
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_B,
                        "key.categories.bossdpstracker");
        public static final KeyMapping CHANGE_TYPE =
                new KeyMapping("key.bossdpstracker.change_data_type",
                        KeyConflictContext.IN_GAME,
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        "key.categories.bossdpstracker");
        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(ClientModEvents.DISPLAY);
            event.register(ClientModEvents.CHANGE_TYPE);
        }
    }
}