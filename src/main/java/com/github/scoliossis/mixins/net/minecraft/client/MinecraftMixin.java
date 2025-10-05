package com.github.scoliossis.mixins.net.minecraft.client;

import com.github.scoliossis.bridge.net.minecraft.client.MinecraftBridge;
import com.github.scoliossis.bridge.net.minecraft.util.SessionBridge;
import com.github.scoliossis.bridge.net.minecraft.util.TimerBridge;
import com.github.scoliossis.events.Bus;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.events.impl.KeyPressedEvent;
import com.github.scoliossis.events.impl.MouseScrolledEvent;
import com.github.scoliossis.events.impl.RotationEvent;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.FrameUtil;
import com.github.scoliossis.utils.PlayerUtil;
import com.github.scoliossis.utils.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin implements MinecraftBridge {
    @Shadow private Timer timer;

    @Shadow private Session session;

    @Inject(method = "createDisplay", at = @At("TAIL"))
    public void onCreateDisplay(CallbackInfo ci) {
        FrameUtil.init();
    }

    @Inject(method = "runTick", at = @At(value = "HEAD"))
    public void onRunTick(CallbackInfo ci) {
        Bus.post(new ClientTickEvent());
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;getMouseOver(F)V"))
    private void onRunGameLoop(CallbackInfo ci) {
        if (C.isInGame()) {
            // i dont like this
            PlayerUtil.fakePlayerPosAndRot();
            PlayerUtil.setRotationEvent(new RotationEvent(RotationUtil.getCurrentClientRotation()));
            PlayerUtil.resetFakePlayerPosAndRot();
        }
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I"))
    private int runTick$getEventDWheel() {
        int scrollAmount = Mouse.getEventDWheel();
        if (scrollAmount != 0) {
            MouseScrolledEvent event = new MouseScrolledEvent(scrollAmount);
            Bus.post(event);

            if (event.isCancelled()) return 0;
        }

        return scrollAmount;
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V"))
    private void runTick$dispatchKeypresses(CallbackInfo ci) {
        int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() : Keyboard.getEventKey();

        if (i != 0 && !Keyboard.isRepeatEvent()) {
            Bus.post(new KeyPressedEvent(i, Keyboard.getEventKeyState()));
        }
    }

    @Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isEntityInsideOpaqueBlock()Z"))
    private boolean overrideCanF5inBlocks(EntityPlayerSP instance) {
        if (PlayerUtil.noClipRender) return false;
        return C.p().isEntityInsideOpaqueBlock();
    }

    public TimerBridge bridge$getTimer() {
        return TimerBridge.from(this.timer);
    }

    public void bridge$setSession(SessionBridge session) {
        this.session = (Session) session;
    }
}
