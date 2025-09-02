package com.whisent.bossdpstracker.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class ClientBossDpsManager {
    private static final ClientBossDpsData currentBossData = new ClientBossDpsData();
    private static boolean displayFlag = true;
    private static boolean enableFlag = true;


    public static void setBossData(int bossId,CompoundTag tag) {
        currentBossData.updateFromTag(bossId,tag);
    }

    public static ClientBossDpsData getCurrentBossData() {
        return currentBossData;
    }
    public static boolean isDisplay() {
        return displayFlag;
    }
    public static void setDisplay(boolean flag) {
        displayFlag = flag;
    }
    public static boolean isEnable() {
        return enableFlag;
    }
    public static void setEnable(boolean flag) {
        enableFlag = flag;
    }
}
