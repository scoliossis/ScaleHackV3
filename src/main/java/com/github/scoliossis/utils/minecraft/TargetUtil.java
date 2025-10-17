package com.github.scoliossis.utils.minecraft;

import com.github.scoliossis.bridge.net.minecraft.client.entity.AbstractClientPlayerBridge;
import com.github.scoliossis.modules.impl.client.Targets;
import com.github.scoliossis.utils.client.C;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TargetUtil {
    public static boolean isValidTarget(Entity entity) {
        return entity != C.p() && entity instanceof EntityPlayer && !entity.isDead && !isBot(entity) && !isTeam(entity);
    }

    public static boolean isBot(Entity entity) {
        if (!Targets.antiBot || !(entity instanceof EntityPlayer)) return false;
        if (!(entity instanceof AbstractClientPlayer)) return true;

        NetworkPlayerInfo playerInfo = AbstractClientPlayerBridge.from(entity).bridge$getPlayerInfo();
        if (playerInfo == null) return true;

        return playerInfo.getResponseTime() <= Targets.botPingMode.ping;
    }

    public static boolean isTeam(Entity entity) {
        if (!Targets.teamsCheck || !(entity instanceof EntityLivingBase)) return false;
        EntityLivingBase entityLivingBase = (EntityLivingBase) entity;

        return Targets.clientCheck && C.p().isOnSameTeam(entityLivingBase)
                || (Targets.colourTeams && getTeamColour(C.p()) == getTeamColour(entityLivingBase));
    }

    // stolen from net.minecraft.client.renderer.entity.RendererLivingEntity.setScoreTeamColor
    private static int getTeamColour(EntityLivingBase entityLivingBaseIn) {
        ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam)entityLivingBaseIn.getTeam();

        if (scoreplayerteam != null)
        {
            String s = FontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());

            if (s.length() >= 2)
            {
                return C.mc.fontRendererObj.getColorCode(s.charAt(1));
            }
        }

        return -1;
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
