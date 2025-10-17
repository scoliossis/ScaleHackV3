package com.github.scoliossis;

import com.github.scoliossis.commands.CommandManager;
import com.github.scoliossis.modules.ModuleManager;
import com.github.scoliossis.utils.alts.microsoft.AuthServer;
import com.github.scoliossis.utils.render.FontUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.opengl.Display;

@Mod(modid = Main.MOD_ID, name = Main.MOD_NAME, version = Main.MOD_VERSION)
public class Main {
    // if you have optifine installed it fricks with the custom jframe used to check for drag and drop.
    // and optifine has no mod id, idk how they do it, just picked a random class in optifine to check if its installed
    public static boolean optifineInstalled = Main.class.getClassLoader().getResource("optifine/LaunchUtils.class") != null;

    public static final String MOD_ID = "@MOD_ID@";
    public static final String MOD_NAME = "@MOD_NAME@";
    public static final String MOD_VERSION = "@MOD_VERSION@";

    public static final String baseConfig = "base";

    public static final String baseFolderPath = "config/" + Main.MOD_ID + "/";
    public static final String configPath = baseFolderPath + "config/";
    public static final String configExtension = ".cfg";
    public static final String extraSavedFeaturesPath = baseFolderPath + "extras/";

    static {
        // load font here so it can be used immediately!!
        FontUtil.loadFont(0);
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        Display.setTitle(MOD_NAME + " " + MOD_VERSION);

        ModuleManager.init();
        CommandManager.init();

        new AuthServer();
    }
}
