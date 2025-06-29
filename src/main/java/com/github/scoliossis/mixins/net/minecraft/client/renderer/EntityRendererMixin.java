package com.github.scoliossis.mixins.net.minecraft.client.renderer;

import com.github.scoliossis.events.Bus;
import com.github.scoliossis.events.impl.RenderTickEvent;
import com.github.scoliossis.events.impl.RenderWorldEvent;
import com.github.scoliossis.modules.impl.client.MovementFix;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.PlayerUtil;
import com.github.scoliossis.utils.RenderUtil;
import com.github.scoliossis.utils.RotationUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    // my only other minecraft mod uses https://github.com/Sunderw3k/InjectAPI which has cuter and mixins <3
    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    private void updateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        RenderUtil.renderSide = RenderUtil.RenderSide.Tick;
        Bus.post(new RenderTickEvent());
    }

    @Inject(method = "renderWorldPass", at = @At(value = "HEAD"))
    private void onRenderWorldPassHEAD(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        RenderUtil.renderSide = RenderUtil.RenderSide.World;
    }

    @Inject(method = "renderWorldPass", at = @At(value = "TAIL"))
    private void onRenderWorldPassTAIL(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        RenderUtil.renderSide = RenderUtil.RenderSide.Else;
    }

    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"))
    private void onRenderWorldPass(Profiler instance, String name, int pass, float partialTicks, long finishTimeNano) {
        if (name.equals("hand")) {
            Bus.post(new RenderWorldEvent(partialTicks));
        }
        instance.endStartSection(name);
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getLook(F)Lnet/minecraft/util/Vec3;"))
    public Vec3 redirectGetLook(Entity instance, float partialTicks) {
        RotationUtil.Rotation prevRot = MovementFix.shouldRotationFix() ? PlayerUtil.prevPlayerUpdateEvent.rotation : RotationUtil.getPreviousClientRotation();
        RotationUtil.Rotation currentRot = MovementFix.shouldRotationFix() ? PlayerUtil.playerUpdateEvent.rotation : RotationUtil.getCurrentClientRotation();

        if (PlayerUtil.shouldFixPlayerFakeLook()) {
            if (PlayerUtil.realRotation != null) {
                prevRot = PlayerUtil.realRotation;
                currentRot = PlayerUtil.realRotation;
            }
        }

        float f = prevRot.pitch + (currentRot.pitch - prevRot.pitch) * partialTicks;
        float f1 = prevRot.yaw + (currentRot.yaw - prevRot.yaw) * partialTicks;
        return PlayerUtil.getVectorForRotation(f, f1);
    }

    @Inject(method = "getMouseOver", at = @At(value = "HEAD"))
    public void onGetMouseOverHEAD(float partialTicks, CallbackInfo ci) {
        // if player rotation or pos is spoofed, we want the real player pos to handle interactions, unless freecam is on
        if (!PlayerUtil.shouldInteractFromFakePos())
            PlayerUtil.fakePlayerPosAndRot();
    }

    @Inject(method = "getMouseOver", at = @At(value = "TAIL"))
    public void onGetMouseOverTAIL(float partialTicks, CallbackInfo ci) {
        if (!PlayerUtil.shouldInteractFromFakePos())
            PlayerUtil.resetFakePlayerPosAndRot();
    }


    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"))
    private boolean overrideIsSpectator(EntityPlayerSP instance) {
        return PlayerUtil.noClipRender || C.p().isSpectator();
    }

}