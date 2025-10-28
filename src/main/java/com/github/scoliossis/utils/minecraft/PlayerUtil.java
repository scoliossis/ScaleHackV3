package com.github.scoliossis.utils.minecraft;

import com.github.scoliossis.bridge.net.minecraft.client.MinecraftBridge;
import com.github.scoliossis.bridge.net.minecraft.client.settings.KeyBindingBridge;
import com.github.scoliossis.events.Bus;
import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.events.impl.MotionEvent;
import com.github.scoliossis.events.impl.RotationEvent;
import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.impl.combat.AutoBlock;
import com.github.scoliossis.modules.impl.render.Freecam;
import com.github.scoliossis.utils.client.C;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class PlayerUtil {
    public static boolean canAttack() {
        return !AutoBlock.isBlockingSwing() || AutoBlock.canSwingWhileBlocking();
    }

    public static boolean attack(Entity target) {
        if (canAttack()) {
            C.p().swingItem();
            C.mc.playerController.attackEntity(C.p(), target);

            return true;
        }

        return false;
    }

    private static int lastUnblock = -1;
    private static boolean wasBlocking = false;

    public static int getLastUnblock() {
        if (wasBlocking && !C.p().isUsingItem()) lastUnblock = MovementUtil.ticks;
        return lastUnblock;
    }

    @SubscribeEvent
    public static void updateLastBlocking(MotionEvent event) {
        wasBlocking = C.p().isUsingItem();
    }

    // i only learnt today (4/10/25) that when using a comma between initializing variables they arnt both set to the final value
    @Getter
    private static RotationEvent previousRotationEvent, currentRotationEvent = new RotationEvent(new RotationUtil.Rotation(0,0));

    public static void setRotationEvent(RotationEvent event) {
        previousRotationEvent = currentRotationEvent;
        currentRotationEvent = event;
        Bus.post(PlayerUtil.getCurrentRotationEvent());
    }

    public static RotationUtil.Rotation lastRotation() {
        return previousRotationEvent == null ? currentRotationEvent.rotation : previousRotationEvent.rotation;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        previousRotationEvent = null;
    }

    public static RotationUtil.Rotation currentRotation() {
        return currentRotationEvent.rotation;
    }

    public static float prevServerRenderYawOffset;
    public static float serverRenderYawOffset;

    public static MotionEvent motionEvent;

    public static boolean noClip = false;
    public static boolean noClipRender = false;

    public static Vec3 prevFakeCameraPos, fakeCameraPos, realPos;
    public static RotationUtil.Rotation fakeRotation, realRotation;

    public static boolean isRenderingGuiInventory = false;

    @SubscribeEvent
    public static void onClientTickEvent(ClientTickEvent event) {
        if (!C.isInGame() || C.p().isDead) {
            PlayerUtil.setFakeCameraPos(null);
            PlayerUtil.fakeRotation = null;
        }
    }

    public static void setFakeCameraPos(Vec3 pos) {
        prevFakeCameraPos = pos == null ? null : fakeCameraPos;
        fakeCameraPos = pos;
    }

    public static boolean shouldFixPlayerFakeLook() {
        return !ModuleManager.isEnabled(Freecam.class) || !Freecam.allowInteract;
    }

    public static void fakePlayerPosAndRot() {
        if (!C.isInGame() || C.p().isDead) {
            PlayerUtil.realPos = null;
            return;
        }

        if (PlayerUtil.realPos != null) {
            MovementUtil.setPrevPos(PlayerUtil.realPos);
            MovementUtil.setPos(PlayerUtil.realPos);
            PlayerUtil.realPos = null;
        }

        if (PlayerUtil.fakeRotation != null) {
            PlayerUtil.fakeRotation = RotationUtil.getCurrentClientRotation();

            C.p().rotationYaw = PlayerUtil.realRotation.yaw;
            C.p().rotationPitch = PlayerUtil.realRotation.pitch;
        }
        else if (PlayerUtil.realRotation != null) {
            C.p().rotationYaw = PlayerUtil.realRotation.yaw;
            C.p().rotationPitch = PlayerUtil.realRotation.pitch;

            PlayerUtil.realRotation = null;
        }
    }

    public static void resetFakePlayerPosAndRot() {
        if (PlayerUtil.prevFakeCameraPos != null && PlayerUtil.realPos == null) {
            PlayerUtil.realPos = C.p().getPositionVector();
            MovementUtil.setPrevPos(PlayerUtil.prevFakeCameraPos);
            MovementUtil.setPos(PlayerUtil.fakeCameraPos);
        }

        if (PlayerUtil.fakeRotation != null) {
            C.p().prevRotationYawHead = C.p().rotationYawHead = C.p().prevRotationYaw = C.p().rotationYaw = PlayerUtil.fakeRotation.yaw;
            C.p().prevRotationPitch = C.p().rotationPitch = PlayerUtil.fakeRotation.pitch;
        }
    }

    // shut up intellij, this shouldn't be inverted, because the name would be longer and its alr too long!
    public static boolean shouldInteractFromFakePos() {
        return Freecam.allowInteract && ModuleManager.isEnabled(Freecam.class) && C.isInGame() && !C.p().isDead;
    }

    public static boolean rightClick(boolean down) {
        if (down && MovementUtil.getOverriddenKeybinds().containsKey(C.mc.gameSettings.keyBindUseItem) && !MovementUtil.getOverriddenKeybinds().get(C.mc.gameSettings.keyBindUseItem))
            return false;

        KeyBindingBridge.from(C.mc.gameSettings.keyBindUseItem).bridge$setDown(down);

        if (down) MinecraftBridge.from(C.mc).bridge$rightClickMouse();
        else C.mc.playerController.onStoppedUsingItem(C.p());

        return true;
    }
}
