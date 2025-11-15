package com.github.scoliossis.modules.impl.render;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PacketEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.minecraft.TargetUtil;
import com.github.scoliossis.utils.render.EasingUtil;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import com.github.scoliossis.utils.render.draggable.Draggable;
import lombok.AllArgsConstructor;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;

import java.awt.*;

// taking 1 hour to animate a box entering and leaving the screen whenever a target is chosen or chat is opened isn't my finest work
@RegisterModule(
        name = "Target HUD",
        description = "why tf did i decide every module should have a quirky description",
        category = Category.RENDER
)
public class TargetHUD extends Module {
    @RegisterSubModule(name = "Target Time", description = "Times before this module forgets who its targeting", max = 5000, increment = 50)
    public static long targetTimeBeforeDementiaKicksIn = 250;

    @RegisterSubModule(name = "Animation In")
    public static SubCategory animationIn = new SubCategory();

    @RegisterSubModule(name = "Easing", parent = "Animation In")
    public static EasingUtil.EasingFunctions easeInFunction = EasingUtil.EasingFunctions.Ease_In_Out_Expo;

    @RegisterSubModule(name = "Pop In Time", description = "Time for the pop in effect to finish", max = 1000, increment = 50, parent = "Animation In")
    public static long popInTime = 250;

    @RegisterSubModule(name = "Animation Out")
    public static SubCategory animationOut = new SubCategory();

    @RegisterSubModule(name = "Easing", parent = "Animation Out")
    public static EasingUtil.EasingFunctions easeOutFunction = EasingUtil.EasingFunctions.Ease_In_Out_Expo;

    @RegisterSubModule(name = "Pop Out Time", description = "Time for the pop out effect to finish", max = 1000, increment = 50, parent = "Animation Out")
    public static long popOutTime = 250;

    private static Target target;

    @AllArgsConstructor
    private static class Target {
        public EntityLivingBase entity;
        public long lastInteract;
    }

    @SubscribeEvent
    public static void registerTarget(PacketEvent.Send event) {
        if (C.mc.currentScreen instanceof GuiChat) {
            updateTarget(C.p());
            return;
        }

        if (!(event.packet instanceof C02PacketUseEntity)) return;

        C02PacketUseEntity attackPacket = (C02PacketUseEntity) event.packet;
        if (attackPacket.getAction() != C02PacketUseEntity.Action.ATTACK) return;

        Entity newTarget = attackPacket.getEntityFromWorld(C.w());
        if (!(newTarget instanceof EntityLivingBase) || !TargetUtil.isValidTarget(newTarget)) return;

        updateTarget((EntityLivingBase) newTarget);
    }

    public static Draggable targetHUD = new Draggable(
            "targetHUD",
            () -> {
                double easingIn = EasingUtil.getAnimation("thIn");
                double easingOut = EasingUtil.getAnimation("thOut");

                if (target == null) return new double[] {0,0};

                if (target.lastInteract + popInTime + targetTimeBeforeDementiaKicksIn + popOutTime <= System.currentTimeMillis()) {
                    target = null;
                    return new double[] {0,0};
                }
                else if (target.lastInteract + popInTime + targetTimeBeforeDementiaKicksIn <= System.currentTimeMillis() && easingOut == -1) {
                    EasingUtil.addAnimation("thOut", popOutTime, true, easeOutFunction);
                }

                float healthNumber = target.entity.getHealth() + target.entity.getAbsorptionAmount();
                float healthPercentage = healthNumber / target.entity.getMaxHealth();

                double distance = TargetUtil.getDistanceToEntity(target.entity);

                String healthString = String.format("%.1f", healthNumber);
                String distanceString = String.format("%.1f", distance);
                String blockingString = target.entity instanceof AbstractClientPlayer && ((AbstractClientPlayer) target.entity).isBlocking() ? "§cBlocking" : "§aUnblocked";

                float healthStringWidth = FontUtil.getStringWidth(healthString + " | ", 6);
                float distanceStringWidth = FontUtil.getStringWidth(distanceString + " | ", 6);
                float blockingStringWidth = FontUtil.getStringWidth(blockingString, 6);

                float infoStringWidth = healthStringWidth + distanceStringWidth + blockingStringWidth;

                float width = Math.max(FontUtil.getStringWidth(target.entity.getName(), 15), infoStringWidth) + 37;
                float height = FontUtil.getFontHeight(15) + FontUtil.getFontHeight(6) + 4;

                double xIn = easingIn == -1 ? 0 : (-C.res().getScaledWidth() - width) * (1-easingIn);
                double xOut = easingOut == -1 ? 0 : (C.res().getScaledWidth() + width) * (easingOut);

                float x = (float) (xIn + xOut);
                float y = 0;

                // literally just the alt manager renderer
                RenderUtil.drawBlurRect(x, y, width, height, 3);
                RenderUtil.drawRect(x, y, width, height, new Color(22,22,22, 100));

                RenderUtil.drawPlayerHead(x + 4, y + 4, 24, 24, Color.WHITE, target.entity);

                Color[] colorsFade = RenderUtil.getColorsFade(x, width, RenderUtil.ThemeColours.Gay.getColours(), 1);
                RenderUtil.drawGradientLR(x, y, width, 1, colorsFade[0], colorsFade[1]);

                FontUtil.drawString(target.entity.getName(), x + 32, y, 15, Color.WHITE, true);
                Color healthColour = RenderUtil.getProgressColour(healthPercentage);
                Color distanceColour = RenderUtil.getProgressColour((float) (1 - (distance / 6f)));
                FontUtil.drawString(healthString + "§f | ", x + 32, y + FontUtil.getFontHeight(15), 6, healthColour, true);
                FontUtil.drawString(distanceString + "§f | ", x + 32 + healthStringWidth, y + FontUtil.getFontHeight(15), 6, distanceColour, true);
                FontUtil.drawString(blockingString, x + 32 + healthStringWidth + distanceStringWidth, y + FontUtil.getFontHeight(15), 6, Color.WHITE, true);

                return new double[] {x + width, y + height};
            },
            e -> ModuleManager.isEnabled(TargetHUD.class),
            e -> target != null || EasingUtil.getAnimation("thIn") != -1 || EasingUtil.getAnimation("thOut") != -1
    );

    private static void updateTarget(EntityLivingBase entity) {
        EasingUtil.Animation easingOutDetails = EasingUtil.getAnimationDetails("thOut");
        boolean isEasingIn = EasingUtil.getAnimationDetails("thIn") != null;
        boolean isEasingOut = easingOutDetails != null && easingOutDetails.up;

        if (target == null && !isEasingOut && !isEasingIn) EasingUtil.addAnimation("thIn", popInTime, true, easeInFunction);
        if (target == null || target.entity != entity) target = new Target(entity, System.currentTimeMillis());
        if (isEasingOut) EasingUtil.addAnimation("thOut", popInTime, false, easeOutFunction);

        target.lastInteract = System.currentTimeMillis();
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
