package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.PlayerUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

@RegisterModule(
        name = "Show Rotations",
        description = "Displays current server rotations client-side in f5",
        category = Category.CLIENT,
        enabledByDefault = true
)
public class ShowRotations extends Module {
    @RegisterSubModule(name = "Head Rotation")
    public static boolean headRotations = true;
    @RegisterSubModule(name = "Body Rotation")
    public static boolean bodyRotations = false;
    @RegisterSubModule(name = "Pitch Rotation")
    public static boolean pitchRotations = true;

    // should always go last.
    @SubscribeEvent(priority = 99999)
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        updateDistance();
    }

    // eriojoghbe8i4g9u80w09ijgb
    public static float getRotation(EntityLivingBase instance, float value, RotationPart rotationPart, boolean current) {
        if (instance != C.p() || !ModuleManager.isEnabled(ShowRotations.class)) return value;

        switch (rotationPart) {
            case PITCH:
                return pitchRotations
                        ? (current ? PlayerUtil.currentRotation().pitch : PlayerUtil.lastRotation().pitch)
                        : value;
            case HEAD_YAW:
                return headRotations
                        ? (current ? PlayerUtil.currentRotation().yaw : PlayerUtil.lastRotation().yaw)
                        : value;
            default:
                return bodyRotations
                        ? (current ? PlayerUtil.serverRenderYawOffset : PlayerUtil.prevServerRenderYawOffset)
                        : value;
        }
    }

    // net.minecraft.entity.EntityLivingBase.updateDistance
    public static void updateDistance() {
        PlayerUtil.prevServerRenderYawOffset = PlayerUtil.serverRenderYawOffset;

        float rotationYaw = getRotation(C.p(), C.p().rotationYaw, RotationPart.HEAD_YAW, true);

        double d0 = C.p().posX - C.p().prevPosX;
        double d1 = C.p().posZ - C.p().prevPosZ;
        float f = (float)(d0 * d0 + d1 * d1);
        float f1 = PlayerUtil.serverRenderYawOffset;

        if (f > 0.0025000002F) {
            f1 = (float)MathHelper.atan2(d1, d0) * 180.0F / (float)Math.PI - 90.0F;
        }

        if (C.p().swingProgress > 0.0F) {
            f1 = rotationYaw;
        }

        float f3 = MathHelper.wrapAngleTo180_float(f1 - PlayerUtil.serverRenderYawOffset);
        PlayerUtil.serverRenderYawOffset += f3 * 0.3F;
        float f4 = MathHelper.wrapAngleTo180_float(rotationYaw - PlayerUtil.serverRenderYawOffset);

        if (f4 < -75.0F)
        {
            f4 = -75.0F;
        }

        if (f4 >= 75.0F)
        {
            f4 = 75.0F;
        }

        PlayerUtil.serverRenderYawOffset = rotationYaw - f4;

        if (f4 * f4 > 2500.0F)
        {
            PlayerUtil.serverRenderYawOffset += f4 * 0.2F;
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
