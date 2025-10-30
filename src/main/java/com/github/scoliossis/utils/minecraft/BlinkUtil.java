package com.github.scoliossis.utils.minecraft;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.ClientTickEvent;
import com.github.scoliossis.events.impl.PacketEvent;
import com.github.scoliossis.utils.client.C;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlinkUtil {
    // todo: wipe on lobby change
    private final static Queue<Packet<?>> blinkedSentPackets = new ConcurrentLinkedQueue<>();
    private final static Queue<Packet<?>> blinkedReceivedPackets = new ConcurrentLinkedQueue<>();

    private static int blinkOutgoingIndex = -1;
    private static int blinkIncomingIndex = -1;

    @SubscribeEvent
    public static void onPacketSent(PacketEvent.Send event) {
        if (blinkOutgoingIndex > -1) {
            blinkedSentPackets.add(event.packet);
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onPacketReceived(PacketEvent.Receive event) {
        if (blinkIncomingIndex > -1) {
            blinkedReceivedPackets.add(event.packet);
            event.setCancelled(true);
        }
    }

    @SubscribeEvent(priority = 999)
    public static void onClientTickEvent(ClientTickEvent event) {
        if (C.w() == null) {
            disableBlink(true, true);
        }
    }

    public static boolean isBlinking(boolean sent, boolean received) {
        return (sent && !blinkedSentPackets.isEmpty()) || (received && !blinkedReceivedPackets.isEmpty());
    }

    public static void pushBlink(boolean sent, boolean received) {
        pushBlink(sent, received, null);
    }

    public static void pushBlink(boolean sent, boolean received, Packet<?> packet) {
        if (sent) {
            blinkOutgoingIndex = Math.max(0, blinkOutgoingIndex+1);
            if (packet != null) blinkedSentPackets.add(packet);
        }

        if (received) {
            blinkIncomingIndex = Math.max(0, blinkIncomingIndex + 1);
            if (packet != null) blinkedReceivedPackets.add(packet);
        }
    }

    public static void popBlink(boolean sent, boolean received) {
        blinkOutgoingIndex -= sent ? 1 : 0;
        blinkIncomingIndex -= received ? 1 : 0;

        if (sent && blinkOutgoingIndex <= -1) {
            Packet<?> packet;

            while ((packet = blinkedSentPackets.poll()) != null) {
                PacketUtil.sendPacket(packet);
            }
        }

        if (received && blinkIncomingIndex <= -1) {
            Packet<?> packet;

            while ((packet = blinkedReceivedPackets.poll()) != null) {
                PacketUtil.receivePacket(packet);
            }
        }
    }

    /// clears all the blinked packets and pretends they didnt happen
    public static void disableBlink(boolean sent, boolean received) {
        if (sent) {
            blinkOutgoingIndex = -1;
            blinkedSentPackets.clear();
        }

        if (received) {
            blinkIncomingIndex = -1;
            blinkedReceivedPackets.clear();
        }
    }
}
