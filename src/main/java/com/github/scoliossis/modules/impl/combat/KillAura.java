package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.MotionEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.events.impl.RotationEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.client.MathUtil;
import com.github.scoliossis.utils.minecraft.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;

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

    @RegisterSubModule(name = "Target Choice", parent = "Targeting")
    public static KillAuraTargeting killAuraTarget = KillAuraTargeting.Best;
    public enum KillAuraTargeting {
        Switch, Best, Single
    }

    @RegisterSubModule(name = "Target Sorting", parent = "Target Choice", modeParentString = {"Best", "Single"})
    public static KillAuraSorting killAuraSorting = KillAuraSorting.Health;
    public enum KillAuraSorting {
        Distance, Hurt_Time, Health
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

    private static EntityLivingBase lastTarget = null;
    private static int switchTargetIndex = 0;

    // goes before scaffold because its less important imo
    @SubscribeEvent(priority = 999)
    public static void onRotationEvent(RotationEvent event) {
        if (!shouldRotate()) return;

        EntityLivingBase target = getTarget();

        if (target == null || !shouldRotateToEntity(target)) return;

        event.rotation = getRotation(target);
    }

    @SubscribeEvent
    public static void tryAttackTarget(PlayerUpdateEvent event) {
        if (!shouldAttack()) return;

        if (rotations == KillAuraRotations.None) {
            EntityLivingBase target = getTarget();

            if (target != null && shouldAttackEntity(target)) attack(target);
            return;
        }

        Entity target = WorldUtil.getMouseOver(PlayerUtil.currentRotation(), killAuraAttackRange, throughWalls);

        if (!TargetUtil.isValidTarget(target)) return;

        attack(target);
    }

    private static boolean shouldAura() {
        return !swordOnlyAura || InventoryUtil.getHeldItem() instanceof ItemSword;
    }

    private static boolean shouldAttack() {
        return shouldAura() && MovementUtil.ticks >= nextAttackTick && PlayerUtil.canAttack();
    }

    private static EntityLivingBase getTarget() {
        List<EntityLivingBase> sortedTargets = TargetUtil.getPossibleTargets(
                        Math.max(killAuraRotationRange, killAuraAttackRange),
                        throughWalls,
                        rotations != KillAuraRotations.None
                )
                .stream()
                .sorted(Comparator.comparingDouble(TargetUtil::getDistanceToEntity))
                .sorted(Comparator.comparingDouble(entity -> {
                    if (killAuraTarget == KillAuraTargeting.Switch) return entity.getEntityId();

                    switch (killAuraSorting) {
                        case Distance:
                            return TargetUtil.getDistanceToEntity(entity);
                        case Health:
                            return entity.getHealth();
                        case Hurt_Time:
                            return entity.hurtTime;
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
                int prevSwitchIndex = switchTargetIndex;

                // dont switch onto a target out of range
                while (!shouldAttackEntity(sortedTargets.get(switchTargetIndex))) {
                    switchTargetIndex = (switchTargetIndex + 1) % sortedTargets.size();
                    if (switchTargetIndex == prevSwitchIndex) break;
                }

                targetIndex = switchTargetIndex;
                break;
        }

        lastTarget = sortedTargets.get(targetIndex);
        return lastTarget;
    }

    private static boolean shouldAttackEntity(EntityLivingBase EntityLivingBase) {
        return TargetUtil.getDistanceToEntity(EntityLivingBase) <= killAuraAttackRange;
    }

    private static boolean shouldRotate() {
        return shouldAura()
                && (rotations != KillAuraRotations.Snap || shouldAttack())
                && rotations != KillAuraRotations.None;
    }

    private static boolean shouldRotateToEntity(EntityLivingBase EntityLivingBase) {
        return TargetUtil.getDistanceToEntity(EntityLivingBase) <= killAuraRotationRange;
    }

    private static RotationUtil.Rotation getRotation(EntityLivingBase entity) {
        RotationUtil.Rotation targetRotation = TargetUtil.getTargetRotation(entity, killAuraRotationRange, throughWalls);

        // should only be null if rotationRange is lower than attack range
        if (targetRotation == null) return PlayerUtil.currentRotation();

        switch (rotations) {
            case Simple:
                return RotationUtil.getLimitedRotation(targetRotation, MathUtil.getRandomInRange(minRotation, maxRotation));
            case Smooth:
                return RotationUtil.getSmoothRotation(targetRotation, smoothRotationSpeed);
            default:
                return RotationUtil.applyGcd(targetRotation);
        }
    }

    private static void attack(Entity target) {
        if (PlayerUtil.attack(target)) {
            int nextCps = (int) MathUtil.getRandomInRange(killAuraAttackSpeedMin, killAuraAttackSpeedMax);

            nextAttackTick = MovementUtil.ticks + (20 / (Math.max(nextCps, 1)));
            switchTargetIndex += 1;
        }
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
