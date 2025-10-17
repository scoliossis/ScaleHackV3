package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.minecraft.BlinkUtil;
import com.github.scoliossis.utils.minecraft.MovementUtil;
import com.github.scoliossis.utils.minecraft.PlayerUtil;
import com.github.scoliossis.utils.minecraft.TargetUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

import java.util.List;

// shoutout to litdab. for the idea to make autoblock a separate module
// a really old mushroom build ~2020 did the same, but badly, its a cool idea i think
@RegisterModule(
        name = "AutoBlock",
        description = "im enderflame and you are BLOCKED",
        category = Category.COMBAT,
        dangerous = true
)
// todo add blink for blink mode
public class AutoBlock extends Module {
    @RegisterSubModule(name = "Range", max = 6)
    public static double autoblockRange = 3;

    @RegisterSubModule(name = "Through Walls")
    public static boolean throughWalls = true;

    @RegisterSubModule(name = "Autoblock Mode", description = "bypahh")
    public static AutoBlockMode autoblockMode = AutoBlockMode.Blink;
    @RegisterSubModule(name = "Blink Mode", parent = "Autoblock Mode", modeParentString = "Blink")
    public static Blink_Mode blinkMode = Blink_Mode.Hypixel;

    @AllArgsConstructor
    public enum Blink_Mode {
        Hypixel(2),
        Reduce(3),
        Legit(4);

        public final int blinkTicks;
    }

    @Getter private static boolean isBlocking, isServerBlocking = false;

    public static boolean isBlockingSwing() {
        return C.p().isUsingItem() || PlayerUtil.getLastUnblock() == MovementUtil.ticks;
    }

    private static ItemStack itemInUse = null;

    private static int blinkTick = 0;
    private static boolean isBlinking = false;

    private static AutoBlockMode lastAutoblockMode;

    // ticks before killaura
    @SubscribeEvent(priority = 999)
    public static void onPlayerUpdateEvent(PlayerUpdateEvent event) {
        if (C.p().getHeldItem() != itemInUse || autoblockMode != lastAutoblockMode) {
            stopBlocking();
        }

        lastAutoblockMode = autoblockMode;

        List<EntityLivingBase> targets = TargetUtil.getPossibleTargets(autoblockRange, throughWalls, true);

        if (targets.isEmpty()) {
            stopBlocking();
            return;
        }

        if (C.p().getHeldItem() != null && C.p().getHeldItem().getItemUseAction() == EnumAction.BLOCK) {
            tickBlocking();
        }
    }

    public static void stopBlocking() {
        if (isBlinking) {
            blinkTick = 0;
            isBlinking = false;
            BlinkUtil.popBlink(true, false);
        }

        setBlocking(false, false);
    }


    public static void tickBlocking() {
        if (autoblockMode == AutoBlockMode.Blink) {
            switch (blinkTick) {
                case 0:
                    if (!setBlocking(true, true)) return;

                    BlinkUtil.popBlink(true, false);
                    isBlinking = false;
                    break;
                case 1:
                    isBlinking = true;
                    BlinkUtil.pushBlink(true, false);
                    setBlocking(true, false);
                    break;
            }

            blinkTick = blinkTick == blinkMode.blinkTicks ? 0 : blinkTick+1;
        }
        else {
            setBlocking(true, true);
        }
    }

    private static boolean setBlocking(boolean clientSide, boolean serverSide) {
        if (serverSide != isServerBlocking) {
            boolean blockSuccess = PlayerUtil.rightClick(serverSide);

            if (!blockSuccess) return false;
            isServerBlocking = serverSide;
        }

        isBlocking = clientSide;
        itemInUse = C.p().getHeldItem();

        return true;
    }

    public static boolean canSwingWhileBlocking() {
        return autoblockMode == AutoBlockMode.Vanilla && ModuleManager.isEnabled(AutoBlock.class) && isServerBlocking;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        stopBlocking();
    }

    @Override
    public String arrayListExtraInfo() {
        return autoblockMode.name();
    }

    public enum AutoBlockMode {
        Vanilla, Blink, Fake
    }
}
