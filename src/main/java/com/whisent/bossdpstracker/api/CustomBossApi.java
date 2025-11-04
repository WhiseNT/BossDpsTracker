package com.whisent.bossdpstracker.api;

import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomBossApi {
    public static Map<EntityType<?>, List<EntityType<?>>> bossMap = new ConcurrentHashMap<>();
    
    public static void addBoss(EntityType<?> bosstype) {
        if (bossMap.containsKey(bosstype)) {
            return;
        }
        bossMap.put(bosstype, new ArrayList<>(List.of(bosstype)));
    }

    private static void addBossBody(EntityType<?> bosstype, EntityType<?> bodytype) {
        if (bossMap.containsKey(bosstype)) {
            bossMap.get(bosstype).add(bodytype);
        } else {
            List<EntityType<?>> list = new ArrayList<>();
            list.add(bodytype);
            bossMap.put(bosstype, list);
        }
    }

    public static boolean containsBoss(EntityType<?> entityType) {
        for (Map.Entry<EntityType<?>, List<EntityType<?>>> entry : bossMap.entrySet()) {
            if (entry.getKey().equals(entityType) && entry.getValue().contains(entityType)) {
                return true;
            }
        }
        return false;
    }
}