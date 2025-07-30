package com.github.scoliossis.modules.impl.render;

import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.EasingUtil;
import net.minecraft.potion.Potion;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@RegisterModule(
        name = "Animations",
        description = "Dude I own this NFT. Do you really think you can get away with theft when you’re showing what you stole directly to my face. My lawyers will make an easy job of this case. Prepare to say goodbye to your luscious life and start preparing for the streets. I will ruin you.",
        category = Category.RENDER
)
public class Animations extends Module {
    @RegisterSubModule(name = "Swinging")
    public static SubCategory swinging = new SubCategory();

    @RegisterSubModule(name = "Smooth Swing", parent = "Swinging")
    public static boolean smoothSwing = true;

    @RegisterSubModule(name = "1.7 Blocking", parent = "Swinging")
    public static boolean oldBlocking = true;

    @RegisterSubModule(name = "Ignore Haste", parent = "Swinging")
    public static boolean ignoreHaste = false;

    @RegisterSubModule(name = "Custom Swing Speed", parent = "Swinging")
    public static boolean customSwingSpeed = false;

    @RegisterSubModule(name = "Arm Swing Speed", parent = "Custom Swing Speed", min = 0.1, max = 2)
    public static float armSwingSpeed = 1;

    @RegisterSubModule(name = "Easing", parent = "Swinging")
    public static EasingUtil.EasingFunctions swingEasing = EasingUtil.EasingFunctions.Normal;

    @RegisterSubModule(name = "Position")
    public static SubCategory position = new SubCategory();

    @RegisterSubModule(name = "Offset X", parent = "Position", min = -2, max = 2)
    public static float offsetX = 0;

    @RegisterSubModule(name = "Offset Y", parent = "Position", min = -2, max = 2)
    public static float offsetY = 0;

    @RegisterSubModule(name = "Offset Z", parent = "Position", min = -2, max = 2)
    public static float offsetZ = 0;

    @RegisterSubModule(name = "Reset Position", parent = "Position")
    public static boolean resetPosition = false;

    @RegisterSubModule(name = "Rotation")
    public static SubCategory rotation = new SubCategory();

    @RegisterSubModule(name = "Rotation X", parent = "Rotation", min = -180, max = 180, increment = 0.5)
    public static float rotationX = 0;

    @RegisterSubModule(name = "Rotation Y", parent = "Rotation", min = -180, max = 180, increment = 0.5)
    public static float rotationY = 0;

    @RegisterSubModule(name = "Rotation Z", parent = "Rotation", min = -180, max = 180, increment = 0.5)
    public static float rotationZ = 0;

    @RegisterSubModule(name = "Reset Rotation", parent = "Rotation")
    public static boolean resetRotation = false;

    @RegisterSubModule(name = "Size")
    public static SubCategory size = new SubCategory();

    @RegisterSubModule(name = "Size Multiplier", parent = "Size", max = 5)
    public static float sizeMultiplier = 1;

    @RegisterSubModule(name = "Uneven Size", parent = "Size", description = "Allows you to modify width, height and depth of held item")
    public static boolean unevenSize = false;

    @RegisterSubModule(name = "Width", parent = "Uneven Size", max = 5)
    public static float width = 1;

    @RegisterSubModule(name = "Height", parent = "Uneven Size", max = 5)
    public static float height = 1;

    @RegisterSubModule(name = "Depth", parent = "Uneven Size", max = 5)
    public static float depth = 1;

    @RegisterSubModule(name = "Reset Size", parent = "Size")
    public static boolean resetSize = false;


    private static int getArmSwingAnimationEnd() {
        return ignoreHaste ? 6 : C.p().isPotionActive(Potion.digSpeed) ? 6 - (1 + C.p().getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (C.p().isPotionActive(Potion.digSlowdown) ? 6 + (1 + C.p().getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
    }

    public static int getFakeArmSwingAnimationEnd() {
        return (int) (getArmSwingAnimationEnd() / (customSwingSpeed ? armSwingSpeed : 1));
    }

    public static float getArmSwingProgress(float partialTickTime) {
        float currentSwing = (fakeSwingProgress == 0 && prevFakeSwingProgress != 0) ? 1 : fakeSwingProgress;
        return (float) swingEasing.ease(prevFakeSwingProgress + ((currentSwing-prevFakeSwingProgress) * partialTickTime));
    }

    public static boolean canReswing() {
        // might aswell put this here its called every tick
        resetPosOrRots();

        return !fakeIsSwingInProgress || fakeSwingProgressInt >= getFakeArmSwingAnimationEnd() / 2 || fakeSwingProgressInt < 0;
    }

    private static void resetPosOrRots() {
        if (resetPosition) {
            offsetX = offsetY = offsetZ = 0;
            resetPosition = false;
        }
        if (resetRotation) {
            rotationX = rotationY = rotationZ = 0;
            resetRotation = false;
        }
        if (resetSize) {
            sizeMultiplier = width = height = depth = 1;
            resetSize = false;
        }
    }

    public static void scaleRotateTranslateHeldItem() {
        GL11.glTranslated(offsetX, offsetY, offsetZ);

        GL11.glRotatef(rotationX, 1, 0, 0);
        GL11.glRotatef(rotationY, 0, 1, 0);
        GL11.glRotatef(rotationZ, 0, 0, 1);
    }

    public static Vec3 getScaleVec() {
        return new Vec3(sizeMultiplier * (unevenSize ? width : 1),  sizeMultiplier * (unevenSize ? height : 1), sizeMultiplier * (unevenSize ? depth : 1));
    }

    public static boolean fakeIsSwingInProgress = false;
    public static int fakeSwingProgressInt = 0;
    public static float prevFakeSwingProgress = 0;
    public static float fakeSwingProgress = 0;

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
