package com.github.scoliossis.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class InventoryUtil {
    public static boolean isSlotEmpty(int slot) {
        return C.p().inventory.getStackInSlot(slot) == null || C.p().inventory.getStackInSlot(slot).stackSize <= 0;
    }

    public static int biggestBlockSlot() {
        int biggestSlot = -1;

        for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
            if (!isValidBlock(getItemInSlot(i))) continue;

            if (biggestSlot != -1 && C.p().inventory.getStackInSlot(i).stackSize < C.p().inventory.getStackInSlot(biggestSlot).stackSize) continue;

            biggestSlot = i;
        }

        return biggestSlot;
    }

    public static Item getHeldItem() {
        return C.p().inventory.getCurrentItem() == null ? null : C.p().inventory.getCurrentItem().getItem();
    }

    public static Item getItemInSlot(int i) {
        return C.p().inventory.getStackInSlot(i) == null ? null : C.p().inventory.getStackInSlot(i).getItem();
    }

    public static boolean isValidBlock() {
        return isValidBlock(getHeldItem());
    }

    public static boolean isValidBlock(Item stack) {
        Block block = Block.getBlockFromItem(stack);
        if (block == null) return false;

        // no falling blocks
        if (block.equals(Blocks.sand) || block.equals(Blocks.gravel) || block.equals(Blocks.anvil)) return false;
        // no interactable blocks
        if (block.equals(Blocks.furnace) || block.equals(Blocks.crafting_table)) return false;

        AxisAlignedBB collisionBox = block.getCollisionBoundingBox(C.w(), BlockPos.ORIGIN, block.getDefaultState());

        if (collisionBox == null) return false;

        // only full cubes
        return collisionBox.minX == 0 && collisionBox.minY == 0 && collisionBox.minZ == 0
                && collisionBox.maxX == 1 && collisionBox.maxY == 1 && collisionBox.maxZ == 1;
    }
}
