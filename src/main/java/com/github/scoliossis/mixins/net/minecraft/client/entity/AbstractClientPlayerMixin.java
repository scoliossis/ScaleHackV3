package com.github.scoliossis.mixins.net.minecraft.client.entity;

import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.impl.render.NoFOV;
import net.minecraft.client.entity.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(at = @At("HEAD"), method = "getFovModifier", cancellable = true)
    public void getFovModifier(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.isEnabled(NoFOV.class)) cir.setReturnValue(1f);
    }
}
