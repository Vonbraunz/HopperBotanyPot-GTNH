package com.vonbraunz.botanypots.compat;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface ICropsNHHandler {

    boolean isSeed(ItemStack stack);

    int getGrowthTicks(ItemStack seed, float soilMultiplier);

    List<ItemStack> getDrops(ItemStack seed);

    String getDisplayName(ItemStack seed);
}
