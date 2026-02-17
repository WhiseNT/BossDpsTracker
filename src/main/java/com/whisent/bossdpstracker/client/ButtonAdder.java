package com.whisent.bossdpstracker.client;

import com.whisent.bossdpstracker.mixin.AccessScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ButtonAdder {
    public static void addButton(Screen screen) {
        Button configButton = Button.builder(
                        Component.literal("BDT"),
                        btn -> {
                            Minecraft.getInstance().setScreen(new BossDpsMainMenu(screen));
                        })
                .pos(screen.width / 2 + 124, screen.height / 4 + 75)
                .size(22, 20)
                .build();
        screen.addRenderableWidget(configButton);

    }
}