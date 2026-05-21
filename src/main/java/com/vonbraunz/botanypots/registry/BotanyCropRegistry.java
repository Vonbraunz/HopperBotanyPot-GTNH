package com.vonbraunz.botanypots.registry;

import com.vonbraunz.botanypots.config.BotanyPotsConfig;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for non-CropsNH crops. Populated automatically from OreDict
 * (seedX -> cropX pairs) in postInit, and can also be populated manually
 * or via config.
 */
public class BotanyCropRegistry {

    public static class CropData {
        public final ItemStack output;
        public final int baseGrowthTicks;

        public CropData(ItemStack output, int baseGrowthTicks) {
            this.output = output;
            this.baseGrowthTicks = baseGrowthTicks;
        }
    }

    private static final Map<String, CropData> crops = new HashMap<>();

    public static void register(ItemStack seed, ItemStack output, int baseGrowthTicks) {
        if (seed == null || output == null) return;
        crops.put(key(seed), new CropData(output, baseGrowthTicks));
    }

    public static boolean isKnownSeed(ItemStack stack) {
        return stack != null && crops.containsKey(key(stack));
    }

    public static int getGrowthTicks(ItemStack seed, float soilMultiplier) {
        CropData data = crops.get(key(seed));
        int base = (data != null) ? data.baseGrowthTicks : BotanyPotsConfig.defaultGrowthTicks;
        return Math.max(20, (int) (base / soilMultiplier));
    }

    public static List<ItemStack> getDrops(ItemStack seed) {
        CropData data = crops.get(key(seed));
        if (data == null || data.output == null) return Collections.emptyList();
        return Collections.singletonList(data.output.copy());
    }

    /**
     * Hardcoded vanilla sapling → log mappings. Call during init (before postInit OreDict scan)
     * so these take priority over any wildcard OreDict matches.
     */
    public static void registerDefaultSaplings() {
        int t = BotanyPotsConfig.defaultSaplingGrowthTicks;
        // Blocks.log: oak=0, spruce=1, birch=2, jungle=3
        register(new ItemStack(Blocks.sapling, 1, 0), new ItemStack(Blocks.log,  1, 0), t);
        register(new ItemStack(Blocks.sapling, 1, 1), new ItemStack(Blocks.log,  1, 1), t);
        register(new ItemStack(Blocks.sapling, 1, 2), new ItemStack(Blocks.log,  1, 2), t);
        register(new ItemStack(Blocks.sapling, 1, 3), new ItemStack(Blocks.log,  1, 3), t);
        // Blocks.log2: acacia=0, dark_oak=1
        register(new ItemStack(Blocks.sapling, 1, 4), new ItemStack(Blocks.log2, 1, 0), t);
        register(new ItemStack(Blocks.sapling, 1, 5), new ItemStack(Blocks.log2, 1, 1), t);
    }

    /**
     * Scans the OreDict for seedX/cropX and treeSaplingX/logX pairs.
     * Call this in postInit so all mods have had a chance to register.
     */
    public static void registerOreDictCrops() {
        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName.startsWith("seed")) {
                // "seedWheat" -> "cropWheat"
                String cropName = "crop" + oreName.substring(4);
                List<ItemStack> seedItems = OreDictionary.getOres(oreName);
                List<ItemStack> cropItems = OreDictionary.getOres(cropName);
                if (seedItems.isEmpty() || cropItems.isEmpty()) continue;

                ItemStack output = cropItems.get(0);
                for (ItemStack seed : seedItems) {
                    if (isKnownSeed(seed)) continue;
                    register(seed, output, BotanyPotsConfig.defaultGrowthTicks);
                }

            } else if (oreName.startsWith("treeSapling")) {
                // "treeSaplingRubber" -> "logRubber"  (catches IC2/GT rubber trees etc.)
                // Generic "treeSapling" won't match anything useful so it's skipped naturally.
                String logName = "log" + oreName.substring("treeSapling".length());
                List<ItemStack> saplings = OreDictionary.getOres(oreName);
                List<ItemStack> logs = OreDictionary.getOres(logName);
                if (saplings.isEmpty() || logs.isEmpty()) continue;

                ItemStack log = logs.get(0);
                for (ItemStack sapling : saplings) {
                    if (isKnownSeed(sapling)) continue;
                    register(sapling, log, BotanyPotsConfig.defaultSaplingGrowthTicks);
                }
            }
        }
    }

    private static String key(ItemStack stack) {
        int id = Item.getIdFromItem(stack.getItem());
        int meta = stack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? 0 : stack.getItemDamage();
        return id + ":" + meta;
    }
}
