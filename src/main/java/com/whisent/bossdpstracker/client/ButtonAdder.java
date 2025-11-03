package com.whisent.bossdpstracker.client;

import com.whisent.bossdpstracker.mixin.AccessPauseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ButtonAdder {
    public static void addButton(Screen screen) {
        Button configButton = Button.builder(
                        Component.literal("BDT"),
                        btn -> {
                            Minecraft.getInstance().setScreen(new BossDpsScreen(screen));
                        })
                .pos(screen.width / 2 + 104, screen.height / 4 + 75)
                .size(22, 20)
                .build();
        ((AccessPauseScreen) screen).bdt$addRenderableWidget(configButton);

    }
}
