package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.events.impl.RotationEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RegisterModule(
        name = "Kill Aura",
        description = "Now I Am Become Death, the Destroyer of Worlds.",
        category = Category.COMBAT
)
public class KillAura extends Module {
    // filler settings from my other client
    @RegisterSubModule(name = "Range")
    public SubCategory rangeSubcategory = new SubCategory();

    @RegisterSubModule(name = "Rotation Range", parent = "Range", max = 8)
    public static double killAuraRotationRange = 5;

    @RegisterSubModule(name = "Attack Range", parent = "Range", min = 1, max = 6)
    public static double killAuraAttackRange = 3.1;


    @RegisterSubModule(name = "Targeting")
    public SubCategory killAuraTargetingSubCategory = new SubCategory();

    @RegisterSubModule(name = "Through Walls", parent = "Targeting")
    public static boolean throughWalls = false;

    @RegisterSubModule(name = "Target Sorting", parent = "Targeting")
    public static KillAuraSorting killAuraSorting = KillAuraSorting.Health;
    public enum KillAuraSorting {
        Distance, Hurt_Time, Health
    }

    // maybe add multi idk
    @RegisterSubModule(name = "Target Choice", parent = "Targeting")
    public static KillAuraTargeting killAuraTarget = KillAuraTargeting.Best;
    public enum KillAuraTargeting {
        Switch, Best, Single
    }

    @RegisterSubModule(name = "Rotation")
    public SubCategory rotationsSubcategory = new SubCategory();

    @RegisterSubModule(name = "Rotation Mode", parent = "Rotation")
    public static KillAuraRotations rotations = KillAuraRotations.Smooth;
    public enum KillAuraRotations {
        Simple, Smooth, Snap, None
    }

    @RegisterSubModule(name = "Min Rotation", parent = "Rotation Mode", modeParentString = "Simple", min = 0, max = 180)
    public static float minRotation = 0;

    @RegisterSubModule(name = "Max Rotation", parent = "Rotation Mode", modeParentString = "Simple", min = 1, max = 180)
    public static float maxRotation = 100;

    @RegisterSubModule(name = "Rotation Smoothing", parent = "Rotation Mode", modeParentString = "Smooth", min = 1f, max = 5)
    public static float smoothRotationSpeed = 1;


    @RegisterSubModule(name = "Attacking")
    public SubCategory attackingSubCat = new SubCategory();

    @RegisterSubModule(name = "Swords Only", parent = "Attacking")
    public static boolean swordOnlyAura = true;

    @RegisterSubModule(name = "Attack Speed Min", min = 0, max = 20, parent = "Attacking")
    public static double killAuraAttackSpeedMin = 5;
    @RegisterSubModule(name = "Attack Speed Max", min = 0, max = 20, parent = "Attacking")
    public static double killAuraAttackSpeedMax = 10;

    private static int nextAttackTick = -1;

    private static EntityPlayer lastTarget = null;
    private static int switchTargetIndex = -1;

    @SubscribeEvent
    public static void onRotationEvent(RotationEvent event) {
        if (!shouldRotate()) return;

        EntityPlayer target = getTarget();

        if (target == null || !shouldRotateToEntity(target)) return;

        event.rotation = getRotation(target);
    }

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!shouldAttack()) return;

        if (rotations == KillAuraRotations.None) {
            EntityPlayer target = getTarget();

            if (target != null) attack(target);
            return;
        }

        EntityPlayer target = getEntityOver(PlayerUtil.currentRotation(), killAuraAttackRange);

        if (target == null) return;

        attack(target);
    }

    private static boolean shouldAura() {
        return !swordOnlyAura || InventoryUtil.getHeldItem() instanceof ItemSword;
    }

    private static boolean shouldAttack() {
        return shouldAura() && MovementUtil.ticks >= nextAttackTick;
    }

    private static EntityPlayer getTarget() {
        List<EntityPlayer> sortedTargets = C.w().playerEntities.stream().filter(
                entityPlayer -> canEntityBeHit(entityPlayer, Math.max(killAuraRotationRange, killAuraAttackRange))
        ).sorted(Comparator.comparingDouble(entityPlayer -> {
            switch (killAuraSorting) {
                case Distance:
                    return getDistanceToPlayer(entityPlayer);
                case Health:
                    return entityPlayer.getHealth();
                case Hurt_Time:
                    return entityPlayer.hurtTime;
                default:
                    return 0;
            }
        })).collect(Collectors.toList());

        if (sortedTargets.isEmpty()) {
            lastTarget = null;
            return null;
        }

        int targetIndex = 0;
        switch (killAuraTarget) {
            case Single:
                targetIndex =
                        (lastTarget != null && sortedTargets.contains(lastTarget) && shouldAttackEntity(lastTarget))
                        ? sortedTargets.indexOf(lastTarget)
                        : 0;
                break;
            case Switch:
                switchTargetIndex = switchTargetIndex % sortedTargets.size();

                // dont switch onto a target out of range
                while (!shouldAttackEntity(sortedTargets.get(switchTargetIndex))) {
                    switchTargetIndex += 1;
                    if (switchTargetIndex >= sortedTargets.size()) {
                        switchTargetIndex = 0;
                        break;
                    }
                }

                targetIndex = switchTargetIndex;
                break;
        }

        lastTarget = sortedTargets.get(targetIndex);
        return lastTarget;
    }

    private static boolean shouldAttackEntity(EntityPlayer entityPlayer) {
        return getDistanceToPlayer(entityPlayer) <= killAuraAttackRange;
    }

    private static boolean shouldRotate() {
        return shouldAura()
                && (rotations != KillAuraRotations.Snap || shouldAttack())
                && rotations != KillAuraRotations.None;
    }

    private static boolean shouldRotateToEntity(EntityPlayer entityPlayer) {
        return getDistanceToPlayer(entityPlayer) <= killAuraRotationRange;
    }

    private static RotationUtil.Rotation getRotation(EntityPlayer entityPlayer) {
        RotationUtil.Rotation targetRotation = getTargetRotation(entityPlayer, killAuraRotationRange);

        // should only be null if rotationRange is lower than attack range
        if (targetRotation == null) {
            return PlayerUtil.currentRotation();
        }

        switch (rotations) {
            case Simple:
                return RotationUtil.getLimitedRotation(targetRotation, MathUtil.getRandomInRange(minRotation, maxRotation));
            case Smooth:
                return RotationUtil.getSmoothRotation(targetRotation, smoothRotationSpeed);
            default:
                return targetRotation;
        }
    }

    // yeah, can cause issues if player is behind a block, idc, just turn on through walls.
    private static RotationUtil.Rotation getTargetRotation(EntityPlayer entityPlayer, double range) {
        // great variable naming
        AxisAlignedBB rotationTarget = entityPlayer.getEntityBoundingBox();
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

        RotationUtil.Rotation bestRotation = RotationUtil.getRotation(new Vec3(posX, posY, posZ));

        // if best rotation works, lock it in.
        if (getEntityOver(bestRotation, range) == entityPlayer) return bestRotation;

        ArrayList<Vec3> possibleRotations = new ArrayList<>();
        // added in binary order (000, 100, 010, 110, 001, 101, 011, 111)
        for (int i = 0; i < 8; i++) {
            possibleRotations.add(new Vec3(
                    i % 2 == 0 ? rotationTarget.minX : rotationTarget.maxX,
                    i % 4 >= 2 ? rotationTarget.minY : rotationTarget.maxY,
                    i % 8 >= 4 ? rotationTarget.minZ : rotationTarget.maxZ
            ));
        }

        // maybe the middle is meta
        possibleRotations.add(new Vec3(entityPlayer.posX, (rotationTarget.maxY + rotationTarget.minY) / 2, entityPlayer.posZ));

        // try other rotations, surely one will work
        for (Vec3 possibleRotationVector : possibleRotations) {
            RotationUtil.Rotation possibleRotation = RotationUtil.getRotation(possibleRotationVector);
            if (getEntityOver(possibleRotation, range) == entityPlayer) {
                return possibleRotation;
            }
        }

        // after all that, we still fail.
        return null;
    }

    // silly code blehhh
    private static boolean canEntityBeHit(EntityPlayer entityPlayer, double range) {
        return entityPlayer != C.p()
                && getDistanceToPlayer(entityPlayer) <= range
                // if the rotation code wont lock onto the entity, dont bother targeting them
                && (rotations == KillAuraRotations.None || getTargetRotation(entityPlayer, range) != null);
    }

    private static double getDistanceToPlayer(EntityPlayer entityPlayer) {
        return Math.min(
                C.p().getPositionEyes(1).distanceTo(entityPlayer.getPositionVector()),
                C.p().getPositionEyes(1).distanceTo(entityPlayer.getPositionVector().add(new Vec3(0, entityPlayer.height, 0)))
        );
    }

    private static EntityPlayer getEntityOver(RotationUtil.Rotation rotation, double range) {
        Entity raytrace = WorldUtil.getMouseOver(range, rotation, throughWalls);

        return !(raytrace instanceof EntityPlayer) ? null : (EntityPlayer) raytrace;
    }

    private static void attack(Entity target) {
        C.p().swingItem();
        C.mc.playerController.attackEntity(C.p(), target);

        int nextCps = (int) MathUtil.getRandomInRange(killAuraAttackSpeedMin, killAuraAttackSpeedMax);

        nextAttackTick = MovementUtil.ticks + (20 / (Math.max(nextCps, 1)));
        switchTargetIndex += 1;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
