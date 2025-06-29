package com.github.scoliossis.mixins.net.minecraft.network;

import com.github.scoliossis.events.Bus;
import com.github.scoliossis.events.impl.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (Bus.post(new PacketEvent.Send(packet))) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onChannelReadHead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (Bus.post(new PacketEvent.Receive(packet)))
            ci.cancel();
    }
}