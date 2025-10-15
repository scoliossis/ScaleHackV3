package com.github.scoliossis.utils;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.RotationEvent;
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

    public static Rotation applyGcd(Rotation targetRotation) {
        return applyGcd(PlayerUtil.lastRotation(), targetRotation);
    }

    public static Rotation applyGcd(Rotation currentRotation, Rotation targetRotation) {
        double f = C.mc.gameSettings.mouseSensitivity * (double) 0.6F + (double) 0.2F;
        double sens = (f * f * f) * (double) 8.0F;

        // fix AimModulo360 flag on grim
        targetRotation.yaw = applyWrap360(currentRotation.yaw, targetRotation.yaw);

        Rotation difference = targetRotation.difference(currentRotation);

        float yawDelta = (float) MathUtil.toNearest(difference.yaw / 0.15d, sens) * 0.15F;
        float pitchDelta = (float) MathUtil.toNearest(difference.pitch / 0.15d, sens) * 0.15F;

        return new Rotation(
                MathHelper.clamp_float(currentRotation.pitch - pitchDelta, -90, 90),
                currentRotation.yaw - yawDelta
        );
    }

    public static float applyWrap360(float currentYaw, float targetYaw) {
        float delta = targetYaw - currentYaw;

        while (delta <= -180) delta += 360;
        while (delta > 180) delta -= 360;

        return currentYaw + delta;
    }

    public static Rotation getRotation(Vec3 to) {
        return getRotation(PlayerUtil.lastRotation(), C.p().getPositionEyes(1), to);
    }

    public static Rotation getRotation(Vec3 from, Vec3 to) {
        return getRotation(PlayerUtil.lastRotation(), from, to);
    }

    public static Rotation getRotation(Rotation currentRotation, Vec3 from, Vec3 to) {
        return applyGcd(currentRotation, getRotationNoGcd(currentRotation, from, to));
    }

    private static Rotation getRotationNoGcd(Rotation currentRotation, Vec3 from, Vec3 to) {
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
                currentRotation.pitch - neededPitchChange,
                currentRotation.yaw - neededYawChange
        );
    }

    public static Rotation getLimitedRotation(Rotation to, float maxTurnAmount) {
        Rotation from = PlayerUtil.lastRotation();
        Rotation rotationDifference = to.subtract(from);
        float pitchDelta = MathHelper.clamp_float(rotationDifference.pitch, -maxTurnAmount, maxTurnAmount);
        float yawDelta = MathHelper.clamp_float(rotationDifference.yaw, -maxTurnAmount, maxTurnAmount);

        return applyGcd(from, new Rotation(pitchDelta, yawDelta).add(from));
    }

    public static Rotation getSmoothRotation(Rotation to, float smoothing) {
        Rotation from = PlayerUtil.lastRotation();
        Rotation rotationDifference = to.subtract(from);
        float pitchDelta = rotationDifference.pitch / smoothing;
        float yawDelta = rotationDifference.yaw / smoothing;

        return applyGcd(from, new Rotation(pitchDelta, yawDelta).add(from));
    }

    @SubscribeEvent(priority = 9999)
    public static void onRotationTAIL(RotationEvent event) {
        applyGcd(PlayerUtil.lastRotation(), event.rotation);
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

        public Rotation subtract(Rotation other) {
            return new Rotation(pitch - other.pitch, yaw - other.yaw);
        }

        @Override
        public String toString() {
            return "Rotation(" + pitch + ", " + yaw + ")";
        }
    }
}
