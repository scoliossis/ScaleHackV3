package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.modules.SubModules.SubCategory;

@RegisterModule(
        name = "Kill Aura",
        description = "Now I Am Become Death, the Destroyer of Worlds.",
        category = Category.COMBAT
)
public class KillAura extends Module {
    // filler settings from my other client
    @RegisterSubModule(name = "Range")
    public SubCategory rangeSubcategory = new SubCategory();

    @RegisterSubModule(name = "Rotation Range", parent = "Range", min = 1, max = 8)
    public double killAuraRotationRange = 5;

    @RegisterSubModule(name = "Attack Range", parent = "Range", min = 1, max = 6)
    public double killAuraAttackRange = 3.1;

    @RegisterSubModule(name = "Through Walls Range", parent = "Range", min = 0, max = 6)
    public double killAuraWallsRange = 2;


    @RegisterSubModule(name = "Targeting")
    public SubCategory killAuraTargetingSubCategory = new SubCategory();

    @RegisterSubModule(name = "Target Sorting", parent = "Targeting")
    public KillAuraSorting killAuraSorting = KillAuraSorting.Health;
    public enum KillAuraSorting {
        Distance, Hurt_Time, Health
    }

    // maybe add multi idk
    @RegisterSubModule(name = "Target Choice", parent = "Targeting")
    public KillAuraTargeting killAuraTarget = KillAuraTargeting.Best;
    public enum KillAuraTargeting {
        Switch, Best, Single
    }

    @RegisterSubModule(name = "Rotation")
    public SubCategory rotationsSubcategory = new SubCategory();

    @RegisterSubModule(name = "Rotation Mode", parent = "Rotation")
    public KillAuraRotations rotations = KillAuraRotations.Smooth;
    public enum KillAuraRotations {
        Simple, Smooth, Snap, None
    }

    @RegisterSubModule(name = "Min Rotation", parent = "Rotation Mode", modeParentString = "Simple", min = 0, max = 180)
    public double minRotation = 0;

    @RegisterSubModule(name = "Max Rotation", parent = "Rotation Mode", modeParentString = "Simple", min = 1, max = 180)
    public double maxRotation = 100;

    @RegisterSubModule(name = "Rotation Smoothing", parent = "Rotation Mode", modeParentString = "Smooth", min = 1f, max = 5)
    public double smoothRotationSpeed = 1;


    @RegisterSubModule(name = "Attacking")
    public SubCategory attackingSubCat = new SubCategory();

    @RegisterSubModule(name = "Keep Sprint", parent = "Attacking", dangerous = true)
    public boolean auraKeepSprint = false;

    @RegisterSubModule(name = "Sword Only", parent = "Attacking")
    public boolean swordOnlyAura = true;

    @RegisterSubModule(name = "Attack Speed Min", min = 0, max = 20, parent = "Attacking")
    public double killAuraAttackSpeedMin = 5;
    @RegisterSubModule(name = "Attack Speed Max", min = 0, max = 20, parent = "Attacking")
    public double killAuraAttackSpeedMax = 10;

    @RegisterSubModule(name = "Autoblock", parent = "Attacking", description = "Automatically blocks with your sword to reduce damage taken", dangerous = true)
    public boolean autoblock = false;

    @RegisterSubModule(name = "Autoblock Mode", parent = "Autoblock")
    public AutoBlockMode autoblockMode = AutoBlockMode.Hypixel;
    public enum AutoBlockMode {
        Vanilla, Hypixel
    }


    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
