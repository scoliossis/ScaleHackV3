package com.github.scoliossis.mixins.net.minecraft.client.renderer.entity;

import com.github.scoliossis.modules.impl.client.ShowRotations;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RendererLivingEntity.class)
public class RenderLivingEntityMixin {
    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRotationPitch:F"))
    public float overridePrevRenderPitch(EntityLivingBase instance) {
        return ShowRotations.getRotation(instance, instance.prevRotationPitch, ShowRotations.RotationPart.PITCH, false);
    }
    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationPitch:F"))
    public float overrideCurrentRenderPitch(EntityLivingBase instance) {
        return ShowRotations.getRotation(instance, instance.rotationPitch, ShowRotations.RotationPart.PITCH, true);
    }

    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRotationYawHead:F"))
    public float overridePrevHeadYaw(EntityLivingBase instance) {
        return ShowRotations.getRotation(instance, instance.prevRotationYawHead, ShowRotations.RotationPart.HEAD_YAW, false);
    }
    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYawHead:F"))
    public float overrideCurrentHeadYaw(EntityLivingBase instance) {
        return ShowRotations.getRotation(instance, instance.rotationYawHead, ShowRotations.RotationPart.HEAD_YAW, true);
    }

    // ik that normally rotations don't look like this, they are slightly smoother, but idc.
    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRenderYawOffset:F"))
    public float overridePrevRenderYaw(EntityLivingBase instance) {
        return ShowRotations.getRotation(instance, instance.prevRenderYawOffset, ShowRotations.RotationPart.BODY_YAW, false);
    }
    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;renderYawOffset:F"))
    public float overrideCurrentRenderYaw(EntityLivingBase instance) {
        return ShowRotations.getRotation(instance, instance.renderYawOffset, ShowRotations.RotationPart.BODY_YAW, true);
    }
}