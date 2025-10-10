package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.bridge.net.minecraft.client.MinecraftBridge;
import com.github.scoliossis.bridge.net.minecraft.client.settings.KeyBindingBridge;
import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.MovementUtil;
import com.github.scoliossis.utils.TargetUtil;
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
    @RegisterSubModule(name = "Autoblock Mode", description = "bypahh")
    public static AutoBlockMode autoblockMode = AutoBlockMode.Blink;

    @RegisterSubModule(name = "Range", max = 6)
    public static double autoblockRange = 3;

    @RegisterSubModule(name = "Through Walls")
    public static boolean throughWalls = true;

    @Getter private static boolean isBlocking, isServerBlocking = false;

    public static boolean isBlockingSwing() {
        return C.p().isUsingItem() || lastUnblock == MovementUtil.ticks;
    }

    private static ItemStack itemInUse = null;

    private static int lastUnblock = -1;
    private static int blinkTick = 0;

    // ticks before killaura
    @SubscribeEvent(priority = 999)
    public static void onPlayerUpdateEvent(PlayerUpdateEvent event) {
        if (C.p().getHeldItem() != itemInUse) {
            setBlocking(false, false);
        }

        List<EntityLivingBase> targets = TargetUtil.getPossibleTargets(autoblockRange, throughWalls, true);

        if (targets.isEmpty()) {
            setBlocking(false, false);
            return;
        }

        if (C.p().getHeldItem() != null && C.p().getHeldItem().getItemUseAction() == EnumAction.BLOCK) {
            tickBlocking();
        }
    }

    public static void tickBlocking() {
        if (autoblockMode == AutoBlockMode.Blink) {
            switch (blinkTick) {
                case 0:
                    setBlocking(true, true);
                    // release blink
                    // start blink
                    break;
                case 1:
                    setBlocking(true, false);
                    break;

                case 2:
                    blinkTick = -1;
                    break;
            }

            blinkTick++;
        }
        else {
            setBlocking(true, true);
        }
    }

    private static void setBlocking(boolean clientSide, boolean serverSide) {
        if (serverSide != isServerBlocking) {
            isServerBlocking = serverSide;

            if (serverSide) {
                KeyBindingBridge.from(C.mc.gameSettings.keyBindUseItem).bridge$setPressed(true);
                MinecraftBridge.from(C.mc).bridge$rightClickMouse();
            }
            else {
                KeyBindingBridge.from(C.mc.gameSettings.keyBindUseItem).bridge$setPressed(false);
                C.mc.playerController.onStoppedUsingItem(C.p());

                lastUnblock = MovementUtil.ticks;
            }
        }

        isBlocking = clientSide;
        itemInUse = C.p().getHeldItem();
    }

    public static boolean canSwingWhileBlocking() {
        return autoblockMode == AutoBlockMode.Vanilla;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        setBlocking(false, false);
    }

    @Override
    public String arrayListExtraInfo() {
        return autoblockMode.name();
    }

    public enum AutoBlockMode {
        Vanilla, Blink, Fake
    }
}
