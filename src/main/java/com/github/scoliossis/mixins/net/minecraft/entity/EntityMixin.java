package com.github.scoliossis.mixins.net.minecraft.entity;

import com.github.scoliossis.events.Bus;
import com.github.scoliossis.events.impl.MoveFlyingEvent;
import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.impl.client.MovementFix;
import com.github.scoliossis.modules.impl.render.Freecam;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.PlayerUtil;
import com.github.scoliossis.utils.RotationUtil;
import com.github.scoliossis.utils.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow private int entityId;

    // modifies friction on "moveFlying" which is called even if you not flying, or moving!
    @ModifyVariable(method = "moveFlying", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    public float editFriction(float friction) {
        if (C.p() == null || this.entityId != C.p().getEntityId()) return friction;

        MoveFlyingEvent moveFlyingEvent = new MoveFlyingEvent(friction);
        Bus.post(moveFlyingEvent);
        return moveFlyingEvent.friction;
    }

    @Redirect(method = "moveFlying", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F"))
    public float editYawOnMoveFlying(Entity instance) {
        return MovementFix.shouldMoveFix(instance) ? PlayerUtil.playerUpdateEvent.rotation.yaw : instance.rotationYaw;
    }

    @Inject(method = "rayTrace", at = @At("HEAD"), cancellable = true)
    public void redirectRayTrace(double blockReachDistance, float partialTicks, CallbackInfoReturnable<MovingObjectPosition> cir) {
        if (this.entityId == C.p().getEntityId()) {
            Vec3 pos = C.p().getPositionEyes(partialTicks);
            RotationUtil.Rotation prevRot = MovementFix.shouldRotationFix() ? PlayerUtil.getPrevPlayerUpdateEvent().rotation : RotationUtil.getPreviousClientRotation();
            RotationUtil.Rotation currentRot = MovementFix.shouldRotationFix() ? PlayerUtil.playerUpdateEvent.rotation : RotationUtil.getCurrentClientRotation();

            if (PlayerUtil.shouldFixPlayerFakeLook()) {
                if (PlayerUtil.realRotation != null) {
                    prevRot = PlayerUtil.realRotation;
                    currentRot = PlayerUtil.realRotation;
                }
                if (PlayerUtil.realPos != null) pos = PlayerUtil.realPos.addVector(0, C.p().getEyeHeight(), 0);
            }

            cir.setReturnValue(WorldUtil.rayTrace(blockReachDistance, partialTicks, pos, prevRot, currentRot));
        }
    }

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    public void isInvisible(CallbackInfoReturnable<Boolean> cir) {
        if (C.p() != null && this.entityId == C.p().getEntityId() && ModuleManager.isEnabled(Freecam.class)) cir.setReturnValue(true);
    }
}