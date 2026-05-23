package com.vonbraunz.botanypots.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class SoilRegistry {

    private static final Map<String, Float> soils = new HashMap<>();

    public static void register(ItemStack stack, float growthMultiplier) {
        soils.put(key(stack), growthMultiplier);
    }

    /**
     * Register all stacks matching an OreDict name as a soil type.
     * Useful for modded dirt/farmland variants.
     */
    public static void registerOreDict(String oreName, float growthMultiplier) {
        List<ItemStack> ores = OreDictionary.getOres(oreName);
        for (ItemStack stack : ores) {
            register(stack, growthMultiplier);
        }
    }

    public static boolean isValidSoil(ItemStack stack) {
        return stack != null && soils.containsKey(key(stack));
    }

    public static float getMultiplier(ItemStack stack) {
        if (stack == null) return 1.0f;
        Float mult = soils.get(key(stack));
        return mult != null ? mult : 1.0f;
    }

    public static void registerDefaults() {
        register(new ItemStack(Blocks.farmland), 1.0f);
        register(new ItemStack(Blocks.dirt), 0.5f);
        register(new ItemStack(Blocks.grass), 0.5f);
        register(new ItemStack(Blocks.mycelium), 0.8f);
        register(new ItemStack(Blocks.soul_sand), 0.75f);
        register(new ItemStack(Blocks.sand), 0.4f);
        register(new ItemStack(Blocks.gravel), 0.3f);
    }

    private static String key(ItemStack stack) {
        int id = Item.getIdFromItem(stack.getItem());
        int meta = stack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? 0 : stack.getItemDamage();
        return id + ":" + meta;
    }
}
