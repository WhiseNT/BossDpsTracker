package com.whisent.bossdpstracker.mixin;

import com.whisent.bossdpstracker.client.BossDpsScreen;
import com.whisent.bossdpstracker.client.ButtonAdder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class IngameMenuScreenMixin  {
    
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        PauseScreen screen = (PauseScreen) (Object) this;
        ButtonAdder.addButton(screen);
    }
}