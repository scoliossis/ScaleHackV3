package com.github.scoliossis.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class InventoryUtil {
    public static boolean isSlotEmpty(int slot) {
        return C.p().inventory.getStackInSlot(slot) == null || C.p().inventory.getStackInSlot(slot).stackSize <= 0;
    }

    public static int biggestBlockSlot() {
        int biggestSlot = -1;

        for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
            if (isSlotEmpty(i)) continue;

            ItemStack stack = C.p().inventory.getStackInSlot(i);

            if (!isValidBlock(stack)) continue;

            if (biggestSlot != -1 && stack.stackSize < C.p().inventory.getStackInSlot(biggestSlot).stackSize) continue;

            biggestSlot = i;
        }

        return biggestSlot;
    }

    public static boolean isValidBlock() {
        return isValidBlock(C.p().inventory.getCurrentItem());
    }

    public static boolean isValidBlock(ItemStack stack) {
        if (stack == null) return false;

        Block block = Block.getBlockFromItem(stack.getItem());
        if (block == null) return false;

        // no falling blocks
        if (block.equals(Blocks.sand) || block.equals(Blocks.gravel)) return false;
        // no interactable blocks
        if (block.equals(Blocks.furnace) || block.equals(Blocks.crafting_table)) return false;
        // only full cubes
        return block.isOpaqueCube();
    }
}
