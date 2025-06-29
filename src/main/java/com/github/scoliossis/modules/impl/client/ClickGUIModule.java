package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.modules.*;
import com.github.scoliossis.modules.SubModules.ColourSubModule;
import com.github.scoliossis.modules.SubModules.SubCategory;
import com.github.scoliossis.screens.ClickGUIScreen;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.EasingUtil;
import com.github.scoliossis.utils.ScreenUtil;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;
import java.util.ArrayList;

@RegisterModule(
        name = "Click GUI",
        description = "Awesome UI to toggle modules",
        category = Category.CLIENT
)
public class ClickGUIModule extends Module {
    @RegisterSubModule(name = "GUI Colour")
    public static ColourSubModule clickGUIenabledColour = new ColourSubModule(new Color(195, 160, 255));

    @RegisterSubModule(name = "Animations")
    public static SubCategory animations = new SubCategory();

    @RegisterSubModule(name = "Open", parent = "Animations")
    public static EasingUtil.EasingFunctions openAnimation = EasingUtil.EasingFunctions.Ease_In_Out_Sine;

    @RegisterSubModule(name = "Open Length", parent = "Animations", max = 5000, increment = 100)
    public static long openAnimationLength = 100;

    @RegisterSubModule(name = "Close", parent = "Animations")
    public static EasingUtil.EasingFunctions closeAnimation = EasingUtil.EasingFunctions.Ease_In_Out_Sine;

    @RegisterSubModule(name = "Close Length", parent = "Animations", max = 5000, increment = 100)
    public static long closeAnimationLength = 100;


    public static final ArrayList<Module> rightClickedModules = new ArrayList<>();

    public static final GuiTextField searchBar = new GuiTextField(0, C.mc.fontRendererObj, 0, 0, 0, 0);


    @Override
    protected void onEnable() {
        searchBar.setFocused(true);
        searchBar.setCanLoseFocus(false);

        if (C.isInGame()) ScreenUtil.setGuiToDisplay(new ClickGUIScreen());
        else ModuleManager.getModule(ClickGUIModule.class).setEnabled(false);
    }

    @Override
    protected void onDisable() {
        if (C.isInGame()) {
            C.mc.displayGuiScreen(null);

            if (C.mc.currentScreen == null) {
                C.mc.setIngameFocus();
            }
        }
    }
}
