package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.utils.minecraft.InventoryUtil;
import com.github.scoliossis.utils.minecraft.PlayerUtil;
import com.github.scoliossis.utils.minecraft.TargetUtil;
import com.github.scoliossis.utils.minecraft.TimerUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;

import java.util.Comparator;
import java.util.List;

@RegisterModule(
        name = "Tick Base",
        description = "cs hake 2005",
        category = Category.COMBAT
)
// this is probably not what tickbase is, im probably dumb.
public class TickBase extends Module {
    @RegisterSubModule(name = "Through Walls")
    public static boolean throughWalls = false;
    @RegisterSubModule(name = "Swords Only")
    public static boolean swordsOnly = true;

    @RegisterSubModule(name = "Target Range", min = 1, max = 8)
    public static double targetRange = 4;
    @RegisterSubModule(name = "Max Ticks", min = 2, max = 20)
    public static int maxTicks = 7;

    private static float currentTimer = 1;
    private static int remainingBalance = 0;

    private static boolean balancing = false;

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        if (currentTimer != 1) {
            if (balancing) {
                remainingBalance--;
                if (remainingBalance <= 1) disable();

                return;
            }

            TimerUtil.popTimer(currentTimer);
            currentTimer = remainingBalance;
            TimerUtil.pushTimer(remainingBalance);
            balancing = true;

            return;
        }

        List<EntityLivingBase> targets = TargetUtil.getPossibleTargets(20, throughWalls, false);

        if (targets.isEmpty() || (swordsOnly && !(InventoryUtil.getHeldItem() instanceof ItemSword))) return;

        targets.sort(Comparator.comparingDouble(TargetUtil::getDistanceToEntity));
        EntityLivingBase closestTarget = targets.get(0);
        double distance = TargetUtil.getDistanceToEntity(closestTarget);
        int ticksUntilInRange = PlayerUtil.ticksUntilInRange(closestTarget, targetRange);

        if (ticksUntilInRange > 0 && distance > targetRange && ticksUntilInRange <= maxTicks) {
            remainingBalance = ticksUntilInRange;
            currentTimer = 1f/ticksUntilInRange;
            TimerUtil.pushTimer(currentTimer);
        }
    }

    @Override
    protected void onEnable() {

    }

    private static void disable() {
        if (currentTimer != 1) {
            TimerUtil.popTimer(currentTimer);
            currentTimer = 1;
            remainingBalance = 0;
            balancing = false;
        }
    }

    @Override
    protected void onDisable() {
        disable();
    }
}
