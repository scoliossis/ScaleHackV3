package com.github.scoliossis.modules.impl.render;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.RenderWorldEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.Render3dUtil;
import com.github.scoliossis.utils.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@RegisterModule(
        name = "ESP",
        description = "I do have are a very particular set of skills. Skills I have acquired over a very long career. Skills that make me a nightmare for people like you.",
        category = Category.RENDER
)
public class ESP extends Module {
    @RegisterSubModule(name = "Health Bar")
    public static boolean healthBar = true;

    @RegisterSubModule(name = "Square")
    public static boolean square = true;

    @RegisterSubModule(name = "Square Colour", parent = "Square")
    public static Color squareColour = new Color(255, 132, 132);

    @SubscribeEvent
    public static void onRenderWorldEvent(RenderWorldEvent event) {
        for (Entity entity : C.w().loadedEntityList) {
            if (!shouldESP(entity)) continue;

            EntityLivingBase livingEntity = (EntityLivingBase) entity;

            if (square) renderSquare(livingEntity, event.partialTicks);
            if (healthBar) renderHealthBar(livingEntity, event.partialTicks);
        }
    }

    private static void renderSquare(EntityLivingBase entity, float partialTicks) {
        GL11.glPushMatrix();
        RenderUtil.glTranslate(Render3dUtil.getRelativeEntityPos(entity, partialTicks));
        Render3dUtil.transform2Dto3D(false);

        RenderUtil.drawRectOutline(-0.5, 0, 1, entity.height, 1, squareColour);

        GL11.glPopMatrix();
    }

    private static void renderHealthBar(EntityLivingBase entity, float partialTicks) {
        GL11.glPushMatrix();
        RenderUtil.glTranslate(Render3dUtil.getRelativeEntityPos(entity, partialTicks));
        Render3dUtil.transform2Dto3D(false);

        float healthPercent = Math.min(entity.getHealth() / entity.getMaxHealth(), 1);
        float extraHealthPercent = entity.getAbsorptionAmount() / entity.getMaxHealth();

        int healthColour = (int) (255 * MathHelper.clamp_float((float) EasingUtil.EasingFunctions.Ease_In_Out_Sine.ease(healthPercent), 0, 1));

        Color backgroundColour = new Color(22, 22, 22);
        Color healthBarColour = new Color(255-healthColour, healthColour, 80);
        Color absorptionColour = new Color(255, 255, 0);

        float backgroundWidth = 0.1f;
        float backgroundHeight = entity.height;

        float healthBarIndent = 0.01f;
        float healthBarWidth = backgroundWidth - healthBarIndent * 2;
        float healthBarHeight = (backgroundHeight - healthBarIndent * 2) * healthPercent;

        float absorptionBarHeight = (backgroundHeight - healthBarIndent * 2) * Math.min(extraHealthPercent, 1);
        
        RenderUtil.drawRect(entity.width, 0, backgroundWidth, backgroundHeight, backgroundColour);
        RenderUtil.drawRect(entity.width+healthBarIndent, healthBarIndent, healthBarWidth, healthBarHeight, healthBarColour);
        RenderUtil.drawRect(entity.width+healthBarIndent, backgroundHeight-healthBarIndent-absorptionBarHeight, healthBarWidth, absorptionBarHeight, absorptionColour);

        GL11.glPopMatrix();
    }

    private static boolean shouldESP(Entity entity) {
        return entity instanceof EntityPlayer && entity != C.p();
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
