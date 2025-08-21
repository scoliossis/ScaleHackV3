package com.github.scoliossis.utils;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class WorldUtil {
    public static Vec3 getEyes(Vec3 vec3) {
        return new Vec3(vec3.xCoord, vec3.yCoord + C.p().getEyeHeight(), vec3.zCoord);
    }

    public static boolean isOverAir() {
        return C.w().getBlockState(new BlockPos(C.p().posX, C.p().posY - 1, C.p().posZ)).getBlock().equals(Blocks.air);
    }

    public static boolean isOverAir(Vec3 vec3) {
        return C.w().getBlockState(new BlockPos(vec3.subtract(0,1,0))).getBlock().equals(Blocks.air);
    }

    public static MovingObjectPosition rayTrace(double blockReachDistance, RotationUtil.Rotation rotation) {
        return rayTrace(blockReachDistance, 1, rotation, rotation);
    }

    public static MovingObjectPosition rayTrace(double blockReachDistance, Vec3 playerPos, RotationUtil.Rotation rotation) {
        return rayTrace(blockReachDistance, 1, playerPos.add(new Vec3(0,C.p().getEyeHeight(),0)), rotation, rotation);
    }

    public static MovingObjectPosition rayTrace(double blockReachDistance, float partialTicks, RotationUtil.Rotation rotationPrev, RotationUtil.Rotation rotationCurrent) {
        return rayTrace(blockReachDistance, partialTicks, C.p().getPositionEyes(partialTicks), rotationPrev, rotationCurrent);
    }

    // from net.minecraft.entity.Entity
    public static MovingObjectPosition rayTrace(double blockReachDistance, float partialTicks, Vec3 position, RotationUtil.Rotation rotationPrev, RotationUtil.Rotation rotationCurrent) {
        float f = rotationPrev.pitch + (rotationCurrent.pitch - rotationPrev.pitch) * partialTicks;
        float f1 = rotationPrev.yaw + (rotationCurrent.yaw - rotationPrev.yaw) * partialTicks;
        Vec3 vec31 = getVectorForRotation(f, f1);

        Vec3 vec32 = position.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);

        return C.p().worldObj.rayTraceBlocks(position, vec32, false, false, true);
    }

    // from net.minecraft.entity.Entity
    public static Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }
}
