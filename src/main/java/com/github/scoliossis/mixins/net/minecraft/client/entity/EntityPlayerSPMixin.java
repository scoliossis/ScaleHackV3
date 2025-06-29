package com.github.scoliossis.mixins.net.minecraft.client.entity;

import com.github.scoliossis.events.Bus;
import com.github.scoliossis.events.impl.MotionEvent;
import com.github.scoliossis.events.impl.MovementInputEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.impl.movement.Sneak;
import com.github.scoliossis.modules.impl.render.Animations;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.PlayerUtil;
import com.github.scoliossis.utils.RotationUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {
    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void onUpdate$pre(CallbackInfo ci) {
        PlayerUtil.fakePlayerPosAndRot();

        PlayerUtil.setLastPlayerUpdateEvent(new PlayerUpdateEvent(RotationUtil.getCurrentClientRotation()));
        Bus.post(PlayerUtil.playerUpdateEvent);
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void onUpdate$post(CallbackInfo ci) {
        PlayerUtil.resetFakePlayerPosAndRot();
    }

    // injects after minecraft decides what keys your pressing
    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V", shift = At.Shift.AFTER))
    public void editPlayerMoveInput(CallbackInfo ci) {
        MovementInputEvent movementInputEvent = new MovementInputEvent(C.p().movementInput);
        Bus.post(movementInputEvent);
        C.p().movementInput = movementInputEvent.movementInput;

        // increase sneak speed
        if (ModuleManager.isEnabled(Sneak.class) && C.p().isSneaking()) {
            C.p().movementInput.moveStrafe /= 0.3f;
            C.p().movementInput.moveForward /= 0.3f;
        }
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/util/MovementInput;sneak:Z"))
    public boolean editPlayerSneaking(MovementInput instance) {
        // the value of this only matters if you're flying
        if (!C.p().onGround) return Keyboard.isKeyDown(C.mc.gameSettings.keyBindSneak.getKeyCode()) && C.mc.currentScreen == null;
        // overwrite isSneaking on ground to make sure sprinting ticks arnt reset!
        return C.p().movementInput.sneak && !ModuleManager.isEnabled(Sneak.class);
    }


    @Inject(method = "onUpdateWalkingPlayer", at = @At(value = "HEAD"))
    public void onUpdateWalkingPlayer(CallbackInfo ci) {
        PlayerUtil.motionEvent = new MotionEvent(
                new Vec3(C.p().posX, C.p().getEntityBoundingBox().minY, C.p().posZ),
                C.p().isSneaking(),
                C.p().isSprinting(),
                C.p().onGround
        );

        Bus.post(PlayerUtil.motionEvent);
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotationYaw:F"))
    public float editRotationYaw(EntityPlayerSP instance) {
        return PlayerUtil.playerUpdateEvent.rotation.yaw;
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotationPitch:F"))
    public float editRotationPitch(EntityPlayerSP instance) {
        return PlayerUtil.playerUpdateEvent.rotation.pitch;
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;posX:D"))
    public double editPosX(EntityPlayerSP instance) {
        return PlayerUtil.motionEvent.pos.xCoord;
    }
    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/util/AxisAlignedBB;minY:D"))
    public double editPosY(AxisAlignedBB instance) {
        return PlayerUtil.motionEvent.pos.yCoord;
    }
    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;posZ:D"))
    public double editPosZ(EntityPlayerSP instance) {
        return PlayerUtil.motionEvent.pos.zCoord;
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSneaking()Z"))
    public boolean editIsSneaking(EntityPlayerSP instance) {
        return PlayerUtil.motionEvent.sneaking;
    }
    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSprinting()Z"))
    public boolean editIsSprinting(EntityPlayerSP instance) {
        return PlayerUtil.motionEvent.sprinting;
    }
    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onGround:Z"))
    public boolean editOnGround(EntityPlayerSP instance) {
        return PlayerUtil.motionEvent.onGround;
    }

    // theres a better way to do this surely.
    @Unique private boolean scale$wasLeftMouseDown = false;

    // mimics EntityLivingBase.updateArmSwingProgress, which is called just after this
    @Inject(method = "updateEntityActionState", at = @At("HEAD"))
    public void onUpdateEntityActionState(CallbackInfo ci) {
        if (Animations.canReswing() && Animations.oldBlocking) {
            if ((!scale$wasLeftMouseDown && C.mc.gameSettings.keyBindAttack.isKeyDown()) || (C.mc.gameSettings.keyBindAttack.isKeyDown() && C.mc.objectMouseOver != null && C.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)) {
                Animations.fakeIsSwingInProgress = true;
                Animations.fakeSwingProgressInt = -1;
            }
        }

        int i = Animations.getFakeArmSwingAnimationEnd();

        if (Animations.fakeIsSwingInProgress) {
            ++Animations.fakeSwingProgressInt;

            if (Animations.fakeSwingProgressInt >= i) {
                Animations.fakeSwingProgressInt = 0;
                Animations.fakeIsSwingInProgress = false;
            }
        }
        else {
            Animations.fakeSwingProgressInt = 0;
        }

        Animations.prevFakeSwingProgress = Animations.fakeSwingProgress;
        Animations.fakeSwingProgress = ((float) Animations.fakeSwingProgressInt / (float) i);
        scale$wasLeftMouseDown = C.mc.gameSettings.keyBindAttack.isKeyDown();
    }

    @Inject(method = "swingItem", at = @At("HEAD"))
    private void swingItem(CallbackInfo ci) {
        if (Animations.canReswing()) {
            Animations.fakeIsSwingInProgress = true;
            Animations.fakeSwingProgressInt = -1;
        }
    }
}
