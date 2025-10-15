package com.github.scoliossis.utils;

import com.github.scoliossis.bridge.net.minecraft.client.settings.KeyBindingBridge;
import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import net.minecraft.util.Vec3;

public class MovementUtil {
    public static int ticks = 0;
    public static int airTicks = 0;
    public static int groundTicks = 0;

    @SubscribeEvent(priority = 0)
    public static void onClientTickEvent(ClientTickEvent e) {
        if (C.isInGame()) {
            ticks++;
            airTicks++;
            groundTicks++;
            if (C.p().onGround) airTicks = 0;
            else groundTicks = 0;
        }
    }

    public static boolean isMoving(boolean y) {
        return C.p().posX != C.p().prevPosX || C.p().posZ != C.p().prevPosZ || (C.p().posY != C.p().prevPosY && y);
    }

    public static void setPos(Vec3 vec3) {
        C.p().setPosition(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public static void setPrevPos(Vec3 vec3) {
        C.p().prevPosX = vec3.xCoord;
        C.p().prevPosY = vec3.yCoord;
        C.p().prevPosZ = vec3.zCoord;
    }

    private static boolean overridingSprintDown = false;

    public static void setSprintPressed(boolean sprint) {
        KeyBindingBridge.from(C.mc.gameSettings.keyBindSprint).bridge$setPressed(sprint);
        overridingSprintDown = true;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (!overridingSprintDown) return;

        setSprintPressed(C.mc.gameSettings.keyBindSprint.isKeyDown());
    }
}
