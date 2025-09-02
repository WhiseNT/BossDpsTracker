package com.whisent.bossdpstracker.core;

import com.whisent.bossdpstracker.BossDpsTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.*;

public class BDTDpsData {
    private final Map<UUID,Float> playerDamageMap = new HashMap<>();
    private final Map<UUID,Float> playerHurtMap = new HashMap<>();
    private final Map<UUID,Float> playerDPSMap = new HashMap<>();
    private final Set<UUID> participants = new HashSet<>();
    private long fightStartTime;
    private final ServerLevel level;
    private final Entity bossEntity;


    public BDTDpsData (ServerLevel level, Entity bossEntity) {
        this.level = level;
        this.bossEntity = bossEntity;
        this.fightStartTime = level.getGameTime();
    }

    public void recordDamage(UUID uuid, float damage) {
        playerDamageMap.merge(uuid, damage, Float::sum);
        if (!uuid.equals(BossDpsTracker.NON_PLAYER_UUID)) {
            if (level.getPlayerByUUID(uuid) != null) {
                BDTDpsTracker.PLAYER_NAME_CACHE.putIfAbsent(uuid, Objects.requireNonNull(level.getPlayerByUUID(uuid)).getName().getString());
            }
        } else {
            BDTDpsTracker.PLAYER_NAME_CACHE.putIfAbsent(uuid, "chat.bossdpstracker.generic");
        }

        participants.add(uuid);
    }

    public double getTotalDamage() {
        return playerDamageMap.values().stream().mapToDouble(Float::doubleValue).sum();
    }

    public float getDamageForPlayer(UUID uuid) {
        return playerDamageMap.getOrDefault(uuid, 0f);
    }

    public float getDPSForPlayer(UUID uuid) {
        float totalDamage = getDamageForPlayer(uuid);
        float timeElapsedSeconds = (float) (level.getGameTime() - fightStartTime) / 20;
        return timeElapsedSeconds > 0 ? totalDamage / timeElapsedSeconds : 0;
    }
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        for (UUID uuid : participants) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putFloat("totalDamage", getDamageForPlayer(uuid));
            playerTag.putFloat("dps", getDPSForPlayer(uuid));
            playerTag.putFloat("percentage", getPercentageForPlayer(uuid));
            tag.put(BDTDpsTracker.PLAYER_NAME_CACHE.getOrDefault(uuid,"Unknown"), playerTag);
        }
        return tag;
    }
    public void setFightStartTime(long time) {
        this.fightStartTime = time;
    }


    public float getPercentageForPlayer(UUID uuid) {
        float total = (float) getTotalDamage();
        if (total <= 0) return 0;
        return (getDamageForPlayer(uuid) / total) * 100F;
    }


    public void endFight() {
        for (UUID uuid : participants) {
            ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(uuid);
            if (player != null) {
                float dmg = getDamageForPlayer(uuid);
                float dps = getDPSForPlayer(uuid);
                float percent = getPercentageForPlayer(uuid);
                player.sendSystemMessage(Component.literal(
                        String.format("你在Boss战中造成了 %.1f 伤害，DPS: %.1f，占比: %.1f%%", dmg, dps, percent)
                ));
            }
        }
    }

    public Set<UUID> getParticipants() {
        return participants;
    }
    public void addParticipant(UUID uuid) {
        participants.add(uuid);
    }

    public Map<UUID, Float> getPlayerDamageMap() {
        return playerDamageMap;
    }
}
