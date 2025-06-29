package com.github.scoliossis.utils;

import lombok.AllArgsConstructor;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class RotationUtil {
    public static Rotation getPreviousClientRotation() {
        return new Rotation(C.p().prevRotationPitch, C.p().prevRotationYaw);
    }
    public static Rotation getCurrentClientRotation() {
        return new Rotation(C.p().rotationPitch, C.p().rotationYaw);
    }

    public static Rotation applyGcd(Rotation currentRotation, Rotation targetRotation) {
        float f = C.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;

        Rotation difference = targetRotation.difference(currentRotation);

        difference.yaw = MathUtil.toNearest(difference.yaw, f1);
        difference.pitch = MathUtil.toNearest(difference.yaw, f1);

        return new Rotation(
                MathHelper.clamp_float(currentRotation.pitch - difference.pitch, -90, 90),
                currentRotation.yaw - difference.yaw
        );
    }

    public static Rotation getRotation(Rotation currentRotation, Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        double dist = Math.sqrt(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.yCoord, dist));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.zCoord, diff.xCoord)) - 90;

        float neededPitchChange = currentRotation.pitch - pitch;
        float neededYawChange = (currentRotation.yaw % 360) - yaw;

        neededYawChange = Math.abs(neededYawChange) > 180
                ? -1 *
                Math.signum(neededYawChange) *
                (360 - Math.abs(neededYawChange))
                : neededYawChange;

        return new Rotation(
                currentRotation.pitch + neededPitchChange,
                currentRotation.yaw + neededYawChange
        );
    }

    @AllArgsConstructor
    public static class Rotation {
        public float pitch;
        public float yaw;

        public Rotation difference(Rotation other) {
            return new Rotation(other.pitch - pitch, other.yaw - yaw);
        }

        public Rotation add(Rotation other) {
            return new Rotation(pitch + other.pitch, yaw + other.yaw);
        }
    }
}
