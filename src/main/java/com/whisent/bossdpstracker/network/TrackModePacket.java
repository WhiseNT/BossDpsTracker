package com.whisent.bossdpstracker.network;

import com.whisent.bossdpstracker.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TrackModePacket {
    private Config.TrackMode trackMode;
    private int hpThreshold;
    
    public TrackModePacket(Config.TrackMode trackMode, int hpThreshold) {
        this.trackMode = trackMode;
        this.hpThreshold = hpThreshold;
    }
    
    public TrackModePacket(FriendlyByteBuf buf) {
        this.trackMode = buf.readEnum(Config.TrackMode.class);
        this.hpThreshold = buf.readInt();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(trackMode);
        buf.writeInt(hpThreshold);
    }
    public static TrackModePacket decode(FriendlyByteBuf buf) {
        Config.TrackMode trackMode = buf.readEnum(Config.TrackMode.class);
        int hpThreshold = buf.readInt();
        return new TrackModePacket(trackMode, hpThreshold);
    }
    
    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            // 检查权限：单人游戏中的玩家或服务器管理员都有权限
            boolean hasPermission = player != null && 
                (player.getServer().isSingleplayer() || player.hasPermissions(2));
                
            if (hasPermission) {
                // 更新服务器配置
                Config.trackMode = this.trackMode;
                Config.trackHpThreshold = this.hpThreshold;
                Config.saveServerConfig();
                
                // 发送确认消息给玩家
                player.sendSystemMessage(Component.literal("追踪模式已更新为: " + 
                    (this.trackMode == Config.TrackMode.BOSS ? "Boss模式" : "血量模式(" + this.hpThreshold + "血)")));
            } else {
                // 没有权限，发送错误消息
                if (player != null) {
                    player.sendSystemMessage(Component.literal("在多人服务器中需要管理员权限才能更改追踪模式"));
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}