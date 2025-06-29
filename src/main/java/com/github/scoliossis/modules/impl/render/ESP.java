package com.github.scoliossis.modules.impl.render;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.RenderWorldEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.modules.SubModules.ColourSubModule;
import com.github.scoliossis.modules.impl.client.ThemeModule;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.Render3dUtil;
import com.github.scoliossis.utils.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@RegisterModule(
        name = "ESP",
        description = "I do have are a very particular set of skills. Skills I have acquired over a very long career. Skills that make me a nightmare for people like you.",
        category = Category.RENDER
)
public class ESP extends Module {
    @RegisterSubModule(name = "Color Mode")
    public static ColourMode colourMode = ColourMode.Theme;
    public enum ColourMode {
        Custom,
        Theme
    }

    @RegisterSubModule(name = "Color 1", parent = "Color Mode", modeParentString = "Custom")
    public static ColourSubModule espCustomColour1 = new ColourSubModule(new Color(250, 80, 180, 150));

    @RegisterSubModule(name = "Color 2", parent = "Color Mode", modeParentString = "Custom")
    public static ColourSubModule espCustomColour2 = new ColourSubModule(new Color(150, 255, 230, 150));

    @SubscribeEvent
    public static void onRenderWorldEvent(RenderWorldEvent event) {
        for (Entity entity : C.w().loadedEntityList) {
            if (!shouldESP(entity)) continue;
            GL11.glPushMatrix();
            RenderUtil.glTranslate(Render3dUtil.getRelativeEntityPos(entity, event.partialTicks));
            Render3dUtil.transform2Dto3D(false);

            Color[] colours = colourMode == ColourMode.Theme ? ThemeModule.getThemeColours() : new Color[]{espCustomColour1.colour, espCustomColour2.colour};
            Color espColor = RenderUtil.getColorsFade((entity.posX+entity.posY+entity.posZ)*100, colours, 1);
            Render3dUtil.drawRoundedRectGlow(-0.5f, 0, 1, 2, 1, espColor);

            GL11.glPopMatrix();
        }
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
