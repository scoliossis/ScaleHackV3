package com.github.scoliossis.utils;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.events.impl.MotionEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.modules.impl.render.Freecam;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class PlayerUtil {
    private static PlayerUpdateEvent prevPlayerUpdateEvent;
    public static PlayerUpdateEvent playerUpdateEvent;

    public static PlayerUpdateEvent getPrevPlayerUpdateEvent() {
        return prevPlayerUpdateEvent != null ? prevPlayerUpdateEvent : playerUpdateEvent;
    }

    public static MotionEvent motionEvent;

    public static RotationUtil.Rotation currentTickClientRotation;

    public static boolean noClip = false;
    public static boolean noClipRender = false;

    public static Vec3 prevFakeCameraPos, fakeCameraPos, realPos;
    public static RotationUtil.Rotation fakeRotation, realRotation;

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


    public static void setLastPlayerUpdateEvent(PlayerUpdateEvent event) {
        if (playerUpdateEvent != null)
            prevPlayerUpdateEvent = new PlayerUpdateEvent(new RotationUtil.Rotation(playerUpdateEvent.rotation.pitch, playerUpdateEvent.rotation.yaw));

        playerUpdateEvent = event;
        currentTickClientRotation = event.rotation;
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
}
