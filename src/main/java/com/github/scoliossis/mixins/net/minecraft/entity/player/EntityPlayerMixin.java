package com.github.scoliossis.mixins.net.minecraft.entity.player;

import com.github.scoliossis.utils.PlayerUtil;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public class EntityPlayerMixin {
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean noClip(EntityPlayer instance) {
        return instance.isSpectator() || PlayerUtil.noClip;
    }
}
