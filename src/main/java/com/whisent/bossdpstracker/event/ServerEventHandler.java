package com.whisent.bossdpstracker.event;

import com.whisent.bossdpstracker.BossDpsTracker;
import com.whisent.bossdpstracker.core.BDTDpsTracker;
import com.whisent.bossdpstracker.network.DamagePacket;
import com.whisent.bossdpstracker.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import net.minecraftforge.event.entity.living.LivingEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;


import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BossDpsTracker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onLivingSpawn(EntityJoinLevelEvent event) {
        if (BDTDpsTracker.isBoss(event.getEntity()) && !event.getLevel().isClientSide) {
            ServerLevel level = (ServerLevel) event.getLevel();
            BDTDpsTracker.BOSS_DPS_MAP.keySet().forEach(bossId -> {
                if (level.getEntity(bossId) == null) {
                    BDTDpsTracker.removeBossDpsData(bossId);
                }
            });
            if (!BDTDpsTracker.hasBossDpsData(event.getEntity().getUUID())) {
                BDTDpsTracker.createBossDpsData((ServerLevel) event.getLevel(),event.getEntity().getUUID());
            }

        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob mob) || mob.level().isClientSide) return;

        ServerLevel level = (ServerLevel) mob.level();
        Entity sourceEntity = event.getSource().getEntity();
        if (!entity.isRemoved() && sourceEntity instanceof LivingEntity) {
            if (BDTDpsTracker.isBoss(mob)) {
                if (!BDTDpsTracker.hasBossDpsData(mob.getUUID())) {
                    BDTDpsTracker.createBossDpsData(level, mob.getUUID());
                }
                UUID bossUuid = mob.getUUID();
                UUID playerUuid = sourceEntity.getUUID();

                // ✅ 先确保 Boss 数据已创建
                if (!BDTDpsTracker.hasBossDpsData( bossUuid)) {
                    BDTDpsTracker.createBossDpsData(level, bossUuid);
                }

                BDTDpsTracker.applyDamage(level, bossUuid, playerUuid, event.getAmount());


                if (mob.isAlive()) {
                    CompoundTag tag = BDTDpsTracker.getBossDpsDataNBT(bossUuid);
                    if (tag != null) {
                        NetworkHandler.sendToAllClient(new DamagePacket(mob.getId(), tag));
                    }
                }
            }
        }else {
            if (!BDTDpsTracker.hasBossDpsData(mob.getUUID())) {
                BDTDpsTracker.createBossDpsData(level, mob.getUUID());
            }
            UUID bossUuid = mob.getUUID();
            UUID playerUuid = BossDpsTracker.NON_PLAYER_UUID;
            // ✅ 先确保 Boss 数据已创建
            if (!BDTDpsTracker.hasBossDpsData( bossUuid)) {
                BDTDpsTracker.createBossDpsData(level, bossUuid);
            }
            BDTDpsTracker.applyDamage(level, bossUuid, playerUuid, event.getAmount());

        }

    }
    private static final int UPDATE_INTERVAL = 20;
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {

        Entity entity = event.getEntity();
        if (entity.level().isClientSide || !BDTDpsTracker.isBoss(entity)) return;
        if (entity == null || entity.isRemoved()) return;
        ServerLevel level = (ServerLevel) entity.level();
        UUID bossUuid = entity.getUUID();

        long worldTick = level.getGameTime();
        if (worldTick % UPDATE_INTERVAL == 0) {
            if (BDTDpsTracker.hasBossDpsData(bossUuid)) {
                CompoundTag tag = BDTDpsTracker.getBossDpsDataNBT(bossUuid);
                if (tag != null) {
                    NetworkHandler.sendToAllClient(new DamagePacket(entity.getId(), tag));
                }
            }
        }
    }



}
