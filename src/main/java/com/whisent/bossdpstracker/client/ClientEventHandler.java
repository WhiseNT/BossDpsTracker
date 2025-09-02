package com.whisent.bossdpstracker.client;

import com.whisent.bossdpstracker.BossDpsTracker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BossDpsTracker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {
    public static int changeDataType = 0;

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (BossDpsTracker.ClientModEvents.DISPLAY.consumeClick()) {
            ClientBossDpsManager.setDisplay(!ClientBossDpsManager.isDisplay());
        }
        if (BossDpsTracker.ClientModEvents.CHANGE_TYPE.consumeClick()) {
            changeDataType++;
            if (changeDataType > 2) changeDataType = 0;
        }
    }
}
