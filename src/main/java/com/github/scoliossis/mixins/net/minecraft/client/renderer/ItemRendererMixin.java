package com.github.scoliossis.mixins.net.minecraft.client.renderer;

import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.impl.combat.AutoBlock;
import com.github.scoliossis.modules.impl.render.Animations;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.minecraft.PlayerUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow private ItemStack itemToRender;

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, CallbackInfo ci) {
        if (entityIn != C.p()) return;

        if (Animations.shouldHideHeldItem()) {
            ci.cancel();
            return;
        }

        if (this.itemToRender != null && C.mc.gameSettings.thirdPersonView == 0 && ModuleManager.isEnabled(Animations.class)) {
            Vec3 itemScale = Animations.getScaleVec();
            GL11.glScaled(itemScale.xCoord, itemScale.yCoord, itemScale.zCoord);
        }
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getItemInUseCount()I"))
    public int onGetItemInUseCount(AbstractClientPlayer instance) {
        return instance.getItemInUseCount() + (AutoBlock.isBlocking() ? 1 : 0);
    }

    @Inject(method = "renderPlayerArm", at = @At(value = "HEAD"), cancellable = true)
    public void onRenderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress, CallbackInfo ci) {
        if (Animations.shouldHideHeldItem()) ci.cancel();
    }

    @Redirect(method = "renderOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isEntityInsideOpaqueBlock()Z"))
    public boolean overrideIsInsideBlock(EntityPlayerSP instance) {
        if (PlayerUtil.noClipRender) return false;
        return C.p().isEntityInsideOpaqueBlock();
    }

    @Inject(method = "doItemUsedTransformations", at = @At(value = "HEAD"), cancellable = true)
    public void onItemUsedTransformations(float swingProgress, CallbackInfo ci) {
        if (Animations.smoothSwing && ModuleManager.isEnabled(Animations.class)) ci.cancel();
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getSwingProgress(F)F"))
    public float onRenderItemInFirstPerson(AbstractClientPlayer instance, float partialTickTime) {
        if (ModuleManager.isEnabled(Animations.class)) return Animations.getArmSwingProgress(partialTickTime);
        return instance.getSwingProgress(partialTickTime);
    }

    @ModifyArgs(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V"))
    public void onTransformFirstPersonItem(Args args, float partialTicks) {
        if (ModuleManager.isEnabled(Animations.class) && Animations.oldBlocking)
            args.set(1, Animations.getArmSwingProgress(partialTicks));
    }

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;rotateWithPlayerRotations(Lnet/minecraft/client/entity/EntityPlayerSP;F)V", shift = At.Shift.AFTER))
    public void onRotateItem(float partialTicks, CallbackInfo ci) {
        if (this.itemToRender != null && ModuleManager.isEnabled(Animations.class)) {
            Animations.scaleRotateTranslateHeldItem();
        }
    }
}
