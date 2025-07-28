package com.whisent.bossdpstracker.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientBossDpsData {
    private final Map<String, PlayerDpsInfo> playerData = new HashMap<>();
    private int bossId = -1;

    public static class PlayerDpsInfo {
        public float totalDamage;
        public float dps;
        public float percentage;

        public PlayerDpsInfo(float totalDamage, float dps, float percentage) {
            this.totalDamage = totalDamage;
            this.dps = dps;
            this.percentage = percentage;
        }
    }

    public void updateFromTag(int bossId,CompoundTag tag) {
        this.bossId = bossId;
        playerData.clear();
        for (String key :tag.getAllKeys()) {
            CompoundTag playerTag = tag.getCompound(key);
            float damage = playerTag.getFloat("totalDamage");
            float dps = playerTag.getFloat("dps");
            float percentage = playerTag.getFloat("percentage");
            playerData.put(key, new PlayerDpsInfo(damage, dps, percentage));
        }
    }


    public Map<String, PlayerDpsInfo> getPlayerData() {
        return playerData;
    }
    public int getBossId() {
        return bossId;
    }
}
