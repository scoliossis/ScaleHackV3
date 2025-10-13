package com.github.scoliossis.modules.impl.combat;

import com.github.scoliossis.bridge.net.minecraft.network.play.server.S12PacketEntityVelocityBridge;
import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.events.impl.PacketEvent;
import com.github.scoliossis.events.impl.PlayerUpdateEvent;
import com.github.scoliossis.modules.Category;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.RegisterModule;
import com.github.scoliossis.modules.RegisterSubModule;
import com.github.scoliossis.utils.BlinkUtil;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.MovementUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;

@RegisterModule(
        name = "Velocity",
        description = "heavy dick n balls or whatever, todo: add original description",
        category = Category.COMBAT
)
public class Velocity extends Module {
    private static int velocityTick = -1;
    private static boolean shouldDelay = false;

    @RegisterSubModule(name = "0 Check")
    public static boolean zeroCheck = true;

    @RegisterSubModule(name = "Mode")
    public static VelocityMode velocityMode = VelocityMode.Dynamic;

    // todo: add mode that delays velocity in air until land, lazy
    public enum VelocityMode {
        Vanilla,
        Keep,
        Delay,
        Jump_Reset,
        Dynamic
    }

    @RegisterSubModule(name = "Velocity X", parent = "Mode", modeParentString = {"Vanilla", "Keep"}, min = -2, max = 2)
    public static float xMulti = 1;
    @RegisterSubModule(name = "Velocity Y", parent = "Mode", modeParentString = {"Vanilla", "Keep"}, min = -2, max = 2)
    public static float yMulti = 1;
    @RegisterSubModule(name = "Velocity Z", parent = "Mode", modeParentString = {"Vanilla", "Keep"}, min = -2, max = 2)
    public static float zMulti = 1;

    @RegisterSubModule(name = "Max Delay Ticks", parent = "Mode", modeParentString = {"Delay", "Dynamic"}, min = 1, max = 20)
    public static int maxDelayTicks = 5;

    @SubscribeEvent
    public static void onPacketReceived(PacketEvent.Receive event) {
        if (!(event.packet instanceof S12PacketEntityVelocity)) return;

        S12PacketEntityVelocity packet = ((S12PacketEntityVelocity) event.packet);
        if (packet.getEntityID() != C.p().getEntityId()) return;

        S12PacketEntityVelocityBridge accessiblePacket = S12PacketEntityVelocityBridge.from(packet);
        Vec3 originalVelocity = new Vec3(packet.getMotionX()/8000d, packet.getMotionY()/8000d, packet.getMotionZ()/8000d);

        if (originalVelocity.xCoord == 0 && originalVelocity.zCoord == 0 && zeroCheck) return;

        switch (velocityMode) {
            case Vanilla:
                accessiblePacket.bridge$setMotionX(originalVelocity.xCoord*xMulti);
                accessiblePacket.bridge$setMotionY(originalVelocity.yCoord*yMulti);
                accessiblePacket.bridge$setMotionZ(originalVelocity.zCoord*zMulti);
                break;

            case Keep:
                accessiblePacket.bridge$setMotionX(originalVelocity.xCoord*xMulti + C.p().motionX*(1-xMulti));
                accessiblePacket.bridge$setMotionY(originalVelocity.yCoord*yMulti + C.p().motionY*(1-yMulti));
                accessiblePacket.bridge$setMotionZ(originalVelocity.zCoord*zMulti + C.p().motionZ*(1-zMulti));
                break;

            case Jump_Reset:
                velocityTick = MovementUtil.ticks;
                break;

            case Dynamic:
                if (C.p().onGround) {
                    velocityTick = MovementUtil.ticks;
                    break;
                }
            case Delay:
                event.setCancelled(true);
                BlinkUtil.pushBlink(false, true, event.packet);

                shouldDelay = true;
                velocityTick = MovementUtil.ticks;
                break;
        }
    }

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerUpdateEvent event) {
        if (velocityTick != -1 && (velocityMode == VelocityMode.Jump_Reset || (velocityMode == VelocityMode.Dynamic && !shouldDelay))) {
            if (C.p().onGround) C.p().jump();

            velocityTick = -1;
        }
    }

    private static boolean shouldStopBlink() {
        return velocityTick != -1 && shouldDelay && MovementUtil.ticks - velocityTick > maxDelayTicks;
    }

    @SubscribeEvent
    public static void onClientTickEvent(ClientTickEvent event) {
        if (!shouldStopBlink()) return;

        BlinkUtil.popBlink(false, true);
        velocityTick = -1;
        shouldDelay = false;
    }

    @Override
    public String arrayListExtraInfo() {
        return velocityMode.name();
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        if (velocityTick == -1) return;

        BlinkUtil.popBlink(false, true);
        velocityTick = -1;
    }
}
