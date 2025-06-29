package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.modules.*;
import com.github.scoliossis.modules.SubModules.SubCategory;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.PlayerUtil;
import net.minecraft.entity.EntityLivingBase;

@RegisterModule(
        name = "Show Rotations",
        description = "Displays current server rotations client-side in f5",
        category = Category.CLIENT,
        enabledByDefault = true
)
public class ShowRotations extends Module {
    @RegisterSubModule(name = "Server Rotations")
    public static SubCategory serverRotations = new SubCategory();

    @RegisterSubModule(name = "Head Rotation", parent = "Server Rotations")
    public static boolean headRotations = true;
    @RegisterSubModule(name = "Body Rotation", parent = "Server Rotations")
    public static boolean bodyRotations = false;
    @RegisterSubModule(name = "Pitch Rotation", parent = "Server Rotations")
    public static boolean pitchRotations = false;

    @RegisterSubModule(name = "Other", description = "Other rotation settings")
    public static SubCategory other = new SubCategory();

    @RegisterSubModule(name = "No Smooth", parent = "Other", description = "Removes the rotation smoothing done by minecraft on yourself in f5")
    public static boolean selfNoSmooth = false;
    @RegisterSubModule(name = "No Smooth Others", parent = "Other", description = "Removes the rotation smoothing done by minecraft for other players")
    public static boolean otherNoSmooth = false;

    public static float getRotation(EntityLivingBase instance, RotationPart rotationPart, boolean current) {
        current |= ((instance == C.p() && selfNoSmooth) || (instance != C.p() && otherNoSmooth));

        if (instance == C.p() && PlayerUtil.prevPlayerUpdateEvent != null && ModuleManager.isEnabled(ShowRotations.class)) {
            if (rotationPart == RotationPart.PITCH && pitchRotations && PlayerUtil.playerUpdateEvent.rotation.pitch != PlayerUtil.currentTickClientRotation.pitch)
                return current ? PlayerUtil.playerUpdateEvent.rotation.pitch : PlayerUtil.prevPlayerUpdateEvent.rotation.pitch;
            else if (PlayerUtil.playerUpdateEvent.rotation.yaw != PlayerUtil.currentTickClientRotation.yaw && (rotationPart == RotationPart.HEAD_YAW && headRotations) || (rotationPart == RotationPart.BODY_YAW && bodyRotations))
                    return current ? PlayerUtil.playerUpdateEvent.rotation.yaw : PlayerUtil.prevPlayerUpdateEvent.rotation.yaw;
        }

        float rotationPitch = PlayerUtil.realRotation != null ? PlayerUtil.realRotation.pitch : current  ? instance.rotationPitch : instance.prevRotationPitch;
        float rotationYaw = PlayerUtil.realRotation != null ? PlayerUtil.realRotation.yaw : current ? instance.rotationYaw : instance.prevRotationYaw;
        float rotationYawHead = PlayerUtil.realRotation != null ? PlayerUtil.realRotation.yaw : current ? instance.rotationYawHead : instance.prevRotationYawHead;

        switch (rotationPart) {
            case PITCH: return rotationPitch;
            case HEAD_YAW: return rotationYaw;
            default: return rotationYawHead;
        }
    }

    public enum RotationPart {
        HEAD_YAW, BODY_YAW, PITCH
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
