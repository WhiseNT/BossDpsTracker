package com.whisent.bossdpstracker.network;

import com.whisent.bossdpstracker.client.ClientBossDpsManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DamagePacket {
    private int bossId;
    private CompoundTag playersData;
    private boolean isFirstUpdate;
    public DamagePacket(int bossId,CompoundTag playersData,boolean isFirstUpdate) {
        this.bossId = bossId;
        this.playersData = playersData;
        this.isFirstUpdate = isFirstUpdate;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(bossId);
        buf.writeNbt(playersData);
        buf.writeBoolean(isFirstUpdate);
    }

    public static DamagePacket decode(FriendlyByteBuf buf) {
        return new DamagePacket(buf.readInt(), buf.readNbt(),buf.readBoolean());
    }
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
                // 客户端处理
                ClientBossDpsManager.setBossData(bossId,playersData);
                if (isFirstUpdate){
                    ClientBossDpsManager.setDisplay(true);
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}