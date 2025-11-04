package com.whisent.bossdpstracker.core;

import com.whisent.bossdpstracker.BossDpsTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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

    public void recordDamage(UUID id, float damage) {
        playerDamageMap.merge(id, damage, Float::sum);
        if (!id.equals(BossDpsTracker.NON_PLAYER_UUID)) {
            Player player = (Player) level.getPlayerByUUID(id);
            if (player != null) {
                BDTDpsTracker.PLAYER_NAME_CACHE.putIfAbsent(id, player.getName().getString());
            }
        } else {
            BDTDpsTracker.PLAYER_NAME_CACHE.putIfAbsent(id, "chat.bossdpstracker.generic");
        }

        participants.add(id);
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

    public static BDTDpsData deserialize(CompoundTag tag, ServerLevel level, Entity bossEntity) {
        BDTDpsData data = new BDTDpsData(level, bossEntity);
        // 从保存的数据中恢复战斗开始时间，这样DPS计算会基于整个战斗过程而不是从服务器重启开始计算
        // 注意：我们不设置fightStartTime为level.getGameTime()，因为我们要保持战斗开始时间的一致性
        // 而是应该从保存的数据中恢复，如果没有则使用当前时间
        
        System.out.println("反序列化Boss数据: " + tag);
        
        for (String playerId : tag.getAllKeys()) {
            CompoundTag playerTag = tag.getCompound(playerId);
            float totalDamage = playerTag.getFloat("totalDamage");
            float dps = playerTag.getFloat("dps");
            
            // 通过玩家名查找UUID
            UUID playerUUID = null;
            ServerPlayer player = level.getServer().getPlayerList().getPlayerByName(playerId);
            if (player != null) {
                playerUUID = player.getUUID();
            } else {
                // 如果玩家不在线，创建一个基于玩家名称的唯一标识符
                // 这样即使玩家离线也能保留他们的数据
                playerUUID = UUID.nameUUIDFromBytes(playerId.getBytes());
                // 为离线玩家使用原始名称作为标识
                BDTDpsTracker.PLAYER_NAME_CACHE.put(playerUUID, playerId);
            }
            
            // 将已保存的伤害数据放入映射
            data.playerDamageMap.put(playerUUID, totalDamage);
            data.playerDPSMap.put(playerUUID, dps);
            data.participants.add(playerUUID);
            System.out.println("恢复玩家数据: UUID=" + playerUUID + ", damage=" + totalDamage + ", dps=" + dps);
        }
        return data;
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
    
    // 用于调试的方法
    @Override
    public String toString() {
        return "BDTDpsData{" +
                "playerDamageMap=" + playerDamageMap +
                ", participants=" + participants +
                ", fightStartTime=" + fightStartTime +
                '}';
    }
}