package com.github.scoliossis.modules.impl.client;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.MovementInputEvent;
import com.github.scoliossis.modules.*;
import com.github.scoliossis.modules.impl.render.Freecam;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.PlayerUtil;
import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;

@RegisterModule(
        name = "Movement Fix",
        description = "Fixes movement client side to be accurate to the server side rotation",
        category = Category.CLIENT,
        enabledByDefault = true
)
public class MovementFix extends Module {
    @RegisterSubModule(name = "Fix Movement", description = "Fixes movement client side to be accurate to the server side rotation")
    public static boolean movementFix = true;
    @RegisterSubModule(name = "Fancy Move Fix", description = "Tries its best to move you in the correct direction when fixing movement", parent = "Fix Movement")
    public static boolean fancyMoveFix = true;

    @RegisterSubModule(name = "Fix Rotation", description = "Makes sure your hovering the correct block/entity that you are server side")
    public static boolean rotationFix = true;


    public static boolean shouldMoveFix(Entity instance) {
        return (movementFix && instance == C.p() && PlayerUtil.playerUpdateEvent != null && ModuleManager.isEnabled(MovementFix.class) && !ModuleManager.isEnabled(Freecam.class));
    }
    public static boolean shouldRotationFix() {
        return (rotationFix && PlayerUtil.prevPlayerUpdateEvent != null && ModuleManager.isEnabled(MovementFix.class) && !ModuleManager.isEnabled(Freecam.class) && PlayerUtil.currentTickClientRotation != PlayerUtil.playerUpdateEvent.rotation);
    }

    @SubscribeEvent
    public static void onMovementInputEvent(MovementInputEvent event) {
        if (!fancyMoveFix || !shouldMoveFix(C.p())) return;

        float speed = Math.max(Math.abs(event.movementInput.moveForward), Math.abs(event.movementInput.moveStrafe));
        // make sure ur moving
        if (speed == 0) return;

        Movement_Direction fixedMovementDirection = getMovementDirection(event);

        // fix movement direction to one closest to rotation
        event.movementInput.moveForward = fixedMovementDirection.forward ? speed : (fixedMovementDirection.back ? -speed : 0);
        event.movementInput.moveStrafe = fixedMovementDirection.left ? speed : (fixedMovementDirection.right ? -speed : 0);
    }

    private static Movement_Direction getMovementDirection(MovementInputEvent event) {
        float yawDifference = C.p().rotationYaw - PlayerUtil.playerUpdateEvent.rotation.yaw;
        yawDifference += yawDifference < 0 ? -22.5f : 22.5f;

        int yawOrdinal = Math.floorMod((int) (yawDifference / 45), Movement_Direction.values().length);

        // get move direction
        Movement_Direction inputMoveDirection = event.movementInput.moveStrafe < 0 ? Movement_Direction.EAST : Movement_Direction.WEST;
        if (event.movementInput.moveForward > 0) inputMoveDirection = event.movementInput.moveStrafe < 0 ? Movement_Direction.NORTH_EAST : (event.movementInput.moveStrafe > 0 ? Movement_Direction.NORTH_WEST : Movement_Direction.NORTH);
        if (event.movementInput.moveForward < 0) inputMoveDirection = event.movementInput.moveStrafe < 0 ? Movement_Direction.SOUTH_EAST : (event.movementInput.moveStrafe > 0 ? Movement_Direction.SOUTH_WEST : Movement_Direction.SOUTH);

        return Movement_Direction.values()[Math.floorMod(inputMoveDirection.ordinal() + yawOrdinal, Movement_Direction.values().length)];
    }

    @AllArgsConstructor
    private enum Movement_Direction {
        NORTH       (   true,    false,  false,  false  ),
        NORTH_EAST  (   true,    true,   false,  false  ),
        EAST        (   false,   true,   false,  false  ),
        SOUTH_EAST  (   false,   true,   true,   false  ),
        SOUTH       (   false,   false,  true,   false  ),
        SOUTH_WEST  (   false,   false,  true,   true   ),
        WEST        (   false,   false,  false,  true   ),
        NORTH_WEST  (   true,    false,  false,  true   );

        private final boolean forward, right, back, left;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
