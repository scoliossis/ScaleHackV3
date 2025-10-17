package com.github.scoliossis.utils.minecraft;

import com.github.scoliossis.utils.client.C;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.util.List;

public class WorldUtil {
    public static Vec3 getEyes(Vec3 vec3) {
        return new Vec3(vec3.xCoord, vec3.yCoord + C.p().getEyeHeight(), vec3.zCoord);
    }

    public static boolean isOverAir() {
        return isOverAir(C.p().getPositionVector());
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

    // net.minecraft.client.renderer.EntityRenderer.getMouseOver
    public static Entity getMouseOver(RotationUtil.Rotation rotation, double reach, boolean ignoreWalls)
    {
        Entity entity = C.mc.getRenderViewEntity();

        float partialTicks = 1;

        if (entity != null)
        {
            if (C.mc.theWorld != null)
            {
                Entity pointedEntity = null;
                MovingObjectPosition objectMouseOver = ignoreWalls ? null : rayTrace(reach+2, rotation);
                double d1 = reach;
                Vec3 vec3 = entity.getPositionEyes(partialTicks);

                if (objectMouseOver != null)
                {
                    d1 = objectMouseOver.hitVec.distanceTo(vec3);
                }

                Vec3 vec31 = getVectorForRotation(rotation.pitch, rotation.yaw);
                Vec3 vec32 = vec3.addVector(vec31.xCoord * reach, vec31.yCoord * reach, vec31.zCoord * reach);
                Vec3 vec33 = null;
                float f = 1.0F;
                List<Entity> list = C.mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * reach, vec31.yCoord * reach, vec31.zCoord * reach).expand((double)f, (double)f, (double)f), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
                {
                    public boolean apply(Entity p_apply_1_)
                    {
                        return p_apply_1_.canBeCollidedWith();
                    }
                }));
                double d2 = d1;

                for (int j = 0; j < list.size(); ++j)
                {
                    Entity entity1 = (Entity)list.get(j);
                    float f1 = entity1.getCollisionBorderSize();
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
                    MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                    if (axisalignedbb.isVecInside(vec3))
                    {
                        if (d2 >= 0.0D)
                        {
                            pointedEntity = entity1;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                            d2 = 0.0D;
                        }
                    }
                    else if (movingobjectposition != null)
                    {
                        double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                        if (d3 < d2 || d2 == 0.0D)
                        {
                            if (entity1 == entity.ridingEntity && !entity.canRiderInteract())
                            {
                                if (d2 == 0.0D)
                                {
                                    pointedEntity = entity1;
                                    vec33 = movingobjectposition.hitVec;
                                }
                            }
                            else
                            {
                                pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }

                if (pointedEntity != null && vec3.distanceTo(vec33) > reach)
                {
                    pointedEntity = null;
                    objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, (EnumFacing)null, new BlockPos(vec33));
                }

                if (pointedEntity != null && (d2 < d1 || objectMouseOver == null))
                {
                    if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame)
                    {
                        return pointedEntity;
                    }
                }

            }
        }

        return null;
    }
}
