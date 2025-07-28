package com.whisent.bossdpstracker.core;

import com.whisent.bossdpstracker.BossDpsTracker;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.mojang.text2speech.Narrator.LOGGER;

public class BDTDpsTracker {
    public static final Map<UUID,BDTDpsData> BOSS_DPS_MAP = new ConcurrentHashMap<>();
    public static final Map<UUID,String> PLAYER_NAME_CACHE = new ConcurrentHashMap<>();
    public static BDTDpsData getBossDpsData(UUID uuid) {
        return BOSS_DPS_MAP.getOrDefault(uuid, null);
    }
    public static boolean hasBossDpsData(UUID uuid) {
        return BOSS_DPS_MAP.containsKey(uuid);
    }
    public static void createBossDpsData(ServerLevel level,UUID uuid) {
        Entity boss = level.getEntity(uuid);
        BDTDpsData bossDpsData = new BDTDpsData(level,boss);
        BOSS_DPS_MAP.putIfAbsent(uuid, bossDpsData);
    }
    public static void removeBossDpsData(UUID uuid) {
        BOSS_DPS_MAP.remove(uuid);
    }
    public static void setBossDpsDataNBT(UUID bossId, CompoundTag tag) {
        // 1. 检查必需的顶层容器
        if (!tag.contains("boss") || !tag.contains("playersInfo")) {
            LOGGER.warn("Invalid NBT structure for Boss DPS data. Missing 'boss' or 'playersInfo'. Boss ID: {}", bossId);
            return;
        }

        CompoundTag bossTag = tag.getCompound("boss");
        CompoundTag playersInfoTag = tag.getCompound("playersInfo");

        // 2. 检查 boss 容器中的关键数据
        if (!bossTag.contains("time", net.minecraft.nbt.Tag.TAG_LONG) || !bossTag.hasUUID("id")) {
            LOGGER.warn("Missing fight start time or boss ID in 'boss' tag. Boss ID: {}", bossId);
            return; // 关键数据缺失，放弃加载
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            LOGGER.warn("Cannot load Boss DPS data: Server is not running.");
            return;
        }

        ServerLevel level = server.overworld();

        // 3. 获取保存的 Boss Entity UUID 和 fightStartTime
        UUID savedBossEntityUUID = bossTag.getUUID("id");
        long savedFightStartTime = bossTag.getLong("time");

        // 4. 尝试获取 Boss 实体
        Entity bossEntity = level.getEntity(savedBossEntityUUID);
        // 如果实体暂时不在世界中，bossEntity 会是 null，但数据仍然可以重建

        // 5. 创建新的 BDTDpsData 实例
        BDTDpsData bossDpsData = new BDTDpsData(level, bossEntity);

        // 6. 精确设置 fightStartTime
        // 需要 BDTDpsData 提供一个包私有的 setter 方法
        bossDpsData.setFightStartTime(savedFightStartTime);
        LOGGER.debug("Recovered exact fight start time: {} for Boss (Saved ID: {})", savedFightStartTime, savedBossEntityUUID);

        // 7. 恢复玩家数据
        // 获取 playersInfo 中的所有玩家名字
        Set<String> playerNames = playersInfoTag.getAllKeys();

        for (String playerName : playerNames) {
            CompoundTag playerData = playersInfoTag.getCompound(playerName);

            // 检查必需的伤害数据
            if (!playerData.contains("totalDamage", net.minecraft.nbt.Tag.TAG_FLOAT)) {
                LOGGER.warn("Player '{}' data missing 'totalDamage'. Skipping. (Boss: {})", playerName, bossId);
                continue;
            }

            float totalDamage = playerData.getFloat("totalDamage");
            // dps 和 percentage 字段用于验证，但 totalDamage 是核心

            // 8. 将玩家名转换为 UUID
            ServerPlayer playerEntity = server.getPlayerList().getPlayerByName(playerName);
            if (playerEntity == null) {
                LOGGER.warn("Player '{}' (from NBT) is not online. Cannot recover UUID. Skipping damage data. (Boss: {})", playerName, bossId);
                continue;
            }
            UUID playerUUID = playerEntity.getUUID();

            // 9. 恢复伤害数据到 BDTDpsData
            // 直接操作内部 map 或使用内部方法
            bossDpsData.getPlayerDamageMap().put(playerUUID, totalDamage);
            bossDpsData.addParticipant(playerUUID);
        }

        // 10. 将重建的精确数据放入全局映射
        // 使用传入的 bossId 作为键 (通常 loadAllBossData 从 "BossId" 读取)
        BOSS_DPS_MAP.put(bossId, bossDpsData);
        LOGGER.info("Successfully loaded Boss DPS data for Boss (Mapped ID: {}, Entity ID: {}) with {} participants",
                bossId, savedBossEntityUUID);
    }
    public static void applyDamage(ServerLevel level, UUID bossUUID,UUID playerUUID, float damage) {
        BDTDpsData bossDpsData = getBossDpsData(bossUUID);
        if (bossDpsData != null) {
            bossDpsData.recordDamage(playerUUID, damage);
        }
    }
    public static final String DATA_FILE_NAME = "boss_dps_data.nbt";
    private static final LevelResource DATA_DIR = new LevelResource("data");

    public static boolean isBoss(Entity entity) {
        TagKey<EntityType<?>> bossTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge","bosses"));
        return entity.getType().is(bossTag);
    }
    public static CompoundTag getBossDpsDataNBT(UUID bossUUID) {
        BDTDpsData bossDpsData = getBossDpsData(bossUUID);
        if (bossDpsData != null) {
            return bossDpsData.serialize();
        }
        return null;
    }



}
