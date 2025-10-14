package com.github.scoliossis.utils;

import com.github.scoliossis.bridge.net.minecraft.network.NetworkManagerBridge;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class PacketUtil {
    public static void sendPacket(Packet<?> packet) {
        C.mc.getNetHandler().addToSendQueue(packet);
    }

    public static void receivePacket(Packet<?> packet) {
        if (packet == null || C.mc.getNetHandler().getNetworkManager() == null) return;
        NetworkManagerBridge.from(C.mc.getNetHandler().getNetworkManager()).bridge$processPacket((Packet<INetHandler>) packet);
    }
}