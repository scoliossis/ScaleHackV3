package com.github.scoliossis.mixins.net.minecraft.entity;

import com.github.scoliossis.modules.impl.client.MovementFix;
import com.github.scoliossis.utils.minecraft.PlayerUtil;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin {
    @Redirect(method = "jump", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYaw:F"))
    public float editYawOnJump(EntityLivingBase instance) {
        return MovementFix.shouldMoveFix(instance) ? PlayerUtil.currentRotation().yaw : instance.rotationYaw;
    }
}