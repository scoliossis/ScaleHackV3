package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.minecraft.BlinkUtil;
import com.github.scoliossis.utils.minecraft.InventoryUtil;
import com.github.scoliossis.utils.minecraft.TargetUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.util.Vec3;

import java.util.Comparator;
import java.util.List;

@RegisterModule(
        name = "Lag Range",
        description = "magical tp hack wow",
        category = Category.COMBAT
)
public class LagRange extends Module {
    @RegisterSubModule(name = "Through Walls")
    public static boolean throughWalls = false;

    @RegisterSubModule(name = "Moving Only", description = "Only blink if moving towards closest entity")
    public static boolean onlyMoving = false;

    @RegisterSubModule(name = "Swords Only")
    public static boolean swordsOnly = true;

    @RegisterSubModule(name = "Max Blink Ticks", min = 1, max = 20)
    public static int maxBlinkTicks = 10;

    @RegisterSubModule(name = "Start Blink Range", min = 2, max = 8)
    public static double startBlinkRange = 5;

    @RegisterSubModule(name = "End Blink Range", min = 1, max = 8)
    public static double endBlinkRange = 3;

    private static int blinkTicks = -1;

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        List<EntityLivingBase> targets = TargetUtil.getPossibleTargets(startBlinkRange, throughWalls, true);

        if (targets.isEmpty() || (swordsOnly && !(InventoryUtil.getHeldItem() instanceof ItemSword))) {
            if (blinkTicks != -1) {
                BlinkUtil.popBlink(true, false);
                blinkTicks = -1;
            }
            return;
        }

        targets.sort(Comparator.comparingDouble(TargetUtil::getDistanceToEntity));

        EntityLivingBase closestTarget = targets.get(0);
        double distance = TargetUtil.getDistanceToEntity(closestTarget);
        Vec3 playerPos = C.p().getPositionVector();
        Vec3 nextPlayerPos = playerPos.add(new Vec3(C.p().motionX, 0, C.p().motionZ));

        Vec3 targetPos = closestTarget.getPositionVector();
        Vec3 nextTargetPos = targetPos.add(new Vec3(targetPos.xCoord - closestTarget.lastTickPosX, 0, targetPos.zCoord - closestTarget.lastTickPosZ));

        boolean movingTowards = playerPos.distanceTo(targetPos) > nextPlayerPos.distanceTo(nextTargetPos) || !onlyMoving;

        if (blinkTicks != -1) {
            if (blinkTicks >= maxBlinkTicks || distance <= endBlinkRange || !movingTowards) {
                BlinkUtil.popBlink(true, false);
                blinkTicks = -1;
                return;
            }

            blinkTicks++;
        }
        else if (distance > endBlinkRange && movingTowards && canBlink()) {
            blinkTicks = 0;
            BlinkUtil.pushBlink(true, false);
        }
    }

    private static boolean canBlink() {
        return !AutoBlock.isBlocking() || AutoBlock.autoblockMode != AutoBlock.AutoBlockMode.Blink;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        if (blinkTicks != -1) {
            BlinkUtil.popBlink(true, false);
            blinkTicks = -1;
        }
    }
}
