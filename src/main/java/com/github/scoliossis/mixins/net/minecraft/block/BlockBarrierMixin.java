package com.github.scoliossis.mixins.net.minecraft.block;

import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.impl.render.RenderBarriers;
import net.minecraft.block.BlockBarrier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBarrier.class)
public class BlockBarrierMixin {
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    public void onGetRenderType(CallbackInfoReturnable<Integer> cir) {
        // The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
        if (ModuleManager.isEnabled(RenderBarriers.class)) cir.setReturnValue(3);
    }
}
