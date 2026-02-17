package com.whisent.bossdpstracker.event;

import com.whisent.bossdpstracker.BossDpsTracker;
import com.whisent.bossdpstracker.core.BDTDpsTracker;
import com.whisent.bossdpstracker.core.BDTNbtSerializer;
import com.whisent.bossdpstracker.network.DamagePacket;
import com.whisent.bossdpstracker.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = BossDpsTracker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onLivingSpawn(EntityJoinLevelEvent event) {
        if (BDTDpsTracker.shouldTrack(event.getEntity()) && !event.getLevel().isClientSide) {
            ServerLevel level = (ServerLevel) event.getLevel();
            UUID bossUUID = event.getEntity().getUUID();

            System.out.println("加载后BOSS_DPS_MAP: " + BDTDpsTracker.BOSS_DPS_MAP);
            if (BDTDpsTracker.BOSS_DPS_MAP.containsKey(bossUUID)) {
                System.out.println("Boss数据内容: " + BDTDpsTracker.BOSS_DPS_MAP.get(bossUUID).serialize());
            }
            
            // 清理不存在的Boss数据
            BDTDpsTracker.BOSS_DPS_MAP.keySet().forEach(bossId -> {
                if (level.getEntity(bossId) == null) {
                    BDTDpsTracker.removeBossDpsData(bossId);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob mob) || mob.level().isClientSide) return;

        ServerLevel level = (ServerLevel) mob.level();
        Entity sourceEntity = event.getSource().getEntity();
        
        // 统一处理Boss伤害逻辑
        if (BDTDpsTracker.shouldTrack(mob)) {
            BossDpsTracker.LOGGER.info("Boss Damage: " + mob.getDisplayName().getString() + " " + event.getAmount());
            boolean firstUpdate = false;
            
            // 确保Boss数据存在
            if (!BDTDpsTracker.hasBossDpsData(mob.getUUID())) {
                BDTNbtSerializer.loadBossData(level, mob);
                BossDpsTracker.LOGGER.info("Create Boss Data: " + mob.getDisplayName().getString());
                //无法从文件中加载数据
                BDTDpsTracker.createBossDpsData(level, mob.getUUID());

                firstUpdate = true;
            }
            
            UUID bossUuid = mob.getUUID();
            UUID playerUUID = BossDpsTracker.NON_PLAYER_UUID; // 默认为无来源伤害
            
            // 如果伤害来源是活体实体，则使用来源实体的UUID
            if (!entity.isRemoved() && sourceEntity instanceof LivingEntity) {
                playerUUID = sourceEntity.getUUID();
            }
            
            // 打印调试信息
            System.out.println("准备应用伤害: bossUuid=" + bossUuid + ", playerUUID=" + playerUUID + ", damage=" + event.getAmount());
            System.out.println("应用前BOSS_DPS_MAP: " + BDTDpsTracker.BOSS_DPS_MAP);
            if (BDTDpsTracker.BOSS_DPS_MAP.containsKey(bossUuid)) {
                System.out.println("应用前Boss数据内容: " + BDTDpsTracker.BOSS_DPS_MAP.get(bossUuid).serialize());
            }
            
            // 应用伤害到Boss数据
            BDTDpsTracker.applyDamage(level, bossUuid, playerUUID, event.getAmount());
            
            // 发送更新到客户端
            if (mob.isAlive()) {
                CompoundTag tag = BDTDpsTracker.getBossDpsDataNBT(bossUuid);
                if (tag != null) {
                    System.out.println("发送给客户端的数据: " + tag);
                    NetworkHandler.sendToAllClient(new DamagePacket(mob.getId(), tag, firstUpdate));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        if (BDTDpsTracker.shouldTrack(event.getEntity())) {
            UUID bossUuid = event.getEntity().getUUID();
            BDTDpsTracker.removeBossDpsData(bossUuid);
        }
    }
    
    private static final int UPDATE_INTERVAL = 20;
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {

        Entity entity = event.getEntity();
        if (entity.level().isClientSide || !BDTDpsTracker.shouldTrack(entity)) return;
        if (entity == null || entity.isRemoved()) return;
        ServerLevel level = (ServerLevel) entity.level();
        UUID bossUuid = entity.getUUID();

        long worldTick = level.getGameTime();
        if (worldTick % UPDATE_INTERVAL == 0) {
            if (BDTDpsTracker.hasBossDpsData(bossUuid)) {
                CompoundTag tag = BDTDpsTracker.getBossDpsDataNBT(bossUuid);
                if (tag != null) {
                    NetworkHandler.sendToAllClient(new DamagePacket(entity.getId(), tag,false));
                }
            }
        }
    }
    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {

    }
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {

    }

    private static int tickCount = 0;
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        tickCount+=1;
        if (tickCount % 20*10 == 0) {
            tickCount = 0;
            //CompoundTag tag = NbtIo.read(BDTNbtSerializer.CURRENT_NBT_FILE.toFile());
            for (ServerLevel level : event.getServer().getAllLevels()) {

            }

        }
    } 
}