package com.github.scoliossis.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TargetUtil {
    public static boolean isValidTarget(Entity entity) {
        return entity != C.p() && entity instanceof EntityPlayer && !entity.isDead && !isBot(entity);
    }

    // todo: fix
    public static boolean isBot(Entity entity) {
        if (true) return false;

        if (!(entity instanceof EntityPlayer)) return false;
        if (!(entity instanceof EntityPlayerMP)) return true;

        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) entity;

        return entityPlayerMP.ping <= 0;
    }

    public static List<EntityLivingBase> getAllValidTargets() {
        return C.w().getEntities(EntityLivingBase.class, TargetUtil::isValidTarget);
    }

    public static List<EntityLivingBase> getPossibleTargets(double reach, boolean throughWalls, boolean rotate) {
        return C.w().getEntities(EntityLivingBase.class, entity -> canEntityBeHit(entity, reach, throughWalls, rotate));
    }

    public static boolean canEntityBeHit(EntityLivingBase entity, double range, boolean throughWalls, boolean rotate) {
        return isValidTarget(entity)
                && getDistanceToEntity(entity) <= range
                // if the rotation code wont lock onto the entity, dont bother targeting them
                && (!rotate || getTargetRotation(entity, range, throughWalls) != null);
    }

    public static double getDistanceToEntity(EntityLivingBase entity) {
        return C.p().getPositionEyes(1).distanceTo(getClosestPointToEntity(entity));
    }

    public static Vec3 getClosestPointToEntity(EntityLivingBase target) {
        AxisAlignedBB rotationTarget = target.getEntityBoundingBox();
        Vec3 eyePos = C.p().getPositionEyes(1);

        double posX = eyePos.xCoord;
        double posY = eyePos.yCoord;
        double posZ = eyePos.zCoord;

        if (eyePos.xCoord < rotationTarget.minX) posX = rotationTarget.minX;
        else if (eyePos.xCoord > rotationTarget.maxX) posX = rotationTarget.maxX;

        if (eyePos.yCoord < rotationTarget.minY) posY = rotationTarget.minY;
        else if (eyePos.yCoord > rotationTarget.maxY) posY = rotationTarget.maxY;

        if (eyePos.zCoord < rotationTarget.minZ) posZ = rotationTarget.minZ;
        else if (eyePos.zCoord > rotationTarget.maxZ) posZ = rotationTarget.maxZ;

        return new Vec3(posX, posY, posZ);
    }

    public static RotationUtil.Rotation getTargetRotation(EntityLivingBase entity, double range, boolean throughWalls) {
        AxisAlignedBB targetBoundingBox = entity.getEntityBoundingBox();

        RotationUtil.Rotation bestRotation = RotationUtil.getRotation(TargetUtil.getClosestPointToEntity(entity));

        // if best rotation works, lock it in.
        if (WorldUtil.getMouseOver(bestRotation, range, throughWalls) == entity) {
            return bestRotation;
        }

        ArrayList<Vec3> possibleRotations = new ArrayList<>();
        // added in binary order (000, 100, 010, 110, 001, 101, 011, 111)
        for (int i = 0; i < 8; i++) {
            possibleRotations.add(new Vec3(
                    i % 2 == 0 ? targetBoundingBox.minX : targetBoundingBox.maxX,
                    i % 4 >= 2 ? targetBoundingBox.minY : targetBoundingBox.maxY,
                    i % 8 >= 4 ? targetBoundingBox.minZ : targetBoundingBox.maxZ
            ));
        }

        // maybe the middle is meta
        possibleRotations.add(new Vec3(entity.posX, (targetBoundingBox.maxY + targetBoundingBox.minY) / 2, entity.posZ));
        possibleRotations.add(new Vec3(entity.posX, targetBoundingBox.minY, entity.posZ));
        possibleRotations.add(new Vec3(entity.posX, targetBoundingBox.maxY, entity.posZ));

        // try other rotations, surely one will work
        for (Vec3 possibleRotationVector : possibleRotations) {
            RotationUtil.Rotation possibleRotation = RotationUtil.getRotation(possibleRotationVector);
            if (WorldUtil.getMouseOver(possibleRotation, range, throughWalls) == entity) {
                return possibleRotation;
            }
        }

        // after all that, we still fail.
        return null;
    }
}
