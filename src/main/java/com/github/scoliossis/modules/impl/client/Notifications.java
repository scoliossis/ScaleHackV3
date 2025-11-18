package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.DrawScreenEvent;
import com.github.scoliossis.events.impl.ModuleStateChangeEvent;
import com.github.scoliossis.events.impl.RenderTickEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.utils.client.C;
import com.github.scoliossis.utils.render.EasingUtil;
import com.github.scoliossis.utils.render.FontUtil;
import com.github.scoliossis.utils.render.RenderUtil;
import lombok.AllArgsConstructor;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

@RegisterModule(
        name = "Notifications",
        description = "Displays notifications from the client",
        category = Category.CLIENT,
        enabledByDefault = true
)
public class Notifications extends Module {
    @RegisterSubModule(name = "Animation In")
    public static SubCategory animationIn = new SubCategory();

    @RegisterSubModule(name = "Ease In", parent = "Animation In")
    public static EasingUtil.EasingFunctions easeInFunction = EasingUtil.EasingFunctions.Ease_In_Out_Sine;

    @RegisterSubModule(name = "Pop In Time", description = "Time for the pop in effect to finish", max = 1000, increment = 50, parent = "Animation In")
    public static long popInTime = 500;

    @RegisterSubModule(name = "Animation Out")
    public static SubCategory animationOut = new SubCategory();

    @RegisterSubModule(name = "Ease Out", parent = "Animation Out")
    public static EasingUtil.EasingFunctions easeOutFunction = EasingUtil.EasingFunctions.Ease_In_Out_Expo;

    @RegisterSubModule(name = "Pop Out Time", description = "Time for the pop out effect to finish", max = 1000, increment = 50, parent = "Animation Out")
    public static long popOutTime = 500;

    @RegisterSubModule(name = "Other")
    public static SubCategory other = new SubCategory();

    @RegisterSubModule(name = "Recent Lower", parent = "Other")
    public static boolean displayRecentAtBottom = true;

    @RegisterSubModule(name = "Notification Length", description = "Notification Length in ms", min = 500, max = 10000, increment = 100, parent = "Other")
    public static long notificationLength = 5000;

    private static final int titleFontSize = 12;
    private static final int messageFontSize = 9;
    private static final Color fontColor = Color.WHITE;

    private static final int widthPadding = 5;
    private static final int gapBetweenLines = 3;
    private static final int minimumWidth = 125;

    private static final int gapBetweenNotifications = 5;
    private static final Color backgroundColor =  new Color(22,22,22, 200);

    @SubscribeEvent
    public static void onModuleStateChangedEvent(ModuleStateChangeEvent event) {
        addNotification("Module Toggled", event.module.getAnnotation().name() + (event.state ? " §aEnabled" : " §cDisabled"));
    }


    private static final ArrayList<Notification> notifications = new ArrayList<>();

    @AllArgsConstructor
    public static class Notification {
        private final String title;
        private final String message;
        private final long time;
    }

    public static void addNotification(String title, String message) {
        notifications.add(displayRecentAtBottom ? 0 : notifications.size()-1, new Notification(title, message, System.currentTimeMillis()));

    }

    @SubscribeEvent
    public static void onRenderTickEvent(RenderTickEvent event) {
        renderNotification();
    }

    @SubscribeEvent
    public static void onDrawScreenEvent(DrawScreenEvent event) {
        if (!C.isInGame()) renderNotification();
    }

    private static void renderNotification() {
        GL11.glPushMatrix();

        float titleFontHeight = FontUtil.getFontHeight(titleFontSize);
        float messageFontHeight = FontUtil.getFontHeight(messageFontSize);

        for (int i = 0; i < notifications.size()-1; i++) {
            Notification notification = notifications.get(i);

            long timePassed = System.currentTimeMillis() - notification.time;

            if (timePassed > notificationLength) {
                notifications.remove(notification);
                continue;
            }

            float popInTimeRatio = MathHelper.clamp_float((float) timePassed / (float) popInTime, 0, 1);
            float popOutTimeRatio = MathHelper.clamp_float((float) (notificationLength-timePassed) / (float) popOutTime, 0, 1);

            float popInEased = (float) easeInFunction.ease(popInTimeRatio);
            float popOutEased = (float) easeOutFunction.ease(popOutTimeRatio);

            float xOffsetMulti = popInEased*popOutEased;
            float yOffsetMulti = popInEased*popOutEased;

            float baseWidth = Math.max(Math.max(FontUtil.getStringWidth(notification.message, messageFontSize), FontUtil.getStringWidth(notification.title, titleFontSize)), minimumWidth);
            float w = (baseWidth + widthPadding*2) * xOffsetMulti;
            float h = (titleFontHeight + messageFontHeight + gapBetweenLines*3) * yOffsetMulti;
            float x = C.res().getScaledWidth() - w;
            float y = C.res().getScaledHeight() - h - gapBetweenNotifications;

            Color[] colorsFade = RenderUtil.getColorsFade(x, w, RenderUtil.ThemeColours.Gay.getColours(), 3f);

            RenderUtil.glScissor(x, y, w, h);

            RenderUtil.drawRect(x, y, w, h, backgroundColor);
            RenderUtil.drawGradientLR(x, y, w, 1, colorsFade[0], colorsFade[1]);

            FontUtil.drawString(notification.title, x + widthPadding, y + gapBetweenLines, titleFontSize, fontColor, false);
            FontUtil.drawString(notification.message, x + widthPadding, y + titleFontHeight + gapBetweenLines*2, messageFontSize, fontColor, false);
            RenderUtil.disableScissor();

            GL11.glTranslated(0, -h - gapBetweenNotifications*yOffsetMulti, 0);
        }
        GL11.glPopMatrix();
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
