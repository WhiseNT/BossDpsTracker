package com.whisent.bossdpstracker.mixin;

import com.whisent.bossdpstracker.client.ButtonAdder;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PauseScreen.class)
public class IngameMenuScreenMixin  {

    
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        PauseScreen screen = (PauseScreen) (Object) this;
        ButtonAdder.addButton(screen);

    }
}