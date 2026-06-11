package com.vonbraunz.botanypots.tileentity;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import com.vonbraunz.botanypots.compat.CropCompatManager;
import com.vonbraunz.botanypots.registry.BotanyCropRegistry;
import com.vonbraunz.botanypots.registry.SoilRegistry;

public class TileEntityBotanyPot extends TileEntity implements ISidedInventory {

    public static final int SLOT_SOIL = 0;
    public static final int SLOT_SEED = 1;
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

        // Always try to push buffered output down, even when not growing
        pushOutputBelow();

        if (slots[SLOT_SOIL] == null || slots[SLOT_SEED] == null) return;
        if (isOutputFull()) return;

        growthTicker++;

        if (growthTicker >= getMaxGrowthTicks()) {
            doHarvest();
            growthTicker = 0;
            markDirty();
        }
    }

    // -------------------------------------------------------------------------
    // Auto-eject — push one item per tick from output slot into inventory below
    // -------------------------------------------------------------------------

    private void pushOutputBelow() {
        if (slots[SLOT_OUTPUT] == null || slots[SLOT_OUTPUT].stackSize <= 0) return;

        TileEntity below = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
        if (!(below instanceof IInventory)) return;

        if (insertOneItem((IInventory) below, slots[SLOT_OUTPUT])) {
            slots[SLOT_OUTPUT].stackSize--;
            if (slots[SLOT_OUTPUT].stackSize <= 0) slots[SLOT_OUTPUT] = null;
            markDirty();
        }
    }

    /**
     * Inserts one unit of {@code stack} into {@code inv} from the top (side=UP).
     * Respects ISidedInventory if present. Modifies {@code stack} in-place on
     * success.
     */
    private static boolean insertOneItem(IInventory inv, ItemStack stack) {
        int fromSide = 1; // UP — we're inserting from above
        if (inv instanceof ISidedInventory) {
            ISidedInventory sided = (ISidedInventory) inv;
            for (int slotId : sided.getAccessibleSlotsFromSide(fromSide)) {
                if (sided.canInsertItem(slotId, stack, fromSide) && tryInsertIntoSlot(inv, slotId, stack)) return true;
            }
        } else {
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (tryInsertIntoSlot(inv, i, stack)) return true;
            }
        }
        return false;
    }

    private static boolean tryInsertIntoSlot(IInventory inv, int slotId, ItemStack stack) {
        if (!inv.isItemValidForSlot(slotId, stack)) return false;
        ItemStack existing = inv.getStackInSlot(slotId);
        if (existing == null) {
            inv.setInventorySlotContents(slotId, stack.copy());
            inv.setInventorySlotContents(slotId, inv.getStackInSlot(slotId)); // trigger dirty
            return true;
        }
        if (existing.isItemEqual(stack) && existing.stackSize < existing.getMaxStackSize()
            && existing.stackSize < inv.getInventoryStackLimit()) {
            existing.stackSize++;
            inv.setInventorySlotContents(slotId, existing);
            return true;
        }
        return false;
    }

    private int getMaxGrowthTicks() {
        float soilMult = SoilRegistry.getMultiplier(slots[SLOT_SOIL]);
        if (CropCompatManager.hasCropsNH() && CropCompatManager.get()
            .isSeed(slots[SLOT_SEED])) {
            return CropCompatManager.get()
                .getGrowthTicks(slots[SLOT_SEED], soilMult);
        }
        return BotanyCropRegistry.getGrowthTicks(slots[SLOT_SEED], soilMult);
    }

    private void doHarvest() {
        List<ItemStack> drops;
        if (CropCompatManager.hasCropsNH() && CropCompatManager.get()
            .isSeed(slots[SLOT_SEED])) {
            drops = CropCompatManager.get()
                .getDrops(slots[SLOT_SEED]);
        } else {
            drops = BotanyCropRegistry.getDrops(slots[SLOT_SEED]);
        }

        TileEntity below = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
        IInventory dest = (below instanceof IInventory) ? (IInventory) below : null;

        for (ItemStack drop : drops) {
            if (drop == null || drop.stackSize <= 0) continue;
            // Push directly into below inventory first (handles multiple item types)
            if (dest != null && insertOneItem(dest, drop)) continue;
            // Buffer in output slot
            if (!addToOutput(drop)) {
                // Last resort: pop into the world above the pot
                worldObj.spawnEntityInWorld(
                    new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.0, zCoord + 0.5, drop.copy()));
            }
        }
    }

    /** Returns true if the item was accepted into the output slot. */
    private boolean addToOutput(ItemStack incoming) {
        if (incoming == null || incoming.stackSize <= 0) return true;

        if (slots[SLOT_OUTPUT] == null) {
            slots[SLOT_OUTPUT] = incoming.copy();
            return true;
        }
        if (slots[SLOT_OUTPUT].isItemEqual(incoming)) {
            int space = slots[SLOT_OUTPUT].getMaxStackSize() - slots[SLOT_OUTPUT].stackSize;
            if (space > 0) {
                slots[SLOT_OUTPUT].stackSize += Math.min(space, incoming.stackSize);
                return true;
            }
        }
        return false;
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
        if (CropCompatManager.hasCropsNH() && CropCompatManager.get().isSeed(stack)) return true;
        return BotanyCropRegistry.isKnownSeed(stack);
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

    @Override
    public int getSizeInventory() {
        return SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slots[slot];
    }

    @Override
    public String getInventoryName() {
        return "container.botany_pot";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

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
        if (slot == SLOT_SOIL) return SoilRegistry.isValidSoil(stack);
        if (slot == SLOT_SEED) return isValidSeed(stack);
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
