package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ModuleStateChangeEvent;
import com.github.scoliossis.events.impl.RenderTickEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.FontUtil;
import com.github.scoliossis.utils.RenderUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

@RegisterModule(
        name = "Array List",
        description = "\"What's Happening?\"",
        category = Category.CLIENT
)
public class ArrayListModule extends Module {
    @RegisterSubModule(name = "Position")
    public SubCategory rendering = new SubCategory();

    @RegisterSubModule(name = "Font Size", parent = "Position", min = 5, max = 40, increment = 1)
    public static int fontSize = 10;

    @RegisterSubModule(name = "indent X", parent = "Position", min = 0, max = 20, increment = 1)
    public static int indentX = 5;
    @RegisterSubModule(name = "indent Y", parent = "Position", min = 0, max = 20, increment = 1)
    public static int indentY = 5;

    @RegisterSubModule(name = "gap Y", parent = "Position", min = 0, max = 10, increment = 1)
    public static int gapY = 5;

    @RegisterSubModule(name = "Colour")
    public SubCategory colour = new SubCategory();

    @RegisterSubModule(name = "Don't Use Theme", parent = "Colour")
    public static boolean dontUseTheme = false;

    @RegisterSubModule(name = "Colour Mode", parent = "Don't Use Theme")
    public static RenderUtil.ThemeColours customColour = RenderUtil.ThemeColours.Trans;

    @RegisterSubModule(name = "Pulsing Colour", parent = "Colour", description = "Epic pulsing hack 2025")
    public static boolean pulsingColour = false;

    @RegisterSubModule(name = "Pulsing Speed", parent = "Pulsing Colour", min = 0.1, max = 10)
    public static float pulseSpeed = 1;

    @RegisterSubModule(name = "Pulsing Multiplier", parent = "Pulsing Colour", min = 0.1, max = 10)
    public static float pulseMulti = 1;

    @RegisterSubModule(name = "Animation")
    public SubCategory animation = new SubCategory();

    @RegisterSubModule(name = "Ease In", parent = "Animation")
    public static EasingUtil.EasingFunctions easeInFunction = EasingUtil.EasingFunctions.Ease_Out_Expo;

    @RegisterSubModule(name = "Ease Out", parent = "Animation")
    public static EasingUtil.EasingFunctions easeOutFunction = EasingUtil.EasingFunctions.Ease_In_Out_Expo;

    @RegisterSubModule(name = "Pop In Time", parent = "Animation", description = "Time for the pop in effect to finish", min = 100, max = 1000, increment = 100)
    public static long popInTime = 500;

    @RegisterSubModule(name = "Pop Out Time", parent = "Animation", description = "Time for the pop out effect to finish", min = 100, max = 1000, increment = 100)
    public static long popOutTime = 500;

    @RegisterSubModule(name = "Other")
    public SubCategory other = new SubCategory();

    @RegisterSubModule(name = "No Capitals", parent = "Other")
    public static boolean noCapitals = true;

    @RegisterSubModule(name = "No Spaces", parent = "Other")
    public static boolean noSpaces = true;

    @RegisterSubModule(name = "Show Mode", parent = "Other")
    public static boolean showMode = true;

    @RegisterSubModule(name = "Hide Client", parent = "Other", description = "Hides modules in the \"Client\" category")
    public static boolean hideClient = true;

    @RegisterSubModule(name = "Hide Render", parent = "Other", description = "Hides modules in the \"Render\" category")
    public static boolean hideRendering = true;

    @SubscribeEvent
    public static void onModuleStateChangedEvent(ModuleStateChangeEvent event) {
        EasingUtil.addAnimation(event.module.getUniqueKey("arraylist"), event.state ? popInTime : popOutTime, event.state, event.state ? easeInFunction : easeOutFunction);
    }

    @SubscribeEvent
    public static void onRenderTickEvent(RenderTickEvent event) {
        List<Module> modules = ModuleManager.getModules();
        modules.removeIf(e -> (!e.isEnabled() && EasingUtil.getAnimation(e.getUniqueKey("arraylist")) == -1) || (hideClient && e.getAnnotation().category()==Category.CLIENT) || (hideRendering && e.getAnnotation().category()==Category.RENDER));
        modules.sort(Comparator.comparingDouble(e -> -FontUtil.getStringWidth(getModuleName(e), fontSize)));

        GL11.glPushMatrix();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            String moduleName = getModuleName(module);

            float width = FontUtil.getStringWidth(moduleName, fontSize) + indentX;
            int x = (int) (C.res().getScaledWidth() - width);
            float yTranslation = RenderUtil.getCurrentTranslation()[1];
            float height = FontUtil.getFontHeight(fontSize) + gapY;

            GL11.glPushMatrix();
            RenderUtil.glScissor(indentX, indentY, C.res().getScaledWidth(), height);

            double animation = EasingUtil.getAnimation(module.getUniqueKey("arraylist"));
            if (animation != -1) GL11.glTranslated(width * (1 - animation), height * (1 - animation), 0);
            else animation = 1;

            Color[] colours = dontUseTheme ? customColour.colours : ThemeModule.getThemeColours();
            Color colour = colours[i % colours.length];
            if (pulsingColour) colour = RenderUtil.getColorsFade(yTranslation*pulseMulti, height*pulseMulti, colours, pulseSpeed)[0];

            FontUtil.drawString(getModuleName(module), x, indentY, fontSize, colour, true);

            GL11.glPopMatrix();
            RenderUtil.disableScissor();

            GL11.glTranslated(0, height*animation, 0);
        }
        GL11.glPopMatrix();
    }

    private static String getModuleName(Module module) {
        String name = module.getAnnotation().name();
        if (noCapitals) name = name.toLowerCase();
        if (noSpaces) name = name.replaceAll(" ", "");
        String arrayListExtraInfo = module.arrayListExtraInfo();
        return name + (arrayListExtraInfo.isEmpty() || !showMode ? "" : " &8[" + arrayListExtraInfo + "]");
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
