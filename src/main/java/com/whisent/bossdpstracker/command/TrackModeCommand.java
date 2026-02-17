package com.whisent.bossdpstracker.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.whisent.bossdpstracker.BossDpsTracker;
import com.whisent.bossdpstracker.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TrackModeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("trackmode")
            .executes(TrackModeCommand::showCurrentMode)
            .then(Commands.literal("boss")
                .executes(TrackModeCommand::setBossMode)
            )
            .then(Commands.literal("hp")
                .executes(context -> setHpMode(context, 200)) // 默认200血
                .then(Commands.argument("threshold", IntegerArgumentType.integer(1, 10000))
                    .executes(context -> setHpMode(context, IntegerArgumentType.getInteger(context, "threshold")))
                )
            )
        );
    }

    private static int showCurrentMode(CommandContext<CommandSourceStack> context) {
        Config.TrackMode mode = Config.trackMode;
        String message = "当前追踪模式: " + 
            (mode == Config.TrackMode.BOSS ? "Boss模式" : "血量模式(" + Config.trackHpThreshold + "血)");
        
        context.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int setBossMode(CommandContext<CommandSourceStack> context) {
        Config.trackMode = Config.TrackMode.BOSS;
        Config.saveServerConfig();
        
        context.getSource().sendSuccess(() -> Component.literal("已切换到Boss追踪模式"), true);
        BossDpsTracker.LOGGER.info("Track mode changed to BOSS by " + context.getSource().getTextName());
        return 1;
    }

    private static int setHpMode(CommandContext<CommandSourceStack> context, int threshold) {
        Config.trackMode = Config.TrackMode.HP;
        Config.trackHpThreshold = threshold;
        Config.saveServerConfig();
        
        context.getSource().sendSuccess(() -> Component.literal("已切换到血量追踪模式，阈值: " + threshold), true);
        BossDpsTracker.LOGGER.info("Track mode changed to HP with threshold " + threshold + " by " + context.getSource().getTextName());
        return 1;
    }
}