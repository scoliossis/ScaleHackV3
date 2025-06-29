package com.github.scoliossis.utils;

import net.minecraft.util.Vec3;

public class MovementUtil {
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
}
