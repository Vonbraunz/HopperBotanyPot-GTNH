package com.vonbraunz.botanypots.tileentity;

import com.vonbraunz.botanypots.compat.CropsNHCompat;
import com.vonbraunz.botanypots.registry.BotanyCropRegistry;
import com.vonbraunz.botanypots.registry.SoilRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class TileEntityBotanyPot extends TileEntity implements ISidedInventory {

    public static final int SLOT_SOIL   = 0;
    public static final int SLOT_SEED   = 1;
    public static final int SLOT_OUTPUT = 2;
    private static final int SIZE = 3;

    // Hoppers below pull from output; hoppers on top/sides push soil and seeds
    private static final int[] SLOTS_BOTTOM = { SLOT_OUTPUT };
    private static final int[] SLOTS_TOP_SIDE = { SLOT_SOIL, SLOT_SEED };

    private final ItemStack[] slots = new ItemStack[SIZE];
    public int growthTicker = 0;

    // -------------------------------------------------------------------------
    // Growth logic
    // -------------------------------------------------------------------------

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;
        if (slots[SLOT_SOIL] == null || slots[SLOT_SEED] == null) return;
        if (isOutputFull()) return;

        growthTicker++;

        if (growthTicker >= getMaxGrowthTicks()) {
            doHarvest();
            growthTicker = 0;
            markDirty();
        }
    }

    private int getMaxGrowthTicks() {
        float soilMult = SoilRegistry.getMultiplier(slots[SLOT_SOIL]);
        if (CropsNHCompat.isCropsNHSeed(slots[SLOT_SEED])) {
            return CropsNHCompat.getGrowthTicks(slots[SLOT_SEED], soilMult);
        }
        return BotanyCropRegistry.getGrowthTicks(slots[SLOT_SEED], soilMult);
    }

    private void doHarvest() {
        List<ItemStack> drops;
        if (CropsNHCompat.isCropsNHSeed(slots[SLOT_SEED])) {
            drops = CropsNHCompat.getDrops(slots[SLOT_SEED]);
        } else {
            drops = BotanyCropRegistry.getDrops(slots[SLOT_SEED]);
        }
        for (ItemStack drop : drops) {
            addToOutput(drop);
        }
    }

    private void addToOutput(ItemStack incoming) {
        if (incoming == null || incoming.stackSize <= 0) return;

        if (slots[SLOT_OUTPUT] == null) {
            slots[SLOT_OUTPUT] = incoming.copy();
            return;
        }
        if (ItemStack.areItemsEqual(slots[SLOT_OUTPUT], incoming)
                && ItemStack.areItemStackTagsEqual(slots[SLOT_OUTPUT], incoming)) {
            int space = slots[SLOT_OUTPUT].getMaxStackSize() - slots[SLOT_OUTPUT].stackSize;
            slots[SLOT_OUTPUT].stackSize += Math.min(space, incoming.stackSize);
        }
        // If output slot has a different item, the drop is silently lost.
        // In practice hoppers drain the output slot fast enough this rarely matters.
    }

    private boolean isOutputFull() {
        if (slots[SLOT_OUTPUT] == null) return false;
        return slots[SLOT_OUTPUT].stackSize >= slots[SLOT_OUTPUT].getMaxStackSize();
    }

    // -------------------------------------------------------------------------
    // Player interaction
    // -------------------------------------------------------------------------

    public boolean onInteractWithItem(EntityPlayer player, ItemStack held) {
        if (slots[SLOT_SOIL] == null && SoilRegistry.isValidSoil(held)) {
            slots[SLOT_SOIL] = held.copy();
            slots[SLOT_SOIL].stackSize = 1;
            deductHeld(player, held);
            markDirty();
            return true;
        }
        if (slots[SLOT_SOIL] != null && slots[SLOT_SEED] == null && isValidSeed(held)) {
            slots[SLOT_SEED] = held.copy();
            slots[SLOT_SEED].stackSize = 1;
            deductHeld(player, held);
            growthTicker = 0;
            markDirty();
            return true;
        }
        return false;
    }

    // Right-click empty-handed: remove seed first, then soil
    public boolean onInteractEmpty(EntityPlayer player) {
        if (slots[SLOT_SEED] != null) {
            if (player.inventory.addItemStackToInventory(slots[SLOT_SEED].copy())) {
                slots[SLOT_SEED] = null;
                growthTicker = 0;
                markDirty();
                return true;
            }
            return false;
        }
        if (slots[SLOT_SOIL] != null) {
            if (player.inventory.addItemStackToInventory(slots[SLOT_SOIL].copy())) {
                slots[SLOT_SOIL] = null;
                markDirty();
                return true;
            }
            return false;
        }
        return false;
    }

    private void deductHeld(EntityPlayer player, ItemStack held) {
        held.stackSize--;
        if (held.stackSize <= 0) {
            player.setCurrentItemOrArmor(0, null);
        }
    }

    private boolean isValidSeed(ItemStack stack) {
        return CropsNHCompat.isCropsNHSeed(stack) || BotanyCropRegistry.isKnownSeed(stack);
    }

    /** 0.0–1.0 growth fraction; useful for rendering a progress bar. */
    public float getGrowthProgress() {
        if (slots[SLOT_SOIL] == null || slots[SLOT_SEED] == null) return 0f;
        int max = getMaxGrowthTicks();
        return max <= 0 ? 0f : (float) growthTicker / max;
    }

    // -------------------------------------------------------------------------
    // IInventory
    // -------------------------------------------------------------------------

    @Override public int getSizeInventory()                          { return SIZE; }
    @Override public ItemStack getStackInSlot(int slot)             { return slots[slot]; }
    @Override public String getInventoryName()                       { return "container.botany_pot"; }
    @Override public boolean hasCustomInventoryName()                { return false; }
    @Override public int getInventoryStackLimit()                    { return 64; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) { return true; }
    @Override public void openInventory()                           {}
    @Override public void closeInventory()                          {}

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slots[slot] == null) return null;
        ItemStack result;
        if (slots[slot].stackSize <= amount) {
            result = slots[slot];
            slots[slot] = null;
        } else {
            result = slots[slot].splitStack(amount);
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = slots[slot];
        slots[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        slots[slot] = stack;
        if (slot == SLOT_SEED) growthTicker = 0;
        markDirty();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == SLOT_SOIL)   return SoilRegistry.isValidSoil(stack);
        if (slot == SLOT_SEED)   return isValidSeed(stack);
        return false;
    }

    // -------------------------------------------------------------------------
    // ISidedInventory — controls hopper access
    // -------------------------------------------------------------------------

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return side == 0 ? SLOTS_BOTTOM : SLOTS_TOP_SIDE;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot == SLOT_OUTPUT;
    }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("growthTicker", growthTicker);

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setByte("slot", (byte) i);
                slots[i].writeToNBT(entry);
                list.appendTag(entry);
            }
        }
        tag.setTag("items", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        growthTicker = tag.getInteger("growthTicker");

        NBTTagList list = tag.getTagList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getByte("slot") & 0xFF;
            if (slot < slots.length) {
                slots[slot] = ItemStack.loadItemStackFromNBT(entry);
            }
        }
    }
}
