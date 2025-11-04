package com.whisent.bossdpstracker.core;

import com.whisent.bossdpstracker.BossDpsTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class BDTNbtSerializer {
    public static Path CURRENT_NBT_FILE = ServerLifecycleHooks.getCurrentServer()
                .getWorldPath(LevelResource.ROOT)
                .getParent().resolve("data")
                .resolve("bdt_current.nbt");
    public static void save(ServerLevel level) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<UUID, BDTDpsData> entry : BDTDpsTracker.BOSS_DPS_MAP.entrySet()) {
            UUID bossUUID = entry.getKey();
            if (!level.isClientSide) {
                var Entity = ((ServerLevel)level).getEntity(bossUUID);
                System.out.println(Entity);
            }
            BDTDpsData bossDpsData = entry.getValue();
            tag.put(bossUUID.toString(), bossDpsData.serialize());
            System.out.println("保存boss数据"+tag);
        }

        try {
            NbtIo.write(tag, CURRENT_NBT_FILE.toFile());
        } catch (IOException e) {
            BossDpsTracker.LOGGER.error("保存boss数据失败", e);
        }
        System.out.println(CURRENT_NBT_FILE);
    }


    public static void loadBossData(ServerLevel level,Entity bossEntity) {
        try {
            CompoundTag tag = NbtIo.read(CURRENT_NBT_FILE.toFile());
            if (tag != null && tag.contains(bossEntity.getUUID().toString()) &&
                    !BDTDpsTracker.BOSS_DPS_MAP.containsKey(bossEntity.getUUID())) {
                //System.out.println("从文件加载Boss数据: " + tag);
                BDTDpsData bossDpsData = BDTDpsData.deserialize(
                        tag.getCompound(bossEntity.getUUID().toString()), level, bossEntity);
                BDTDpsTracker.BOSS_DPS_MAP.put(bossEntity.getUUID(), bossDpsData);
            }
        } catch (IOException e) {
            //BossDpsTracker.LOGGER.error("加载boss数据失败", e);
            //System.out.println("加载Boss数据时发生错误: " + e.getMessage());
        }
    }
    public static void cleanBossData(CompoundTag tag,ServerLevel level) {
        try {

            if (tag != null) {
                for (String stringUUID : tag.getAllKeys()) {
                    UUID bossUUID = UUID.fromString(stringUUID);
                    Entity bossEntity = level.getEntity(bossUUID);
                    if (bossEntity == null) {
                        tag.remove(stringUUID);
                        return;
                    }
                }
                NbtIo.write(tag, CURRENT_NBT_FILE.toFile());
            }
        } catch (IOException e) {
            BossDpsTracker.LOGGER.error("清理boss数据失败", e);
        }

    }

}