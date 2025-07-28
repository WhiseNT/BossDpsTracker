package com.whisent.bossdpstracker.network;

import com.whisent.bossdpstracker.client.ClientBossDpsManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DamagePacket {
    private final int bossId;
    private final CompoundTag playersData;
    public DamagePacket(int bossId,CompoundTag playersData) {
        this.bossId = bossId;
        this.playersData = playersData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(bossId);
        buf.writeNbt(playersData);
    }

    public static DamagePacket decode(FriendlyByteBuf buf) {
        return new DamagePacket(buf.readInt(), buf.readNbt());
    }
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(()->{
            contextSupplier.get().enqueueWork(() -> {
                if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
                    // 客户端处理
                    ClientBossDpsManager.setBossData(bossId,playersData);
                    ClientBossDpsManager.setDisplay(true);
                }
            });
            contextSupplier.get().setPacketHandled(true);
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
