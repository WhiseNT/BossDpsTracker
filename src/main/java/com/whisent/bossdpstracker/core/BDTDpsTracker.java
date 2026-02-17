package com.whisent.bossdpstracker.core;

import com.whisent.bossdpstracker.Config;
import com.whisent.bossdpstracker.api.CustomBossApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BDTDpsTracker {
    public static final Map<UUID,BDTDpsData>  BOSS_DPS_MAP = new ConcurrentHashMap<>();
    public static final Map<UUID,String> PLAYER_NAME_CACHE = new ConcurrentHashMap<>();
    public static BDTDpsData getBossDpsData(UUID uuid) {
        return BOSS_DPS_MAP.getOrDefault(uuid, null);
    }
    public static boolean hasBossDpsData(UUID uuid) {
        return BOSS_DPS_MAP.containsKey(uuid);
    }
    public static void createBossDpsData(ServerLevel level,UUID uuid) {
        Entity boss = level.getEntity(uuid);
        if (boss != null && shouldTrack(boss)) {

            BDTDpsData bossDpsData = new BDTDpsData(level, boss);
            // 检查是否已经有该Boss的数据（例如从文件加载的）
            if (BOSS_DPS_MAP.containsKey(uuid)) {
                return;
            }
            BOSS_DPS_MAP.putIfAbsent(uuid, bossDpsData);
        }

    }
    public static void removeBossDpsData(UUID uuid) {
        BOSS_DPS_MAP.remove(uuid);
    }
    public static void applyDamage(ServerLevel level, UUID bossUUID,UUID playerUUID, float damage) {
        BDTDpsData bossDpsData = getBossDpsData(bossUUID);
        if (bossDpsData != null) {
            bossDpsData.recordDamage(playerUUID, damage);
        }
        BDTNbtSerializer.save(level);

    }

    public static boolean isBoss(Entity entity) {
        TagKey<EntityType<?>> bossTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge","bosses"));
        boolean tagBoss = entity.getType().is(bossTag);

        return tagBoss || CustomBossApi.containsBoss(entity.getType());
    }

    public static boolean shouldTrack(Entity entity) {
        // 根据配置决定是否追踪该生物
        if (Config.trackMode == Config.TrackMode.BOSS) {
            return isBoss(entity);
        } else if (Config.trackMode == Config.TrackMode.HP && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            return livingEntity.getMaxHealth() >= Config.trackHpThreshold;
        }
        return false;
    }
    public static CompoundTag getBossDpsDataNBT(UUID bossUUID) {
        BDTDpsData bossDpsData = getBossDpsData(bossUUID);
        if (bossDpsData != null) {
            return bossDpsData.serialize();
        }
        return null;
    }

}